package com.leroymerlin.plugins.core.configurators

import com.leroymerlin.plugins.DeliveryPlugin
import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.tasks.build.JavaBuild
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by florian on 30/01/2017.
 */
class JavaConfigurator extends ProjectConfigurator {

    private final String JAVA_PLUGIN_ID = "java"
    private final Logger logger = LoggerFactory.getLogger('JavaConfigurator')
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
        if (!project.group) {
            throw new GradleException("Project group is not defined. Please use a gradle properties group")
        }
        logger.info("group used : ${project.group}")

        logger.info("Generate Java Build tasks")
        project.task("build${project.projectName}Artifacts", type: JavaBuild, group: DeliveryPlugin.TASK_GROUP) {
            variantName project.projectName
        }
    }

    @Override
    boolean handleProject(Project project) {
        return project.plugins.hasPlugin(JAVA_PLUGIN_ID)
    }
}
