package com.leroymerlin.plugins.test.integration

import groovy.io.FileType
import org.junit.Assert
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
            if (!file.name.contains("myapp-1.0.0-SNAPSHOT-debug")
                    && !file.name.contains("myapp-1.0.0-SNAPSHOT-release")
                    && !file.name.contains("myapp-1.0.0-SNAPSHOT-sources-sources")
                    && !file.name.contains("myapp-1.0.0-SNAPSHOT-test-debug")
                    && !file.name.contains("myapp-1.0.0-SNAPSHOT-myapp"))
                throw new AssertionError("${file.name} has not a correct name")
        }

        Assert.assertEquals("archive folder should contain 10 files", 10, list.size())
    }
}
