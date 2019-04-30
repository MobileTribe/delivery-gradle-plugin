package com.leroymerlin.plugins.core.configurators

import com.leroymerlin.plugins.DeliveryPlugin
import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.cli.Executor
import com.leroymerlin.plugins.entities.SigningProperty
import com.leroymerlin.plugins.tasks.build.PrepareBuildTask
import com.leroymerlin.plugins.utils.PropertiesUtils
import com.leroymerlin.plugins.utils.SystemUtils
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.GradleBuild
import org.gradle.api.tasks.Upload

/**
 * Created by alexandre on 27/03/2017.
 */
class IonicConfigurator extends ProjectConfigurator {

    private final String IONIC_BUILD = 'ionicBuild'
    private ProjectConfigurator nestedConfigurator

    @Override
    void setup(Project project, DeliveryPluginExtension extension) {
        super.setup(project, extension)
        String signingBuild = SystemUtils.getEnvProperty(IONIC_BUILD)

        def npmResult = Executor.exec(["npm", "-v"]) {
            needSuccessExitCode = false
            silent = true
        }

        if (npmResult.exitValue == Executor.EXIT_CODE_NOT_FOUND) {
            throw new GradleException("I don't find npm :(, please look at https://www.npmjs.com/get-npm for more information")
        }

        def ionicResult = Executor.exec(["ionic", "--version"]) {
            needSuccessExitCode = false
            silent = true
            inputPatterns = ["The Ionic CLI has an update available": "y"]
        }

        if (ionicResult.exitValue == Executor.EXIT_CODE_NOT_FOUND) {
            throw new GradleException("I don't find ionic :(, please look at https://ionicframework.com/ for more information")
        }


        if (signingBuild == 'ios') {
            nestedConfigurator = new IOSConfigurator()
        } else if (signingBuild == 'android') {
            nestedConfigurator = new AndroidConfigurator()
        }
        nestedConfigurator?.setup(project, extension)

        project.task("prepareIonic", type: PrepareBuildTask, group: DeliveryPlugin.TASK_GROUP).doLast {
            project.file("platforms")?.deleteDir()
            deliveryLogger.logInfo("Delivery support Ionic > 3.0 & Cordova > 7.0")

            Executor.exec(["npm", "install"]) {
                directory = project.projectDir
            }

            Executor.exec(["ionic", "cordova", "prepare"]) {
                directory = project.projectDir
                needSuccessExitCode = false
            }
        }

    }

    @Override
    void configure() {
        if (nestedConfigurator) {
            if (SystemUtils.getEnvProperty(IONIC_BUILD) == 'android') {
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
            File config = project.file("config.xml")
            Node widget = new XmlParser(false, false).parse(config)
            if (!PropertiesUtils.userHasDefineProperty(project, "group")) {
                project.ext.group = widget."@id"
                project.group = project.ext.group
            }
            project.artifact = widget.name[0].value()[0]

            if (!project.group) {
                throw new GradleException("Project group is not defined. Please use a gradle properties or configure your id in config.xml")
            }
            extension.signingProperties.each { signingProperty -> handleProperty(signingProperty) }
        }
    }

    @Override
    void applyProperties() {
        if (nestedConfigurator) {
            nestedConfigurator.applyProperties()
        } else {
            def config = project.file("config.xml")
            def widget = new XmlParser(false, false).parse(config)
            widget."@version" = project.version
            def writer = new PrintWriter(config)
            def xmlNodePrinter = new XmlNodePrinter(writer)
            xmlNodePrinter.preserveWhitespace = true
            xmlNodePrinter.print(widget)
            writer.flush()
            writer.close()
        }
    }

    def handleProperty(SigningProperty signingProperty) {
        def signingName = signingProperty.name.toLowerCase()
        def buildTaskName = "${DeliveryPlugin.UPLOAD_TASK_PREFIX}Ionic${signingName.capitalize()}Artifacts"

        if (signingName == 'android' || signingName == 'ios') {
            def preparePlatformTask = "prepareIonic${signingName.capitalize()}Platform"

            project.task(buildTaskName, type: Upload, group: DeliveryPlugin.TASK_GROUP) {
                configuration = project.configurations.create("ionic${signingName.capitalize()}")
                repositories {}
            }.dependsOn([preparePlatformTask, "${buildTaskName}Process"])

            def newBuildGradleFile = project.file("platforms/${signingName}/${signingName == 'android' ? "app/" : ""}build.gradle")
            def settingsGradle = project.file("platforms/${signingName}/${signingName == 'android' ? "delivery-" : ""}settings.gradle")

            project.task(preparePlatformTask, type: PrepareBuildTask, group: DeliveryPlugin.TASK_GROUP).doLast {
                Executor.exec(["ionic", "cordova", "build", signingName, "--release"]) {
                    directory = project.projectDir
                }

                if (signingName == 'android') {
                    settingsGradle << "\n" + project.file("platforms/${signingName}/settings.gradle").text
                }
                newBuildGradleFile << "\n" + project.file('build.gradle').text
                newBuildGradleFile.text = newBuildGradleFile.text.replace("com.android.tools.build:gradle:2.2.3", "com.android.tools.build:gradle:2.3.0")

            }.dependsOn('prepareIonic')

            def newStartParameter = project.getGradle().startParameter.newInstance()
            newStartParameter.systemPropertiesArgs.put(IONIC_BUILD, signingName)
            newStartParameter.systemPropertiesArgs.put(DeliveryPlugin.VERSION_ARG, project.version as String)
            newStartParameter.systemPropertiesArgs.put(DeliveryPlugin.VERSION_ID_ARG, project.versionId as String)
            newStartParameter.systemPropertiesArgs.put(DeliveryPlugin.PROJECT_NAME_ARG, project.artifact as String)
            newStartParameter.systemPropertiesArgs.put(DeliveryPlugin.GROUP_ARG, project.group as String)
            if (signingName == 'android') {
                newStartParameter.settingsFile = settingsGradle
            }
            newStartParameter.projectDir = settingsGradle.getParentFile()

            project.task("${buildTaskName}Process", type: GradleBuild, group: DeliveryPlugin.TASK_GROUP) {
                startParameter = newStartParameter
                tasks = ['uploadArtifacts']
            }.shouldRunAfter preparePlatformTask
        } else {
            throw new GradleException("SigningProperty ${signingProperty.name} is not supported, please use Android or IOS")
        }
    }

    @Override
    void applySigningProperty(SigningProperty signingProperty) {
        def signingName = signingProperty.name.toLowerCase()

        if (nestedConfigurator && signingName == SystemUtils.getEnvProperty(IONIC_BUILD)) {
            SigningProperty signingPropertyCopy = new SigningProperty('release')
            signingPropertyCopy.setProperties(signingProperty.properties)
            signingPropertyCopy.target = project.artifact
            signingPropertyCopy.scheme = project.artifact
            nestedConfigurator.applySigningProperty(signingPropertyCopy)
        }
    }

    @Override
    boolean handleProject(Project project) {
        return SystemUtils.getEnvProperty(IONIC_BUILD) != null || project.file('ionic.project').exists() || (project.file('ionic.config.json').exists())
    }
}
