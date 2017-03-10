package com.leroymerlin.plugins.tasks.scm

import org.gradle.api.tasks.TaskAction

/**
 * Created by alexandre on 09/02/2017.
 */
class AddFilesTask extends ScmBaseTask {

    @TaskAction
    addAllFiles() {
        scmAdapter.addAllFiles()
    }
}
