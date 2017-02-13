package com.leroymerlin.plugins.tasks

import org.gradle.api.tasks.TaskAction

/**
 * Created by alexandre on 09/02/2017.
 */
class TagTask extends ScmBaseTask {

    String annotation, message
    static String description = 'Tag commit'

    @TaskAction
    tag() {
        return scmHandler.tag(annotation, message)
    }
}
