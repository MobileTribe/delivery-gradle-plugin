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
        initCommit{
            commit 'init commit', true
        }
        addFile{
            add 'fichier.txt'
            commit 'fichier.txt'
        }
    }
}
''')

        def file = new File(workingDirectory, "fichier.txt")
        file << "init"
        testTask('initCommitFlow')
        file << "salut !"
        def gitStatus = Executor.exec(["git", "status"], directory: workingDirectory)
        println gitStatus
        Assert.assertTrue("fichier.txt should be modified :\n$gitStatus", gitStatus.contains("fichier.txt"))
        testTask('addFileFlow')
        gitStatus = Executor.exec(["git", "status"], directory: workingDirectory)
        Assert.assertTrue("Commit init file should be :\n$gitStatus", gitStatus.contains("nothing to commit"))

    }


}
