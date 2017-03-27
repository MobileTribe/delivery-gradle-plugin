package com.leroymerlin.plugins

import com.leroymerlin.plugins.core.configurators.AndroidConfigurator
import com.leroymerlin.plugins.core.configurators.IOSConfigurator
import com.leroymerlin.plugins.core.configurators.JavaConfigurator
import com.leroymerlin.plugins.core.configurators.ProjectConfigurator
import com.leroymerlin.plugins.tasks.build.DeliveryBuildTask
import com.leroymerlin.plugins.utils.PropertiesFileUtils
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Upload
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DeliveryPlugin implements Plugin<Project> {
    Logger logger = LoggerFactory.getLogger('DeliveryPlugin')

    static final String TASK_GROUP = 'delivery'
    static final String DELIVERY_CONF_FILE = 'delivery.properties'

    def configurators = [AndroidConfigurator, JavaConfigurator, IOSConfigurator]

    Project project
    DeliveryPluginExtension deliveryExtension

    void apply(Project project) {
        this.project = project
        this.deliveryExtension = project.extensions.create(TASK_GROUP, DeliveryPluginExtension, project, this)

        setupProperties()

        ProjectConfigurator detectedConfigurator = configurators.find {
            configurator ->
                configurator.newInstance().handleProject(project)
        }?.newInstance()
        if (detectedConfigurator == null) {
            detectedConfigurator = []
        }
        this.deliveryExtension.configurator = detectedConfigurator
        project.afterEvaluate {
            if (deliveryExtension.configurator == null) {
                throw new GradleException("Configurator is null. Can't configure your project. Please set the configurator or apply the plugin after your project plugin")
            }
            deliveryExtension.configurator.configure();

            //TODO generate task if project type detected  deliveryExtension.configurator.configureBuildTasks()

            def buildTasks = []
            buildTasks.addAll(project.tasks.withType(DeliveryBuildTask))
            buildTasks.each {
                task ->

                    def configurationName = task.variantName + "Config"


                    if (!project.configurations.hasProperty(configurationName)) {
                        project.configurations.create(configurationName)
                        project.dependencies.add(configurationName, 'org.apache.maven.wagon:wagon-http:2.2')


                        project.task("upload${task.variantName.capitalize()}Artifacts", type: Upload, group: TASK_GROUP) {
                            configuration = project.configurations."${configurationName}"
                            repositories deliveryExtension.archiveRepositories
                        }
                    }

                    ((Configuration) project.configurations."${configurationName}").artifacts.addAll(
                            task.getArtifacts()
                    )


            }

            def uploadArtifacts = project.task("uploadArtifacts", group: TASK_GROUP, dependsOn: project.tasks.withType(Upload))
            if (project.tasks.findByPath("check") != null) {
                uploadArtifacts.dependsOn += project.tasks.findByPath("check")
            }
        }
    }


    void setupProperties() {
        //Read and apply Delivery.properties file to override default version.properties path and version, versionId, projectName keys
        PropertiesFileUtils.readAndApplyPropertiesFile(project, project.file(DELIVERY_CONF_FILE))

        //Apply default value if needed
        File versionFile = getVersionFile()
        if (!project.hasProperty('versionIdKey')) {
            project.ext.versionIdKey = 'versionId'
        }
        PropertiesFileUtils.setDefaultProperty(versionFile, project.ext.versionIdKey, "2")

        if (!project.hasProperty('versionKey')) {
            project.ext.versionKey = 'version'
        }
        PropertiesFileUtils.setDefaultProperty(versionFile, project.ext.versionKey, "1.0.0-SNAPSHOT")

        if (!project.hasProperty('projectNameKey')) {
            project.ext.projectNameKey = 'projectName'
        }
        PropertiesFileUtils.setDefaultProperty(versionFile, project.ext.projectNameKey, project.name)
        applyDeliveryProperties(versionFile)
    }

    File getVersionFile() {
        if (project.hasProperty('versionFilePath')) {
            return project.file(project.property('versionFilePath'))
        } else {
            return project.file('version.properties')
        }
    }

    void applyDeliveryProperties(File versionFile) {
        PropertiesFileUtils.readAndApplyPropertiesFile(project, versionFile)
        project.ext.versionId = project.ext."${project.ext.versionIdKey}"
        project.ext.version = project.ext."${project.ext.versionKey}"
        project.version = project.ext."${project.ext.versionKey}"
        if (project.extensions.getExtraProperties().has("group")){
            project.group = project.ext.group
        }
        project.ext.projectName = project.ext."${project.ext.projectNameKey}"
        if (deliveryExtension.configurator != null) {
            deliveryExtension.configurator.applyVersion(project.version, project.ext.versionId, project.ext.projectName)
        }
    }
}
