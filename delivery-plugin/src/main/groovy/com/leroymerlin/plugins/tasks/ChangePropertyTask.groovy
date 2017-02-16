package com.leroymerlin.plugins.tasks

import com.leroymerlin.plugins.utils.PropertiesFileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

/**
 * Created by alexandre on 15/02/2017.
 */
class ChangePropertyTask extends DefaultTask {

    String key, value
    Project myProject

    @TaskAction
    changeProperty() {
        File versionFile

        if (myProject.hasProperty('versionFilePath')) {
            versionFile = myProject.file(myProject.property('versionFilePath'))
        } else {
            versionFile = myProject.file('version.properties')
        }

        switch (key) {
            case 'changeVersion':
                PropertiesFileUtils.setProperty(versionFile, 'version', value)
                break
            case 'changeVersionId':
                PropertiesFileUtils.setProperty(versionFile, 'versionId', value)
                break
        }
        PropertiesFileUtils.readAndApplyPropertiesFile(myProject, versionFile)
    }
}
