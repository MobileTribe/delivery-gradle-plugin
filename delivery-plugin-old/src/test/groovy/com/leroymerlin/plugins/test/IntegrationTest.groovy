package com.leroymerlin.plugins.test

import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.Before
import org.junit.Test

/**
 * Created by florian on 17/12/15.
 */
class IntegrationTest {

    @Before
    public void setUp() {
        GradleConnector connector = GradleConnector.newConnector()
        connector.forProjectDirectory(new File("delivery-plugin-old"))
        ProjectConnection connection = connector.connect()
        try {
            BuildLauncher launcher = connection.newBuild()
            launcher.forTasks("install")
            launcher.run()
        } finally {
            connection.close()
        }
    }

    /**
     *
     * Testing Methods
     *
     */

    private static void testTask(String... tasks) {
        GradleConnector connector = GradleConnector.newConnector()
        connector.forProjectDirectory(new File("android"))
        ProjectConnection connection = connector.connect()
        try {
            BuildLauncher launcher = connection.newBuild()
            launcher.forTasks(tasks)
            launcher.run()
        } finally {
            connection.close()
        }
    }


    @Test
    public void testInstall() {
        testTask("install")
    }

    @Test
    public void testUpload() {
        testTask("uploadArtifacts")
    }


}
