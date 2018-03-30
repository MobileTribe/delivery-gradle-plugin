package com.leroymerlin.plugins.test.integration

import groovy.io.FileType
import org.junit.Assert
import org.junit.Test

/**
 * Created by alexandre on 17/07/17.
 */
class FlutterTest extends AbstractIntegrationTest {

    @Override
    String getProjectName() {
        return "flutterproject"
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

        Assert.assertEquals("archive folder should contain 12 files", 12, list.size())
    }
}
