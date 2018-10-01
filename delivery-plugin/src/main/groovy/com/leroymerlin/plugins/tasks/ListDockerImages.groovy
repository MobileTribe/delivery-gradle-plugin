package com.leroymerlin.plugins.tasks

import com.leroymerlin.plugins.cli.DeliveryLogger
import com.leroymerlin.plugins.entities.RegistryProperty
import com.leroymerlin.plugins.tasks.build.DockerBuild
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class ListDockerImages extends DefaultTask {

    private static final DeliveryLogger deliveryLogger = new DeliveryLogger()

    @TaskAction
    def listArtifacts() {
        def taskContainer = project.tasks.withType(DockerUpload)
        if (taskContainer != null && taskContainer.size() > 0) {
            taskContainer.eachWithIndex {
                task, index ->
                    def buildTask = task.buildTask
                    deliveryLogger.logInfo("Image n°${index + 1}")
                    deliveryLogger.logInfo("Name: ${buildTask.imageName}")
                    deliveryLogger.logInfo("Version: ${project.version}")
                    deliveryLogger.logInfo("Registry: ${buildTask.registry}")
                    RegistryProperty property = task.getRegistry()
                    if (property != null && property.url != null) {
                        deliveryLogger.logInfo("Full name: ${property.url}/${buildTask.getImageName()}")
                    } else {
                        buildTask.deliveryLogger.logWarning("Registry not configured ${buildTask.registry} not configured")
                    }
                    deliveryLogger.logInfo("\n")
            }

        } else {
            deliveryLogger.logWarning("No artifacts found")
        }
    }

}
