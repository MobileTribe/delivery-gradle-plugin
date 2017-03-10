package com.leroymerlin.plugins.entities

import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.core.BaseScmAdapter
import com.leroymerlin.plugins.tasks.ChangePropertiesTask
import com.leroymerlin.plugins.tasks.scm.*
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Created by alexandre on 08/02/2017.
 */
class Flow {
    String name, lastTaskName
    Project project
    ArrayList<String> tasksList = new ArrayList<>()
    BaseScmAdapter adapter
    Task taskFlow
    DeliveryPluginExtension delivery

    Flow(String name, DeliveryPluginExtension extension) {
        this.name = name
        this.project = extension.project
        this.delivery = extension
        this.adapter = this.delivery.scmAdapter
        taskFlow = project.task(name.capitalize()+"Flow")
    }

    private String formatTaskName(String baseName) {
        "${name}_step${tasksList.size()}_${baseName}"
    }

    private void createTask(Class className, HashMap<String, Object> parameters) {
        Task task = project.task(formatTaskName(className.simpleName), type: className) {
            scmAdapter adapter
        }
        if (parameters != null) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
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
        createTask(SwitchTask, [branch: branchName, createIfNeeded: create])
    }

    def commitFiles(String commitComment) {
        createTask(AddFilesTask, null)
        createTask(CommitTask, [comment: commitComment])
    }

    def tag(String tagMessage, String tagAnnotation) {
        createTask(TagTask, [annotation: tagAnnotation, message: tagMessage])
    }

    def merge(String branch) {
        createTask(MergeTask, [from: branch])
    }

    def push() {
        createTask(PushTask, null)
    }

    def delete(String branchName) {
        createTask(DeleteTask, [branch: branchName])
    }

    def changeProperties(version, versionId, projectName) {
        createTask(ChangePropertiesTask, [version: version, versionId: versionId, projectName: projectName, project: project])
    }

    def execTask(String taskName) {
        Task task = project.getTasksByName(taskName, true)[0]
        if (task != null) {
            if (lastTaskName != null)
                task.dependsOn(lastTaskName)
            lastTaskName = task.name
            tasksList.add(task.name)
            taskFlow.dependsOn(tasksList)
        }
    }

    def get(String name) {
        return this."$name"()
    }
}