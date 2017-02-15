package com.leroymerlin.plugins.entities

import com.leroymerlin.plugins.core.BaseScmAdapter
import com.leroymerlin.plugins.tasks.*
import org.gradle.api.Project

/**
 * Created by alexandre on 08/02/2017.
 */
class Flow {
    String name, lastTaskName, taskName
    Project project
    ArrayList<String> tasksList = new ArrayList<>()
    BaseScmAdapter adapter
    def taskFlow, task

    Flow(String name, Project project) {
        this.name = name
        this.project = project
        this.adapter = project.delivery.scmAdapter
        taskFlow = project.task("start" + name.capitalize())
    }

    def methodMissing(String methodName, args) {
        taskName = 'Step' + tasksList.size() + '/' + name + methodName.capitalize()
        switch (methodName) {
            case "branch":
                task = project.task(taskName, type: SwitchTask) {
                    scmAdapter adapter
                    branch args[0]
                    createIfNeeded args[1]
                }
                break
            case 'commitFiles':
                task = project.task(name + 'AddFiles', type: AddFilesTask) {
                    scmAdapter adapter
                }
                if (lastTaskName != null)
                    task.dependsOn(lastTaskName)

                task = project.task(taskName, type: CommitTask) {
                    scmAdapter adapter
                    comment args[0]
                }.dependsOn(task.name)
                break
            case 'tag':
                task = project.task(taskName, type: TagTask) {
                    scmAdapter adapter
                    annotation args[0]
                    message args[1]
                }
                break
            case 'merge':
                task = project.task(taskName, type: MergeTask) {
                    scmAdapter adapter
                    from args[0]
                }
                break
            case 'push':
                task = project.task(taskName, type: PushTask) {
                    scmAdapter adapter
                }
                break
            case 'delete':
                task = project.task(taskName, type: DeleteTask) {
                    scmAdapter adapter
                    branch args[0]
                }
                break
            case 'changeVersion':
            case 'changeVersionId':
                task = project.task(taskName, type: ChangePropertyTask) {
                    key methodName
                    value args[0] as String
                    myProject project
                }
                break
        }
        if (task != null && lastTaskName != null)
            task.dependsOn(lastTaskName)

        lastTaskName = taskName
        tasksList.add(taskName)
        taskFlow.dependsOn(tasksList)
        return null
    }
}