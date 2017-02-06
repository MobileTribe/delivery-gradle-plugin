package com.leroymerlin.plugins

import com.leroymerlin.plugins.core.BaseScmAdapter
import com.leroymerlin.plugins.core.ProjectConfigurator
import com.leroymerlin.plugins.utils.PropertiesFileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.GradleBuild
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DeliveryPlugin implements Plugin<Project> {
    Logger logger = LoggerFactory.getLogger('DeliveryPlugin')
    static final String TASK_GROUP = 'delivery'
    static final String DELIVERY_CONF_FILE = 'delivery.properties'

    Project project
    DeliveryPluginExtension deliveryExtension


    void apply(Project project) {
        this.project = project
        this.deliveryExtension = project.extensions.create(TASK_GROUP, DeliveryPluginExtension, project)
        setupProperties()

        project.afterEvaluate {

            ProjectConfigurator configurator = deliveryExtension.configurator
            BaseScmAdapter scmAdapter = deliveryExtension.scmAdapter
            scmAdapter.setup(this.project, this.deliveryExtension, "init release")

            project.task("initReleaseBranch", description: "Creates or switch to the release branch").doFirst(scmAdapter.&initReleaseBranch)

            /*project.task("prepareReleaseFiles", description: "Prepare project file for release", dependsOn: "initReleaseBranch").doFirst(scmAdapter.&prepareReleaseFiles)
            project.task("commitReleaseFiles", description: "Changes the version with the one given in parameters or Unsnapshots the current one", dependsOn: "initReleaseBranch") << this.&changeAndCommitReleaseVersion
            project.task('runBuildTasks', description: 'Runs the build process in a separate gradle run.', dependsOn: "commitReleaseFiles", type: GradleBuild) {
                startParameter = project.getGradle().startParameter.newInstance()
                tasks = [
                        'uploadArtifacts'
                ]
            }
            project.task("startRelease", description: "Prepares release branch, change version, builds the app", group: TASK_GROUP, dependsOn: "runBuildTasks")


            project.task("mergeAndTagRelease", description: "Merge on the main Branch and tag", mustRunAfter: "startRelease") << this.&mergeAndTagRelease
            project.task("changeAndCommitNewVersion", description: "Changes the version with the one given in parameters or Snapshots the next one", dependsOn: "mergeAndTagRelease") << this.&changeAndCommitNewVersion
            project.task("mergeOnWorkingBranch", description: "Merge on the default branch or on the develop branch by default", dependsOn: "changeAndCommitNewVersion") << this.&mergeOnWorkingBranch
            project.task("endRelease", description: "Merge on master, Tags, change version, merge on develop", group: TASK_GROUP, dependsOn: "mergeOnWorkingBranch", mustRunAfter: "startRelease")
            project.task("delivery", description: "Performs a full release of your app", group: TASK_GROUP, dependsOn: ["startRelease", "endRelease"])

            //Build tasks
            Task buildArtifacts = project.task("buildArtifacts", description: "build all artifacts", group: TASK_GROUP)
            Task uploadArtifacts = project.task("uploadArtifacts", description: "upload built artifacts to repository", group: TASK_GROUP, dependsOn: "buildArtifacts") << this.&uploadArtifact
            configurator.configureBuildTasks(buildArtifacts, uploadArtifacts);*/
        }
    }


    void setupProperties() {
        PropertiesFileUtils.readAndApplyPropertiesFile(project, project.file(DELIVERY_CONF_FILE));
        File versionFile

        if (project.hasProperty('versionFilePath')) {
            versionFile = project.file(project.property('versionFilePath'))
        } else {
            versionFile = project.file('version.properties')
        }

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

        PropertiesFileUtils.readAndApplyPropertiesFile(project, versionFile)
        project.ext.versionId = project.ext."${project.ext.versionIdKey}"
        project.ext.version = project.ext."${project.ext.versionKey}"
        project.version = project.ext."${project.ext.versionKey}"
    }
}
