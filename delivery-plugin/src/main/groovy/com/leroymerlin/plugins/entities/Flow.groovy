package com.leroymerlin.plugins.entities

import org.gradle.api.Project

/**
 * Created by alexandre on 08/02/2017.
 */
class Flow {
    String name
    Project project
    List<Step> steps = []

    Flow(String name, Project project) {
        this.name = name
        this.project = project
    }

    void step(Closure closure) {
        Step step = new Step()
        project.configure(step, closure)
        steps.add(step)
    }
}