package com.leroymerlin.plugins.tasks

import org.gradle.api.tasks.TaskAction

/**
 * Created by alexandre on 06/02/2017.
 */
class InitTask extends ScmBaseTask {

    static String description = 'Init git'

    @TaskAction
    init() {
        return scmHandler.init()
    }
}
