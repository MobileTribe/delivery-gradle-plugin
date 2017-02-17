package com.leroymerlin.plugins.entities

import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.core.BaseScmAdapter
import com.leroymerlin.plugins.tasks.*
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Created by alexandre on 08/02/2017.
 */
class Flow {
    String name, taskName, lastTaskName
    Project project
    ArrayList<String> tasksList = new ArrayList<>()
    BaseScmAdapter adapter
    def taskFlow
    DeliveryPluginExtension delivery

    Flow(String name, DeliveryPluginExtension extension) {
        this.name = name
        this.project = extension.project
        this.delivery = extension
        this.adapter = this.delivery.scmAdapter
        taskFlow = project.task("Start" + name.capitalize())
    }

    void flow(String flowName) {
        taskName = 'Step' + tasksList.size() + '/' + name + 'Flow'

        task = project.task(taskName, type: FlowTask) {
            scmAdapter adapter
            flowTitle flowName
        }

        if (lastTaskName != null)
            task.dependsOn(lastTaskName)

        lastTaskName = taskName

        tasksList.add(taskName)
        taskFlow.dependsOn(tasksList)
    }

    void task(String taskIdentifier) {
        lastTaskName = taskName

        tasksList.add(taskName)
        taskFlow.dependsOn(tasksList)
    }

    void switchBranch(String branchName, boolean create) {
        taskName = 'Step' + tasksList.size() + '/' + name + 'SwitchBranch'

        task = project.task(taskName, type: SwitchTask) {
            scmAdapter adapter
            branch branchName
            createIfNeeded create
        }

        if (lastTaskName != null)
            task.dependsOn(lastTaskName)

        lastTaskName = taskName

        tasksList.add(taskName)
        taskFlow.dependsOn(tasksList)
    }

    void commitFiles(String commitComment, Closure closure) {
        registerTask(
                project.task(formatTaskName(AddFilesTask.simpleName), type: AddFilesTask) {
                    scmAdapter adapter
                }
        )
        registerTask(
                project.task(formatTaskName(CommitTask.simpleName), type: CommitTask) {
                    scmAdapter adapter
                    comment commitComment
                }
        )
    }

    void tag(String tagMessage, String tagAnnotation) {
        registerTask
        (project.task(formatTaskName(TagTask.simpleName), type: TagTask) {
            scmAdapter adapter
            annotation tagAnnotation
            message tagMessage
        }
        )
    }

    def registerTask(Task task) {
        if (lastTaskName != null)
            task.dependsOn(lastTaskName)
        lastTaskName = task.name
        tasksList.add(task.name)
        taskFlow.dependsOn(tasksList)
    }

    private String formatTaskName(String baseName) {
        "${name}_step${tasksList.size()}_${baseName}"
    }

    void merge(String branch) {
        taskName = 'Step' + tasksList.size() + '/' + name + 'Merge'

        task = project.task(taskName, type: MergeTask) {
            scmAdapter adapter
            from branch
        }

        if (lastTaskName != null)
            task.dependsOn(lastTaskName)

        lastTaskName = taskName

        tasksList.add(taskName)
        taskFlow.dependsOn(tasksList)
    }

    def push() {
        taskName = 'Step' + tasksList.size() + '/' + name + 'Push'

        task = project.task(taskName, type: PushTask) {
            scmAdapter adapter
        }

        if (lastTaskName != null)
            task.dependsOn(lastTaskName)

        lastTaskName = taskName

        tasksList.add(taskName)
        taskFlow.dependsOn(tasksList)
    }

    def propertyMissing(String name) {
        switch (name) {
            case 'push':
                push()
                break
        }
        return null
    }

    def propertyMissing(String name, arg) {

    }

    void delete(String branchName) {
        taskName = 'Step' + tasksList.size() + '/' + name + 'Delete'

        task = project.task(taskName, type: DeleteTask) {
            scmAdapter adapter
            branch branchName
        }

        if (lastTaskName != null)
            task.dependsOn(lastTaskName)

        lastTaskName = taskName

        tasksList.add(taskName)
        taskFlow.dependsOn(tasksList)
    }

    void changeVersion(String method, String val) {
        taskName = 'Step' + tasksList.size() + '/' + name + 'ChangeVersion'

        task = project.task(taskName, type: ChangePropertyTask) {
            key method
            value val
            myProject project
        }

        if (lastTaskName != null)
            task.dependsOn(lastTaskName)

        lastTaskName = taskName

        tasksList.add(taskName)
        taskFlow.dependsOn(tasksList)
    }
}