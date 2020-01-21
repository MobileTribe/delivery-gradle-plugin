package com.leroymerlin.plugins.tasks.build

import com.leroymerlin.plugins.DeliveryPlugin
import com.leroymerlin.plugins.cli.DeliveryLogger
import org.gradle.api.Task
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.bundling.Jar

/**
 * Created by alexandre on 15/02/2017.
 */
class AndroidBuild extends DeliveryBuild {

    private DeliveryLogger deliveryLogger = new DeliveryLogger()


    @Input
    void addVariant(variant) {
        String classifier = variant.buildType.name
        if (variant.signingReady) {
            //After Android plugin 3.0.0
            try {
                String fileName = "$variantName-${variant.versionName}-${classifier}.apk"
                variant.outputs.all {
                    outputFileName = fileName
                }
                boolean isFlutter = project.plugins.find { it.class.simpleName.equals("FlutterPlugin") } != null

                //String flavorName = variantName.replace(project.artifact.toLowerCase(), "").replaceFirst("-", "").toLowerCase()
                String flavorName = variant.flavorName

                if (isFlutter) {
                    outputFiles.put(classifier as String, new File(project.rootProject
                            .file("build/app/outputs/apk/$flavorName/$classifier/$fileName").path.replace("android/", "")))
                    def assembleTask = getAssembleTask(variant)
                    dependsOn.add(assembleTask)
                    assembleTask.dependsOn += project.tasks.withType(PrepareBuildTask)
                    if (variant.testVariant) {
                        outputFiles.put("test-$classifier" as String, new File(project.rootProject
                                .file("build/app/outputs/apk/$flavorName/$classifier/$fileName").path.replace("android/", "")))
                        dependsOn.add(getAssembleTask(variant.testVariant))
                    }
                } else {
                    outputFiles.put(classifier as String, project
                            .file("build/outputs/apk/$flavorName/$classifier/$fileName"))

                    def assembleTask = getAssembleTask(variant)
                    dependsOn.add(assembleTask)
                    assembleTask.dependsOn += project.tasks.withType(PrepareBuildTask)

                    if (variant.testVariant) {
                        outputFiles.put("test-$classifier" as String, project
                                .file("build/outputs/apk/$flavorName/$classifier/$fileName"))
                        dependsOn.add(getAssembleTask(variant.testVariant))
                    }
                }
            }
            // Before Android plugin 3.0.0
            catch (MissingMethodException ignored) {
                outputFiles.put(classifier as String, variant.outputs.get(0).outputFile as File)
                dependsOn.add(getAssembleTask(variant))
                if (variant.testVariant) {
                    outputFiles.put("test-$classifier" as String, variant.testVariant.outputs.get(0).outputFile as File)
                    dependsOn.add(getAssembleTask(variant.testVariant))
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
                    outputFiles.put("sources-$classifier" as String, sourcesJar.outputs.getFiles().getSingleFile())
                    dependsOn.add(sourcesJar)
                }
            }
        } else {
            deliveryLogger.logWarning("$classifier has no valid signing config and will not be archived")
        }
    }

    static Task getAssembleTask(variant){
        return variant.hasProperty("assembleProvider") ? variant.assembleProvider.get() : variant.assemble
    }
}
