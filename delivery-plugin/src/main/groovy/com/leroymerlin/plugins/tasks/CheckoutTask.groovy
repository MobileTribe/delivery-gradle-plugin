package com.leroymerlin.plugins.tasks

import org.gradle.api.tasks.TaskAction

/**
 * Created by alexandre on 06/02/2017.
 */
class CheckoutTask extends ScmBaseTask {

    String branch

    @TaskAction
    goToBranch() {
        return scmHandler.goToBranch(branch)
    }
}
