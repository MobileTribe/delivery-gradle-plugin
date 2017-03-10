package com.leroymerlin.plugins.core

import com.leroymerlin.plugins.DeliveryPlugin
import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.tasks.DeliveryBuildTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Upload
import org.gradle.api.tasks.bundling.Jar
import org.gradle.internal.impldep.com.google.common.base.CaseFormat
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by florian on 30/01/2017.
 */
class AndroidConfigurator extends ProjectConfigurator {

    Logger logger = LoggerFactory.getLogger('AndroidConfigurator')
    boolean isAndroidApp, isAndroidLibrary

    @Override
    void setup(Project project, DeliveryPluginExtension extension) {
        super.setup(project, extension)
        isAndroidApp = project.plugins.hasPlugin("com.android.application")
        isAndroidLibrary = project.plugins.hasPlugin("com.android.library")
        if (handleProject(project)) {
            throw new IllegalStateException("Your project must apply com.android.application or com.android.library to use " + getClass().simpleName)
        }

        //TODO def signingProperties = project.container(SigningProperty)
        //TODO check que les version / versionId soient bien configurées sur l'extension android
        /*if (isAndroidApp) {
            project.android {
                buildTypes.all {
                    buildType ->
                        signingProperties.maybeCreate(buildType.name)
                }
            }
        }*/
    }

    @Override
    boolean handleProject(Project project) {
        return project.plugins.hasPlugin("com.android.application") || project.plugins.hasPlugin("com.android.library")
    }

    @Override
    void configureBuildTasks(Task buildTask, Task archiveTask) {

        if (isAndroidApp) {
            String flavorName = project.projectName.toString().split(' ').collect({ m -> return m.toLowerCase().capitalize() }).join("") + variant.flavorName.capitalize()
            flavorName = flavorName[0].toLowerCase() + flavorName.substring(1)

            project.android.applicationVariants.all { variant ->


            }
        }

        //TODO remove
        logger.info("Generate Android Build and Archive tasks")
        def uploadArtifactTasks = ['check']

        if (isAndroidApp) {
            project.android.applicationVariants.all { variant ->
                createBuildTask(variant)
            }
        } else {
            project.android.libraryVariants.all { variant ->
                def name = variant.buildType.name
                if (name.equals('release')) {
                    def sourcesJar = project.task("sources${variant.name.capitalize()}Jar", type: Jar) {
                        classifier = 'sources'
                        from variant.javaCompile.destinationDir
                    }
                    sourcesJar.dependsOn variant.javaCompile
                    project.artifacts.add('archives', sourcesJar)
                }
            }
            //for library or java code we use the standard plugin
            uploadArtifactTasks += "uploadArchives"
            project.uploadArchives {
                repositories extension.archiveRepositories
            }
        }
        buildTask.dependsOn 'build'
        archiveTask.dependsOn uploadArtifactTasks
    }

    @Override
    void applyVersion(String version, String versionId, String projectName) {
        project.android {
            defaultConfig {
                versionName version
                versionCode Integer.parseInt(versionId)
            }
        }
    }

    private void createBuildTask(variant) {
        String flavorName = project.projectName.toString().split(' ').collect({ m -> return m.toLowerCase().capitalize() }).join("") + variant.flavorName.capitalize()
        flavorName = flavorName[0].toLowerCase() + flavorName.substring(1)


        Map<String, File> flavorOutputFiles = []

        def classifier = variant.buildType.name
        if (variant.signingReady) {
            flavorOutputFiles.put(classifier, variant.outputs.get(0).outputFile)
            if (variant.testVariant) {
                flavorOutputFiles.put("test-" + classifier, variant.testVariant.outputs.get(0).outputFile)
            }
            if (variant.mappingFile) {
                if (!variant.mappingFile.exists()) {
                    variant.mappingFile.parentFile.mkdirs()
                    variant.mappingFile.createNewFile()
                }
                flavorOutputFiles.put("mapping-" + classifier, variant.mappingFile)
            }

            def sourcesJar = project.task("sources${variant.name.capitalize()}Jar", type: Jar) {
                classifier = 'sources'
                from variant.javaCompile.destinationDir
            }
            sourcesJar.dependsOn variant.javaCompile
            flavorOutputFiles.put("sources-" + classifier, sourcesJar.outputs.getFiles().getSingleFile())
        } else {
            logger.warn("$classifier has no valid signing config and will not be archived")
        }


        project.task("build${flavorName.capitalize()}Artufacts", type: DeliveryBuildTask, group: DeliveryPlugin.TASK_GROUP) {
            configurationName = flavorName
            outputFiles = flavorOutputFiles
        }

    }

/*    private void addArtifacts(File outputFile, Task depTask, String variantName, String flavorName, String classifier, String extension) {
        project.configurations."${variantName}".artifacts.add(
                new ArchiveArtifact(flavorName, extension, classifier, outputFile, depTask)
        )
    }*/
}
