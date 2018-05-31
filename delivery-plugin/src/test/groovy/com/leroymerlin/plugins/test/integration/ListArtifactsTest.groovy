package com.leroymerlin.plugins.test.integration

import org.junit.Test

/**
 * Created by alexandre on 17/12/15.
 */
class ListArtifactsTest extends AbstractIntegrationTest {

    @Override
    String getProjectName() {
        return "java"
    }

    @Test
    void testMaven() {
        def archiveDirectory = new File(workingDirectory, "build/archive")
        applyExtraGradle('''
delivery{
    archiveRepositories = {
        maven {
            url uri("''' + archiveDirectory.absolutePath.replace('\\', "/") + '''")
        }
    }
}
''')
        testTask('listArtifacts')
    }

    @Test
    void testMavenDeployer() {
        applyExtraGradle('''
delivery{
    archiveRepositories = {
        mavenDeployer {
                repository(url: "http://forge-xnet.fr.corp.leroymerlin.com/nexus/content/repositories/releases") {
                }
                snapshotRepository(url: "http://forge-xnet.fr.corp.leroymerlin.com/nexus/content/repositories/snapshots") {
                }
            }
    }
}
''')
        testTask('listArtifacts')
    }
}
