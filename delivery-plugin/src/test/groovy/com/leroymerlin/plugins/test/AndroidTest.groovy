package com.leroymerlin.plugins.test

import org.junit.Test

/**
 * Created by florian on 17/12/15.
 */
class AndroidTest extends IntegrationTest {

    @Override
    String getProjectName() {
        return "android"
    }

    @Test
    void testBuildTaskGeneration() {
        testTask('initCustomStartFlow')
    }
}
