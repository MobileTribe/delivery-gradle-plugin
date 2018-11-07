package com.leroymerlin.plugins.core.configurators

import com.leroymerlin.plugins.DeliveryPlugin
import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.cli.Executor
import com.leroymerlin.plugins.entities.SigningProperty
import com.leroymerlin.plugins.tasks.build.PrepareBuildTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.GradleBuild
import org.gradle.api.tasks.Upload

/**
 * Created by alexandre on 17/07/2017.
 */
class FlutterConfigurator extends ProjectConfigurator {

    private final String PLATFORM_ANDROID = 'android'
    private final String PLATFORM_IOS = 'ios'


    @Override
    void setup(Project project, DeliveryPluginExtension extension) {
        super.setup(project, extension)

        def result = Executor.exec(["flutter", "--version"]) {
            needSuccessExitCode = false
            silent = true
        }

        if (result.exitValue == Executor.EXIT_CODE_NOT_FOUND) {
            throw new GradleException("I don't find flutter :(, please look at https://flutter.io/ for more information")
        }

        //this task will be link by subproject prepare task
        project.task("prepareFlutter", type: PrepareBuildTask, group: DeliveryPlugin.TASK_GROUP).doLast {
            Executor.exec(["flutter", "build", "bundle", "--release"], {
                directory = project.projectDir
            })
        }
    }

    @Override
    void configure() {

        [PLATFORM_ANDROID, PLATFORM_IOS].each {
            platform ->
//                    def buildTask = project.task("build${platform.capitalize()}", type: DeliveryBuild){
//                        variantName = platform
//                    }
                def newStartParameter = project.getGradle().startParameter.newInstance()
                newStartParameter.projectDir = project.file(platform)
                newStartParameter.buildFile = new File(newStartParameter.projectDir, "build.gradle")
                newStartParameter.systemPropertiesArgs.put(PARENT_BUILD_ROOT, project.rootDir.path)
                newStartParameter.systemPropertiesArgs.put(DeliveryPlugin.VERSION_ARG, project.version as String)
                newStartParameter.systemPropertiesArgs.put(DeliveryPlugin.VERSION_ID_ARG, project.versionId as String)
                newStartParameter.systemPropertiesArgs.put(DeliveryPlugin.PROJECT_NAME_ARG, project.artifact as String)
                newStartParameter.systemPropertiesArgs.put(DeliveryPlugin.GROUP_ARG, project.group as String)

                def nestedBuild = project.task("${platform}NestedBuild", type: GradleBuild, group: DeliveryPlugin.TASK_GROUP) {
                    startParameter = newStartParameter
                    tasks = ['uploadArtifacts']
                } as GradleBuild


                println("buildFile ${nestedBuild.buildFile}")
                println("dir ${nestedBuild.dir}")
                nestedBuild.dependsOn += project.tasks.withType(PrepareBuildTask.class)


                def uploadTask = project.task("${DeliveryPlugin.UPLOAD_TASK_PREFIX}${platform.capitalize()}", type: Upload, group: DeliveryPlugin.TASK_GROUP) {
                    configuration = project.configurations.create("flutter${platform.capitalize()}")
                    repositories {}
                }

                uploadTask.dependsOn += nestedBuild
        }


    }

    @Override
    void applyProperties() {
        def file = project.file("pubspec.yaml")

//            DumperOptions options = new DumperOptions()
//            options.setPrettyFlow(true)
//            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
//            def yaml = new Yaml(options)
//            def dico = yaml.load(file.text)
//            dico["version"] = "${project.version}+${project.versionId}"
//            file.text = yaml.dump(dico)

        String fileStr = ""
        file.eachLine { line ->
            if (line.startsWith("version:")) {
                fileStr += "version: ${project.version}+${project.versionId}" + System.getProperty("line.separator")
            } else {
                fileStr += line + System.getProperty("line.separator")
            }
        }
        file.text = fileStr

    }

    @Override
    void applySigningProperty(SigningProperty signingProperty) {
        throw new GradleException("SigningProperty ${signingProperty.name} should be configured in Android or IOS project")
    }

    @Override
    boolean handleProject(Project project) {
        return flutterFileExist(project.projectDir) //|| flutterFileExist(project.rootDir.parentFile)
    }

    private static boolean flutterFileExist(File file) {
        def pubspecFile = new File(file, "pubspec.yaml")
        return pubspecFile.exists() && pubspecFile.text.contains("flutter:")
    }

}
