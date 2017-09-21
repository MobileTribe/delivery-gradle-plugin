package com.leroymerlin.plugins.tasks

import com.leroymerlin.plugins.utils.PropertiesUtils
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

/**
 * Created by alexandre on 15/02/2017.
 */
class ChangePropertiesTask extends DefaultTask {

    String version, versionId, artifact
    Project project

    @TaskAction
    changeProperties() {
        File versionFile = project.delivery.plugin.getVersionFile()

        if (version != null) {
            PropertiesUtils.setProperty(versionFile, project.ext.versionKey, version)
        }
        if (versionId != null) {
            PropertiesUtils.setProperty(versionFile, project.ext.versionIdKey, versionId)
        }
        if (artifact != null) {
            PropertiesUtils.setProperty(versionFile, project.ext.artifactKey, artifact)
        }
        project.delivery.plugin.applyDeliveryProperties(versionFile)
    }
}
