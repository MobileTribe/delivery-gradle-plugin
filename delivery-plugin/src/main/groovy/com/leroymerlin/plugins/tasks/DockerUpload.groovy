package com.leroymerlin.plugins.tasks

import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.entities.RegistryProperty
import com.leroymerlin.plugins.tasks.build.DockerBuild
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Created by alexandre on 15/02/2017.
 */
class DockerUpload extends DefaultTask {

    @Input
    DockerBuild buildTask

    @TaskAction
    void run() {
        RegistryProperty registryProperties = getRegistry()

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

            def fullName = "${url}/${buildTask.getImageName()}"
            buildTask.cmd("docker tag ${buildTask.getImageName()} " + fullName)
            buildTask.cmd("docker login $passwordParam $userParam $url", [hideCommand:true])
            buildTask.cmd("docker push $fullName")
        } else {
            buildTask.deliveryLogger.logWarning("can't create docker image. Registry ${buildTask.registry} not configured")
        }


    }

    RegistryProperty getRegistry() {
        def extension = project.delivery as DeliveryPluginExtension
        def registryProperties = extension.registryProperties.getByName(buildTask.registry)
        registryProperties
    }
}