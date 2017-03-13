package com.leroymerlin.plugins.test.integration

import com.leroymerlin.plugins.test.TestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass

import java.nio.file.Files

/**
 * Created by florian on 17/12/15.
 */
abstract class AbstractIntegrationTest {

    Project project
    File workingDirectory, projectTemplate

    abstract String getProjectName()

    @BeforeClass
    static void setUpPlugin() {
        ProjectConnection connection = GradleConnector.newConnector()
                .forProjectDirectory(new File(""))
                .connect()
        try {
            connection.newBuild()
                    .forTasks(":delivery-plugin:install")
                    .run()
        } finally {
            connection.close()
        }
    }

    @Before
    void setUp() {
        projectTemplate = new File(TestUtils.getPluginBaseDir(), "src/test/resources/${getProjectName()}")
        workingDirectory = new File(Files.createTempDirectory(getProjectName()) as String)
        FileUtils.deleteDirectory(workingDirectory)
        FileUtils.copyDirectory(projectTemplate, workingDirectory)
        project = ProjectBuilder.builder().withProjectDir(workingDirectory).build()
    }

    @After
    void tearDown() {
        project = null
        FileUtils.deleteDirectory(workingDirectory)
    }

    protected void applyExtraGradle(String string) {
        new File(workingDirectory, "extra.gradle") << string
    }

    protected void testTask(String... tasks) {
        ProjectConnection connection = GradleConnector.newConnector()
                .forProjectDirectory(workingDirectory)
                .connect()
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