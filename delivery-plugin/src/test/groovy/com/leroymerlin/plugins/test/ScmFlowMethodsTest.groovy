package com.leroymerlin.plugins.test

import org.junit.After
import org.junit.Before
import org.junit.Test
/**
 * Created by florian on 17/12/15.
 */
class ScmFlowMethodsTest extends BasePluginTest {


    @Before
    public void setUp() {
        super.setUp()
        setupProject()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    public void testGitIsAvailable() {
        assert adapter.isSupported(project.projectDir)
    }

    /*
      @Test
      public void testCreateReleaseBranch() {
          project.evaluate()
          project.tasks['prepareScmAdapter'].execute()
          project.tasks['createReleaseBranch'].execute()
          assert adapter.exec(['git', 'status']).contains(project.version)
          project.tasks['createReleaseBranch'].execute()//should stay on branch
          assert adapter.exec(['git', 'status']).contains(project.version)
      }

    @Test
      public void testValidateDelivery() {
          project.evaluate()
          project.tasks['prepareScmAdapter'].execute()
          project.tasks['createReleaseBranch'].execute()
          project.tasks['mergeOnDevelop'].execute()
          assert adapter.exec(['git', 'status']).contains('develop')
      }*/


}
