package com.leroymerlin.plugins.test.integration

import com.leroymerlin.plugins.cli.Executor
import groovy.io.FileType
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Created by alexandre on 04/04/17.
 */
class AndroidDeliveryTest extends AbstractIntegrationTest {

    @Override
    String getProjectName() {
        return "android"
    }

    @Before
    void initGit() {
        testTask()
        println(Executor.exec(["git", "init"], directory: workingDirectory))
    }

    @Test
    void testBuildTaskGeneration() {
        def archiveDirectory = new File(workingDirectory, "build/archive")
        applyExtraGradle('''
delivery {
    archiveRepositories {
        maven {
            url uri("''' + archiveDirectory.absolutePath + '''")
        }
    }
    flows {
        release {
            def releaseVersion = System.getProperty("VERSION", project.version - '-SNAPSHOT')
            def releaseBranch = "release/${project.versionId}-$releaseVersion"
            def matcher = releaseVersion =~ /(\\d+)([^\\d]*$)/
            def newVersion = System.getProperty("NEW_VERSION", matcher.replaceAll("${(matcher[0][1] as int) + 1}${matcher[0][2]}")) - "-SNAPSHOT" + "-SNAPSHOT"
            def baseBranch = System.getProperty("BASE_BRANCH", 'master')
            def workBranch = System.getProperty("BRANCH", 'develop')
            def newVersionId = Integer.parseInt(project.versionId) + 1
            
            branch workBranch
            branch releaseBranch, true
            changeProperties releaseVersion
            add 'version.properties'
            commit "chore (version) : Update version to $releaseVersion"
            build
            tag "$project.projectName-$project.versionId-$releaseVersion"
            if (baseBranch) {
                branch baseBranch
                merge releaseBranch
                push
            }
            branch releaseBranch
            changeProperties newVersion, newVersionId
            add 'version.properties'
            commit "chore (version) : Update to new version $releaseVersion and versionId $newVersionId"
            push
            branch workBranch
            merge releaseBranch
            push
        }
    } 
}
''')
        testTask('releaseFlow')
        def list = []
        archiveDirectory.eachFileRecurse(FileType.FILES, {
            f ->
                list << f
        })
        Assert.assertEquals("archive folder should contain 16 files", 16, list.size());
    }
}
