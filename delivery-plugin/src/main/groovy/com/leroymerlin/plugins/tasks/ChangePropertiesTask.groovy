package com.leroymerlin.plugins.tasks

import com.leroymerlin.plugins.utils.PropertiesFileUtils
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Created by alexandre on 15/02/2017.
 */
class ChangePropertiesTask extends ScmBaseTask {

    @Input
    String version, versionId, projectName

    Project project

    @TaskAction
    changeProperties() {
        File versionFile = project.delivery.plugin.getVersionFile()

        if (version != null) {
            PropertiesFileUtils.setProperty(versionFile, project.ext.versionKey, version)
        }
        if (versionId != null) {
            PropertiesFileUtils.setProperty(versionFile, project.ext.versionIdKey, versionId)
        }
        if (projectName != null) {
            PropertiesFileUtils.setProperty(versionFile, project.ext.projectNameKey, projectName)
        }


        project.delivery.plugin.applyDeliveryProperties(versionFile)

    }
}
