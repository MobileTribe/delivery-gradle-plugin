package com.leroymerlin.plugins.tasks

import org.gradle.api.tasks.TaskAction

/**
 * Created by alexandre on 16/02/2017.
 */
class FlowTask extends ScmBaseTask {

    String flowTitle

    @TaskAction
    flow() {
        println("flow name : $flowTitle")
    }
}
