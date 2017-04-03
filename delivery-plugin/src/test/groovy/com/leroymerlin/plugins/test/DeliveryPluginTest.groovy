package com.leroymerlin.plugins.test

import com.leroymerlin.plugins.DeliveryPlugin
import org.gradle.api.Project
import org.gradle.internal.impldep.org.apache.http.util.Asserts
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Created by florian on 17/12/15.
 */
class DeliveryPluginTest {

    Project project

    @Before
    void setUp() {
        project = ProjectBuilder.builder().build()
        def manager = project.pluginManager
        manager.apply(DeliveryPlugin.class)
    }

    @After
    void tearDown() {
        project = null
    }


    @Test
    void testDeliveryExtension() {
        Asserts.notNull(project.delivery, "Delivery extension don't exist")
    }

    @Test
    void testBuildTaskGeneration() {
        project.delivery {
            flows {
                azerty {
                }
            }
        }
        project.evaluate()
        Asserts.notNull(project.tasks.findByPath('azertyFlow'), "Flow azerty")
    }
}
