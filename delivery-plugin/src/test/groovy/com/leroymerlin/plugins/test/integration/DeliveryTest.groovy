package com.leroymerlin.plugins.test.integration

import com.leroymerlin.plugins.cli.Executor
import org.junit.Before
import org.junit.Test

/**
 * Created by alexandre on 04/04/17.
 */
class DeliveryTest extends AbstractIntegrationTest {

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
            def workBranch = System.getProperty("BRANCH", 'develop')
            def newVersionId = Integer.parseInt(project.versionId) + 1
                            
            branch workBranch, true // we create a branch
            add
            commit "feat (android) : first commit"
            changeProperties releaseVersion // we change the version
            add 'version.properties'
            commit "chore (version) : Update version to $releaseVersion"
            build // we build the project
            tag "$project.projectName-$project.versionId-$releaseVersion" // we tag the commit
            branch releaseBranch, true
            changeProperties newVersion, newVersionId
            add 'version.properties'
            commit "chore (version) : Update to new version $releaseVersion and versionId $newVersionId"
            push
            branch workBranch
            merge releaseBranch // we merge the branch
            delete releaseBranch // we delete the branch we just merged
            push // we push the changes
            cmd "touch hello.world" // we execute a command
            discardChange // we undo the changes
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
    }
}
