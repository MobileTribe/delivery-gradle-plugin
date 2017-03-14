package com.leroymerlin.plugins.tasks.scm

import org.gradle.api.tasks.TaskAction

/**
 * Created by alexandre on 09/02/2017.
 */
class AddFilesTask extends ScmBaseTask {

    String[] files

    @TaskAction
    addAllFiles() {
        if (files == null || files.length == 0) {
            files = ["."]
        }
        scmAdapter.addFiles(files)
    }
}
