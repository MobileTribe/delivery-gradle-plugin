package com.leroymerlin.plugins.core.configurators

import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.entities.SigningProperty
import com.leroymerlin.plugins.tasks.build.DeliveryBuild
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.GradleBuild
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by alexandre on 27/03/2017.
 */
class IonicConfigurator extends ProjectConfigurator {

    Logger logger = LoggerFactory.getLogger('IonicConfigurator')

    @Override
    public void setup(Project project, DeliveryPluginExtension extension) {
        super.setup(project, extension)
    }

    @Override
    public void configure() {
        if (project.group == null) {
            def config = project.file("config.xml")
            def widget = new XmlParser(false, false).parse(config)
            project.group = widget."@id"
        }

        if (!project.group) {
            throw new GradleException("Project group is not defined. Please use a gradle properties or configure your id in config.xml")
        }
        logger.info("group used : ${project.group}")
    }

    @Override
    public void applyProperties() {
        def config = project.file("config.xml")
        def widget = new XmlParser(false, false).parse(config)
        widget."@version" = project.version
        def xmlNodePrinter = new XmlNodePrinter(new PrintWriter(config))
        xmlNodePrinter.preserveWhitespace = true
        xmlNodePrinter.print(widget)
    }

    @Override
    public void applySigningProperty(SigningProperty signingProperty) {
        if (signingProperty.name.toLowerCase() == 'android') {
            def buildTaskName = "buildIonicAndroidArtifacts"
            project.task(buildTaskName, type: DeliveryBuild) {
                outputFiles = []
            }.dependsOn("${buildTaskName}Process")
            project.task("${buildTaskName}Process", type: GradleBuild) {
                startParameter = project.getGradle().startParameter.newInstance()
                tasks = ['buildFlow']
            }
            project.task(taskName + "Process", type: GradleBuild) {

            }
        } else if (signingProperty.name.toLowerCase() == 'ios') {
        } else
            throw new GradleException("SigningProperty ${signingProperty.name} is not supported, please use Android or IOS")
    }

    @Override
    public boolean handleProject(Project project) {
        return project.file('ionic.config.json').exists() && project.file('config.xml').exists()
    }
}
