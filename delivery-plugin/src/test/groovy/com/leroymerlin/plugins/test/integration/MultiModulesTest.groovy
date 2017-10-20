package com.leroymerlin.plugins.test.integration

import groovy.io.FileType
import org.junit.Assert
import org.junit.Test

class MultiModulesTest extends AbstractIntegrationTest {

    @Override
    String getProjectName() {
        return "multiModules"
    }

    @Test
    void testBuildTaskGeneration() {
        def archiveDirectory = new File(workingDirectory, "build/archive")
        applyExtraGradle('''
    apply plugin: \'com.leroymerlin.delivery\'

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
android{
    productFlavors {
        dev {}
        prod {}
    }
}
''')
        testTask('buildFlow')
        def list = []
        archiveDirectory.eachFileRecurse(FileType.FILES, {
            f ->
                list << f
        })
        Assert.assertEquals("archive folder should contain 24 files", 24, list.size())
    }
}
