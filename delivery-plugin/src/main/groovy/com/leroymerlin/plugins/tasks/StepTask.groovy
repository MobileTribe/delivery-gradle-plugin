package com.leroymerlin.plugins.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.util.logging.Logger

/**
 * Created by alexandre on 13/04/2017.
 */
class StepTask extends DefaultTask {

    String title
    StringBuilder space = new StringBuilder()
    final static String equals = "=========================================================================================="

    @TaskAction
    step() {
        space.append("\n")
        space.append("${equals}\n")
        for (int i = 0; i < (equals.size() / 2 - title.size() / 2); i++) {
            space.append(" ")
        }
        space.append("${title.toUpperCase()}\n")
        space.append("${equals}\n")
        Logger.global.warning(space.toString())
    }
}
