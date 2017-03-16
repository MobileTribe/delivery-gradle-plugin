package com.leroymerlin.plugins

import com.leroymerlin.plugins.adapters.GitAdapter
import com.leroymerlin.plugins.internal.SigningProperty
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

import java.util.regex.Matcher

class DeliveryPluginExtension {

    final NamedDomainObjectContainer<SigningProperty> signingProperties
    final DeliveryPlugin plugin;
    final Project project

    DeliveryPluginExtension(Project project, DeliveryPlugin plugin, NamedDomainObjectContainer<SigningProperty> signingProperties) {
        this.project = project
        this.plugin = plugin;
        this.signingProperties = signingProperties;
    }

    def archiveRepositories = project.ext.properties.containsKey('archiveRepositories') ? project.ext.archiveRepositories : {}

    //commit text
    def newVersionCommitPattern = 'chore (version) : Update version to $version'

    def releaseTagPattern = '$projectName-$versionId-$version'

    def releaseBranchPattern = 'release/$versionId-$version'

    boolean failOnSnapshotDependency = true

    def scmAdapters = [
            "git": GitAdapter
            //"svn":SvnAdapter
    ];

    def versionPatterns = [
            // Increments last number: "2.5-SNAPSHOT" => "2.6-SNAPSHOT"
            /(\d+)([^\d]*$)/: { Matcher m, Project p -> m.replaceAll("${(m[0][1] as int) + 1}${m[0][2]}") }
    ]

    void signingProperties(Action<? super NamedDomainObjectContainer<SigningProperty>> action) {
        action.execute(signingProperties)
        signingProperties.each {
            SigningProperty signingProperty ->
                plugin.applySigningProperty(signingProperty)
        }
    }

}
