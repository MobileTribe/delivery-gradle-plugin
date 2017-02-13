package com.leroymerlin.plugins.tasks

import org.gradle.api.tasks.TaskAction

/**
 * Created by alexandre on 09/02/2017.
 */
class DeleteTask extends ScmBaseTask {

    String branch

    @TaskAction
    deleteBranch() {
        scmHandler.deleteBranch(branch)
    }
}
