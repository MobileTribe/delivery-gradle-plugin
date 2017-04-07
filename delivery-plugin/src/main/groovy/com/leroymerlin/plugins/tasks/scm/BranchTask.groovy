package com.leroymerlin.plugins.tasks.scm

import org.gradle.api.tasks.TaskAction

/**
 * Created by alexandre on 15/02/2017.
 */
class BranchTask extends ScmBaseTask {

    String branch
    boolean createIfNeeded = false

    @TaskAction
    switchBranch() {
        scmAdapter.switchBranch(branch, createIfNeeded)
    }
}
