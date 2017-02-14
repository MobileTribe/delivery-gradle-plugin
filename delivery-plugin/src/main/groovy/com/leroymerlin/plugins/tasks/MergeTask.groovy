package com.leroymerlin.plugins.tasks

import org.gradle.api.tasks.TaskAction

/**
 * Created by alexandre on 09/02/2017.
 */
class MergeTask extends ScmBaseTask {

    String from, to

    @TaskAction
    mergeToNewBranch() {
        return scmHandler.merge(from, to)
    }
}
