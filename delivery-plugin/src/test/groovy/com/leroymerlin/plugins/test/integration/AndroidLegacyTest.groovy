package com.leroymerlin.plugins.test.integration

import groovy.io.FileType
import org.junit.Assert
import org.junit.Test

/**
 * Created by florian on 17/12/15.
 */
class AndroidLegacyTest extends AbstractIntegrationTest {

    @Override
    String getProjectName() {
        return "androidLegacy"
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
            if (!file.path.contains("$archiveDirectory/com/leroymerlin/pandroid/plugin/testapp/androidLegacy/1.0.0-SNAPSHOT/android-app-dev-1.0.0-SNAPSHOT-debug")
                    && !file.path.contains("$archiveDirectory/com/leroymerlin/pandroid/plugin/testapp/androidLegacy/1.0.0-SNAPSHOT/android-app-dev-1.0.0-SNAPSHOT-mapping-debug")
                    && !file.path.contains("$archiveDirectory/com/leroymerlin/pandroid/plugin/testapp/androidLegacy/1.0.0-SNAPSHOT/android-app-dev-1.0.0-SNAPSHOT-sources-sources")
                    && !file.path.contains("$archiveDirectory/com/leroymerlin/pandroid/plugin/testapp/androidLegacy/1.0.0-SNAPSHOT/android-app-dev-1.0.0-SNAPSHOT-test-debug")
                    && !file.path.contains("$archiveDirectory/com/leroymerlin/pandroid/plugin/testapp/androidLegacy/1.0.0-SNAPSHOT/android-app-prod-1.0.0-SNAPSHOT-debug")
                    && !file.path.contains("$archiveDirectory/com/leroymerlin/pandroid/plugin/testapp/androidLegacy/1.0.0-SNAPSHOT/android-app-prod-1.0.0-SNAPSHOT-mapping-debug")
                    && !file.path.contains("$archiveDirectory/com/leroymerlin/pandroid/plugin/testapp/androidLegacy/1.0.0-SNAPSHOT/android-app-prod-1.0.0-SNAPSHOT-sources-sources")
                    && !file.path.contains("$archiveDirectory/com/leroymerlin/pandroid/plugin/testapp/androidLegacy/1.0.0-SNAPSHOT/android-app-prod-1.0.0-SNAPSHOT-test-debug"))
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
                propertiesFile = file("${System.properties['user.home']}/.gradle/signing.properties")
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
            if (!file.path.contains("$archiveDirectory/com/leroymerlin/pandroid/plugin/testapp/androidLegacy/1.0.0-SNAPSHOT/android-app-1.0.0-SNAPSHOT-debug")
                    && !file.path.contains("$archiveDirectory/com/leroymerlin/pandroid/plugin/testapp/androidLegacy/1.0.0-SNAPSHOT/android-app-1.0.0-SNAPSHOT-mapping-debug")
                    && !file.path.contains("$archiveDirectory/com/leroymerlin/pandroid/plugin/testapp/androidLegacy/1.0.0-SNAPSHOT/android-app-1.0.0-SNAPSHOT-mapping-release")
                    && !file.path.contains("$archiveDirectory/com/leroymerlin/pandroid/plugin/testapp/androidLegacy/1.0.0-SNAPSHOT/android-app-1.0.0-SNAPSHOT-release")
                    && !file.path.contains("$archiveDirectory/com/leroymerlin/pandroid/plugin/testapp/androidLegacy/1.0.0-SNAPSHOT/android-app-1.0.0-SNAPSHOT-sources-sources")
                    && !file.path.contains("$archiveDirectory/com/leroymerlin/pandroid/plugin/testapp/androidLegacy/1.0.0-SNAPSHOT/android-app-1.0.0-SNAPSHOT-test-debug"))
                throw new AssertionError("${file.name} has not a correct name or a correct path")
        }

        Assert.assertEquals("archive folder should contain 12 files", 12, list.size())
    }
}
