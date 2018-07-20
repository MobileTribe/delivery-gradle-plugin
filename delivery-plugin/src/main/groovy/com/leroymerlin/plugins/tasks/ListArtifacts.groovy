package com.leroymerlin.plugins.tasks

import com.leroymerlin.plugins.cli.DeliveryLogger
import com.leroymerlin.plugins.tasks.build.DeliveryBuild
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Upload

/**
 * Created by alexandre on 13/04/2017.
 */
class ListArtifacts extends DefaultTask {

    private static final DeliveryLogger deliveryLogger = new DeliveryLogger()

    @TaskAction
    listArtifacts() {
        def taskContainer = project.tasks.withType(DeliveryBuild)
        Upload uploadTask
        for (Upload upload : project.tasks.withType(Upload)) {
            uploadTask = upload.identityPath.name.contains("upload") ? upload : null
        }
        int numberOfFiles = 0
        if (taskContainer != null && taskContainer.size() > 0 && uploadTask != null) {
            for (DeliveryBuild build : taskContainer) {
                for (PublishArtifact artifact : build.artifacts) {
                    numberOfFiles++
                    deliveryLogger.logInfo("File n°$numberOfFiles")
                    deliveryLogger.logInfo("Name: ${artifact.file.name}")
                    deliveryLogger.logInfo("Extension: ${artifact.extension}")
                    deliveryLogger.logInfo("Type: ${artifact.type}")
                    artifact.classifier ? deliveryLogger.logInfo("Classifier: ${artifact.classifier}") : ""
                    deliveryLogger.logInfo("Group: ${project.group}")
                    deliveryLogger.logInfo("Path: ${artifact.file.path}")
                    for (ArtifactRepository repo : uploadTask.repositories) {
                        Map<String, String> repos = getUrls(repo)
                        for (Map.Entry<String, String> entry : repos.entrySet()) {
                            deliveryLogger.logInfo("${entry.key} ${entry.getValue()}")
                        }
                    }
                    deliveryLogger.logInfo("\n")
                }
            }
        } else {
            deliveryLogger.logWarning("No artifacts found")
        }
    }

    static Map<String, String> getUrls(ArtifactRepository repo) {
        Map<String, String> repos = new HashMap<>()
        try {
            switch (repo.name) {
                case "maven":
                case "ivy":
                    repos.put("Url:", repo.url as String)
                    break
                case "mavenDeployer":
                    repos.put("Url:", repo.repository.url as String)
                    repos.put("Snapshot url:", repo.snapshotRepository.url as String)
                    break
            }
        } catch (Exception ignored) {
            deliveryLogger.logWarning("An error occured when getting repo urls from ${repo.name}")
        }
        return repos
    }
}
