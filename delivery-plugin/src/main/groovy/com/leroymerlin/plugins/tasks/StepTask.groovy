package com.leroymerlin.plugins.tasks

import com.leroymerlin.plugins.cli.DeliveryLogger
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by alexandre on 13/04/2017.
 */
class StepTask extends DefaultTask {

    String title
    StringBuilder space = new StringBuilder()
    final
    static String equals = "=========================================================================================="
    private final DeliveryLogger deliveryLogger = new DeliveryLogger()

    @TaskAction
    step() {
        space.append("\n")
        space.append("${equals}\n")
        for (int i = 0; i < (equals.size() / 2 - title.size() / 2); i++) {
            space.append(" ")
        }
        space.append("${title.toUpperCase()}\n")
        space.append("${equals}\n")
        deliveryLogger.logInfo(space.toString())
    }
}
