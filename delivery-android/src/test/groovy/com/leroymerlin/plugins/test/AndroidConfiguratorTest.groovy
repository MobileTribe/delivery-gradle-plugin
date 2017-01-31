package com.leroymerlin.plugins.test

import com.leroymerlin.plugins.AndroidConfigurator
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Created by florian on 17/12/15.
 */
class AndroidConfiguratorTest extends BasePluginTest {

    @Before
    public void setUp() {
        super.setUp()
        setupProject()

        project.delivery {
            configurator = AndroidConfigurator.class
        }
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    /**
     *
     * Testing Methods
     *
     */


    @Test
    public void testBuildTaskGeneration() {
        project.evaluate()


        /*project.delivery {
            git {
                requireBranch = 'banane'
            }
        }

        project.evaluate()

        Assert.assertEquals("banane", project.delivery.gitConfig.requireBranch)
        Assert.assertEquals("origin", project.delivery.gitConfig.pushToRemote)*/
    }


}
