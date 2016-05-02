package com.leroymerlin.plugins.utils

import com.google.common.base.CaseFormat
import com.leroymerlin.plugins.DeliveryPlugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.tasks.GradleBuild
import org.gradle.api.tasks.TaskDependency
import org.gradle.api.tasks.Upload
import org.gradle.api.tasks.bundling.Jar

/**
 * Created by fduhen on 22/02/16.
 */
public class BuildMethods {

    private Project project;
    private DeliveryPlugin parent;

    public BuildMethods(Project project, DeliveryPlugin parent) {
        this.project = project
        this.parent = parent
    }


    public createTasksArchiveAPKs() {
        parent.logger.info("Archives the APKs on the choosen file manager")

        def uploadArtifactTasks = [];


        if (project.plugins.hasPlugin("com.android.application")) {
            project.android.applicationVariants.all { variant ->
                handleAPKVariant(variant, uploadArtifactTasks)
            }

        } else {

            if (project.plugins.hasPlugin("com.android.library")) {
                project.android.libraryVariants.all { variant ->
                    def name = variant.buildType.name
                    if (name.equals('release')) {
                        def sourcesJar = project.task("sources${variant.name.capitalize()}Jar", type: Jar) {
                            classifier = 'sources'
                            from variant.javaCompile.destinationDir
                        }
                        sourcesJar.dependsOn variant.javaCompile
                        project.artifacts.add('archives', sourcesJar);
                    }
                }
            }
            def sourcesJar = project.task('sourcesJar', type: Jar) {
                classifier = 'sources'
                from project.sourceSets.main.allJava;
            }
            project.artifacts.add('archives', sourcesJar);

            //for library or java code we use the standard plugin
            uploadArtifactTasks.add("uploadArchives")

            project.uploadArchives {
                repositories parent.extension.archiveRepositories
            }

        }


        project.task('runBuildTasks', group: DeliveryPlugin.TASK_GROUP, description:
                'Runs the build process in a separate gradle run.', type: GradleBuild) {
            startParameter = project.getGradle().startParameter.newInstance()
            tasks = [
                    'beforeReleaseBuild',
                    'uploadArtifacts',
                    'afterReleaseBuild'
            ]
        }
        project.task('beforeReleaseBuild', group: DeliveryPlugin.TASK_GROUP, description:
                'Runs immediately before the build when doing a release') {
        }
        project.task('afterReleaseBuild', group: DeliveryPlugin.TASK_GROUP, description:
                'Runs immediately after the build when doing a release') {
        }
        project.task("uploadArtifacts", dependsOn: uploadArtifactTasks, group:
                DeliveryPlugin.TASK_GROUP);


    }


    private void handleAPKVariant(variant, uploadArtifactTasks) {
        String flavorName = project.projectName.toString().split(' ').collect({ m -> return m.toLowerCase().capitalize() }).join("") + variant.flavorName.capitalize();
        flavorName = flavorName[0].toLowerCase() + flavorName.substring(1)
        def uploadTask = "upload${flavorName.capitalize()}Artifacts"
        def configurationName = flavorName + "Config";
        def flavorLowerCase = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, flavorName);

        if (!project.configurations.hasProperty(configurationName)) {
            project.configurations.create(configurationName)
            project.dependencies.add(configurationName, 'org.apache.maven.wagon:wagon-http:2.2')

            uploadArtifactTasks.add(uploadTask);
            project.task(uploadTask, type: Upload, group: DeliveryPlugin.TASK_GROUP) {
                configuration = project.configurations."${configurationName}"
                repositories parent.extension.archiveRepositories
            }
        }

        def classifier = variant.buildType.name
        if (variant.signingReady) {
            addArtifacts(variant.outputs.get(0).outputFile, variant.assemble, configurationName, flavorLowerCase, classifier, "apk")
            if (variant.testVariant) {
                addArtifacts(variant.testVariant.outputs.get(0).outputFile, variant.testVariant.assemble, configurationName, flavorLowerCase, "test-" + classifier, "apk")
            }
            if (variant.mappingFile) {
                addArtifacts(variant.mappingFile, variant.assemble, configurationName, flavorLowerCase, "mapping-" + classifier, "txt")
            }


            def sourcesJar = project.task("sources${variant.name.capitalize()}Jar", type: Jar) {
                classifier = 'sources'
                from variant.javaCompile.destinationDir
            }

            sourcesJar.dependsOn variant.javaCompile
            addArtifacts(sourcesJar.outputs.getFiles().getSingleFile(), sourcesJar, configurationName, flavorLowerCase,  "sources-" +classifier, "jar")

        } else {
            parent.warnOrThrow(false, "$classifier has no valid signing config and will not be archived")
        }

    }

    private void addArtifacts(File outputFile, Task depTask, String configurationName, String flavorName, String classifier, String extension) {
        project.configurations."${configurationName}".artifacts.add(
                new PublishArtifact() {
                    @Override
                    String getName() {
                        return flavorName
                    }

                    @Override
                    String getExtension() {
                        return extension
                    }

                    @Override
                    String getType() {
                        return extension
                    }

                    @Override
                    String getClassifier() {
                        return classifier
                    }

                    @Override
                    File getFile() {
                        return outputFile
                    }

                    @Override
                    Date getDate() {
                        return new Date()
                    }

                    @Override
                    TaskDependency getBuildDependencies() {
                        return new TaskDependency() {
                            @Override
                            Set<? extends Task> getDependencies(Task task) {
                                return Collections.singleton(depTask)
                            }
                        }
                    }
                }
        );
    }

}
