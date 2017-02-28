package com.leroymerlin.plugins.core

import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.cli.Executor
import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * Created by alexandre on 06/02/2017.
 */
class GitHandlerTest extends Executor implements BaseScmAdapter {

    String email, username
    List<String> list

    @Override
    void setup(Project project, DeliveryPluginExtension extension) {
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
        return println(generateGitCommand(['git', 'add', '.']))
    }

    @Override
    String commit(String comment) {
        return println(generateGitCommand(['git', 'commit', '-am', "\'" + comment + "\'"]))
    }

    @Override
    String deleteBranch(String branchName) {
        return println(generateGitCommand(['git', 'branch', '-d', branchName]))
    }

    @Override
    String switchBranch(String branchName, boolean createIfNeeded) {
        return println(generateGitCommand(['git', 'checkout', '-B', branchName]))
    }

    @Override
    String tag(String annotation, String message) {
        return println(generateGitCommand(['git', 'tag', '-a', annotation, '-m', '\'' + message + '\'']))
    }

    @Override
    String merge(String from) {
        return println(generateGitCommand(['git', 'merge', '--no-ff', from]))
    }

    @Override
    String push() {
        return println(generateGitCommand(['git', 'push']))
    }

    @Override
    List<String> generateGitCommand(List<String> command) {
        list = command
        if (username != null && email != null)
            list.addAll(1, ['-c', "user.name=$username", '-c', "user.email=$email"])
        return list
    }
}