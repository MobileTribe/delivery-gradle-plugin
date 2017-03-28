package com.leroymerlin.plugins.core.configurators

import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.entities.SigningProperty
import org.gradle.api.Project

/**
 * Created by florian on 30/01/2017.
 */
public class ProjectConfigurator {

    Project project
    DeliveryPluginExtension extension

    public void setup(Project project, DeliveryPluginExtension extension) {
        this.project = project
        this.extension = extension
    }

    public boolean handleProject(Project project){
        return false;
    }

    public void configure() {}

    public void applySigningProperty(SigningProperty property) {}

    public void applyProperties() {}

    def teste(){}
}
