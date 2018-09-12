package com.leroymerlin.plugins.tasks

import com.leroymerlin.plugins.cli.DeliveryLogger
import com.leroymerlin.plugins.tasks.build.DockerBuild
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class ListDockerImages extends DefaultTask {

    private static final DeliveryLogger deliveryLogger = new DeliveryLogger()

    @TaskAction
    listArtifacts() {
        def taskContainer = project.tasks.withType(DockerBuild)
        if (taskContainer != null && taskContainer.size() > 0) {
            taskContainer.eachWithIndex {
                build, index ->
                    deliveryLogger.logInfo("Image n°${index + 1}")
                    deliveryLogger.logInfo("Name: ${build.imageName}")
                    deliveryLogger.logInfo("Registry: ${build.registry}")
                    deliveryLogger.logInfo("Version: ${project.version}")
                    deliveryLogger.logInfo("Full name: ${build.getFullName()}")
                    deliveryLogger.logInfo("\n")
            }

        } else {
            deliveryLogger.logWarning("No artifacts found")
        }
    }

}
