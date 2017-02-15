package com.leroymerlin.plugins.tasks

import org.gradle.api.tasks.TaskAction

/**
 * Created by nextoo on 15/02/2017.
 */
class SwitchTask extends ScmBaseTask {

    String branch
    boolean createIfNeeded = false

    @TaskAction
    switchBranch() {
        return scmAdapter.switchBranch(branch, createIfNeeded)
    }
}
