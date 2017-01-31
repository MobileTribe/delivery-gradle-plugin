package com.leroymerlin.plugins.core

import com.leroymerlin.plugins.DeliveryPluginExtension
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Created by florian on 30/01/2017.
 */
abstract class BaseScmAdapter {

    Project project
    DeliveryPluginExtension extension

    void setup(Project project, DeliveryPluginExtension extension) {
        this.project = project;
        this.extension = extension;
    }

    void initReleaseBranch() {}
}
