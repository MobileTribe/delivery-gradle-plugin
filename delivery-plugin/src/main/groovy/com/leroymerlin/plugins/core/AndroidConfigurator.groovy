package com.leroymerlin.plugins.core

import com.leroymerlin.plugins.DeliveryPlugin
import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.entities.SigningProperty
import com.leroymerlin.plugins.tasks.build.AndroidBuild
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by florian on 30/01/2017.
 */
class AndroidConfigurator extends ProjectConfigurator {

    static String ANDROID_PLUGIN_ID = "com.android.application"
    static String ANDROID_LIBRARY_PLUGIN_ID = "com.android.library"

    Logger logger = LoggerFactory.getLogger('AndroidConfigurator')
    boolean isAndroidApp, isAndroidLibrary

    @Override
    def setup(Project project, DeliveryPluginExtension extension) {
        super.setup(project, extension)
        isAndroidApp = project.plugins.hasPlugin(ANDROID_PLUGIN_ID)
        isAndroidLibrary = project.plugins.hasPlugin(ANDROID_LIBRARY_PLUGIN_ID)
        if (!isAndroidApp && !isAndroidLibrary) {
            throw new GradleException("Your project must apply com.android.application or com.android.library to use " + getClass().simpleName)
        }


        project.android {
            defaultConfig {
                versionName project.version
                versionCode Integer.parseInt(project.versionId)
            }

            buildTypes.all {
                buildType ->
                    extension.signingProperties.maybeCreate(buildType.name)
            }
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
    def configure() {

        //Check that properties are applied on android extension
        String version = project.version
        if (isAndroidApp) {
            if (!(project.android.defaultConfig.versionName == version)) {
                throw new GradleException("app versionName is ${project.android.defaultConfig.versionName} but should be $version. Please set: android.defaultConfig.versionName version")
            }
            if (!(project.android.defaultConfig.versionCode == Integer.parseInt(project.versionId))) {
                throw new GradleException("app versionCode is ${project.android.defaultConfig.versionCode} but should be ${project.versionId}. Please set: android.defaultConfig.versionCode Integer.parseInt(versionId)")
            }

            if (project.android.defaultConfig.applicationId)
                project.group = project.android.defaultConfig.applicationId
        } else if (isAndroidLibrary) {
            def manifestFile = project.file("src/main/AndroidManifest.xml")
            if (manifestFile.exists()) {
                def manifest = new XmlParser(false, false).parse(manifestFile)
                project.group = manifest."@package"
            }
        }


        if (!project.group) {
            throw new GradleException("Project group is not defined. Please use a gradle properties or configure your defaultConfig.applicationId")
        }
        logger.info("group used : ${project.group}")


        logger.info("Generate Android Build tasks")
        if (isAndroidApp) {
            project.android.applicationVariants.all { currentVariant ->
                String flavorName = project.projectName.toString().split(' ').collect({ m -> return m.toLowerCase().capitalize() }).join("") + currentVariant.flavorName.capitalize()
                flavorName = flavorName[0].toLowerCase() + flavorName.substring(1)

                def buildTaskName = "build${flavorName.capitalize()}Artifacts"
                if (project.tasks.findByPath(buildTaskName) == null) {
                    project.task(buildTaskName, type: AndroidBuild, group: DeliveryPlugin.TASK_GROUP) {
                        variantName flavorName
                    }
                }
                project.tasks.findByPath(buildTaskName).addVariant(currentVariant)
            }
        } else {
            //TODO check if Buildtask should be created
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
        }
    }

    @Override
    def applyProperties(String version, String versionId, String projectName) {
    }

    @Override
    def applySigningProperty(SigningProperty signingProperty) {
        if (isAndroidApp) {
            def buildType = project.android.buildTypes.findByName(signingProperty.name)


            Properties properties = new Properties()

            if (buildType == null) {
                throw new IllegalStateException("Signing property can't apply on missing buildType : " + signingProperty.name)
            } else if (signingProperty.propertiesFile == null) {
                logger.warn("Signing property file not defined");
                return;
            } else if (!signingProperty.propertiesFile.exists()) {
                logger.warn("Signing property file doesn't exist : " + signingProperty.propertiesFile)
                return;
            } else {
                signingProperty.propertiesFile.withInputStream {
                    stream -> properties.load(stream)
                }
                def filePath = properties.getProperty(signingProperty.storeFileField)
                if (filePath == null) {
                    throw new IllegalStateException("KS can't be found with null filePath. Please add ${signingProperty.storeFileField} in $signingProperty.propertiesFile")
                } else if (!project.file(filePath).exists()) {
                    throw new IllegalStateException("KS not found for buildType '${signingProperty.name}' at path $filePath")
                }
            }

            def ksFile = project.file(properties.getProperty(signingProperty.storeFileField))

            project.android.signingConfigs {
                "${signingProperty.name}Signing" {
                    storeFile ksFile
                    storePassword properties.getProperty(signingProperty.storePasswordField)
                    keyAlias properties.getProperty(signingProperty.keyAliasField)
                    keyPassword properties.getProperty(signingProperty.keyAliasPasswordField)
                }
            }
            buildType.signingConfig project.android.signingConfigs."${signingProperty.name}Signing"
        }
    }
/*    private void addArtifacts(File outputFile, Task depTask, String variantName, String flavorName, String classifier, String extension) {
        project.configurations."${variantName}".artifacts.add(
                new ArchiveArtifact(flavorName, extension, classifier, outputFile, depTask)
        )
    }*/

    @Override
    boolean handleProject(Project project) {
        return project.plugins.hasPlugin(ANDROID_PLUGIN_ID) || project.plugins.hasPlugin(ANDROID_LIBRARY_PLUGIN_ID)
    }
}
