package com.leroymerlin.plugins.test.integration

import groovy.io.FileType
import org.junit.Assert
import org.junit.Test

/**
 * Created by alexandre on 17/07/17.
 */
class ReactTest extends AbstractIntegrationTest {

    @Override
    String getProjectName() {
        return "reactNative"
    }

    @Test
    void testBuildTaskGeneration() {
        def archiveDirectory = new File(workingDirectory, "build/archive_react")
        testTask('reactFlow')
        def list = []
        archiveDirectory.eachFileRecurse(FileType.FILES, {
            f ->
                list << f
        })

        Assert.assertEquals("archive folder should contain 10 files", 10, list.size())
    }
}
