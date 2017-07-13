package com.leroymerlin.plugins.tasks.build

import com.leroymerlin.plugins.DeliveryPlugin
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.bundling.Jar

import java.util.logging.Logger

/**
 * Created by alexandre on 15/02/2017.
 */
class AndroidBuild extends DeliveryBuild {
    @Input
    void addVariant(variant) {
        def classifier = variant.buildType.name
        if (variant.signingReady) {
            outputFiles.put(classifier, variant.outputs.get(0).outputFile)
            dependsOn.add(variant.assemble)
            if (variant.testVariant) {
                outputFiles.put("test-$classifier" as String, variant.testVariant.outputs.get(0).outputFile)
                dependsOn.add(variant.testVariant.assemble)
            }
            if (variant.mappingFile) {
                doLast {
                    if (!variant.mappingFile.exists()) {
                        variant.mappingFile.parentFile.mkdirs()
                        variant.mappingFile.createNewFile()
                    }
                }


                outputFiles.put("mapping-$classifier" as String, variant.mappingFile)
            }

            def sourcesJar = project.task("sources${variant.name.capitalize()}Jar", type: Jar, group: DeliveryPlugin.TASK_GROUP) {
                classifier = 'sources'
                from variant.javaCompile.destinationDir
            }
            outputFiles.put("sources-" + classifier, sourcesJar.outputs.getFiles().getSingleFile())
            dependsOn.add(sourcesJar)
        } else {
            Logger.global.info("$classifier has no valid signing config and will not be archived")
        }
    }



}
