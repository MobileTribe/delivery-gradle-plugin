package com.leroymerlin.plugins.test.integration

import com.leroymerlin.plugins.cli.Executor
import org.junit.Before
import org.junit.Test

/**
 * Created by florian on 17/12/15.
 */
class GitFlowTest extends AbstractIntegrationTest {

    @Override
    String getProjectName() {
        return "unknow"
    }

    @Before
    void initGit() {
        println(Executor.exec(["git", "init"], directory: workingDirectory))
    }

    @Test
    void testBuildTaskGeneration() {
        applyExtraGradle('''

println "yes !!!!!!! c'est lundi"

delivery{
    flows{
        gitFlow{
            commitFiles 'test commit'
        }
    }
}

''')
        testTask('gitFlow')
    }
}
