package com.leroymerlin.plugins.tasks.build

import com.leroymerlin.plugins.cli.Executor
import com.leroymerlin.plugins.entities.ArchiveArtifact
import org.apache.commons.io.FilenameUtils
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.tasks.Input

/**
 * Created by alexandre on 15/02/2017.
 */
class DeliveryBuild extends DefaultTask {

    String variantName

    @Input
    String setVariant(String variantName) {
        this.variantName = variantName
    }

    @Input
    public Map<String, File> outputFiles = [:]

    PublishArtifact[] getArtifacts() {
        if (variantName == null) {
            variantName = project.artifact
        }
        return outputFiles.collect { classifier, file ->
            String extension = FilenameUtils.getExtension(file.name)
            return new ArchiveArtifact(variantName, extension, classifier, file, this)
        }
    }

    def cmd(String cmd) {
        return Executor.exec(Executor.convertToCommandLine(cmd), [directory: project.projectDir])
    }

    static def cmd(List<String> commands, Map options = [:]) {
        Executor.exec(commands, options)
    }
}
