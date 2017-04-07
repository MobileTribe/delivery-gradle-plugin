package com.leroymerlin.plugins.entities

import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.cli.Executor
import com.leroymerlin.plugins.tasks.ChangePropertiesTask
import com.leroymerlin.plugins.tasks.scm.*
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.GradleBuild

/**
 * Created by alexandre on 08/02/2017.
 */
class Flow {
    Project project
    ArrayList<String> tasksList = new ArrayList<>()
    Task taskFlow
    DeliveryPluginExtension delivery

    String name

    Flow(String name, DeliveryPluginExtension extension) {
        this.name = name
        this.project = extension.project
        this.delivery = extension
        taskFlow = project.task(name + Flow.simpleName)
    }

    private String formatTaskName(String baseName) {
        "${name}Step${tasksList.size()}${baseName}"
    }

    private void createTask(Class className, HashMap<String, Object> parameters) {

        Task task = project.task(formatTaskName(className.simpleName), type: className);
        if (task.hasProperty("scmAdapter")) {
            task.setProperty("scmAdapter", delivery.getScmAdapter())
        }
        if (parameters != null) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                task.setProperty(entry.getKey(), entry.getValue())
            }
        }
        if (!tasksList.isEmpty())
            task.dependsOn(tasksList.last())
        tasksList.add(task.name)
        taskFlow.dependsOn(tasksList)
    }

    def branch(String name, boolean create = false) {
        createTask(BranchTask, [branch: name, createIfNeeded: create])
    }

    def add(String... files) {
        createTask(AddFilesTask, [files: files])
    }

    def commit(String message, boolean addAll = false) {
        if (addAll) {
            add()
        }
        createTask(CommitTask, [message: message])
    }

    def tag(def message = "", def annotation = "") {
        createTask(TagTask, [annotation: annotation, message: message])
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

    def changeProperties(version = null, versionId = null, projectName = null) {
        createTask(ChangePropertiesTask, [version: version, versionId: versionId, projectName: projectName, project: project])
    }

    def build() {
        task("uploadArtifacts", true)
    }

    def cmd(String cmd) {
        if (cmd.length() == 0)
            throw new IllegalArgumentException("Empty command")
        String[] cmdarray = Executor.convertToCommandLine(cmd)
        createTask(Exec, [commandLine: cmdarray])
    }

    def discardChange() {
        createTask(DiscardFilesTask, null)
    }

    def task(String taskName, boolean newBuild = false) {
        if (newBuild) {
            createTask(GradleBuild,
                    [
                            startParameter: project.getGradle().startParameter.newInstance(),
                            tasks         : [taskName]
                    ])
        } else {
            Task task = project.getTasksByName(taskName, true)[0]
            if (task != null) {
                if (!tasksList.isEmpty())
                    task.dependsOn(tasksList.last())
                tasksList.add(task.name)
                taskFlow.dependsOn(tasksList)
            }
        }
    }

    def get(String name) {
        if (this.metaClass.respondsTo(this, name)) {
            return this."$name"()
        }
        throw new GradleException("Flow doesn't handle method $name");
    }
}