package com.leroymerlin.plugins.test.integration

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
        testTask('initReleaseFlow')
    }
}
