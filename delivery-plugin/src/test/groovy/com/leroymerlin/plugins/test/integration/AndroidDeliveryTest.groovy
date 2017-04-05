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
    //we set a local repo for tests
        maven {
            url uri("''' + archiveDirectory.absolutePath + '''")
        }
    }
    flows {
    //we declare a release flow
        release {
            def releaseVersion = System.getProperty("VERSION", project.version - '-SNAPSHOT')
            def releaseBranch = "release/${project.versionId}-$releaseVersion"
            def matcher = releaseVersion =~ /(\\d+)([^\\d]*$)/
            def newVersion = System.getProperty("NEW_VERSION", matcher.replaceAll("${(matcher[0][1] as int) + 1}${matcher[0][2]}")) - "-SNAPSHOT" + "-SNAPSHOT"
            def baseBranch = System.getProperty("BASE_BRANCH", 'master')
            def workBranch = System.getProperty("BRANCH", 'develop')
            def newVersionId = Integer.parseInt(project.versionId) + 1
            
            branch workBranch, true // we create a branch
            add
            commit "feat (android) : first commit"
            branch releaseBranch, true
            changeProperties releaseVersion // we change the version
            add 'version.properties'
            commit "chore (version) : Update version to $releaseVersion"
            build // we build the project
            tag "$project.projectName-$project.versionId-$releaseVersion" // we tag the commit
            if (baseBranch) {
                branch baseBranch, true
                merge releaseBranch
                push // we push the changes
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
    // we declare our properties file to sign the application
    signingProperties {
        release {
            propertiesFile = file("signing.properties")
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
        Assert.assertEquals("archive folder should contain 12 files", 12, list.size());
    }
}
