package com.leroymerlin.plugins

import com.leroymerlin.plugins.core.BaseScmAdapter
import com.leroymerlin.plugins.core.GitAdapter
import com.leroymerlin.plugins.core.configurators.ProjectConfigurator
import com.leroymerlin.plugins.entities.Flow
import com.leroymerlin.plugins.entities.RegistryProperty
import com.leroymerlin.plugins.entities.SigningProperty
import com.leroymerlin.plugins.utils.PropertiesUtils
import com.leroymerlin.plugins.utils.SystemUtils
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.artifacts.maven.MavenDeployer
import org.gradle.testfixtures.ProjectBuilder

class DeliveryPluginExtension {

    private ProjectConfigurator mConfigurator
    private BaseScmAdapter mScmAdapter
    private Closure mArchiveRepositories;

    NamedDomainObjectContainer<SigningProperty> signingProperties
    NamedDomainObjectContainer<RegistryProperty> dockerRegistries
    NamedDomainObjectContainer<Flow> flowsContainer
    Project project
    DeliveryPlugin plugin

    DeliveryPluginExtension(Project project, DeliveryPlugin deliveryPlugin) {
        this.project = project
        this.plugin = deliveryPlugin
        this.flowsContainer = project.container(Flow, { String name ->
            return Flow.newInstance(name, this)
        })
        this.signingProperties = project.container(SigningProperty)
        this.dockerRegistries = project.container(RegistryProperty)
    }

    void setArchiveRepositories(@DelegatesTo(MavenDeployer) Closure closure) {
        mArchiveRepositories = closure
    }

    Closure getArchiveRepositories() {
        //if archive repositories is not define we look for a parent configuration
        if (mArchiveRepositories == null) {
            def parent = PropertiesUtils.findParentProjectWithDelivery(project)
            mArchiveRepositories = parent?.delivery?.archiveRepositories

            if (plugin.isChildEvaluation()) {
                Project parentProject = ProjectBuilder.builder()
                        .withProjectDir(new File(SystemUtils.getEnvProperty(ProjectConfigurator.PARENT_BUILD_ROOT)))
                        .build()
                parentProject.evaluate()
                mArchiveRepositories = parentProject.delivery?.archiveRepositories
            }


            if (mArchiveRepositories == null) {
                mArchiveRepositories = project.ext.properties.containsKey('archiveRepositories') ? project.ext.archiveRepositories : {
                }
            }
        }
        return mArchiveRepositories
    }

    void dockerRegistries(
            @DelegatesTo(RegistryProperty) Action<? super NamedDomainObjectContainer<RegistryProperty>> action) {
        action.execute(dockerRegistries)
    }


    void setEnableReleaseGitFlow(boolean enable) {
        this.plugin.enableReleaseGitFlow(enable)
    }

    boolean autoLinkSubModules = false

    String[] linkedSubModules = []

    void signingProperties(
            @DelegatesTo(SigningProperty) Action<? super NamedDomainObjectContainer<SigningProperty>> action) {
        action.execute(signingProperties)
        signingProperties.each {
            SigningProperty signingProperty ->
                def configurator = getConfigurator()
                if(configurator != null){
                    configurator.applySigningProperty(signingProperty)
                }
        }
    }

    def flows(@DelegatesTo(Flow) Action<? super NamedDomainObjectContainer<Flow>> action) {
        action.execute(flowsContainer)
    }

    void setConfigurator(LinkedHashMap configurator) {
        setConfigurator(configurator as ProjectConfigurator)
    }

    void setConfigurator(ProjectConfigurator configurator) {
        this.mConfigurator = configurator
        this.mConfigurator.setup(project, this)
        this.mConfigurator.applyProperties()
        this.signingProperties.each {
            SigningProperty signingProperty ->
                this.configurator.applySigningProperty(signingProperty)
        }
    }

    ProjectConfigurator getConfigurator() {
        return mConfigurator
    }

    void setScmAdapter(BaseScmAdapter scmAdapter) {
        this.mScmAdapter = scmAdapter
        this.mScmAdapter.setup(project, this)
    }

    BaseScmAdapter getScmAdapter() {
        if (mScmAdapter == null) {
            setScmAdapter(new GitAdapter())
        }
        return mScmAdapter
    }

}
