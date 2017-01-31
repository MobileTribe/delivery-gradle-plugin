package com.leroymerlin.plugins.test

import com.leroymerlin.plugins.AndroidConfigurator
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Created by florian on 17/12/15.
 */
class AndroidConfiguratorTest extends BasePluginTest {

    @Before
    void setUp() {
        super.setUp()
        setupProject()

        GradleConnector connector = GradleConnector.newConnector()
        connector.forProjectDirectory(new File(""))
        ProjectConnection connection = connector.connect()
        try {
            BuildLauncher launcher = connection.newBuild()
            launcher.forTasks("install")
            launcher.run()
        } finally {
            connection.close()
        }

        project.delivery {
            configurator = AndroidConfigurator.class
        }
    }

    @After
    void tearDown() {
        super.tearDown()
    }

    /**
     *
     * Testing Methods
     *
     */

    private static void testTask(String... tasks) {
        GradleConnector connector = GradleConnector.newConnector()
        connector.forProjectDirectory(new File("delivery-test"))
        ProjectConnection connection = connector.connect()
        try {
            BuildLauncher launcher = connection.newBuild()
            launcher.forTasks(tasks)
            launcher.setStandardOutput(System.out)
            launcher.run()
        } finally {
            connection.close()
        }
    }

    @Test
    void testBuildTaskGeneration() {
        project.evaluate()
        testTask("initReleaseBranch")

        /*project.delivery {
            git {
                requireBranch = 'banane'
            }
        }

        project.evaluate()

        Assert.assertEquals("banane", project.delivery.gitConfig.requireBranch)
        Assert.assertEquals("origin", project.delivery.gitConfig.pushToRemote)*/
    }
}
