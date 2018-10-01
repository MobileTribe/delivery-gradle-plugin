package com.leroymerlin.plugins.tasks

import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.cli.Executor
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


            def fullName = "${url}/${buildTask.getImageName()}"


            Executor.exec(["docker", "tag", buildTask.getImageName(), fullName]){
                directory = project.projectDir
            }


            def loginParams = ["docker", "login"]

            if (password != null) {
                loginParams.add("-p")
                loginParams.add(password)
            }
            if (user != null) {
                loginParams.add("-u")
                loginParams.add(user)
            }
            loginParams.add(url)

            Executor.exec(loginParams){
                directory = project.projectDir
                hideCommand = true
            }
            Executor.exec(["docker", "push", fullName]){
                directory = project.projectDir
            }
        } else {
            buildTask.deliveryLogger.logWarning("can't create docker image. Registry ${buildTask.registry} not configured")
        }


    }

    RegistryProperty getRegistry() {
        def extension = project.delivery as DeliveryPluginExtension
        def registryProperties = extension.dockerRegistries.getByName(buildTask.registry)
        registryProperties
    }
}