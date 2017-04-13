package com.leroymerlin.plugins.tasks.scm

import org.gradle.api.tasks.TaskAction

/**
 * Created by paul-hubert on 13/04/2017.
 */
class PushTagTask extends ScmBaseTask {

    String tagName

    @TaskAction
    pushTag() {
        scmAdapter.pushTag(tagName)
    }
}
