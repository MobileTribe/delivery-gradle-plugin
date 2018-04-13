package com.leroymerlin.plugins.core.configurators

import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.cli.DeliveryLogger
import com.leroymerlin.plugins.entities.SigningProperty
import org.gradle.api.Project

/**
 * Created by florian on 30/01/2017.
 */
class ProjectConfigurator {

    protected Project project
    protected DeliveryPluginExtension extension
    public final DeliveryLogger deliveryLogger = new DeliveryLogger()

    void setup(Project project, DeliveryPluginExtension extension) {
        this.project = project
        this.extension = extension
    }

    boolean handleProject(Project project) {
        return false
    }

    void configure() {}

    void applySigningProperty(SigningProperty property) {}

    void applyProperties() {}
}
