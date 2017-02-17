package com.leroymerlin.plugins.core

import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.cli.Executor
import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * Created by alexandre on 06/02/2017.
 */
class GitHandler extends Executor implements BaseScmAdapter {

    URI baseUri

    @Override
    void setup(Project project, DeliveryPluginExtension extension) {
        if (!"git --version".execute().text.contains('git version')) {
            throw new GradleException("Git not found, install Git before continue")
        } else {
            String password = System.getProperty('SCM_PASSWORD')
            String user = System.getProperty('SCM_USER')
            if (password == null || user == null) {
                throw new GradleException('Add credentials to continue')
            }

            if (!new File('.git').exists()) {
                println(exec(['git', 'init'], directory: 'delivery-test', errorMessage: "Failed to init Git", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']))
            } else {
                baseUri = new URI(exec(['git', 'config', '--local', '--get', 'remote.origin.url'], errorMessage: "Fail to read origin url").readLines()[0])
                String domain = baseUri.getHost() + baseUri.path
                String credential = "${baseUri.getScheme()}://$user:$password@$domain"
                exec(['git', 'remote', 'rm', 'origin'], errorMessage: "Fail to remove origin")
                exec(['git', 'remote', 'add', 'origin', credential], errorMessage: "Fail to add origin")
            }
        }
    }

    @Override
    void release() {
        System.clearProperty('SCM_PASSWORD')
        System.clearProperty('SCM_USER')
    }

    @Override
    String addAllFiles() {
        return println(exec(['git', 'add', '.'], directory: 'delivery-test', errorMessage: "Failed to add files", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']))
    }

    @Override
    String commit(String comment) {
        return println(exec(['git', 'commit', '-am', "\'" + comment + "\'"], directory: 'delivery-test', errorMessage: "Failed to commit", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']))
    }

    @Override
    String deleteBranch(String branchName) {
        return println(exec(['git', 'branch', '-d', branchName], directory: 'delivery-test', errorMessage: "Failed to delete $branchName", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']))
    }

    @Override
    String switchBranch(String branchName, boolean createIfNeeded) {
        if (createIfNeeded)
            return println(exec(['git', 'checkout', '-B', branchName], directory: 'delivery-test', errorMessage: "Couldn't create $branchName", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']))
        else
            return println(exec(['git', 'checkout', branchName], directory: 'delivery-test', errorMessage: "Failed to switch to $branchName", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']))
    }

    @Override
    String tag(String annotation, String message) {
        return println(exec(['git', 'tag', '-a', annotation, '-m', '\'' + message + '\''], directory: 'delivery-test', errorMessage: "Duplicate tag [$annotation]", errorPatterns: ['already exists']))
    }

    @Override
    String merge(String from) {
        return println(exec(['git', 'merge', '--no-ff', from], directory: 'delivery-test', errorMessage: "Failed to merge $from", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']))
    }

    @Override
    String push() {
        return println(exec(['git', 'push'], directory: 'delivery-test', errorMessage: 'Failed to push to remote', errorPatterns: ['[rejected]', 'error: ', 'fatal: ']))
    }
}