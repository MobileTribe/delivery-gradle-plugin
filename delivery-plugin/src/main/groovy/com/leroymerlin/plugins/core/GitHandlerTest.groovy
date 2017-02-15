package com.leroymerlin.plugins.core

import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.cli.Executor
import org.gradle.api.Project

/**
 * Created by alexandre on 06/02/2017.
 */
class GitHandlerTest extends Executor implements BaseScmAdapter {
    Map params = ['directory': 'delivery-test', 'errorMessage': 'An error occured']

    @Override
    void setup(Project project, DeliveryPluginExtension extension) {
        println("git --version".execute().text)
        if (!new File('.git').exists())
            println('create')
        //recuperation des credentials
    }

    @Override
    void release() {
        println('release')
        //enlever les infos git
    }

    @Override
    String addAllFiles() throws ScmException {
        return println('addAllFiles')
    }

    @Override
    String commit(String comment) throws ScmException {
        return println('commmit ' + comment)
    }

    @Override
    String deleteBranch(String branchName) throws ScmException {
        return println('deleteBranch ' + branchName)
    }

    @Override
    String switchBranch(String branchName, boolean createIfNeeded) throws ScmException {
        return println('switchBranch ' + branchName + createIfNeeded)
    }

    @Override
    String tag(String annotation, String message) throws ScmException {
        return println('tag ' + annotation + message)
    }

    @Override
    String merge(String from) throws ScmException {
        return println('merge ' + from)
    }

    @Override
    String push() throws ScmException {
        return println('push')
    }
}