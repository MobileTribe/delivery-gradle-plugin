package com.leroymerlin.plugins.test.integration

import groovy.io.FileType
import org.junit.Assert
import org.junit.Test

/**
 * Created by florian on 17/12/15.
 */
class AndroidTest extends AbstractIntegrationTest {

    @Override
    String getProjectName() {
        return "android"
    }

    @Test
    void testBuildTaskGeneration() {
        def archiveDirectory = new File(workingDirectory, "build/archive")
        applyExtraGradle('''
delivery{
    archiveRepositories = {
        maven {
            url uri("''' + archiveDirectory.absolutePath + '''")
        }
    }
    flows{
        build{
            build
        }
    }
}
''')
        testTask('buildFlow')
        def list = []
        archiveDirectory.eachFileRecurse(FileType.FILES, {
            f ->
                list << f
        })
        Assert.assertEquals("archive folder should contain 8 files", 8, list.size());
    }
}
