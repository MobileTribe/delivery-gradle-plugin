package com.leroymerlin.plugins.test.integration

import com.leroymerlin.plugins.cli.Executor
import groovy.io.FileType
import org.junit.Assert
import org.junit.Assume
import org.junit.Before
import org.junit.Test

class FlutterTest extends AbstractIntegrationTest {

    @Override
    String getProjectName() {
        return "flutterproject"
    }


    @Before
    void beforeMethod() {
        def exec = Executor.exec(Executor.convertToCommandLine("flutter --version")) {
            needSuccessExitCode = false
        }
        Assume.assumeTrue("flutter is not installed or found", exec.exitValue == Executor.EXIT_CODE_OK)
    }

    @Test
    void testBuildTaskGeneration() {
        def archiveDirectory = new File(workingDirectory, "build/archive_flutter")
        testTask('flutterFlow')
        def list = []
        archiveDirectory.eachFileRecurse(FileType.FILES, {
            f ->
                list << f
        })

        Assert.assertEquals("archive folder should contain 16 files", 16, list.size())
    }
}