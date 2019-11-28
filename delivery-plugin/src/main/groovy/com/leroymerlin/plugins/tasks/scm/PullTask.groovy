package com.leroymerlin.plugins.tasks.scm

import org.gradle.api.tasks.TaskAction

/**
 * Created by paul-hubert on 18/05/2017.
 */
class PullTask extends ScmBaseTask {

    String branchName

    @TaskAction
    pull() {
        scmAdapter.pull(branchName)
    }
}
