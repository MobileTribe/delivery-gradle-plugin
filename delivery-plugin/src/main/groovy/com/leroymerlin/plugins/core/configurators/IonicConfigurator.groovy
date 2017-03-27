package com.leroymerlin.plugins.core.configurators

import com.leroymerlin.plugins.DeliveryPlugin
import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.entities.SigningProperty
import com.leroymerlin.plugins.tasks.build.AndroidBuild
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by alexandre on 27/03/2017.
 */
class IonicConfigurator extends ProjectConfigurator {


    static String ANDROID_PLUGIN_ID = "com.android.application"

    Logger logger = LoggerFactory.getLogger('AndroidConfigurator')
    boolean isAndroidApp

    @Override
    def setup(Project project, DeliveryPluginExtension extension) {
        super.setup(project, extension)
        isAndroidApp = project.plugins.hasPlugin(ANDROID_PLUGIN_ID)
        if (!isAndroidApp) {
            throw new GradleException("Your project must apply com.android.application to use " + getClass().simpleName)
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
    }

    @Override
    def configure() {

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
        }
    }

    @Override
    def applyProperties(String version, String versionId, String projectName) {
    }

    @Override
    def applySigningProperty(SigningProperty signingProperty) {
        if (isAndroidApp) {
            def buildType = project.android.buildTypes.findByName(signingProperty.name)

            if (buildType == null) {
                throw new IllegalStateException("Signing property can't apply on missing buildType : " + signingProperty.name)
            }

            if (signingProperty.storeFile == null) {
                return
            }

            if (!project.file(signingProperty.storeFile).exists()) {
                throw new IllegalStateException("KS not found for buildType '${signingProperty.name}' at path $filePath")
            }

            def ksFile = project.file(signingProperty.storeFile)

            project.android.signingConfigs {
                "${signingProperty.name}Signing" {
                    storeFile ksFile
                    storePassword signingProperty.storePassword
                    keyAlias signingProperty.keyAlias
                    keyPassword signingProperty.keyAliasPassword
                }
            }
            buildType.signingConfig project.android.signingConfigs."${signingProperty.name}Signing"
        }
    }

    @Override
    boolean handleProject(Project project) {
        return project.plugins.hasPlugin(ANDROID_PLUGIN_ID)
    }
}
