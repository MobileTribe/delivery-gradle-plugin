package com.leroymerlin.plugins.core.configurators

import com.leroymerlin.plugins.DeliveryPlugin
import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.tasks.build.JavaBuild
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.maven.Conf2ScopeMappingContainer

import java.util.logging.Logger

/**
 * Created by florian on 30/01/2017.
 */
class JavaConfigurator extends ProjectConfigurator {

    private final String JAVA_PLUGIN_ID = "java"
    private boolean isJavaProject

    @Override
    void setup(Project project, DeliveryPluginExtension extension) {
        super.setup(project, extension)
        isJavaProject = project.plugins.hasPlugin(JAVA_PLUGIN_ID)
        if (!isJavaProject) {
            throw new GradleException("Your project must apply java plugin to use " + getClass().simpleName)
        }
    }

    @Override
    void configure() {
        //configure project with maven convention
        this.extension.plugin.mapToMavenConfiguration(DeliveryPlugin.COMPILE_PRIORITY, "compile", Conf2ScopeMappingContainer.COMPILE)
        this.extension.plugin.mapToMavenConfiguration(DeliveryPlugin.RUNTIME_PRIORITY, "runtime", Conf2ScopeMappingContainer.RUNTIME)
        this.extension.plugin.mapToMavenConfiguration(DeliveryPlugin.RUNTIME_PRIORITY, "implementation", Conf2ScopeMappingContainer.RUNTIME)
        this.extension.plugin.mapToMavenConfiguration(DeliveryPlugin.TEST_COMPILE_PRIORITY, "testCompile", Conf2ScopeMappingContainer.TEST)
        this.extension.plugin.mapToMavenConfiguration(DeliveryPlugin.TEST_RUNTIME_PRIORITY, "testRuntime", Conf2ScopeMappingContainer.TEST)
        this.extension.plugin.mapToMavenConfiguration(DeliveryPlugin.TEST_RUNTIME_PRIORITY, "testImplementation", Conf2ScopeMappingContainer.TEST)
        if (project.plugins.hasPlugin("java-library") || project.plugins.hasPlugin(AndroidConfigurator.ANDROID_LIBRARY_PLUGIN_ID)) {
            this.extension.plugin.mapToMavenConfiguration(DeliveryPlugin.COMPILE_PRIORITY, "api", Conf2ScopeMappingContainer.COMPILE)
        }

        if (!project.group) {
            throw new GradleException("Project group is not defined. Please use a gradle properties group")
        }
        Logger.global.info("group used : ${project.group}")

        Logger.global.info("Generate Java Build tasks")
        project.task("build${project.artifact.capitalize()}Artifacts", type: JavaBuild, group: DeliveryPlugin.TASK_GROUP) {
            variantName project.artifact
        }
    }

    @Override
    boolean handleProject(Project project) {
        return project.plugins.hasPlugin(JAVA_PLUGIN_ID)
    }
}
