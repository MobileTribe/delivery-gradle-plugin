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
    String name, lastTaskName
    Project project
    ArrayList<String> tasksList = new ArrayList<>()
    HashMap<String, Object> parameters = new HashMap<>()
    BaseScmAdapter adapter
    Task taskFlow
    DeliveryPluginExtension delivery

    Flow(String name, DeliveryPluginExtension extension) {
        this.name = name
        this.project = extension.project
        this.delivery = extension
        this.adapter = this.delivery.scmAdapter
        taskFlow = project.task("init" + name.capitalize())
    }

    private String formatTaskName(String baseName) {
        "${name}_step${tasksList.size()}_${baseName}"
    }

    def createTask(Class className, HashMap<String, ?> parameters) {
        Task task = project.task(formatTaskName(className.simpleName), type: className) {
            scmAdapter adapter
        }
        if (parameters != null) {
            for (Map.Entry<String, ?> entry : parameters.entrySet()) {
                task.setProperty(entry.getKey(), entry.getValue())
            }
        }
        if (lastTaskName != null)
            task.dependsOn(lastTaskName)
        lastTaskName = task.name
        tasksList.add(task.name)
        taskFlow.dependsOn(tasksList)
    }

    def switchBranch(String branchName, boolean create) {
        parameters.clear()
        parameters.put('branch', branchName)
        parameters.put('createIfNeeded', create)
        createTask(SwitchTask, parameters)
    }

    def commitFiles(String commitComment) {
        parameters.clear()
        parameters.put('comment', commitComment)
        createTask(AddFilesTask, null)
        createTask(CommitTask, parameters)
    }

    def tag(String tagMessage, String tagAnnotation) {
        parameters.clear()
        parameters.put('annotation', tagAnnotation)
        parameters.put('message', tagMessage)
        createTask(TagTask, parameters)
    }

    def merge(String branch) {
        parameters.clear()
        parameters.put('from', branch)
        createTask(MergeTask, parameters)
    }

    def push() {
        createTask(PushTask, null)
    }

    def delete(String branchName) {
        parameters.clear()
        parameters.put('branch', branchName)
        createTask(DeleteTask, parameters)
    }

    def changeVersion(String method, String val) {
        parameters.clear()
        parameters.put('key', method)
        parameters.put('value', val)
        parameters.put('myProject', project)
        createTask(ChangePropertyTask, parameters)
    }

    def task(String taskName) {
        Task task = project.getTasksByName(taskName, false)[0]
        if (task != null) {
            if (lastTaskName != null)
                task.dependsOn(lastTaskName)
            lastTaskName = task.name
            tasksList.add(task.name)
            taskFlow.dependsOn(tasksList)
        }
    }

    def propertyMissing(String name, arg) {
        switch (name) {
            case 'push':
                push()
                break
            case 'switchBranch':
                switchBranch(arg[0], arg[1])
                break
            case 'commitFiles':
                commitFiles(arg[0])
                break
            case 'tag':
                tag(arg[0], arg[1])
                break
            case 'merge':
                merge(arg[0])
                break
            case 'delete':
                delete(arg[0])
                break
            case 'changeVersion':
                changeVersion(arg[0], arg[1])
                break
            case 'task':
                task(arg[0])
                break
        }
    }
}