package com.leroymerlin.plugins.core

import com.leroymerlin.plugins.DeliveryPlugin
import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.core.ProjectConfigurator
import com.leroymerlin.plugins.entities.ArchiveArtifact
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Upload
import org.gradle.api.tasks.bundling.Jar
import org.gradle.internal.impldep.com.google.common.base.CaseFormat
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by florian on 30/01/2017.
 */
class ArchiveConfigurator extends ProjectConfigurator {

    Logger logger = LoggerFactory.getLogger('ArchiveConfigurator')

    boolean isAndroidApp
    boolean isAndroidLibrary

    @Override
    void setup(Project project, DeliveryPluginExtension extension) {
        super.setup(project, extension)
        isAndroidApp = project.plugins.hasPlugin("com.android.application")
        isAndroidLibrary = project.plugins.hasPlugin("com.android.library")
        if (!isAndroidApp && !isAndroidLibrary) {
            throw new IllegalStateException("Your project must apply com.android.application or com.android.library to use " + getClass().simpleName)
        }

        //TODO def signingProperties = project.container(SigningProperty)

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
    void configureBuildTasks(Task buildTask, Task archiveTask) {
        logger.info("Generate Android Build and Archive tasks")
        def uploadArtifactTasks = ['check'];

        if (isAndroidApp) {
            project.android.applicationVariants.all { variant ->
                handleAPKVariant(variant, uploadArtifactTasks)
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
                    project.artifacts.add('archives', sourcesJar);
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
                repositories extension.archiveRepositories
            }
        }

        def classifier = variant.buildType.name
        if (variant.signingReady) {
            addArtifacts(variant.outputs.get(0).outputFile, variant.assemble, configurationName, flavorLowerCase, classifier, "apk")
            if (variant.testVariant) {
                addArtifacts(variant.testVariant.outputs.get(0).outputFile, variant.testVariant.assemble, configurationName, flavorLowerCase, "test-" + classifier, "apk")
            }
            if (variant.mappingFile) {
                if (!variant.mappingFile.exists()) {
                    variant.mappingFile.parentFile.mkdirs()
                    variant.mappingFile.createNewFile()
                }
                addArtifacts(variant.mappingFile, variant.assemble, configurationName, flavorLowerCase, "mapping-" + classifier, "txt")
            }


            def sourcesJar = project.task("sources${variant.name.capitalize()}Jar", type: Jar) {
                classifier = 'sources'
                from variant.javaCompile.destinationDir
            }

            sourcesJar.dependsOn variant.javaCompile
            addArtifacts(sourcesJar.outputs.getFiles().getSingleFile(), sourcesJar, configurationName, flavorLowerCase, "sources-" + classifier, "jar")

        } else {
            logger.warn("$classifier has no valid signing config and will not be archived")
        }

    }

    private void addArtifacts(File outputFile, Task depTask, String configurationName, String flavorName, String classifier, String extension) {
        project.configurations."${configurationName}".artifacts.add(
                new ArchiveArtifact(flavorName, extension, classifier, outputFile, depTask)
        )
    }
}
