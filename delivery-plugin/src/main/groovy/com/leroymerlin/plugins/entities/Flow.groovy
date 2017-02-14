package com.leroymerlin.plugins.entities

import com.leroymerlin.plugins.tasks.*
import org.gradle.api.Project

/**
 * Created by alexandre on 08/02/2017.
 */
class Flow {
    String name, lastTaskName
    Project project
    ArrayList<String> tasksName = new ArrayList<>()

    Flow(String name, Project project) {
        this.name = name
        this.project = project
        //taskFlow = project.task("start"+name).dependsOn
    }

    def methodMissing(String methodName, args) {
        lastTaskName = methodName
        switch (methodName) {
            case 'commitFiles':
                project.task(name + 'ADDFILES', type: AddFilesTask) {}
                project.task(name + methodName.capitalize(), type: CommitTask) {
                    comment args[0]
                }.dependsOn(lastTaskName)
                break
            case "branch":
                project.task(name + methodName.capitalize(), type: GoToTask) {
                    branch args[0]
                }.dependsOn(lastTaskName)
                break
            case 'tag':
                project.task(name + methodName.capitalize(), type: TagTask) {
                    branch args[0]
                }.dependsOn(lastTaskName)
                break
            case 'push':
                project.task(name + methodName.capitalize(), type: PushTask) {
                    branch args[0]
                }.dependsOn(lastTaskName)
                break
            case 'delete':
                project.task(name + methodName.capitalize(), type: DeleteTask) {
                    branch args[0]
                }.dependsOn(lastTaskName)
        }
        tasksName.add(name + methodName.capitalize())

        //taskFlow.dependeOn tasksName
        return null
    }
}