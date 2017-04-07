package com.leroymerlin.plugins.tasks.scm

import org.gradle.api.tasks.TaskAction

/**
 * Created by alexandre on 09/02/2017.
 */
class MergeTask extends ScmBaseTask {

    String from

    @TaskAction
    mergeToNewBranch() {
        scmAdapter.merge(from)
    }
}
