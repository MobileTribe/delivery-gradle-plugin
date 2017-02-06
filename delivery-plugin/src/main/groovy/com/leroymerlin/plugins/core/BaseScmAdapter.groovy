package com.leroymerlin.plugins.core

import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.cli.Executor
import org.gradle.api.Project

/**
 * Created by florian on 30/01/2017.
 */
class BaseScmAdapter extends Executor implements BaseScmInterface {

    Project project
    DeliveryPluginExtension extension
    String name, version, comment, releaseBranchName
    Map params = ['directory': 'delivery-test', 'errorMessage': 'An error occured']

    void setup(Project project, DeliveryPluginExtension extension, String comment) {
        this.project = project
        this.extension = extension
        this.name = project.properties["projectName"]
        this.version = project.properties["version"]
        this.comment = comment
        this.releaseBranchName = this.name + "-" + this.version
    }

    @Override
    void initReleaseBranch() {
        if (!new File("delivery-test/.git").exists())
            println exec(params, ["git", "init"])

        println exec(params, ["git", "checkout", "-b", releaseBranchName, "develop"])
    }

    @Override
    void commitChanges() {
        println exec(params, ['git', 'commit', '-am', "\"" + comment + "\""])
    }

    @Override
    void prepareReleaseFiles() {
        println exec(params, ['git', 'checkout', 'master'])
        println exec(params, ['git', 'merge', '--no-ff', releaseBranchName])
        println exec(params, ['git', 'tag', '-a', version])
        println exec(params, ['git', 'checkout', 'develop'])
        println exec(params, ['git', 'merge', '--no-ff', releaseBranchName])
    }

    @Override
    void runBuild() {
        println exec(['directory': ''], ['./gradlew', 'build'])
    }

    @Override
    void makeRelease() {
        println exec(params, ['git', 'checkout', 'master'])
        println exec(params, ['git', 'push'])
        println exec(params, ['git', 'checkout', 'develop'])
        println exec(params, ['git', 'push'])
        println exec(params, ['git', 'checkout', releaseBranchName])
        println exec(params, ['git', 'push'])
    }
}
