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
        initCommit{
            commit 'init commit', true
            step 'myStep','hello'
        }
    }
}

''')
        def file = new File(workingDirectory, "fichier.txt")
        file << "init"
        testTask('initCommitFlow')
        def gitStatus = getGitStatus()
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
        def gitStatus = getGitStatus()
        Assert.assertTrue("fichier.txt should be modified :\n$gitStatus", gitStatus.contains("fichier.txt"))
        testTask('addFileFlow')
        gitStatus = getGitStatus()
        Assert.assertTrue("Commit init file should be :\n$gitStatus", gitStatus.contains("nothing to commit"))
    }

    @Test
    void testDiscardFile() {


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
        discardChange{
            add 'fichier2.txt'
            discardChange
        }
    }
}
''')

        def file = new File(workingDirectory, "fichier.txt")
        file << "init"
        testTask('initCommitFlow')
        file << "salut !"
        def gitStatus = getGitStatus()
        Assert.assertTrue("fichier.txt should be modified :\n$gitStatus", gitStatus.contains("fichier.txt"))
        testTask('addFileFlow')
        gitStatus = getGitStatus()
        Assert.assertTrue("Commit init file should be :\n$gitStatus", gitStatus.contains("nothing to commit"))
        file << "!!!!!!!"
        def file2 = new File(workingDirectory, "fichier2.txt")
        file2 << "toto"
        gitStatus = getGitStatus()
        Assert.assertTrue("fichier2.txt should be added", gitStatus.contains("fichier2.txt"))
        testTask('discardChangeFlow')
        gitStatus = getGitStatus()
        Assert.assertTrue("discard didn't work", gitStatus.contains("working tree clean"))
    }

    @Test
    void testSwitchBranch() {

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
        switchBranch{
            branch 'branchTest', true
        }
    }
}
''')

        def file = new File(workingDirectory, "fichier.txt")
        file << "init"
        testTask('initCommitFlow')
        file << "salut !"
        def gitStatus = getGitStatus()
        Assert.assertTrue("fichier.txt should be modified :\n$gitStatus", gitStatus.contains("fichier.txt"))
        testTask('addFileFlow')
        gitStatus = getGitStatus()
        Assert.assertTrue("Commit init file should be :\n$gitStatus", gitStatus.contains("nothing to commit"))
        testTask('switchBranchFlow')
        gitStatus = getGitStatus()
        Assert.assertTrue("Branch should be changed", gitStatus.contains("branchTest"))
    }

    @Test
    void testMerge() {

        applyExtraGradle('''
delivery{
    flows{
        commitAll{
            commit 'commit all', true
        }
        switchBranch{
            branch 'branchTest', true
        }
        switchToMaster{
            branch 'master'
        }
        merge{
            merge 'branchTest'
        }
    }
}
''')

        def file = new File(workingDirectory, "fichier.txt")
        file << "init"
        testTask('commitAllFlow')
        testTask('switchBranchFlow')
        file << "salut !"
        testTask('commitAllFlow')
        Assert.assertTrue("File should contain 'salut'", file.text.contains("salut !"));
        testTask('switchToMasterFlow')
        Assert.assertTrue("Should be on master :\n$gitStatus", gitStatus.contains("master"))
        Assert.assertTrue("File should not contain 'salut'", !file.text.contains("salut !"));
        testTask('mergeFlow')
        Assert.assertTrue("File should contain 'salut'", file.text.contains("salut !"));
    }


    @Test
    void testPush() {

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
        switchBranch{
            branch 'branchTest', true
        }
        push{
            push
        }
    }
}
''')

        def file = new File(workingDirectory, "fichier.txt")
        file << "init"
        testTask('initCommitFlow')
        file << "salut !"
        def gitStatus = getGitStatus()
        Assert.assertTrue("fichier.txt should be modified :\n$gitStatus", gitStatus.contains("fichier.txt"))
        testTask('addFileFlow')
        gitStatus = getGitStatus()
        Assert.assertTrue("Commit init file should be :\n$gitStatus", gitStatus.contains("nothing to commit"))
        testTask('switchBranchFlow')
        gitStatus = getGitStatus()
        Assert.assertTrue("Didn't switch", gitStatus.contains("On branch branchTest"))
        testTask('pushFlow')
        gitStatus = getGitStatus()
        Assert.assertTrue("Could not push :\n$gitStatus", gitStatus.contains("nothing to commit"))
    }


    def getGitStatus() {
        return Executor.exec(["git", "status"], directory: workingDirectory)
    }
}
