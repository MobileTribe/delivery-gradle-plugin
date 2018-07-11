package com.leroymerlin.plugins.core.configurators

import com.leroymerlin.plugins.DeliveryPlugin
import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.cli.Executor
import com.leroymerlin.plugins.entities.SigningProperty
import com.leroymerlin.plugins.utils.SystemUtils
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.GradleBuild
import org.gradle.api.tasks.Upload

/**
 * Created by alexandre on 17/07/2017.
 */
class FlutterConfigurator extends ProjectConfigurator {

    private final String FLUTTER_BUILD = 'flutterBuild'
    private ProjectConfigurator nestedConfigurator

    @Override
    void setup(Project project, DeliveryPluginExtension extension) {
        super.setup(project, extension)
        def signingBuild = SystemUtils.getEnvProperty(FLUTTER_BUILD)

        Executor.exec(["flutter"], ["failOnStderr": true, "failOnStderrMessage": "I don't find flutter :(, please look at https://flutter.io/ for more information"])

        if (signingBuild == 'ios') {
            nestedConfigurator = new IOSConfigurator()
        } else if (signingBuild == 'android') {
            nestedConfigurator = new AndroidConfigurator()
        }

        nestedConfigurator?.isFlutterProject = true
        nestedConfigurator?.setup(project, extension)
    }

    @Override
    void configure() {
        if (nestedConfigurator) {
            if (SystemUtils.getEnvProperty(FLUTTER_BUILD) == 'android') {
                try {
                    project.android.defaultConfig.versionName = project.version
                    project.android.defaultConfig.versionCode = Integer.parseInt(project.versionId as String)
                } catch (Exception e) {
                    throw new GradleException("${project.versionKey} or ${project.versionIdKey} is null, please set it in version.properties file. " +
                            "For more informations : $e")
                }
            }
            nestedConfigurator.configure()
        } else {
            Executor.exec(["flutter", "build", "apk", "--debug"], [directory: project.projectDir.toString()])

            extension.signingProperties.each { signingProperty ->
                project.file("pubspec.yaml").eachLine {
                    if (it.contains("name:")) project.artifact = it.replace("name:", "").trim()
                }
                if (project.artifact != null) handleProperty(signingProperty)
                else throw new Exception("Missing artifact name")
            }
        }
    }

    @Override
    void applyProperties() {
        if (nestedConfigurator) {
            nestedConfigurator.applyProperties()
        }
    }

    def handleProperty(SigningProperty signingProperty) {
        def signingName = getTypeOfProject(project.file(signingProperty.name.toLowerCase()))
        def buildTaskName = "${DeliveryPlugin.UPLOAD_TASK_PREFIX}Flutter${signingName.capitalize()}Artifacts"

        if (signingName == 'android' || signingName == 'ios') {
            def preparePlatformTask = "prepareFlutter${signingName.capitalize()}Platform"

            project.task(buildTaskName, type: Upload, group: DeliveryPlugin.TASK_GROUP) {
                configuration = project.configurations.create("flutter${signingName.capitalize()}")
                repositories {}
            }.dependsOn([preparePlatformTask, "${buildTaskName}Process"])

            File newBuildGradleFile
            if (signingName == "android")
                newBuildGradleFile = project.file("${signingName}/app/build.gradle")
            else
                newBuildGradleFile = project.file("${signingName}/build.gradle")

            if (!newBuildGradleFile.exists())
                newBuildGradleFile.createNewFile()

            project.task(preparePlatformTask, group: DeliveryPlugin.TASK_GROUP).doLast {
                String deliveryConfig = project.file('build.gradle').text

                if (signingName == 'ios')
                    deliveryConfig = project.file('build.gradle')
                            .text
                            .replace("url uri(\"../../build/archive_flutter\")", "url uri(\"../build/archive_flutter\")")

                if (!newBuildGradleFile.text.contains(deliveryConfig)) {
                    newBuildGradleFile << deliveryConfig
                }
            }

            def newStartParameter = project.getGradle().startParameter.newInstance()
            newStartParameter.systemPropertiesArgs.put(FLUTTER_BUILD, signingName)
            newStartParameter.systemPropertiesArgs.put(DeliveryPlugin.VERSION_ARG, project.version as String)
            newStartParameter.systemPropertiesArgs.put(DeliveryPlugin.VERSION_ID_ARG, project.versionId as String)
            newStartParameter.systemPropertiesArgs.put(DeliveryPlugin.PROJECT_NAME_ARG, project.artifact as String)
            if (project.group)
                newStartParameter.systemPropertiesArgs.put(DeliveryPlugin.GROUP_ARG, project.group as String)

            if (signingName == 'android') {
                newStartParameter.settingsFile = project.file("${signingName}/settings.gradle")
                newStartParameter.projectDir = newBuildGradleFile.getParentFile().parentFile
            } else newStartParameter.projectDir = newBuildGradleFile.getParentFile()

            project.task("${buildTaskName}Process", type: GradleBuild, group: DeliveryPlugin.TASK_GROUP) {
                startParameter = newStartParameter
                tasks = ['uploadArtifacts']
            }.shouldRunAfter preparePlatformTask
        } else {
            throw new GradleException("SigningProperty ${signingProperty.name} is not supported, please use Android or IOS")
        }
    }

    static String getTypeOfProject(File folder) {
        for (File file in folder.listFiles()) {
            if (file.name.contains("xcodeproj"))
                return 'ios'
            else if (file.name == "build.gradle" && file.text.contains("android")) {
                return 'android'
            }
        }
        return "unknow"
    }

    @Override
    void applySigningProperty(SigningProperty signingProperty) {
        def signingName = signingProperty.name.toLowerCase()

        if (nestedConfigurator && signingName == SystemUtils.getEnvProperty(FLUTTER_BUILD)) {
            SigningProperty signingPropertyCopy = new SigningProperty('release')
            signingPropertyCopy.setProperties(signingProperty.properties)
            if (signingName == "ios") {
                if (signingProperty.target != null && signingProperty.scheme != null) {
                    signingPropertyCopy.target = signingProperty.target
                    signingPropertyCopy.scheme = signingProperty.scheme
                } else {
                    signingPropertyCopy.target = "Runner"
                    signingPropertyCopy.scheme = "Runner"
                }
            }
            nestedConfigurator.applySigningProperty(signingPropertyCopy)
        }
    }

    @Override
    boolean handleProject(Project project) {
        boolean flutterProject = false
        if (project.file("pubspec.yaml").exists() && project.file("pubspec.yaml").text.contains("flutter:")) {
            flutterProject = true
        }
        return (SystemUtils.getEnvProperty(FLUTTER_BUILD) != null || flutterProject)
    }

}
