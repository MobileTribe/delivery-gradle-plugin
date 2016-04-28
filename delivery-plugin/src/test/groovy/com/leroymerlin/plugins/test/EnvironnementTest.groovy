package com.leroymerlin.plugins.test

import com.leroymerlin.plugins.utils.Utils
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Created by florian on 17/12/15.
 */
class EnvironnementTest extends BasePluginTest {


    @Before
    public void setUp() {
        super.setUp()

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
    public void testCheckProperties() {
        Utils.setPropertyInFile(project.file('delivery.properties'), ['versionfilepath': 'custom.properties'])
        setupProject()
        project.evaluate()

        assert project.version == '1.0.1'

    }

    @Test
    public void testParameterNewVersion() {


        System.setProperty("VERSION", "13.3.8")
        System.setProperty("VERSION_CODE", "1338")

        setupProject()


        project.evaluate()
        project.tasks['updateVersionsFile'].execute()





        Assert.assertEquals("13.3.8", project.version)
        Assert.assertEquals("1338", project.versioncode)


    }


    @Test
    public void testUpdateVersionsFile() {
        setupProject()

        project.evaluate()

        def newVersion = '2.0.0-SNAPSHOT'
        assert newVersion != project.version
        project.version = newVersion;
        Assert.assertNotNull(project.tasks['updateVersionsFile'])
        project.tasks['updateVersionsFile'].execute()

        File f = project.file('version.properties');
        Properties properties = new Properties();
        properties.load(new FileInputStream(f))
        assert properties['version'].equals(project.version)

    }

}
