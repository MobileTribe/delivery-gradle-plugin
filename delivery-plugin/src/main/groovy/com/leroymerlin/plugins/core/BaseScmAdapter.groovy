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
    String name, version, comment
    Map params = ['directory': 'delivery-test', 'errorMessage': 'An error occured']

    void setup(Project project, DeliveryPluginExtension extension, String comment) {
        this.project = project
        this.extension = extension
        this.name = project.properties["projectName"]
        this.version = project.properties["version"]
        this.comment = comment
    }

    @Override
    void initReleaseBranch() {
        if (!new File("delivery-test/.git").exists())
            exec(params, ["git", "init"])

        exec(params, ["git", "checkout", "-B", "\"" + name + "_" + version + "\""])
    }

    @Override
    void prepareReleaseFiles() {
    }

    @Override
    void commitChanges() {
        exec(params, ['git', 'add', '.'])
        exec(params, ['git', 'commit', '-am', "\"" + comment + "\""])
    }

    @Override
    void runBuild() {
        exec(['directory': ''], ['./gradlew', 'build'])
    }

    @Override
    void makeRelease() {
        exec(params, ['git', 'tag', '-a', "\"" + version + "\"", '-m', "\"" + comment + "\""])
        exec(params, ['git', 'push'])
    }
}
