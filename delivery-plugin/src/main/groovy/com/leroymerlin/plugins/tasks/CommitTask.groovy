package com.leroymerlin.plugins.tasks

import org.gradle.api.tasks.TaskAction

/**
 * Created by alexandre on 09/02/2017.
 */
class CommitTask extends ScmBaseTask {

    String comment

    @TaskAction
    commit() {
        return scmHandler.commit(comment)
    }
}
