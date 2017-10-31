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
        Executor.exec(['git', 'init'], [directory: project.rootDir, errorMessage: "Failed to init git", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']])
        Executor.exec(['git', 'remote', 'add', 'origin', '.'], [directory: project.rootDir, errorMessage: "Failed to add origin", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']])
        Executor.exec(['git', 'add', '.'], [directory: project.rootDir, errorMessage: "Failed to add", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']])
        Executor.exec(['git', 'commit', '-am', 'first commit'], [directory: project.rootDir, errorMessage: "Failed to commit", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']])
        Executor.exec(['git', 'push', '--set-upstream', 'origin', 'master'], [directory: project.rootDir, errorMessage: "Failed to push", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']])
        Executor.exec(['git', 'checkout', '-B', 'develop'], [directory: project.rootDir, errorMessage: "Failed to create branch", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']])
        Executor.exec(['git', 'add', '.'], [directory: project.rootDir, errorMessage: "Failed to add files", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']])
        Executor.exec(['git', 'commit', '-am', 'first commit'], [directory: project.rootDir, errorMessage: "Failed to commit files", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']])
        Executor.exec(['git', 'push', '--set-upstream', 'origin', 'develop'], [directory: project.rootDir, errorMessage: "Failed to push", errorPatterns: ['[rejected]', 'error: ', 'fatal: ']])
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
}
