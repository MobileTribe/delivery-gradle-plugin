package com.leroymerlin.plugins.test.integration

import com.leroymerlin.plugins.cli.Executor
import org.junit.Test

/**
 * Created by alexandre on 17/12/15.
 */
class ReleaseGitFlowTest extends AbstractIntegrationTest {

    @Override
    String getProjectName() {
        return "flowProject"
    }

    @Test
    void releaseGitFLowTest() {
        def archiveDirectory = new File(workingDirectory, "build/archive")
        exec(['git', 'init'], "Failed to init git")
        exec(['git', 'remote', 'add', 'origin', '.'], "Failed to add origin")
        exec(['git', 'add', '.'], "Failed to add")
        exec(['git', 'commit', '-am', 'first commit'], "Failed to commit")
        exec(['git', 'push', '--set-upstream', 'origin', 'master'], "Failed to push")
        exec(['git', 'checkout', '-B', 'develop'], "Failed to create branch")
        exec(['git', 'add', '.'], "Failed to add files")
        exec(['git', 'commit', '-am', 'first commit'], "Failed to commit files")
        exec(['git', 'push', '--set-upstream', 'origin', 'develop'], "Failed to push")
        applyExtraGradle('''
delivery{

    enableReleaseGitFlow = true
    
    archiveRepositories = {
        maven {
            url uri("''' + archiveDirectory.absolutePath + '''")
        }
    }
}
''')
        testTask('releaseGitFlow')
    }

    private void exec(List<String> cmd, String errorMessage) {
        Executor.exec(cmd) {
            directory = project.rootDir
            needSuccessExitCode = false
            handleError(['[rejected]', 'error: ', 'fatal: '], {
                fatal = true
                message = errorMessage
            })
        }
    }
}
