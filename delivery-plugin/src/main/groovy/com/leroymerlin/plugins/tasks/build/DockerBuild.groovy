package com.leroymerlin.plugins.tasks.build

import com.leroymerlin.plugins.cli.DeliveryLogger
import com.leroymerlin.plugins.cli.Executor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class DockerBuild extends DefaultTask {

    public final DeliveryLogger deliveryLogger = new DeliveryLogger()


    @Input
    String buildPath = "."

    @Input
    String registry

    @Input
    String imageName = project.artifact

    String getImageName() {
        return "${imageName}:${project.version}"
    }

    @TaskAction
    void run() {
        def fullImageName = getImageName()
        Executor.exec(["docker" ,"build", "-t" , fullImageName ,buildPath]){
            directory = project.projectDir
        }
    }

}


