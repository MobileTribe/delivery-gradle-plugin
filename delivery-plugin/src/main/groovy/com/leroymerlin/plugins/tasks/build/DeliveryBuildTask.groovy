package com.leroymerlin.plugins.tasks.build

import com.leroymerlin.plugins.entities.ArchiveArtifact
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.tasks.Input

/**
 * Created by alexandre on 15/02/2017.
 */
abstract class DeliveryBuildTask extends DefaultTask {

    @Input
    String variantName

    @Input
    Map<String, File> outputFiles = [:]

    PublishArtifact[] getArtifacts() {
        if (variantName == null) {
            variantName = project.projectName
        }



        return outputFiles.collect { classifier, file ->
            String extension = "";
            int i = file.name.lastIndexOf('.');
            if (i > 0) {
                extension = file.name.substring(i+1);
            }
            return new ArchiveArtifact(variantName, extension, classifier, file, this)
        }
    }
}
