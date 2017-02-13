package com.leroymerlin.plugins.entities

import com.leroymerlin.plugins.tasks.CheckoutTask
import org.gradle.api.Project

/**
 * Created by alexandre on 08/02/2017.
 */
class Flow {
    String name
    Project project

    Flow(String name, Project project) {
        this.name = name
        this.project = project
        //taskFlow = project.task("start"+name).dependsOn
    }

    //def tasksName[];
    def lastTask
    def methodMissing(String methodName, args) {


        switch (methodName){
            case "branch":
                project.task(name+methodName.capitalize(), type:CheckoutTask){
                    branch args[0]
                }.dependsOn taskName

                taskName += name+methodName.capitalize()
                break
        }


        taskFlow.dependeOn tasksName
        return null
    }
}