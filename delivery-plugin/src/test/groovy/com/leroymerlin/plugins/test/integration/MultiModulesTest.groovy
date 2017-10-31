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
''')
        applySecondExtraGradle('''
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
''')
        testTask('buildFlow')
        def list = []
        archiveDirectory.eachFileRecurse(FileType.FILES, {
            f ->
                list << f
        })
        Assert.assertEquals("archive folder should contain 16 files", 16, list.size())
    }

    @Test
    void changePropertiesTest(){
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
''')
        applySecondExtraGradle('''
    apply plugin: \'com.leroymerlin.delivery\'

delivery{
    archiveRepositories = {
        maven {
            url uri("''' + archiveDirectory.absolutePath + '''")
        }
    }
    flows{
        build{
            changeProperties '1.1.1', 111
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
        Assert.assertEquals("archive folder should contain 16 files", 16, list.size())
    }
}
