package com.leroymerlin.plugins.core.configurators

import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.entities.SigningProperty
import org.gradle.api.Project

/**
 * Created by florian on 30/01/2017.
 */
class ProjectConfigurator {

    Project project
    DeliveryPluginExtension extension

    def setup(Project project, DeliveryPluginExtension extension) {
        this.project = project
        this.extension = extension
    }

    boolean handleProject(Project project){
        return false;
    }

    def configure() {}

    def applySigningProperty(SigningProperty property) {}

    def applyProperties(String version, String versionId, String projectName) {}
}
