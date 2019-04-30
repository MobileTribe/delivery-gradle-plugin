package com.leroymerlin.plugins.core

import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.cli.DeliveryLogger
import com.leroymerlin.plugins.cli.Executor
import com.leroymerlin.plugins.utils.SystemUtils
import org.gradle.api.Project

/**
 * Created by alexandre on 06/02/2017.
 */

class GitAdapter implements BaseScmAdapter {

    private final DeliveryLogger deliveryLogger = new DeliveryLogger()

    private String email, username, password
    private List<String> list
    private Project project

    def gitEnv = [:]

    @Override
    void setup(Project project, DeliveryPluginExtension extension) {
        this.project = project
        def result = Executor.exec(["git", "status"]) {
            needSuccessExitCode = false
            silent = true
        }
        if (result.exitValue != Executor.EXIT_CODE_OK) {
            deliveryLogger.logWarning("Git is not initialized on this project")
        } else {
            email = SystemUtils.getEnvProperty('SCM_EMAIL')
            username = SystemUtils.getEnvProperty('SCM_USER')
            password = SystemUtils.getEnvProperty('SCM_PASSWORD')

            if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {

                def credentialFile = File.createTempFile("git", "cred")
                credentialFile << '''#!/bin/bash

if [[ "$1" =~ ^Password.*$ ]] ;
then
        echo "''' + password + '''"
fi

if [[ "$1" =~ ^Username.*$ ]] ;
then
        echo "''' + username + '''"
fi

'''
                credentialFile.setExecutable(true)
                credentialFile.deleteOnExit()
                deliveryLogger.logInfo("GIT_ASKPASS configured")
                gitEnv.put("GIT_ASKPASS", credentialFile.absolutePath)
            }

            //configure origin
            Executor.exec(['git', 'config', 'remote.origin.fetch', "+refs/heads/*:refs/remotes/origin/*"]) {
                silent = true
            }
        }
    }

    @Override
    void addFiles(String[] files) {
        files.each {
            f ->
                exec(generateGitCommand(['git', 'add', f]))
        }
    }

    @Override
    void commit(String message) {
        exec(generateGitCommand(['git', 'commit', '-m', "\'" + message + "\'"]))
    }

    @Override
    void deleteBranch(String branchName) {
        exec(generateGitCommand(['git', 'branch', '-d', branchName]))
    }

    @Override
    void switchBranch(String branchName, boolean createIfNeeded) {
        def params = ['git', 'checkout', branchName]
        if (createIfNeeded) {
            params.add(2, "-B")
        }
        exec(generateGitCommand(params))
    }

    @Override
    void tag(String annotation, String message) {
        def tagName = annotation.replace(" ", "")
        exec(generateGitCommand(['git', 'tag', '-a', tagName, '-m', '\'' + message + '\'']))
    }

    @Override
    void merge(String from) {
        exec(generateGitCommand(['git', 'merge', '--no-ff', from]))
    }

    @Override
    void push(String branch = "", boolean tags) {
        if (branch == null || branch.isEmpty()) {
            branch = exec(generateGitCommand(['git', 'rev-parse', '--abbrev-ref', "HEAD"])).logs.replace("\n", "").replace("\r", "")
        }
        def params = ['git', 'push', '-u', 'origin', branch]
        if (tags) {
            params << '--follow-tags'
        }
        exec(generateGitCommand(params))
    }

    @Override
    void pushTag(String tagName) {
        if (tagName == null) {
            // If no tag is specified, push all sane tags (means annotated and reachable)
            exec(generateGitCommand(['git', 'push', '--follow-tags']))
        } else {
            // If a tag is specified, only push this one
            exec(generateGitCommand(['git', 'push', 'origin', tagName]))
        }
    }

    @Override
    void pull() {
        exec(generateGitCommand(['git', 'pull']))
    }

    List<String> generateGitCommand(List<String> command) {
        list = command
        if (email != null && !email.isEmpty())
            list.addAll(1, ['-c', "user.email=$email"])
        return list
    }

    @Override
    void discardChange() {
        exec(['git', 'checkout', '--', '.'])
        exec(['git', 'reset', 'HEAD', '.'])
        exec(['git', 'clean', '-df'])
    }

    private Executor.ExecutorResult exec(List<String> cmd) {
        return Executor.exec(cmd, {
            directory = project.rootDir
            env += gitEnv
        })
    }
}
