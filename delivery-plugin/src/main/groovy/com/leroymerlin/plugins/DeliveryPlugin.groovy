package com.leroymerlin.plugins

import com.leroymerlin.plugins.core.AndroidConfigurator
import com.leroymerlin.plugins.core.BaseScmAdapter
import com.leroymerlin.plugins.core.ProjectConfigurator
import com.leroymerlin.plugins.tasks.DeliveryBuildTask
import com.leroymerlin.plugins.utils.PropertiesFileUtils
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

    ProjectConfigurator[] configurators = [new AndroidConfigurator()]

    Project project
    DeliveryPluginExtension deliveryExtension

    void apply(Project project) {
        this.project = project
        this.deliveryExtension = project.extensions.create(TASK_GROUP, DeliveryPluginExtension, project, this)
        setupProperties()

        project.afterEvaluate {
            /*if (deliveryExtension.configurator == null) {
                deliveryExtension.configurator = configurators.findResult { it.handleProject(project) ? it : null }
            }*/
            BaseScmAdapter scmAdapter = deliveryExtension.scmAdapter
            scmAdapter.setup(this.project, this.deliveryExtension)


            //TODO generate task if project type detected  deliveryExtension.configurator.configureBuildTasks()

            project.tasks.withType(DeliveryBuildTask).each {
                task ->

                    def configurationName = task.variantName + "Config"


                    if (!project.configurations.hasProperty(configurationName)) {
                        project.configurations.create(configurationName)
                        project.dependencies.add(configurationName, 'org.apache.maven.wagon:wagon-http:2.2')


                        project.task("upload${task.variantName.capitalize()}Artifacts", type: Upload, group: TASK_GROUP) {
                            configuration = project.configurations."${configurationName}"
                            repositories extension.archiveRepositories
                        }
                    }

                    ((Configuration) project.configurations."${configurationName}").artifacts.addAll(
                            task.getArtifacts()
                    )


            }

            project.task("uploadArtifacts", group: TASK_GROUP, dependsOn: project.tasks.withType(Upload))
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
        project.ext.projectName = project.ext."${project.ext.projectNameKey}"
        if (deliveryExtension.configurator != null) {
            deliveryExtension.configurator.applyVersion(project.version, project.ext.versionId, project.ext.projectName)
        }
    }
}
