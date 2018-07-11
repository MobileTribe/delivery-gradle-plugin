package com.leroymerlin.plugins.core.configurators

import com.leroymerlin.plugins.DeliveryPlugin
import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.cli.Executor
import com.leroymerlin.plugins.entities.SigningProperty
import com.leroymerlin.plugins.utils.SystemUtils
import groovy.json.JsonSlurper
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.GradleBuild
import org.gradle.api.tasks.Upload

/**
 * Created by alexandre on 17/07/2017.
 */
class ReactConfigurator extends ProjectConfigurator {

    private final String REACT_BUILD = 'reactBuild'
    private ProjectConfigurator nestedConfigurator

    @Override
    void setup(Project project, DeliveryPluginExtension extension) {
        super.setup(project, extension)
        def signingBuild = SystemUtils.getEnvProperty(REACT_BUILD)

        Executor.exec(["npm"], ["failOnStderr": true, "failOnStderrMessage": "I don't find npm :(, please look at https://www.npmjs.com/get-npm for more information"])

        if (signingBuild == 'ios') {
            nestedConfigurator = new IOSConfigurator()
            nestedConfigurator.hybridBuild = true
        } else if (signingBuild == 'android') {
            nestedConfigurator = new AndroidConfigurator()
        }
        nestedConfigurator?.setup(project, extension)

        project.task("prepareNpm", group: DeliveryPlugin.TASK_GROUP).doLast {
            Executor.exec(["npm", "install"], [directory: project.projectDir], true)
        }
    }

    @Override
    void configure() {
        if (nestedConfigurator) {
            if (SystemUtils.getEnvProperty(REACT_BUILD) == 'android') {
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
            extension.signingProperties.each { signingProperty ->
                def object = new JsonSlurper().parseText(project.file("package.json").text)
                project.artifact = object.name
                handleProperty(signingProperty)
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
        def buildTaskName = "${DeliveryPlugin.UPLOAD_TASK_PREFIX}React${signingName.capitalize()}Artifacts"

        if (signingName == 'android' || signingName == 'ios') {
            def preparePlatformTask = "prepareIonic${signingName.capitalize()}Platform"

            project.task(buildTaskName, type: Upload, group: DeliveryPlugin.TASK_GROUP) {
                configuration = project.configurations.create("react${signingName.capitalize()}")
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
                            .replace("url uri(\"../../build/archive_react\")", "url uri(\"../build/archive_react\")")

                if (!newBuildGradleFile.text.contains(deliveryConfig)) {
                    newBuildGradleFile << deliveryConfig
                }
            }.dependsOn('prepareNpm')

            def newStartParameter = project.getGradle().startParameter.newInstance()
            newStartParameter.systemPropertiesArgs.put(REACT_BUILD, signingName)
            newStartParameter.systemPropertiesArgs.put(DeliveryPlugin.VERSION_ARG, project.version as String)
            newStartParameter.systemPropertiesArgs.put(DeliveryPlugin.VERSION_ID_ARG, project.versionId as String)
            newStartParameter.systemPropertiesArgs.put(DeliveryPlugin.PROJECT_NAME_ARG, project.artifact as String)
            if (project.group)
                newStartParameter.systemPropertiesArgs.put(DeliveryPlugin.GROUP_ARG, project.group as String)

            if (signingName == 'android') {
                newStartParameter.settingsFile = project.file("${signingName}/settings.gradle")
                newStartParameter.projectDir = newBuildGradleFile.getParentFile().parentFile
            } else
                newStartParameter.projectDir = newBuildGradleFile.getParentFile()

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

        if (nestedConfigurator && signingName == SystemUtils.getEnvProperty(REACT_BUILD)) {
            SigningProperty signingPropertyCopy = new SigningProperty('release')
            signingPropertyCopy.setProperties(signingProperty.properties)
            signingPropertyCopy.target = project.artifact
            signingPropertyCopy.scheme = project.artifact
            nestedConfigurator.applySigningProperty(signingPropertyCopy)
        }
    }

    @Override
    boolean handleProject(Project project) {
        boolean reactProject = false
        if (project.file('package.json').exists()) {
            def json = new JsonSlurper().parseText(project.file('package.json').text)
            if (json.dependencies.react != null || json.dependencies."react-native" != null)
                reactProject = true
        }
        return (SystemUtils.getEnvProperty(REACT_BUILD) != null || reactProject)
    }
}
