package com.leroymerlin.plugins.entities

import com.leroymerlin.plugins.core.BaseScmAdapter
import com.leroymerlin.plugins.tasks.*
import org.gradle.api.Project

/**
 * Created by alexandre on 08/02/2017.
 */
class Flow {
    String name, taskName, lastTaskName
    Project project
    ArrayList<String> tasksList = new ArrayList<>()
    BaseScmAdapter adapter
    def task, taskFlow

    Flow(String name, Project project) {
        this.name = name
        this.project = project
        this.adapter = project.delivery.scmAdapter
        taskFlow = project.task("start" + name.capitalize())
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

    void commitFiles(String com) {
        task = project.task(name + 'AddFiles', type: AddFilesTask) {
            scmAdapter adapter
        }

        if (lastTaskName != null)
            task.dependsOn(lastTaskName)

        taskName = 'Step' + tasksList.size() + '/' + name + 'CommitFiles'

        task = project.task(taskName, type: CommitTask) {
            scmAdapter adapter
            comment com
        }.dependsOn(name + 'AddFiles')

        lastTaskName = taskName

        tasksList.add(taskName)
        taskFlow.dependsOn(tasksList)
    }

    void tag(String annot, String mess) {
        taskName = 'Step' + tasksList.size() + '/' + name + 'Tag'

        task = project.task(taskName, type: TagTask) {
            scmAdapter adapter
            annotation annot
            message mess
        }

        if (lastTaskName != null)
            task.dependsOn(lastTaskName)

        lastTaskName = taskName

        tasksList.add(taskName)
        taskFlow.dependsOn(tasksList)
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

    void push() {
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