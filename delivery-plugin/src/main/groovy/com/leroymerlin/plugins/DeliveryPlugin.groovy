package com.leroymerlin.plugins

import com.leroymerlin.plugins.cli.Executor
import com.leroymerlin.plugins.core.configurators.*
import com.leroymerlin.plugins.tasks.build.DeliveryBuild
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

    static final String VERSION_ARG = 'VERSION'
    static final String VERSION_ID_ARG = 'VERSION_ID'
    static final String GROUP_ARG = 'GROUP'
    static final String PROJECT_NAME_ARG = 'PROJECT_NAME'


    static final String TASK_GROUP = 'delivery'
    static final String DELIVERY_CONF_FILE = 'delivery.properties'

    def configurators = [IonicConfigurator, AndroidConfigurator, JavaConfigurator, IOSConfigurator]

    Project project
    DeliveryPluginExtension deliveryExtension

    void apply(Project project) {
        this.project = project
        this.deliveryExtension = project.extensions.create(TASK_GROUP, DeliveryPluginExtension, project, this)
        project.plugins.apply('maven')
        Executor.logger = logger
        project.ext.DeliveryBuild = DeliveryBuild

        setupProperties()

        ProjectConfigurator detectedConfigurator = configurators.find {
            configurator ->
                configurator.newInstance().handleProject(project)
        }?.newInstance()
        if (detectedConfigurator == null) {
            detectedConfigurator = [] as ProjectConfigurator
        } else {
            logger.warn("Project of type ${detectedConfigurator.class.simpleName - "Configurator"} found")
        }
        this.deliveryExtension.configurator = detectedConfigurator
        project.afterEvaluate {
            if (deliveryExtension.configurator == null) {
                throw new GradleException("Configurator is null. Can't configure your project. Please set the configurator or apply the plugin after your project plugin")
            }
            deliveryExtension.configurator.configure();

            def buildTasks = []
            buildTasks.addAll(project.tasks.withType(DeliveryBuild))
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
                    ((Configuration) project.configurations."${configurationName}").artifacts.addAll(task.getArtifacts())
            }

            def uploadArtifacts = project.task("uploadArtifacts", group: TASK_GROUP, dependsOn: project.tasks.withType(Upload))
            if (project.tasks.findByPath("check") != null) {
                uploadArtifacts.dependsOn += project.tasks.findByPath("check")
            }

            //create default release git flow

            if (!deliveryExtension.flowsContainer.hasProperty("releaseGit")) {
                deliveryExtension.flowsContainer.create('releaseGit',
                        {
                            def releaseVersion = System.getProperty("VERSION", project.version - '-SNAPSHOT')
                            def releaseBranch = "release/${project.versionId}-$releaseVersion"
                            def matcher = releaseVersion =~ /(\d+)([^\d]*$)/
                            def newVersion = System.getProperty("NEW_VERSION", matcher.replaceAll("${(matcher[0][1] as int) + 1}${matcher[0][2]}")) - "-SNAPSHOT" + "-SNAPSHOT"
                            def baseBranch = System.getProperty("BASE_BRANCH", 'master')
                            def workBranch = System.getProperty("BRANCH", 'develop')
                            def newVersionId = Integer.parseInt(project.versionId) + 1

                            branch workBranch
                            branch releaseBranch, true
                            changeProperties version: releaseVersion
                            add 'version.properties'
                            commit "chore (version) : Update version to $releaseVersion"
                            build
                            tag annotation: "$project.projectName-$project.versionId-$releaseVersion"
                            if (baseBranch) {
                                branch baseBranch
                                merge releaseBranch
                                push
                            }
                            branch releaseBranch
                            changeProperties version: newVersion, versionId: newVersionId
                            add 'version.properties'
                            commit "chore (version) : Update to new version $releaseVersion and versionId $newVersionId"
                            push
                            branch workBranch
                            merge releaseBranch
                            push
                        }
                )
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




        if (System.getProperty(VERSION_ID_ARG) != null) {
            PropertiesFileUtils.setProperty(versionFile, project.ext.versionIdKey, System.getProperty(VERSION_ID_ARG))
        }
        if (System.getProperty(VERSION_ARG) != null) {
            PropertiesFileUtils.setProperty(versionFile, project.ext.versionKey, System.getProperty(VERSION_ARG))
        }
        if (System.getProperty(GROUP_ARG) != null) {
            PropertiesFileUtils.setProperty(versionFile, 'group', System.getProperty(GROUP_ARG))
        }
        if (System.getProperty(PROJECT_NAME_ARG) != null) {
            PropertiesFileUtils.setProperty(versionFile, project.ext.projectNameKey, System.getProperty(PROJECT_NAME_ARG))
        }


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
        if (project.extensions.getExtraProperties().has("group")) {
            project.group = project.ext.group
        }
        project.ext.projectName = project.ext."${project.ext.projectNameKey}"
        if (deliveryExtension.configurator != null) {
            deliveryExtension.configurator.applyVersion(project.version, project.ext.versionId, project.ext.projectName)
        }
    }
}
