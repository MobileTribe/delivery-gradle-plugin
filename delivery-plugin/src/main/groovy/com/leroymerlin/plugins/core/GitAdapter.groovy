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
        def result = exec(["git", "status"]) {
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
                deliveryLogger.logInfo("GIT_ASKPASS configured")
                gitEnv.put("GIT_ASKPASS", credentialFile.absolutePath)
            }

            //configure origin
            exec(['git', 'config', 'remote.origin.fetch', "+refs/heads/*:refs/remotes/origin/*"])
        }
    }

    @Override
    void addFiles(String[] files) {
        files.each {
            f ->
                exec(generateGitCommand(['git', 'add', f])) {
                    env += gitEnv
                    handleError(['[rejected]', 'error: ', 'fatal: '], {
                        fatal = true
                        message = "Failed to add files"
                    })
                }
        }
    }

    @Override
    void commit(String message) {
        exec(generateGitCommand(['git', 'commit', '-m', "\'" + message + "\'"])) {
            env += gitEnv
            handleError(['[rejected]', 'error: ', 'fatal: '], {
                fatal = true
                message = "Failed to commit"
            })
        }
    }

    @Override
    void deleteBranch(String branchName) {
        exec(generateGitCommand(['git', 'branch', '-d', branchName])) {
            env += gitEnv
            handleError(['[rejected]', 'error: ', 'fatal: '], {
                fatal = true
                message = "Failed to delete $branchName"
            })
        }
    }

    @Override
    void switchBranch(String branchName, boolean createIfNeeded) {
        def params = ['git', 'checkout', branchName]
        if (createIfNeeded) {
            params.add(2, "-B")
        }
        exec(generateGitCommand(params)) {
            env += gitEnv
            handleError(['[rejected]', 'error: ', 'fatal: '], {
                fatal = true
                message = "Couldn't switch to $branchName"
            })
        }
    }

    @Override
    void tag(String annotation, String message) {
        def tagName = annotation.replace(" ", "")
        exec(generateGitCommand(['git', 'tag', '-a', tagName, '-m', '\'' + message + '\''])) {
            env += gitEnv
            handleError(['already exists'], {
                fatal = true
                message = "Duplicate tag [$annotation]"
            })
        }
    }

    @Override
    void merge(String from) {
        exec(generateGitCommand(['git', 'merge', '--no-ff', from])) {
            env += gitEnv
            handleError(['[rejected]', 'error: ', 'fatal: '], {
                fatal = true
                message = "Failed to merge $from"
            })
        }
    }

    @Override
    void push(String branch = "", boolean tags) {
        if (branch == null || branch.isEmpty()) {
            branch = exec(generateGitCommand(['git', 'rev-parse', '--abbrev-ref', "HEAD"])) {
                env += gitEnv
                handleError(['HEAD'], {
                    fatal = true
                    message = "Can't push detached HEAD"
                })
            }.logs.replace("\n", "").replace("\r", "")
        }
        def params = ['git', 'push', '-u', 'origin', branch]
        if (tags) {
            params << '--follow-tags'
        }
        exec(generateGitCommand(params)) {
            env += gitEnv
            handleError(['[rejected] ', 'error: ', 'fatal: '], {
                fatal = true
                message = "Failed to push to remote"
            })
        }
    }

    @Override
    void pushTag(String tagName) {
        if (tagName == null) {
            // If no tag is specified, push all sane tags (means annotated and reachable)
            exec(generateGitCommand(['git', 'push', '--follow-tags'])) {
                env += gitEnv
                handleError(['[rejected] ', 'error: ', 'fatal: '], {
                    fatal = true
                    message = "Failed to push tags to remote"
                })
            }
        } else {
            // If a tag is specified, only push this one
            exec(generateGitCommand(['git', 'push', 'origin', tagName])) {
                env += gitEnv
                handleError(['[rejected] ', 'error: ', 'fatal: '], {
                    fatal = true
                    message = "Failed to push tags to remote"
                })
            }
        }
    }

    @Override
    void pull() {
        exec(generateGitCommand(['git', 'pull'])) {
            env += gitEnv
            handleError(['[rejected] ', 'error: ', 'fatal: '], {
                fatal = true
                message = "Failed to pull from remote"
            })
        }
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

    private Executor.ExecutorResult exec(List<String> cmd, @DelegatesTo(Executor.ExecutorParams) Closure closure = {
    }) {

        def baseParams = {
            directory = project.rootDir
        }
        def finalClosure = baseParams << closure
        return Executor.exec(cmd, finalClosure)
    }
}