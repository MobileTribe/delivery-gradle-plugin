package com.leroymerlin.plugins.core.configurators

import com.leroymerlin.plugins.DeliveryPlugin
import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.cli.Executor
import com.leroymerlin.plugins.entities.SigningProperty
import com.leroymerlin.plugins.tasks.build.DeliveryBuild
import com.leroymerlin.plugins.tasks.build.PrepareBuildTask
import com.leroymerlin.plugins.utils.SystemUtils
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.GradleBuild

/**
 * Created by florian on 30/01/2017.
 */
class IOSConfigurator extends ProjectConfigurator {

    static def pluginId = "org.openbakery.xcode-plugin"
    boolean isFlutterProject

    @Override
    void setup(Project project, DeliveryPluginExtension extension) {
        if (!Os.isFamily(Os.FAMILY_MAC)) {
            throw new Exception("Please use macOS to use this feature")
        }

        super.setup(project, extension)

        if (SystemUtils.getEnvProperty("KEYCHAIN_PASSWORD") != null) {
            Executor.exec(["security", "unlock-keychain", "-p", SystemUtils.getEnvProperty("KEYCHAIN_PASSWORD"), "~/Library/Keychains/login.keychain-db"]) {
                directory = project.projectDir
            }
        }

        project.task("prepareIOSProject", type: PrepareBuildTask, group: DeliveryPlugin.TASK_GROUP).doLast {
            if (isFlutterProject) {
                project.file("Flutter/Generated.xcconfig").delete()
                Executor.exec(["flutter", "build", "ios", "--no-codesign"]) {
                    directory = new File(project.projectDir.toString().replace("/ios", ""))
                }
            }
        }

    }

    @Override
    void configure() {
        if (!project.group) {
            throw new GradleException("Project group is not defined. Please use a gradle properties group")
        }
        if (!project.plugins.hasPlugin(pluginId)) {
            project.plugins.apply(pluginId)
            applyProperties()
        }
    }

    @Override
    void applyProperties() {
        if (project.plugins.hasPlugin(pluginId)) {
            this.project.infoplist.version = project.version
        }
    }

    @Override
    void applySigningProperty(SigningProperty property) {
        if (!project.plugins.hasPlugin(pluginId)) {
            configure()
        }

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
                variantName project.artifact.toString().split(' ').collect({ m -> return m.toLowerCase() }).join("-") + ("-" + target.trim().toLowerCase())
                outputFiles = [("${scheme.trim().toLowerCase().replace(" ", "-")}".toString()): project.file("${project.getBuildDir()}/package/${variantCodeName}.ipa")]
            }.dependsOn(taskName + "Process")

            def parameter = project.getGradle().startParameter.newInstance()
            parameter.systemPropertiesArgs.put("xcodebuild", property.name)
            def buildTaskProcess = project.task(taskName + "Process", type: GradleBuild, group: DeliveryPlugin.TASK_GROUP) {
                startParameter = parameter
                tasks = ['archive', 'package']
            }

            buildTaskProcess.dependsOn += project.tasks.withType(PrepareBuildTask)
        }
        if (SystemUtils.getEnvProperty("xcodebuild") == property.name) {
            project.xcodebuild.target = target
            project.xcodebuild.scheme = scheme
            project.xcodebuild {
                bundleName = scheme
                ipaFileName = variantCodeName
                configuration = "Release"
                simulator = false
                signing {
                    certificateURI = project.file(property.certificateURI).toURI()
                    certificatePassword = property.certificatePassword
                    mobileProvisionURI = property.mobileProvisionURI.split(',').collect { path -> return project.file(path).toURI() }
                }
            }
            project.infoplist {
                version = project.version
                shortVersionString = project.versionId
            }
        }
    }

    @Override
    boolean handleProject(Project project) {
        File file = getProjectFile(project)
        return file != null
    }

    private static File getProjectFile(Project project) {
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
