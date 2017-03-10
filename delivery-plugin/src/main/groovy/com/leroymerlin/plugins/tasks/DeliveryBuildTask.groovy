package com.leroymerlin.plugins.tasks

import com.leroymerlin.plugins.entities.ArchiveArtifact
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.tasks.Input

/**
 * Created by alexandre on 15/02/2017.
 */
class DeliveryBuildTask extends DefaultTask {

    @Input
    String variantName

    @Input
    Map<String, File> outputFiles

    PublishArtifact[] getArtifacts() {
        return outputFiles.collect { classifier, file ->
            return new ArchiveArtifact(variantName, file.name.replaceFirst(~/\.[^\.]+$/, ''), classifier, file, this)
        }
    }
}
