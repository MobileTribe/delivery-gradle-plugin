package com.leroymerlin.plugins.core.configurators

import com.leroymerlin.plugins.DeliveryPlugin
import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.cli.Executor
import com.leroymerlin.plugins.entities.SigningProperty
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.GradleBuild
import org.gradle.api.tasks.Upload
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by alexandre on 27/03/2017.
 */
class IonicConfigurator extends ProjectConfigurator {

    public static final String IONIC_BUILD = 'ionicBuild'


    Logger logger = LoggerFactory.getLogger('IonicConfigurator')

    ProjectConfigurator nestedConfigurator;

    @Override
    public void setup(Project project, DeliveryPluginExtension extension) {
        super.setup(project, extension)

        def signingBuild = System.getProperty(IONIC_BUILD)
        if (signingBuild == 'ios') {
            nestedConfigurator = new IOSConfigurator()
        } else if (signingBuild == 'android') {
            nestedConfigurator = new AndroidConfigurator()
        }
        nestedConfigurator?.setup(project, extension)


        project.task("prepareNpm").doFirst {
            Executor.exec(["npm", "install"], directory: project.projectDir)
        }

    }

    @Override
    public void configure() {
        if (nestedConfigurator) {
            if (System.getProperty(IONIC_BUILD) == 'android') {
                project.android.defaultConfig.versionName = project.version
                project.android.defaultConfig.versionCode = Integer.parseInt(project.versionId)
            }
            nestedConfigurator.configure()
        } else {
            def config = project.file("config.xml")
            def widget = new XmlParser(false, false).parse(config)
            if (!project.group) {
                project.ext.group = widget."@id"
                project.group = project.ext.group
            }
            project.projectName = widget.name[0].value()[0]

            if (!project.group) {
                throw new GradleException("Project group is not defined. Please use a gradle properties or configure your id in config.xml")
            }
            logger.info("group used : ${project.group}")



            extension.signingProperties.each { signingProperty -> handleProperty(signingProperty) }
        }
    }

    @Override
    public void applyProperties() {
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
        def buildTaskName = "buildIonic${signingName.capitalize()}Artifacts"

        if (signingName == 'android' || signingName == 'ios') {
            def preparePlatformTask = "prepareIonic${signingName.capitalize()}Platform"

            project.task(buildTaskName, type: Upload) {
                configuration = project.configurations.create("ionic${signingName.capitalize()}")
                repositories {}
            }.dependsOn([preparePlatformTask, "${buildTaskName}Process"])

            def newBuildGradleFile = project.file("platforms/${signingName}/${signingName == 'android' ? "delivery-" : ""}build.gradle")
            def settingsGradle = project.file("platforms/${signingName}/${signingName == 'android' ? "delivery-" : ""}settings.gradle")




            project.task(preparePlatformTask).doFirst {
                Executor.exec(["ionic", "platform", "add", signingName], directory: project.projectDir)
                Executor.exec(["ionic", "resources"], directory: project.projectDir)
                Executor.exec(["ionic", "platform", "remove", signingName], directory: project.projectDir)
                Executor.exec(["ionic", "platform", "add", signingName], directory: project.projectDir)
                Executor.exec(["ionic", "build", signingName, "--release"], directory: project.projectDir)

                newBuildGradleFile.delete()
                if (signingName == 'android') {
                    newBuildGradleFile << project.file("platforms/${signingName}/build.gradle").text
                    settingsGradle << project.file("platforms/${signingName}/settings.gradle").text

                    settingsGradle << "\nrootProject.buildFileName = 'delivery-build.gradle'"
                }
                newBuildGradleFile << project.file('build.gradle').text
            }.dependsOn('prepareNpm')



            def newStartParameter = project.getGradle().startParameter.newInstance()
            newStartParameter.systemPropertiesArgs.put(IONIC_BUILD, signingName)
            newStartParameter.systemPropertiesArgs.put(DeliveryPlugin.VERSION_ARG, project.version)
            newStartParameter.systemPropertiesArgs.put(DeliveryPlugin.VERSION_ID_ARG, project.versionId)
            newStartParameter.systemPropertiesArgs.put(DeliveryPlugin.PROJECT_NAME_ARG, project.projectName)
            newStartParameter.systemPropertiesArgs.put(DeliveryPlugin.GROUP_ARG, project.group)
            if (signingName == 'android') {
                newStartParameter.settingsFile = settingsGradle
            }
            newStartParameter.projectDir = settingsGradle.getParentFile()

            project.task("${buildTaskName}Process", type: GradleBuild) {
                startParameter = newStartParameter
                tasks = ['uploadArtifacts']
            }.shouldRunAfter preparePlatformTask


        } else {
            throw new GradleException("SigningProperty ${signingProperty.name} is not supported, please use Android or IOS")
        }


    }

    @Override
    public void applySigningProperty(SigningProperty signingProperty) {

        def signingName = signingProperty.name.toLowerCase()

        if (nestedConfigurator && signingName == System.getProperty(IONIC_BUILD)) {
            SigningProperty signingPropertyCopy = new SigningProperty('release')
            signingPropertyCopy.setProperties(signingProperty.properties)
            signingPropertyCopy.target = project.projectName
            signingPropertyCopy.scheme = project.projectName
            nestedConfigurator.applySigningProperty(signingPropertyCopy)
        }
    }

    @Override
    public boolean handleProject(Project project) {
        return System.getProperty(IONIC_BUILD) != null || (project.file('ionic.config.json').exists() && project.file('config.xml').exists())
    }
}
