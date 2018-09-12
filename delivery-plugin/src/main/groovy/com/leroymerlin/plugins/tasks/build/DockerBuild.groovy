package com.leroymerlin.plugins.tasks.build

import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.cli.DeliveryLogger
import com.leroymerlin.plugins.cli.Executor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Created by alexandre on 15/02/2017.
 */
class DockerBase extends DefaultTask {

    public final DeliveryLogger deliveryLogger = new DeliveryLogger()

    @Input
    String registry

    @Input
    String imageName = project.artifact

    String getName() {
        return "${imageName}:${project.version}"
    }

}

class DockerBuild extends DockerBase {

    @Input
    String buildPath = "."

    @TaskAction
    void run() {
        def fullImageName = getName()
        cmd("docker build -t $fullImageName ${buildPath}")
    }

    def cmd(String cmd) {
        return Executor.exec(Executor.convertToCommandLine(cmd), [directory: project.projectDir])
    }

}


class DockerUpload extends DefaultTask {

    @TaskAction
    void run() {
        def extension = project.delivery as DeliveryPluginExtension
        def registryProperties = extension.registryProperties.getByName(registry)

        if (registryProperties != null && registryProperties.url != null) {
            def url = registryProperties.url
            def password = registryProperties.password
            def user = registryProperties.user
            def passwordParam = ""
            if (password != null) {
                passwordParam = "-p $password "
            }
            def userParam = ""
            if (user != null) {
                userParam = "-u $user"
            }

            def fullName = "${url}/${getName()}"
            cmd("docker tag ${getName()} " + fullName)
            cmd("docker login $passwordParam $userParam $url")
            cmd("docker push $fullName")
        } else {
            deliveryLogger.logWarning("can't create docker image. Registry $registry not configured")
        }


    }

}