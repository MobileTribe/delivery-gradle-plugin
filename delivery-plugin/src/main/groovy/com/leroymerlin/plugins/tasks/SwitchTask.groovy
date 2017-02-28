package com.leroymerlin.plugins.tasks

import org.gradle.api.tasks.TaskAction

/**
 * Created by alexandre on 15/02/2017.
 */
class SwitchTask extends ScmBaseTask {

    String branch
    boolean createIfNeeded = false

    @TaskAction
    switchBranch() {
        scmAdapter.switchBranch(branch, createIfNeeded)
    }
}
