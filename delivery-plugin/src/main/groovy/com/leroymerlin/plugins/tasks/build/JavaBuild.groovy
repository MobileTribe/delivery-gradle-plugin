package com.leroymerlin.plugins.tasks.build

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.bundling.Jar

/**
 * Created by alexandre on 15/02/2017.
 */
class JavaBuild extends DeliveryBuildTask {


    @Override
    void setVariantName(String variantName) {
        super.setVariantName(variantName)

        outputFiles.put("", project.jar.outputs.getFiles()[0])
        dependsOn.add(project.build)
        dependsOn.add(project.jar)

        def sourcesJar = project.task("sources${variantName}Jar", type: Jar) {
            classifier = 'sources'
            from project.compileJava.destinationDir
        }
        outputFiles.put("sources", sourcesJar.outputs.getFiles().getSingleFile())
        dependsOn.add(sourcesJar)


    }


}
