package com.leroymerlin.plugins.core

import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.cli.Executor
import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * Created by alexandre on 06/02/2017.
 */
class GitHandler extends Executor implements BaseScmAdapter {

    String email, username

    @Override
    void setup(Project project, DeliveryPluginExtension extension) {
        if (!"git --version".execute().text.contains('git version')) {
            throw new GradleException("Git not found, install Git before continue")
        } else {
            email = System.getProperty('SCM_EMAIL')
            username = System.getProperty('SCM_USER')

            if (!new File('.git').exists()) {
                println(exec(['git', 'init'], directory: 'android', errorMessage: "Failed to init Git", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']))
            }
        }
    }

    @Override
    void release() {
    }

    @Override
    String addAllFiles() {
        return println(exec(['git', '-c', "user.name=$username", '-c', "user.email=$email", 'add', '.'], directory: 'android', errorMessage: "Failed to add files", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']))
    }

    @Override
    String commit(String comment) {
        return println(exec(['git', '-c', "user.name=$username", '-c', "user.email=$email", 'commit', '-am', "\'" + comment + "\'"], directory: 'android', errorMessage: "Failed to commit", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']))
    }

    @Override
    String deleteBranch(String branchName) {
        return println(exec(['git', '-c', "user.name=$username", '-c', "user.email=$email", 'branch', '-d', branchName], directory: 'android', errorMessage: "Failed to delete $branchName", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']))
    }

    @Override
    String switchBranch(String branchName, boolean createIfNeeded) {
        if (createIfNeeded)
            return println(exec(['git', '-c', "user.name=$username", '-c', "user.email=$email", 'checkout', '-B', branchName], directory: 'android', errorMessage: "Couldn't create $branchName", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']))
        else
            return println(exec(['git', '-c', "user.name=$username", '-c', "user.email=$email", 'checkout', branchName], directory: 'android', errorMessage: "Failed to switch to $branchName", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']))
    }

    @Override
    String tag(String annotation, String message) {
        return println(exec(['git', '-c', "user.name=$username", '-c', "user.email=$email", 'tag', '-a', annotation, '-m', '\'' + message + '\''], directory: 'android', errorMessage: "Duplicate tag [$annotation]", errorPatterns: ['already exists']))
    }

    @Override
    String merge(String from) {
        return println(exec(['git', '-c', "user.name=$username", '-c', "user.email=$email", 'merge', '--no-ff', from], directory: 'android', errorMessage: "Failed to merge $from", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']))
    }

    @Override
    String push() {
        return println(exec(['git', '-c', "user.name=$username", '-c', "user.email=$email", 'push'], directory: ' delivery - test ', errorMessage: ' Failed to push to remote ', errorPatterns: ['[rejected] ', ' error: ', ' fatal: ']))
    }

    @Override
    List<String> generateGitCommand(List<String> command) {
        return null
    }
}