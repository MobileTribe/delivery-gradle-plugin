package com.leroymerlin.plugins.core.configurators

import com.leroymerlin.plugins.DeliveryPlugin
import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.entities.SigningProperty
import com.leroymerlin.plugins.tasks.build.DeliveryBuild
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

    private final Logger logger = LoggerFactory.getLogger('IOSConfigurator')

    @Override
    void setup(Project project, DeliveryPluginExtension extension) {
        super.setup(project, extension)
        project.plugins.apply("org.openbakery.xcode-plugin")
    }

    @Override
    void configure() {
        if (!project.group) {
            throw new GradleException("Project group is not defined. Please use a gradle properties group")
        }
        logger.info("group used : ${project.group}")
    }

    @Override
    void applyProperties() {
        this.project.infoplist.version = project.version
    }

    @Override
    void applySigningProperty(SigningProperty property) {
        String target = property.target
        String scheme = property.scheme
        if (target == null || scheme == null) {
            throw new GradleException("signing config needs target and scheme properties")
        }
        def variantCodeName = scheme.trim().capitalize() + target.trim().capitalize()
        def taskName = "build${variantCodeName}Artifacts"
        Task buildTask = project.tasks.findByPath(taskName)
        if (buildTask == null) {
            project.task(taskName, type: DeliveryBuild, group: DeliveryPlugin.TASK_GROUP) {
                variantName project.projectName.toString().split(' ').collect({ m -> return m.toLowerCase() }).join("-") + "-" + scheme.trim().toLowerCase()
                outputFiles = ["": project.file("${project.getBuildDir()}/package/${variantCodeName}.ipa")]
            }.dependsOn(taskName + "Process")

            def parameter = project.getGradle().startParameter.newInstance()
            parameter.systemPropertiesArgs.put("xcodebuild", property.name)
            project.task(taskName + "Process", type: GradleBuild, group: DeliveryPlugin.TASK_GROUP) {
                startParameter = parameter
                tasks = ['archive', 'package']
            }
        }
        if (System.getProperty("xcodebuild")?.equals(property.name)) {
            project.xcodebuild.target = target
            project.xcodebuild.scheme = scheme
            project.xcodebuild {
                bundleName = scheme
                ipaFileName = variantCodeName
                configuration = "release"
                simulator = false
                signing {
                    certificateURI = project.file(property.certificateURI).toURI()
                    certificatePassword = property.certificatePassword
                    mobileProvisionURI = property.mobileProvisionURI.split(",").collect { path -> return project.file(path).toURI() }
                }
            }
            project.infoplist {
                version = project.version
            }
        }
    }

    @Override
    boolean handleProject(Project project) {
        File file = getProjectFile(project)
        return file != null
    }

    private File getProjectFile(Project project) {
        def files = project.projectDir.listFiles(new FilenameFilter() {
            @Override
            boolean accept(File dir, String name) {
                return name.contains("xcodeproj") || name.contains("xcworkspace")
            }
        })
        if (files.length > 1) {
            return files.find { file -> file.name.contains("xcworkspace") }
        } else if (files.length == 1) {
            return files[0]
        }
        return null
    }
}
