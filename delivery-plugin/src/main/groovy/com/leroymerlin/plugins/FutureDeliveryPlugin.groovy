package com.leroymerlin.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.GradleBuild
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class FutureDeliveryPlugin implements Plugin<Project> {
    public Logger logger = LoggerFactory.getLogger('DeliveryPlugin')

    static final String TASK_GROUP = 'delivery'


    @Override
    void apply(Project project) {

        //config project
        //detect ios / android / library
        project.task("initReleaseBranch", description: "Creates or switch to the release branch") << this.&initReleaseBranch
        project.task("changeAndCommitReleaseVersion", description: "Changes the version with the one given in parameters or Unsnapshots the current one", dependsOn: "initReleaseBranch") << this.&changeAndCommitReleaseVersion
        project.task('runBuildTasks', description: 'Runs the build process in a separate gradle run.', dependsOn: "changeAndCommitReleaseVersion", type: GradleBuild) {
            startParameter = project.getGradle().startParameter.newInstance()
            tasks = [
                    'uploadArtifacts'
            ]
        }
        project.task("startRelease", description: "Prepares release branch, change version, builds the app", group: TASK_GROUP, dependsOn:"runBuildTasks")


        project.task("mergeAndTagRelease", description: "Merge on the main Branch and tag", mustRunAfter: "startRelease") << this.&mergeAndTagRelease
        project.task("changeAndCommitNewVersion", description: "Changes the version with the one given in parameters or Snapshots the next one", dependsOn:"mergeAndTagRelease") << this.&changeAndCommitNewVersion
        project.task("mergeOnWorkingBranch", description: "Merge on the default branch or on the develop branch by default", dependsOn:"changeAndCommitNewVersion") << this.&mergeOnWorkingBranch
        project.task("endRelease", description: "Merge on master, Tags, change version, merge on develop", group: TASK_GROUP,dependsOn: "mergeOnWorkingBranch", mustRunAfter: "startRelease")
        project.task("delivery", description: "Performs a full release of your app", group: TASK_GROUP, dependsOn: ["startRelease", "endRelease"])

        //Build tasks
        project.task("buildArtifacts", description: "build all artifacts", group: TASK_GROUP) << this.&buildArtifacts
        project.task("uploadArtifacts", description: "upload built artifacts to repository", group: TASK_GROUP, dependsOn: "buildArtifacts") << this.&uploadArtifact

    }


    public buildArtifacts() {}

    public uploadArtifact() {}

    public initReleaseBranch() {}

    public changeAndCommitReleaseVersion() {}

    public mergeAndTagRelease() {}

    public changeAndCommitNewVersion() {}

    public mergeOnWorkingBranch() {}
}
