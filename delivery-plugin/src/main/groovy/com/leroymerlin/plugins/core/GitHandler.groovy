package com.leroymerlin.plugins.core

import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.cli.Executor
import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * Created by alexandre on 06/02/2017.
 */
class GitHandler extends Executor implements BaseScmAdapter {

    String email, username, branchToUse
    List<String> list
    Project project

    @Override
    void setup(Project project, DeliveryPluginExtension extension) {
        this.project = project;
        if (!"git --version".execute().text.contains('git version')) {
            throw new GradleException("Git not found, install Git before continue")
        } else {
            email = System.getProperty('SCM_EMAIL')
            username = System.getProperty('SCM_USER')
        }
    }

    @Override
    void release() {
    }

    @Override
    String addAllFiles() {
        return println(exec(generateGitCommand(['git', 'add', '.']), directory: project.rootDir, errorMessage: "Failed to add files", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']))
    }

    @Override
    String commit(String comment) {
        return println(exec(generateGitCommand(['git', 'commit', '-am', "\'" + comment + "\'"]), directory: project.rootDir, errorMessage: "Failed to commit", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']))
    }

    @Override
    String deleteBranch(String branchName) {
        return println(exec(generateGitCommand(['git', 'branch', '-d', branchName]), directory: project.rootDir, errorMessage: "Failed to delete $branchName", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']))
    }

    @Override
    String switchBranch(String branchName, boolean createIfNeeded) {
        branchToUse = branchName
        if (createIfNeeded)
            return println(exec(generateGitCommand(['git', 'checkout', '-B', branchName]), directory: project.rootDir, errorMessage: "Couldn't create $branchName", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']))
        else
            return println(exec(generateGitCommand(['git', 'checkout', branchName]), directory: project.rootDir, errorMessage: "Failed to switch to $branchName", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']))
    }

    @Override
    String tag(String annotation, String message) {
        return println(exec(generateGitCommand(['git', 'tag', '-a', annotation, '-m', '\'' + message + '\'']), directory: project.rootDir, errorMessage: "Duplicate tag [$annotation]", errorPatterns: ['already exists']))
    }

    @Override
    String merge(String from) {
        return println(exec(generateGitCommand(['git', 'merge', '--no-ff', from]), directory: project.rootDir, errorMessage: "Failed to merge $from", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']))
    }

    @Override
    String push() {
        return println(exec(generateGitCommand(['git', 'push', '-u', 'origin', branchToUse != null ? branchToUse : 'master']), directory: project.rootDir, errorMessage: ' Failed to push to remote ', errorPatterns: ['[rejected] ', ' error: ', ' fatal: ']))
    }

    @Override
    List<String> generateGitCommand(List<String> command) {
        list = command
        if (username != null && email != null)
            list.addAll(1, ['-c', "user.name=$username", '-c', "user.email=$email"])
        return list
    }
}