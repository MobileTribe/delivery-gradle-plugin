package com.leroymerlin.plugins.core

import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.tasks.GitBranchTask
import org.gradle.api.Project

/**
 * Created by florian on 30/01/2017.
 */
class BaseScmAdapter {

    Project project
    DeliveryPluginExtension extension
    String name, version, comment, releaseBranchName
    GitBranchTask gitBranchTask

    void setup(Project project, DeliveryPluginExtension extension, String comment) {
        this.project = project
        this.extension = extension
        this.name = project.properties["projectName"]
        this.version = project.properties["version"]
        this.comment = comment
        this.releaseBranchName = this.name + "-" + this.version
    }
}
