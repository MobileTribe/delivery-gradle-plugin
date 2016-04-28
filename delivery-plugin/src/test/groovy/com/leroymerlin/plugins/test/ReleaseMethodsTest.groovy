package com.leroymerlin.plugins.test

import com.leroymerlin.plugins.DeliveryPlugin
import com.leroymerlin.plugins.utils.ReleaseMethods
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Created by florian on 17/12/15.
 */
class ReleaseMethodsTest extends BasePluginTest {

    ReleaseMethods releaseMethods

    @Before
    public void setUp() {
        super.setUp()
        setupProject()
        releaseMethods = new ReleaseMethods(project, project.plugins.getPlugin(DeliveryPlugin))
    }

    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    public void testCheckSnapshotDependencies() {
        def returnString = releaseMethods.checkSnapshotDependencies()
        Assert.assertEquals("", returnString)
    }


    @Test
    public void testUnSnapVersion() {
        String version = project.version
        assert version.endsWith('-SNAPSHOT')
        releaseMethods.unSnapVersion()
        Assert.assertEquals(version - '-SNAPSHOT', project.version)
    }



}
