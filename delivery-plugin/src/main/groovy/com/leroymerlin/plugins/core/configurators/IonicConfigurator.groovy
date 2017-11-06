package com.leroymerlin.plugins.core.configurators

import com.leroymerlin.plugins.DeliveryPlugin
import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.cli.Executor
import com.leroymerlin.plugins.entities.SigningProperty
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.GradleBuild
import org.gradle.api.tasks.Upload

import java.util.logging.Logger

/**
 * Created by alexandre on 27/03/2017.
 */
class IonicConfigurator extends ProjectConfigurator {

    private final String IONIC_BUILD = 'ionicBuild'
    private ProjectConfigurator nestedConfigurator

    @Override
    void setup(Project project, DeliveryPluginExtension extension) {
        super.setup(project, extension)
        String signingBuild = System.getProperty(IONIC_BUILD)
        if (signingBuild == 'ios') {
            nestedConfigurator = new IOSConfigurator()
            nestedConfigurator.hybridBuild = true
        } else if (signingBuild == 'android') {
            nestedConfigurator = new AndroidConfigurator()
        }
        nestedConfigurator?.setup(project, extension)

        project.task("prepareProject", group: DeliveryPlugin.TASK_GROUP).doLast {
            project.file("platforms")?.deleteDir()
        }
        project.task("prepareNpm", group: DeliveryPlugin.TASK_GROUP).doLast {
            Logger.global.warning("Delivery support Ionic > 3.0 & Cordova > 7.0")
            Executor.exec(["npm", "install"], [directory: project.projectDir], true)
            Executor.exec(["ionic", "-v"], [directory: project.projectDir], true)
        }.dependsOn("prepareProject")
    }

    @Override
    void configure() {
        if (nestedConfigurator) {
            if (System.getProperty(IONIC_BUILD) == 'android') {
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
            if (!project.group) {
                project.ext.group = widget."@id"
                project.group = project.ext.group
            }
            project.artifact = widget.name[0].value()[0]

            if (!project.group) {
                throw new GradleException("Project group is not defined. Please use a gradle properties or configure your id in config.xml")
            }
            Logger.global.info("group used : ${project.group}")

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
            def xmlNodePrinter = new XmlNodePrinter(new PrintWriter(config))
            xmlNodePrinter.preserveWhitespace = true
            xmlNodePrinter.print(widget)
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

            def newBuildGradleFile = project.file("platforms/${signingName}/${signingName == 'android' ? "delivery-" : ""}build.gradle")
            def settingsGradle = project.file("platforms/${signingName}/${signingName == 'android' ? "delivery-" : ""}settings.gradle")

            project.task(preparePlatformTask, group: DeliveryPlugin.TASK_GROUP).doLast {
                Executor.exec(["ionic", "cordova", "build", signingName, "--release"], [directory: project.projectDir], true)

                newBuildGradleFile.delete()
                if (signingName == 'android') {
                    newBuildGradleFile << project.file("platforms/${signingName}/build.gradle").text
                    settingsGradle << project.file("platforms/${signingName}/settings.gradle").text

                    settingsGradle << "\nrootProject.buildFileName = 'delivery-build.gradle'"
                }
                newBuildGradleFile << project.file('build.gradle').text
                newBuildGradleFile.text = newBuildGradleFile.text.replace("com.android.tools.build:gradle:2.2.3", "com.android.tools.build:gradle:2.3.0")

            }.dependsOn('prepareNpm')

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

        if (nestedConfigurator && signingName == System.getProperty(IONIC_BUILD)) {
            SigningProperty signingPropertyCopy = new SigningProperty('release')
            signingPropertyCopy.setProperties(signingProperty.properties)
            signingPropertyCopy.target = project.artifact
            signingPropertyCopy.scheme = project.artifact
            nestedConfigurator.applySigningProperty(signingPropertyCopy)
        }
    }

    @Override
    boolean handleProject(Project project) {
        return System.getProperty(IONIC_BUILD) != null || project.file('ionic.project').exists() || (project.file('ionic.config.json').exists())
    }
}
