package com.leroymerlin.plugins.entities

import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.core.BaseScmAdapter
import com.leroymerlin.plugins.tasks.ChangePropertiesTask
import com.leroymerlin.plugins.tasks.scm.*
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Exec

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
        taskFlow = project.task(name + Flow.simpleName)
    }

    private String formatTaskName(String baseName) {
        "${name}Step${tasksList.size()}${baseName}"
    }

    private void createTask(Class className, HashMap<String, Object> parameters) {

        Task task = project.task(formatTaskName(className.simpleName), type: className);
        if (task.hasProperty("scmAdapter")) {
            task.setProperty("scmAdapter", adapter)
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

    def branch(String branchName, boolean create = false) {
        createTask(BranchTask, [branch: branchName, createIfNeeded: create])
    }

    def add(String... files) {
        createTask(AddFilesTask, [files: files])
    }

    def commit(String commitComment, boolean addAll = false) {
        if (addAll) {
            add()
        }
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

    def cmd(String cmd) {
        if (cmd.length() == 0)
            throw new IllegalArgumentException("Empty command")

        StringTokenizer st = new StringTokenizer(cmd)
        String[] cmdarray = new String[st.countTokens()]
        for (int i = 0; st.hasMoreTokens(); i++)
            cmdarray[i] = st.nextToken()

        createTask(Exec, [commandLine: cmdarray])
    }

    def task(String taskName) {
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