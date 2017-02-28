package com.leroymerlin.plugins.entities

import org.gradle.api.Task
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.tasks.TaskDependency

/**
 * Created by florian on 30/01/2017.
 */
class ArchiveArtifact implements PublishArtifact {
    String name
    String extension
    String classifier
    File file
    Date date
    Task dependencyTask

    ArchiveArtifact(String name, String extension, String classifier, File file, Task dependencyTask) {
        this.name = name
        this.extension = extension
        this.classifier = classifier
        this.file = file
        this.date = new Date()
    }

    @Override
    String getName() {
        return name
    }

    @Override
    String getExtension() {
        return extension
    }

    @Override
    String getType() {
        return extension
    }

    @Override
    String getClassifier() {
        return classifier
    }

    @Override
    File getFile() {
        return file
    }

    @Override
    Date getDate() {
        return date
    }

    @Override
    TaskDependency getBuildDependencies() {
        return new TaskDependency() {
            @Override
            Set<? extends Task> getDependencies(Task task) {
                return Collections.singleton(dependencyTask)
            }
        }
    }
}
