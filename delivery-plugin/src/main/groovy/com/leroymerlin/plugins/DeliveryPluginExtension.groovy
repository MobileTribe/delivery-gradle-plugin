package com.leroymerlin.plugins

import com.leroymerlin.plugins.core.BaseScmAdapter
import com.leroymerlin.plugins.core.ProjectConfigurator
import org.gradle.api.Project

class DeliveryPluginExtension {

    Project project
    ProjectConfigurator mConfigurator;
    BaseScmAdapter scmAdapter;

    DeliveryPluginExtension(Project project) {
        this.project = project;
    }

    def archiveRepositories = project.ext.properties.containsKey('archiveRepositories') ? project.ext.archiveRepositories : {
    }


    void setConfigurator(Class<? extends ProjectConfigurator> configuratorClass) {
        setConfigurator(configuratorClass.newInstance())

    }

    void setConfigurator(ProjectConfigurator configurator) {
        this.mConfigurator = configurator
        this.mConfigurator.setup(project, this)
        this.mConfigurator.applyVersion(project.version, project.ext.versionId, project.ext.projectName)
    }

    ProjectConfigurator getConfigurator() {
        return mConfigurator;
    }

}
