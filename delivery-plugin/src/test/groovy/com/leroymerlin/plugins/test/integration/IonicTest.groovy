package com.leroymerlin.plugins.test.integration

import com.leroymerlin.plugins.cli.Executor
import groovy.io.FileType
import org.junit.Assert
import org.junit.Assume
import org.junit.Before
import org.junit.Test

/**
 * Created by alexandre on 27/03/17.
 */
class IonicTest extends AbstractIntegrationTest {

    @Override
    String getProjectName() {
        return "ionic"
    }


    @Before
    void beforeMethod() {
        def exec = Executor.exec(Executor.convertToCommandLine("ionic -v")) {
            needSuccessExitCode = false
        }
        Assume.assumeTrue("ionic is not installed", exec.exitValue == Executor.EXIT_CODE_OK)
    }

    @Test
    void testBuildTaskGeneration() {
        testTask('ionicFlow')
        def list = []
        new File(workingDirectory, "platforms/android/build/archive_ionic").eachFileRecurse(FileType.FILES, {
            f ->
                list << f
        })
        new File(workingDirectory, "platforms/ios/build/archive_ionic").eachFileRecurse(FileType.FILES, {
            f ->
                list << f
        })

        for (file in list) {
            if (!file.name.contains("myapps-1.0.0-SNAPSHOT-debug")
                    && !file.name.contains("myapps-1.0.0-SNAPSHOT-release")
                    && !file.name.contains("myapps-1.0.0-SNAPSHOT-sources-sources")
                    && !file.name.contains("myapps-1.0.0-SNAPSHOT-test-debug")
                    && !file.name.contains("myapps-1.0.0-SNAPSHOT-myapp"))
                throw new AssertionError("${file.name} has not a correct name")
        }

        Assert.assertEquals("archive folder should contain 10 files", 10, list.size())
    }
}
