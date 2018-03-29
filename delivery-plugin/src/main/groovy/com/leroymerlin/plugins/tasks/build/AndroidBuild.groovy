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
            //After Android plugin 3.0.0
            try {
                String fileName = "$variantName-${variant.versionName}-${classifier}.apk"
                variant.outputs.all {
                    outputFileName = fileName
                }
                if (flutterProject) {
                    outputFiles.put(classifier as String, new File(project.rootProject
                            .file("build/app/outputs/apk/" +
                            "${variantName.replace("${project.artifact.toLowerCase()}", "").replaceFirst("-", "")}/$classifier/$fileName").path.replace("android/", "")))
                    dependsOn.add(variant.assemble)
                    if (variant.testVariant) {
                        outputFiles.put("test-$classifier" as String, new File(project.rootProject
                                .file("build/app/outputs/apk/" +
                                "${variantName.replace("${project.artifact.toLowerCase()}", "").replaceFirst("-", "")}/$classifier/$fileName").path.replace("android/", "")))
                        dependsOn.add(variant.testVariant.assemble)
                    }
                } else {
                    outputFiles.put(classifier as String, project
                            .file("build/outputs/apk/" +
                            "${variantName.replace("${project.artifact.toLowerCase()}", "").replaceFirst("-", "")}/$classifier/$fileName"))
                    dependsOn.add(variant.assemble)
                    if (variant.testVariant) {
                        outputFiles.put("test-$classifier" as String, project
                                .file("build/outputs/apk/" +
                                "${variantName.replace("${project.artifact.toLowerCase()}", "").replaceFirst("-", "")}/$classifier/$fileName"))
                        dependsOn.add(variant.testVariant.assemble)
                    }
                }
            }
            // Before Android plugin 3.0.0
            catch (MissingMethodException ignored) {
                outputFiles.put(classifier as String, variant.outputs.get(0).outputFile as File)
                dependsOn.add(variant.assemble)
                if (variant.testVariant) {
                    outputFiles.put("test-$classifier" as String, variant.testVariant.outputs.get(0).outputFile as File)
                    dependsOn.add(variant.testVariant.assemble)
                }
            }
            if (variant.mappingFile) {
                doLast {
                    if (!variant.mappingFile.exists()) {
                        variant.mappingFile.parentFile.mkdirs()
                        variant.mappingFile.createNewFile()
                    }
                }
                outputFiles.put("mapping-$classifier" as String, variant.mappingFile as File)
            }
            variant.sourceSets.each { sourceSet ->
                if (sourceSet.name == "main") {
                    def sourcesJar = project.task("sources${variant.name.capitalize()}Jar", type: Jar, group: DeliveryPlugin.TASK_GROUP) {
                        classifier = 'sources'
                        from sourceSet.java.srcDirs
                    }
                    outputFiles.put("sources-" + classifier, sourcesJar.outputs.getFiles().getSingleFile())
                    dependsOn.add(sourcesJar)
                }
            }
        } else {
            Logger.global.info("$classifier has no valid signing config and will not be archived")
        }
    }
}
