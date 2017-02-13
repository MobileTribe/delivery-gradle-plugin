package com.leroymerlin.plugins.tasks

import org.gradle.api.tasks.TaskAction

/**
 * Created by alexandre on 09/02/2017.
 */
class AddFilesTask extends ScmBaseTask {

    static String description = 'Add files to be commited'

    @TaskAction
    addAllFiles() {
        return scmHandler.addAllFiles()
    }
}
