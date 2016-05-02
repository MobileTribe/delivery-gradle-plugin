package com.leroymerlin.plugins.utils

import com.leroymerlin.plugins.DeliveryPlugin
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency

import java.util.regex.Matcher

/**
 * Created by root on 22/02/16.
 */
public class ReleaseMethods {

    private Project project;
    private DeliveryPlugin parent;

    public ReleaseMethods(Project project, DeliveryPlugin parent) {
        this.project = project
        this.parent = parent
    }

    public def checkSnapshotDependencies() {
        def matcher = { Dependency d -> d.version?.contains('SNAPSHOT') }
        def collector = { Dependency d -> "${d.group ?: ''}:${d.name}:${d.version ?: ''}" }

        def message = ""

        project.allprojects.each { proj ->
            def snapshotDependencies = [] as Set
            proj.configurations.all { cfg ->
                snapshotDependencies += cfg.dependencies?.matching(matcher)?.collect(collector)
            }
            if (snapshotDependencies.size() > 0) {
                message += "\n\t${proj.name}: ${snapshotDependencies}"
            }
        }

        if (message) {
            message = "Snapshot dependencies detected: ${message}"
            parent.warnOrThrow(parent.extension.failOnSnapshotDependency, message)
        }
        message
    }


    public def unSnapVersion() {
        def version = project.version

        if (version.toString().endsWith('-SNAPSHOT')) {
            version -= '-SNAPSHOT'
            parent.logger.info("New version name will be " + version)
            project.version = version;
        } else {
            parent.logger.warn("Nothing to do because the version is already unSnapped '$version")
        }
    }

    public def updateVersionsFile() {
        def ant = new AntBuilder()

        ant.propertyfile(file: parent.versionFile) {
            entry(key: parent.versionKey, value: project.version)
            entry(key: parent.versionIdKey, value: project.versionId)
        }
    }

    public def prepareNextVersion() {
        def newVersion;
        if (System.getProperty("NEW_VERSION")) {
            newVersion = System.getProperty("NEW_VERSION");
        } else {
            def version = project.version
            Map<String, Closure> patterns = parent.extension.versionPatterns
            for (entry in patterns) {
                String pattern = entry.key
                Closure handler = entry.value
                Matcher matcher = version =~ pattern
                if (matcher.find()) {
                    newVersion = handler(matcher, project)
                }
            }
        }
        if (newVersion) {
            newVersion -= '-SNAPSHOT'
            newVersion += '-SNAPSHOT'
            def versionId = "" + (Integer.parseInt(project.versionId) + 1)

            parent.setProjectProperty(parent.versionKey, newVersion)
            parent.setProjectProperty(parent.versionIdKey, versionId)

            updateVersionsFile()
            this.parent.scmFlowMethods.basicCommit("updates version to $versionId-$newVersion")

        } else {
            throw new GradleException("Failed to increase version [$project.version] - unknown pattern")
        }
    }

}
