package com.leroymerlin.plugins.core.configurators

import com.leroymerlin.plugins.DeliveryPlugin
import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.entities.SigningProperty
import com.leroymerlin.plugins.tasks.build.DeliveryBuildTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.GradleBuild
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by florian on 30/01/2017.
 */
class IOSConfigurator extends ProjectConfigurator {

    static String JAVA_PLUGIN_ID = "java"

    Logger logger = LoggerFactory.getLogger('IOSConfigurator')

    @Override
    def setup(Project project, DeliveryPluginExtension extension) {
        super.setup(project, extension)
        project.plugins.apply("org.openbakery.xcode-plugin")
    }

    @Override
    def configure() {

        if (!project.group) {
            throw new GradleException("Project group is not defined. Please use a gradle properties group")
        }
        logger.info("group used : ${project.group}")


    }

    @Override
    def applyProperties(String version, String versionId, String projectName) {
        xcodebuild{
            infoplist {
                version = version
            }
        }

    }

    @Override
    def applySigningProperty(SigningProperty property) {
        String target = property.target
        String scheme = property.scheme
        if (target == null || scheme == null) {
            def split = property.name.split(".")
            if (split.length == 2) {
                scheme = split[0]
                target = split[1]
            }
        }

        if (target != null && scheme != null) {
            def variantCodeName = scheme.trim().capitalize() + target.trim().capitalize()
            def taskName = "build${variantCodeName}Artifacts"
            Task buildTask = project.tasks.findByPath(taskName)
            if (buildTask == null) {
                project.task(taskName, type: DeliveryBuildTask, group: DeliveryPlugin.TASK_GROUP) {
                    variantName variantCodeName
                    outputFiles = ["": project.file("build/package/${variantName}.ipa")]
                }.dependsOn(taskName + "Process")

                def parameter = project.getGradle().startParameter.newInstance()
                parameter.systemPropertiesArgs.put("xcodebuild", property.name)
                project.task(taskName + "Process", type: GradleBuild) {
                    startParameter: parameter
                    tasks: ['archive', 'package']
                }
            }
        }


        if(System.hasProperty("xcodebuild") && System.getProperty("xcodebuild").equals(property.name)){

            xcodebuild {
                buildRoot = project.file("build")
                ipaFileName = variantName
                target = target
                scheme = scheme
                configuration = "release"
                simulator = false
                signing {
                    certificateURI = project.file(signing.certificateURI)
                    certificatePassword = signing.certificatePassword
                    mobileProvisionURI = signing.mobileProvisionURI.split(",").collect {path -> project.file(path)}
                }

            }



        }




    }

    @Override
    boolean handleProject(Project project) {
        def files = project.projectDir.listFiles({ File dir, String name -> name.contains("xcodeproj") || name.contains("xcworkspace") })
        return files.length != 0
    }
}
