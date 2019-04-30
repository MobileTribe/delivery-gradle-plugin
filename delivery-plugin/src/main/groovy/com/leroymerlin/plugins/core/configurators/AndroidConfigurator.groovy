package com.leroymerlin.plugins.core.configurators

import com.leroymerlin.plugins.DeliveryPlugin
import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.entities.SigningProperty
import com.leroymerlin.plugins.tasks.build.AndroidBuild
import com.leroymerlin.plugins.tasks.build.AndroidLibBuild
import com.leroymerlin.plugins.tasks.build.PrepareBuildTask
import com.leroymerlin.plugins.utils.PropertiesUtils
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.maven.Conf2ScopeMappingContainer
import org.gradle.api.tasks.Delete

/**
 * Created by florian on 30/01/2017.
 */
class AndroidConfigurator extends ProjectConfigurator {

    private final String ANDROID_PLUGIN_ID = "com.android.application"
    public static final String ANDROID_LIBRARY_PLUGIN_ID = "com.android.library"
    boolean isAndroidApp, isAndroidLibrary

    @Override
    void setup(Project project, DeliveryPluginExtension extension) {
        super.setup(project, extension)
        isAndroidApp = project.plugins.hasPlugin(ANDROID_PLUGIN_ID)
        isAndroidLibrary = project.plugins.hasPlugin(ANDROID_LIBRARY_PLUGIN_ID)
        isAndroidLibrary = project.plugins.hasPlugin(ANDROID_LIBRARY_PLUGIN_ID)
        if (!isAndroidApp && !isAndroidLibrary) {
            throw new GradleException("Your project must apply com.android.application or com.android.library to use " + getClass().simpleName)
        }

        project.android {
            defaultConfig {
                try {
                    versionName project.version
                    versionCode Integer.parseInt(project.versionId as String)
                } catch (Exception e) {
                    throw new GradleException("${project.versionKey} or ${project.versionIdKey} is null, please set it in version.properties file. " +
                            "For more informations : $e")
                }
            }
            buildTypes.all {
                buildType ->
                    extension.signingProperties.maybeCreate(buildType.name as String)
            }
        }

        project.task("prepareAndroidBuild", type: PrepareBuildTask, group: DeliveryPlugin.TASK_GROUP)

    }

    @Override
    void configure() {

        project.tasks.findByPath("assemble")
                .dependsOn(project.task("clearGeneratedFiles", type: Delete, group: DeliveryPlugin.TASK_GROUP).doFirst {
            delete project.rootProject.file("/build/generated")
        })

        //configure project with maven convention
        this.extension.plugin.mapToMavenConfiguration(DeliveryPlugin.COMPILE_PRIORITY, "compile", Conf2ScopeMappingContainer.COMPILE)
        this.extension.plugin.mapToMavenConfiguration(DeliveryPlugin.COMPILE_PRIORITY + 1, "implementation", Conf2ScopeMappingContainer.COMPILE)
        this.extension.plugin.mapToMavenConfiguration(DeliveryPlugin.COMPILE_PRIORITY + 2, "api", Conf2ScopeMappingContainer.COMPILE)
        this.extension.plugin.mapToMavenConfiguration(DeliveryPlugin.RUNTIME_PRIORITY, "runtime", Conf2ScopeMappingContainer.RUNTIME)
        this.extension.plugin.mapToMavenConfiguration(DeliveryPlugin.TEST_COMPILE_PRIORITY, "testCompile", Conf2ScopeMappingContainer.TEST)
        this.extension.plugin.mapToMavenConfiguration(DeliveryPlugin.TEST_COMPILE_PRIORITY + 1, "testImplementation", Conf2ScopeMappingContainer.TEST)
        this.extension.plugin.mapToMavenConfiguration(DeliveryPlugin.TEST_RUNTIME_PRIORITY, "testRuntime", Conf2ScopeMappingContainer.TEST)

        //Check that properties are applied on android extension
        String version = project.version
        if (isAndroidApp) {
            if (!(project.android.defaultConfig.versionName == version)) {
                throw new GradleException("app versionName is ${project.android.defaultConfig.versionName} but should be $version. Please set: android.defaultConfig.versionName version")
            }
            if (!(project.android.defaultConfig.versionCode == Integer.parseInt(project.versionId as String))) {
                throw new GradleException("app versionCode is ${project.android.defaultConfig.versionCode} but should be ${project.versionId}. Please set: android.defaultConfig.versionCode Integer.parseInt(versionId)")
            }
        }
        if (!PropertiesUtils.userHasDefineProperty(project, "group")) {
            if (isAndroidApp) {
                if (project.android.defaultConfig.applicationId) {
                    project.group = project.android.defaultConfig.applicationId
                    project.ext.group = project.group
                }
            } else if (isAndroidLibrary) {
                def manifestFile = project.file("src/main/AndroidManifest.xml")
                if (manifestFile.exists()) {
                    def manifest = new XmlParser(false, false).parse(manifestFile)
                    project.group = manifest."@package"
                    project.ext.group = project.group
                }
            }
        }

        if (!project.group) {
            throw new GradleException("Project group is not defined. Please use a gradle properties or configure your defaultConfig.applicationId")
        }
        if (isAndroidApp) {

            boolean isFlutter = project.plugins.find { it.class.simpleName.equals("FlutterPlugin") } != null
            project.android.applicationVariants.all { currentVariant ->
                if (isFlutter && currentVariant.buildType.name.startsWith("dynamic")) {
                    //we skip dynamic build to avoid flutter error
                    //issue: https://github.com/flutter/flutter/issues/23208

                    project.tasks.findByName("flutterBuild${currentVariant.buildType.name.capitalize()}").enabled = false
                    //currentVariant.assemble.enabled = false
                    //project.tasks.findByName('test').dependsOn -= "test${currentVariant.buildType.name.capitalize()}UnitTest"
                    deliveryLogger.logInfo("${currentVariant.buildType.name} flutter buildtype skipped")
                } else {


                    String flavorName = project.artifact.toString().split(' ').collect({ m -> return m.toLowerCase().capitalize() }).join("") + (currentVariant.flavorName.capitalize() ? "-${currentVariant.flavorName.capitalize()}" : "")
                    flavorName = flavorName[0].toLowerCase() + flavorName.substring(1)
                    String flavorNameNexus = project.artifact.toString().split(' ').collect({ m -> return m.toLowerCase() }).join("-") + (currentVariant.flavorName.toLowerCase() ? "-${currentVariant.flavorName.toLowerCase()}" : "")

                    def buildTaskName = "build${flavorName.capitalize()}Artifacts"
                    if (project.tasks.findByPath(buildTaskName) == null) {
                        project.task(buildTaskName, type: AndroidBuild, group: DeliveryPlugin.TASK_GROUP) {
                            variantName flavorNameNexus
                        }
                    }
                    project.tasks.findByPath(buildTaskName).addVariant(currentVariant)
                }
            }
        } else {
            project.android.libraryVariants.all { currentVariant ->
                if (currentVariant.buildType.name == "release") {
                    String flavorName = project.artifact.toString().split(' ').collect({ m -> return m.toLowerCase().capitalize() }).join("") + (currentVariant.flavorName.capitalize() ? "-${currentVariant.flavorName.capitalize()}" : "")
                    flavorName = flavorName[0].toLowerCase() + flavorName.substring(1)
                    String flavorNameNexus = project.artifact.toString().split(' ').collect({ m -> return m.toLowerCase() }).join("-") + (currentVariant.flavorName.toLowerCase() ? "-${currentVariant.flavorName.toLowerCase()}" : "")

                    def buildTaskName = "build${flavorName.capitalize()}Artifacts"
                    if (project.tasks.findByPath(buildTaskName) == null) {
                        project.task(buildTaskName, type: AndroidLibBuild, group: DeliveryPlugin.TASK_GROUP) {
                            variantName flavorNameNexus
                        }
                    }
                    project.tasks.findByPath(buildTaskName).addVariant(currentVariant)
                }
            }
        }
    }

    @Override
    void applySigningProperty(SigningProperty signingProperty) {
        if (isAndroidApp) {
            def buildType = project.android.buildTypes.findByName(signingProperty.name)

            if (buildType == null) {
                throw new IllegalStateException("Signing property can't apply on missing buildType : " + signingProperty.name)
            }

            if (signingProperty.storeFile == null) {
                return
            }

            if (!project.file(signingProperty.storeFile).exists()) {
                throw new IllegalStateException("KS not found for buildType '${signingProperty.name}' at path ${signingProperty.storeFile}")
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
        return project.plugins.hasPlugin(ANDROID_PLUGIN_ID) || project.plugins.hasPlugin(ANDROID_LIBRARY_PLUGIN_ID)
    }
}
