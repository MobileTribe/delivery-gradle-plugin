package com.leroymerlin.plugins.test.integration

import groovy.io.FileType
import org.junit.Assert
import org.junit.Test

/**
 * Created by alexandre on 17/12/15.
 */
class JavaTest extends AbstractIntegrationTest {

    @Override
    String getProjectName() {
        return "java"
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
        Assert.assertEquals("archive folder should contain 4 files", 4, list.size());
    }
}
