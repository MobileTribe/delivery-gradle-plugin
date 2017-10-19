package com.leroymerlin.plugins.test.integration

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
        def test
    }
}
