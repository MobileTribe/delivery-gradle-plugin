package com.leroymerlin.plugins.tasks

import org.gradle.api.tasks.TaskAction

/**
 * Created by alexandre on 09/02/2017.
 */
class PushTask extends ScmBaseTask {

    static String description = 'Push files'
    @TaskAction
    push() {
        return scmHandler.push()
    }
}
