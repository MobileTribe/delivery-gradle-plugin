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

    @Before
    void setupIonic() {
        "chmod -R a+rwx ${workingDirectory.absolutePath}".execute()
    }

    @Test
    void testBuildTaskGeneration() {
        def archiveDirectory = new File(workingDirectory, "build/archive_ionic")
        testTask('ionicFlow')
        def list = []
        archiveDirectory.eachFileRecurse(FileType.FILES, {
            f ->
                list << f
        })

        for (file in list) {
            if (!file.path.contains("$archiveDirectory/com/leroymerlin/delivery/ionic/android/1.0.0-SNAPSHOT/myapp-1.0.0-SNAPSHOT-debug")
                    && !file.path.contains("$archiveDirectory/com/leroymerlin/delivery/ionic/android/1.0.0-SNAPSHOT/myapp-1.0.0-SNAPSHOT-release")
                    && !file.path.contains("$archiveDirectory/com/leroymerlin/delivery/ionic/android/1.0.0-SNAPSHOT/myapp-1.0.0-SNAPSHOT-sources-sources")
                    && !file.path.contains("$archiveDirectory/com/leroymerlin/delivery/ionic/android/1.0.0-SNAPSHOT/myapp-1.0.0-SNAPSHOT-test-debug")
                    && !file.path.contains("$archiveDirectory/com/leroymerlin/delivery/ionic/ios/1.0.0-SNAPSHOT/myapp-1.0.0-SNAPSHOT-myapp"))
                throw new AssertionError("${file.name} has not a correct name or a correct path")
        }

        Assert.assertEquals("archive folder should contain 10 files", 10, list.size())
    }
}
