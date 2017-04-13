package com.leroymerlin.plugins.core

import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.cli.Executor
import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * Created by alexandre on 06/02/2017.
 */

class GitAdapter extends Executor implements BaseScmAdapter {

    private String email, username, branchToUse
    private List<String> list
    private Project project

    @Override
    void setup(Project project, DeliveryPluginExtension extension) {
        logger = project.getLogger()
        this.project = project
        if (!"git --version".execute().text.contains('git version')) {
            throw new GradleException("Git not found, install Git before continue")
        } else {
            email = System.getProperty('SCM_EMAIL')
            username = System.getProperty('SCM_USER')
        }
    }

    @Override
    String addFiles(String[] files) {
        def result = ""
        files.each {
            f ->
                result = exec(generateGitCommand(['git', 'add', f]), directory: project.rootDir, errorMessage: "Failed to add files", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']) + '\n'
        }
        return result
    }

    @Override
    String commit(String message) {
        def result = exec(generateGitCommand(['git', 'commit', '-m', "\'" + message + "\'"]), directory: project.rootDir, errorMessage: "Failed to commit", errorPatterns: ['[rejected]', 'error: ', 'fatal: '])
        return result
    }

    @Override
    String deleteBranch(String branchName) {
        def result = exec(generateGitCommand(['git', 'branch', '-d', branchName]), directory: project.rootDir, errorMessage: "Failed to delete $branchName", errorPatterns: ['[rejected]', 'error: ', 'fatal: '])
        return result
    }

    @Override
    String switchBranch(String branchName, boolean createIfNeeded) {
        branchToUse = branchName

        def params = ['git', 'checkout', branchName]
        if (createIfNeeded) {
            params.add(2, "-B")
        }
        def result = exec(generateGitCommand(params), directory: project.rootDir, errorMessage: "Couldn't switch to $branchName", errorPatterns: ['[rejected]', 'error: ', 'fatal: '])

        return result
    }

    @Override
    String tag(String annotation, String message) {
        def result = exec(generateGitCommand(['git', 'tag', '-a', annotation, '-m', '\'' + message + '\'']), directory: project.rootDir, errorMessage: "Duplicate tag [$annotation]", errorPatterns: ['already exists'])

        return result
    }

    @Override
    String merge(String from) {
        def result = exec(generateGitCommand(['git', 'merge', '--no-ff', from]), directory: project.rootDir, errorMessage: "Failed to merge $from", errorPatterns: ['[rejected]', 'error: ', 'fatal: '])
        return result
    }

    @Override
    String push() {
        def result = exec(generateGitCommand(['git', 'push', '-u', 'origin', branchToUse != null ? branchToUse : 'master']), directory: project.rootDir, errorMessage: ' Failed to push to remote ', errorPatterns: ['[rejected] ', ' error: ', ' fatal: '])
        return result
    }

    @Override
    String pushTag(String tagName) {
        def result = ""

        if (tagName == null) {
            // If no tag is specified, push all sane tags (means annotated and reachable)
            result = exec(generateGitCommand(['git', 'push', '--follow-tags']), directory: project.rootDir, errorMessage: ' Failed to push tags to remote ', errorPatterns: ['[rejected] ', ' error: ', ' fatal: '])
        } else {
            // If a tag is specified, only push this one
            result = exec(generateGitCommand(['git', 'push', 'origin', tagName]), directory: project.rootDir, errorMessage: ' Failed to push tag to remote ', errorPatterns: ['[rejected] ', ' error: ', ' fatal: '])
        }

        return result
    }

    @Override
    List<String> generateGitCommand(List<String> command) {
        list = command
        if (username != null && email != null)
            list.addAll(1, ['-c', "user.name=$username", '-c', "user.email=$email"])
        return list
    }

    @Override
    String discardChange() {
        def result = exec(['git', 'checkout', '--', '.'], directory: project.rootDir)
        result += exec(['git', 'reset', 'HEAD', '.'], directory: project.rootDir)
        result += exec(['git', 'clean', '-df'], directory: project.rootDir)
        return result
    }
}