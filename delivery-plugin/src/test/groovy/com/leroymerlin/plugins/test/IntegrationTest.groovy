package com.leroymerlin.plugins.test

import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass

/**
 * Created by florian on 17/12/15.
 */
abstract class IntegrationTest {

    Project project
    File workingDirectory, projectTemplate

    abstract String getProjectName()

    @BeforeClass
    static void setUpPlugin() {
        GradleConnector connector = GradleConnector.newConnector()
        connector.forProjectDirectory(new File(""))
        ProjectConnection connection = connector.connect()
        try {
            BuildLauncher launcher = connection.newBuild()
            launcher.forTasks(":delivery-plugin:install")
            launcher.run()
        } finally {
            connection.close()
        }
    }

    @Before
    void setUp() {
        projectTemplate = new File(TestUtils.getPluginBaseDir(), "src/test/resources/${getProjectName()}")
        workingDirectory = new File(TestUtils.getPluginBaseDir(), "build/testDir/${getProjectName()}")
        FileUtils.deleteDirectory(workingDirectory)
        FileUtils.copyDirectory(projectTemplate, workingDirectory)
        project = ProjectBuilder.builder().withProjectDir(workingDirectory).build()
    }

    @After
    void tearDown() {
        project = null
        FileUtils.deleteDirectory(workingDirectory)
    }

    protected void testTask(String... tasks) {
        GradleConnector connector = GradleConnector.newConnector()
        connector.forProjectDirectory(workingDirectory)
        ProjectConnection connection = connector.connect()
        Properties props = new Properties()
        props.load(new FileInputStream(new File(TestUtils.getPluginBaseDir(), "../gradle.properties")))
        def versionPlugin = props.getProperty('version')

        try {
            connection.newBuild()
                    .forTasks(tasks)
                    .withArguments("--stacktrace", "-DDELIVERY_VERSION=$versionPlugin")
                    .setStandardOutput(System.out)
                    .run()
        } finally {
            connection.close()
        }
    }
}