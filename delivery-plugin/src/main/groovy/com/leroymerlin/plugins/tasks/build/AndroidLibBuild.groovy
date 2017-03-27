package com.leroymerlin.plugins.tasks.build

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.bundling.Jar

/**
 * Created by alexandre on 15/02/2017.
 */
class AndroidLibBuild extends DeliveryBuild {

    @Input
    void addVariant(variant) {
        def classifier = variant.buildType.name
        outputFiles.put("", variant.outputs.get(0).outputFile)
        dependsOn.add(variant.assemble)
        if (variant.mappingFile) {
            if (!variant.mappingFile.exists()) {
                variant.mappingFile.parentFile.mkdirs()
                variant.mappingFile.createNewFile()
            }
            outputFiles.put("mapping", variant.mappingFile)
        }

        def sourcesJar = project.task("sources${variant.name.capitalize()}Jar", type: Jar) {
            classifier = 'sources'
            from variant.javaCompile.destinationDir
        }
        outputFiles.put("sources", sourcesJar.outputs.getFiles().getSingleFile())
        dependsOn.add(sourcesJar)
    }
}
