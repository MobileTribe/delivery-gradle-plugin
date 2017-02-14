package com.leroymerlin.plugins.entities

import com.leroymerlin.plugins.tasks.*
import org.gradle.api.Project

/**
 * Created by alexandre on 08/02/2017.
 */
class Flow {
    String name, lastTaskName
    Project project
    ArrayList<String> tasksList = new ArrayList<>()
    def taskFlow

    Flow(String name, Project project) {
        this.name = name
        this.project = project
        taskFlow = project.task("start" + name)
    }

    def methodMissing(String methodName, args) {
        def task
        String taskName = name + methodName.capitalize()
        println(methodName)
        for (def arg : args)
            println(arg)
        switch (methodName) {
            case "branch":
                task = project.task(taskName, type: GoToTask) {
                    branch args[0]
                }
                break
            case 'commitFiles':
                project.task(name + 'ADDFILES', type: AddFilesTask) {}
                task = project.task(taskName, type: CommitTask) {
                    comment args[0]
                }
                break
            case 'tag':
                task = project.task(taskName, type: TagTask) {
                    annotation args[0]
                    message args[1]
                }
                break
            case 'merge':
                task = project.task(taskName, type: MergeTask) {
                    from args[0]
                    to args[1]
                }
                break
            case 'push':
                task = project.task(taskName, type: PushTask) {}
                break
            case 'delete':
                task = project.task(taskName, type: DeleteTask) {
                    branch args[0]
                }
                break
        }
        if (task != null && lastTaskName != null)
            task.dependsOn(lastTaskName)

        lastTaskName = methodName
        tasksList.add(name + methodName.capitalize())
        taskFlow.dependsOn(tasksList)
        return null
    }
}