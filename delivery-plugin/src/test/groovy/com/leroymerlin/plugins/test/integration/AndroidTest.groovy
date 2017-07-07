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

        for (file in list) {
            if (!file.path.contains("$archiveDirectory/com/leroymerlin/pandroid/plugin/testapp/android/2.0.0-SNAPSHOT/android-app-dev-2.0.0-SNAPSHOT-debug")
                    && !file.path.contains("$archiveDirectory/com/leroymerlin/pandroid/plugin/testapp/android/2.0.0-SNAPSHOT/android-app-dev-2.0.0-SNAPSHOT-mapping-debug")
                    && !file.path.contains("$archiveDirectory/com/leroymerlin/pandroid/plugin/testapp/android/2.0.0-SNAPSHOT/android-app-dev-2.0.0-SNAPSHOT-sources-sources")
                    && !file.path.contains("$archiveDirectory/com/leroymerlin/pandroid/plugin/testapp/android/2.0.0-SNAPSHOT/android-app-dev-2.0.0-SNAPSHOT-test-debug")
                    && !file.path.contains("$archiveDirectory/com/leroymerlin/pandroid/plugin/testapp/android/2.0.0-SNAPSHOT/android-app-prod-2.0.0-SNAPSHOT-debug")
                    && !file.path.contains("$archiveDirectory/com/leroymerlin/pandroid/plugin/testapp/android/2.0.0-SNAPSHOT/android-app-prod-2.0.0-SNAPSHOT-mapping-debug")
                    && !file.path.contains("$archiveDirectory/com/leroymerlin/pandroid/plugin/testapp/android/2.0.0-SNAPSHOT/android-app-prod-2.0.0-SNAPSHOT-sources-sources")
                    && !file.path.contains("$archiveDirectory/com/leroymerlin/pandroid/plugin/testapp/android/2.0.0-SNAPSHOT/android-app-prod-2.0.0-SNAPSHOT-test-debug"))
                throw new AssertionError("${file.name} has not a correct name or a correct path")
        }

        Assert.assertEquals("archive folder should contain 16 files", 16, list.size())
    }

    @Test
    void testSignTask() {
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
    signingProperties {
        all {
            propertiesFile = file("signing.properties")
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

        for (file in list) {
            if (!file.path.contains("$archiveDirectory/com/leroymerlin/pandroid/plugin/testapp/android/2.0.0-SNAPSHOT/android-app-2.0.0-SNAPSHOT-debug")
                    && !file.path.contains("$archiveDirectory/com/leroymerlin/pandroid/plugin/testapp/android/2.0.0-SNAPSHOT/android-app-2.0.0-SNAPSHOT-mapping-debug")
                    && !file.path.contains("$archiveDirectory/com/leroymerlin/pandroid/plugin/testapp/android/2.0.0-SNAPSHOT/android-app-2.0.0-SNAPSHOT-mapping-release")
                    && !file.path.contains("$archiveDirectory/com/leroymerlin/pandroid/plugin/testapp/android/2.0.0-SNAPSHOT/android-app-2.0.0-SNAPSHOT-release")
                    && !file.path.contains("$archiveDirectory/com/leroymerlin/pandroid/plugin/testapp/android/2.0.0-SNAPSHOT/android-app-2.0.0-SNAPSHOT-sources-sources")
                    && !file.path.contains("$archiveDirectory/com/leroymerlin/pandroid/plugin/testapp/android/2.0.0-SNAPSHOT/android-app-2.0.0-SNAPSHOT-test-debug"))
                throw new AssertionError("${file.name} has not a correct name or a correct path")
        }

        Assert.assertEquals("archive folder should contain 12 files", 12, list.size())
    }
}
