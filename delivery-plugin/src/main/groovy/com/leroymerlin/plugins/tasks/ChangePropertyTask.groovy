package com.leroymerlin.plugins.tasks

import com.leroymerlin.plugins.utils.PropertiesFileUtils
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

/**
 * Created by alexandre on 15/02/2017.
 */
class ChangePropertyTask extends ScmBaseTask {

    String key, value
    Project project

    @TaskAction
    changeProperty() {
        File versionFile

        if (project.hasProperty('versionFilePath')) {
            versionFile = project.file(project.property('versionFilePath'))
        } else {
            versionFile = project.file('version.properties')
        }

        switch (key) {
            case 'changeVersion':
                PropertiesFileUtils.setProperty(versionFile, 'version', value)
                break
            case 'changeVersionId':
                PropertiesFileUtils.setProperty(versionFile, 'versionId', value)
                break
        }
        PropertiesFileUtils.readAndApplyPropertiesFile(project, versionFile)
    }
}
