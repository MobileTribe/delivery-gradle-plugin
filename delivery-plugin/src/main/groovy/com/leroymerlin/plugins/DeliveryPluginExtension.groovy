package com.leroymerlin.plugins

import com.leroymerlin.plugins.core.BaseScmAdapter
import com.leroymerlin.plugins.core.GitAdapter
import com.leroymerlin.plugins.core.ProjectConfigurator
import com.leroymerlin.plugins.entities.Flow
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

class DeliveryPluginExtension {

    Project project
    ProjectConfigurator mConfigurator
    BaseScmAdapter scmAdapter
    NamedDomainObjectContainer<Flow> flowsContainer
    DeliveryPlugin plugin

    DeliveryPluginExtension(Project project, DeliveryPlugin deliveryPlugin) {
        this.project = project
        this.plugin = deliveryPlugin
        this.scmAdapter = new GitAdapter()
        this.flowsContainer = project.container(Flow, { String name ->
            return Flow.newInstance(name, this)
        })
    }

    void setConfigurator(Class<? extends ProjectConfigurator> configuratorClass) {
        setConfigurator(configuratorClass.newInstance())
    }

    def flows(Action<? super NamedDomainObjectContainer<Flow>> action) {
        action.execute(flowsContainer)
    }

    void setConfigurator(ProjectConfigurator configurator) {
        this.mConfigurator = configurator
        this.mConfigurator.setup(project, this)
        this.mConfigurator.applyVersion(project.version, project.ext.versionId, project.ext.projectName)
    }

    ProjectConfigurator getConfigurator() {
        return mConfigurator
    }
}
