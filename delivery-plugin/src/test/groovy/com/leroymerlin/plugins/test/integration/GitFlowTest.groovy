package com.leroymerlin.plugins.test.integration

import com.leroymerlin.plugins.cli.Executor
import org.junit.Assert
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
        testTask()
        println(Executor.exec(["git", "init"], directory: workingDirectory))
        println(Executor.exec(["git", "commit", '-am', '"init commit"'], directory: workingDirectory))
    }

    @Test
    void testCommitInitFile() {
        applyExtraGradle('''
delivery{
    flows{
        git{
            commit 'init commit', true
        }
    }
}

''')
        testTask('gitFlow')
        def gitStatus = Executor.exec(["git", "status"], directory: workingDirectory)
        Assert.assertTrue("Commit init file should be :\n$gitStatus", gitStatus.contains("nothing to commit"))

    }


    @Test
    void testAddFile() {
        applyExtraGradle('''
delivery{
    flows{
        git{
            cmd 'echo', '-ne', '"text de test"', '>>', 'fichier.txt'
            commit 'init commit', true
            cmd 'echo', '-ne', '"Changement du contenu"', '>>', 'fichier.txt'
            commit 'change fichier.txt'
        }
    }
}
''')

        testTask('gitFlow')
        def gitStatus = Executor.exec(["git", "status"], directory: workingDirectory)
        println gitStatus
        Assert.assertTrue("Commit init file should be :\n$gitStatus", gitStatus.contains("nothing to commit"))
    }


}
