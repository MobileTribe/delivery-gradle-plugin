package com.leroymerlin.plugins.core

import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.cli.Executor
import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * Created by alexandre on 06/02/2017.
 */

class GitAdapter extends Executor implements BaseScmAdapter {

    private String email, username, password
    private List<String> list
    private Project project

    def gitEnv = [:]

    @Override
    void setup(Project project, DeliveryPluginExtension extension) {
        this.project = project
        if (!"git --version".execute().text.contains('git version')) {
            throw new GradleException("Git not found, install Git before continue")
        } else {
            email = System.getProperty('SCM_EMAIL')
            username = System.getProperty('SCM_USER')
            password = System.getProperty('SCM_PASSWORD')

            if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {

                def credentialFile = File.createTempFile("git", "cred")
                credentialFile << '''
if [[ $1 == Password* ]] ;
then
        echo "''' + password + '''"
fi

if [[ $1 == Username* ]] ;
then
        echo "''' + username + '''"
fi
'''
                credentialFile.setExecutable(true)
                credentialFile.deleteOnExit()
                logger.warn("GIT_ASKPASS configured")
                gitEnv.put("GIT_ASKPASS", credentialFile.absolutePath)


            }

            //configure origin
            exec(['git', 'config', 'remote.origin.fetch', "+refs/heads/*:refs/remotes/origin/*"], [directory: project.rootDir])
        }
    }

    @Override
    String addFiles(String[] files) {
        def result = ""
        files.each {
            f ->
                result = exec(generateGitCommand(['git', 'add', f]), [env: gitEnv, directory: project.rootDir, errorMessage: "Failed to add files", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']]) + '\n'
        }
        return result
    }

    @Override
    String commit(String message) {
        def result = exec(generateGitCommand(['git', 'commit', '-m', "\'" + message + "\'"]), [env: gitEnv, directory: project.rootDir, errorMessage: "Failed to commit", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']])
        return result
    }

    @Override
    String deleteBranch(String branchName) {
        def result = exec(generateGitCommand(['git', 'branch', '-d', branchName]), [env: gitEnv, directory: project.rootDir, errorMessage: "Failed to delete $branchName", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']])
        return result
    }

    @Override
    String switchBranch(String branchName, boolean createIfNeeded) {
        def params = ['git', 'checkout', branchName]
        if (createIfNeeded) {
            params.add(2, "-B")
        }
        def result = exec(generateGitCommand(params), [env: gitEnv, directory: project.rootDir, errorMessage: "Couldn't switch to $branchName", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']])
        return result
    }

    @Override
    String tag(String annotation, String message) {
        def tagName = annotation.replace(" ", "")
        def result = exec(generateGitCommand(['git', 'tag', '-a', tagName, '-m', '\'' + message + '\'']), [env: gitEnv, directory: project.rootDir, errorMessage: "Duplicate tag [$annotation]", errorPatterns: ['already exists']])
        return result
    }

    @Override
    String merge(String from) {
        def result = exec(generateGitCommand(['git', 'merge', '--no-ff', from]), [env: gitEnv, directory: project.rootDir, errorMessage: "Failed to merge $from", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']])
        return result
    }

    @Override
    String push(String branch = "", boolean tags) {
        if (branch == null || branch.isEmpty()) {
            branch = exec(['git', 'rev-parse', '--abbrev-ref', "HEAD"], [env: gitEnv, directory: project.rootDir, errorPatterns: ['HEAD'], errorMessage: "Can't push detached HEAD"]).replace("\n", "").replace("\r", "")
        }
        def params = ['git', 'push', '-u', 'origin', branch]
        if (tags) {
            params << '--follow-tags'
        }
        def result = exec(generateGitCommand(params), [env: gitEnv, directory: project.rootDir, errorMessage: ' Failed to push to remote ', errorPatterns: ['[rejected] ', 'error: ', 'fatal: ']])
        return result
    }

    @Override
    String pushTag(String tagName) {
        def result
        if (tagName == null) {
            // If no tag is specified, push all sane tags (means annotated and reachable)
            result = exec(generateGitCommand(['git', 'push', '--follow-tags']), [env: gitEnv, directory: project.rootDir, errorMessage: ' Failed to push tags to remote ', errorPatterns: ['[rejected] ', 'error: ', 'fatal: ']])
        } else {
            // If a tag is specified, only push this one
            result = exec(generateGitCommand(['git', 'push', 'origin', tagName]), [env: gitEnv, directory: project.rootDir, errorMessage: ' Failed to push tag to remote ', errorPatterns: ['[rejected] ', 'error: ', 'fatal: ']])
        }
        return result
    }

    @Override
    String pull() {
        return exec(generateGitCommand(['git', 'pull']), [env: gitEnv, directory: project.rootDir, errorMessage: ' Failed to pull from remote ', errorPatterns: ['[rejected] ', 'error: ', 'fatal: ']])
    }

    @Override
    List<String> generateGitCommand(List<String> command) {
        list = command
        if (email != null && !email.isEmpty())
            list.addAll(1, ['-c', "user.email=$email"])
        return list
    }

    @Override
    String discardChange() {
        def result = exec(['git', 'checkout', '--', '.'], [directory: project.rootDir])
        result += exec(['git', 'reset', 'HEAD', '.'], [directory: project.rootDir])
        result += exec(['git', 'clean', '-df'], [directory: project.rootDir])
        return result
    }
}