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
    void setupIonic(){
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
        Assert.assertEquals("archive folder should contain 2 files", 2, list.size())
    }
}
