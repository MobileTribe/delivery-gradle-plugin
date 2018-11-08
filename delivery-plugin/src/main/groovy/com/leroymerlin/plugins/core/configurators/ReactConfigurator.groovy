package com.leroymerlin.plugins.core.configurators

import com.leroymerlin.plugins.DeliveryPlugin
import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.cli.Executor
import com.leroymerlin.plugins.entities.SigningProperty
import com.leroymerlin.plugins.tasks.build.PrepareBuildTask
import groovy.json.JsonSlurper
import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * Created by alexandre on 17/07/2017.
 */
class ReactConfigurator extends ProjectConfigurator {


    @Override
    void setup(Project project, DeliveryPluginExtension extension) {
        super.setup(project, extension)

        def npmResult = Executor.exec(["npm"]) {
            needSuccessExitCode = false
            silent = true
            directory = project.rootDir
        }

        if (npmResult.exitValue == Executor.EXIT_CODE_NOT_FOUND) {
            throw new GradleException("I don't find npm :(, please look at https://www.npmjs.com/get-npm for more information")
        }

        //if node module does't exist we start the command in order to sync gradle
        if (!new File(project.rootDir, "node_modules").exists()) {
            Executor.exec(["npm", "install"]) {
                directory = project.rootDir
            }
        }

        //this task will be link by subproject prepare task
        project.task("prepareNpm", type: PrepareBuildTask, group: DeliveryPlugin.TASK_GROUP).doLast {
            Executor.exec(["npm", "install"]) {
                directory = project.rootDir
            }

            // Temporary fix the issue https://github.com/facebook/react-native/issues/21168 from Xcode 10

            Executor.exec(["./scripts/ios-install-third-party.sh"]) {
                directory = new File(project.rootDir, "node_modules/react-native")
                needSuccessExitCode = false
            }
            def glogFolder = new File(project.rootDir, "node_modules/react-native/third-party").listFiles().find {
                it.name.startsWith("glog")
            }
            if (glogFolder?.exists()) {
                Executor.exec(["../../scripts/ios-configure-glog.sh"]) {
                    directory = glogFolder
                    needSuccessExitCode = false
                }
            }
        }
    }

    @Override
    void configure() {
    }

    @Override
    void applyProperties() {

    }


    @Override
    void applySigningProperty(SigningProperty signingProperty) {
        throw new GradleException("SigningProperty ${signingProperty.name} should be configured in Android or IOS submodule")
    }

    @Override
    boolean handleProject(Project project) {
        boolean reactProject = false
        if (project.file('package.json').exists()) {
            def json = new JsonSlurper().parseText(project.file('package.json').text)
            if (json.dependencies.react != null || json.dependencies."react-native" != null)
                reactProject = true
        }
        return reactProject
    }

}
