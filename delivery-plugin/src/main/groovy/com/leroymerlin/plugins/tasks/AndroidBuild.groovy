package com.leroymerlin.plugins.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.bundling.Jar

/**
 * Created by alexandre on 15/02/2017.
 */
class AndroidBuild extends DeliveryBuildTask {

    @Input
    void setVariant(variant) {
        outputFiles.clear()
        def classifier = variant.buildType.name
        if (variant.signingReady) {
            outputFiles.put(classifier, variant.outputs.get(0).outputFile)
            if (variant.testVariant) {
                outputFiles.put("test-" + classifier, variant.testVariant.outputs.get(0).outputFile)
            }
            if (variant.mappingFile) {
                if (!variant.mappingFile.exists()) {
                    variant.mappingFile.parentFile.mkdirs()
                    variant.mappingFile.createNewFile()
                }
                outputFiles.put("mapping-$classifier", variant.mappingFile)
            }

            def sourcesJar = project.task("sources${variant.name.capitalize()}Jar", type: Jar) {
                classifier = 'sources'
                from variant.javaCompile.destinationDir
            }
            sourcesJar.dependsOn variant.javaCompile
            outputFiles.put("sources-" + classifier, sourcesJar.outputs.getFiles().getSingleFile())
        } else {
            logger.warn("$classifier has no valid signing config and will not be archived")
        }
    }
}
