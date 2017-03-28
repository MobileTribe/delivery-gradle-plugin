package com.leroymerlin.plugins.test.integration

import groovy.io.FileType
import org.junit.Assert
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

        def archiveDirectory = new File(workingDirectory, "platforms/android/build/archive_ipa")


        applyExtraGradle('''

delivery{
    archiveRepositories = {
        maven {
            url uri("''' + archiveDirectory.absolutePath + '''")
        }
    }
    flows{
        ionic{
            build
        }
    }
}
''')

        testTask('ionicFlow')
        def list = []
        archiveDirectory.eachFileRecurse(FileType.FILES, {
            f ->
                list << f
        })
        Assert.assertEquals("archive folder should contain 16 files", 16, list.size())
    }
}
