package com.leroymerlin.plugins.tasks

import org.gradle.api.tasks.TaskAction

/**
 * Created by alexandre on 09/02/2017.
 */
class CreateTask extends ScmBaseTask {

    String branch
    static String description = 'Create branch'

    @TaskAction
    createBranch() {
        scmHandler.createBranch(branch)
    }
}
