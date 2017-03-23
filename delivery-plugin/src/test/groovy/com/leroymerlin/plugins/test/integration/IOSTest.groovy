package com.leroymerlin.plugins.test.integration

import groovy.io.FileType
import org.junit.Assert
import org.junit.Test

/**
 * Created by alexandre on 17/12/15.
 */
class IOSTest extends AbstractIntegrationTest {

    @Override
    String getProjectName() {
        return "ios"
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
        xcodeBuild{
            build
        }
    }

if(project.file("${System.properties['user.home']}/.gradle/signing_ios.properties").exist(){
    signingProperties {
        release {
            target = "delivery"
            scheme = "delivery"
            propertiesFile = project.file("${System.properties['user.home']}/.gradle/signing_ios.properties")
        }
    }
}
}
''')
        //testTask('xcodeBuildFlow')
        def list = []
        archiveDirectory.eachFileRecurse(FileType.FILES, {
            f ->
                list << f
        })
        Assert.assertEquals("archive folder should contain 4 files", 4, list.size());
    }
}