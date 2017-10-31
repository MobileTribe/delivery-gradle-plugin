package com.leroymerlin.plugins.test.integration

import org.junit.Test

/**
 * Created by alexandre on 17/12/15.
 */
class ReleaseGitFlowTest extends AbstractIntegrationTest {

    @Override
    String getProjectName() {
        return "releaseGitFlowProject"
    }

    @Test
    void releaseGitFLowTest() {
        def archiveDirectory = new File(workingDirectory, "build/archive")
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
