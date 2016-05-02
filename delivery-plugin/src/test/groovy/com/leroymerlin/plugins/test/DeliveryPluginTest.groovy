package com.leroymerlin.plugins.test

import com.leroymerlin.plugins.DeliveryPlugin
import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.utils.ScmFlowMethods
import com.leroymerlin.plugins.utils.Utils
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
/**
 * Created by florian on 17/12/15.
 */
class DeliveryPluginTest extends BasePluginTest {

    @Before
    public void setUp() {
        super.setUp()
        setupProject()
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
    public void testCustomGitConfig() {
        project.delivery {
            git {
                requireBranch = 'banane'
            }
        }

        project.evaluate()

        Assert.assertEquals("banane", project.delivery.gitConfig.requireBranch)
        Assert.assertEquals("origin", project.delivery.gitConfig.pushToRemote)
    }






    @Test
    public void testSaveLatestReleaseInfos() {
        File file = new File(project.projectDir.path+"/delivery.properties")
        file.delete()

        project.version = "1.0.0-SNAPSHOT";
        ScmFlowMethods scmFlowMethods = new ScmFlowMethods(project, project.plugins.getPlugin(DeliveryPlugin))
        scmFlowMethods.saveLatestReleaseInfos()

        Assert.assertNotEquals(0, file.length())
    }

    /**
     *
     * Testing Conf
     *
     */
    @Test
    public void testBasicTagName() {
        project.evaluate()
        Assert.assertEquals(project.projectName +"-"+ project.versionId + "-" + project.version, Utils.tagName(this.project, this.project.extensions['delivery'] as DeliveryPluginExtension))
    }


    @Test
    public void testCustomTagName() {
        project.delivery {
            releaseTagPattern = '$projectName(v$version)'
        }

        project.evaluate()

        String string = "${project.name}(v" + project.version + ")"
        String name = Utils.tagName(this.project, this.project.extensions['delivery'] as DeliveryPluginExtension)
        Assert.assertEquals(string, name)
    }

    @Test
    public void testBasicReleaseBranchName() {
        project.evaluate()
        def version = project.version

        String string = "release/$project.versionId-$version"

        String branchName = Utils.releaseBranchName(this.project, this.project.extensions['delivery'] as DeliveryPluginExtension)
        assert string.equals(branchName)
    }


    @Test
    public void testCustomReleaseBranchName() {
        project.delivery {
            releaseBranchPattern = '$projectName(v$version)'
        }

        project.evaluate()
        def version = project.version - '-SNAPSHOT'
        assert "${project.name}(v" + version + ")".equals(Utils.releaseBranchName(this.project, this.project.extensions['delivery'] as DeliveryPluginExtension))
    }

    @Test
    public void testBasicReleaseMessage() {
        project.evaluate()
        def version = project.version
        def message = "chore (version) : Update version to " + version

        Assert.assertEquals(message, Utils.newVersionCommitMessage(this.project, this.project.extensions['delivery'] as DeliveryPluginExtension))
    }


    @Test
    public void testCustomReleaseMessage() {
        project.delivery {
            newVersionCommitPattern = 'new version commit $version $versionId'
        }

        project.evaluate()

        Assert.assertEquals("new version commit " + project.version + " " + project.versionId, Utils.newVersionCommitMessage(this.project, this.project.extensions['delivery'] as DeliveryPluginExtension))
    }


    @Test
    public void testAddExtensionToProject() {
        assert project.delivery instanceof DeliveryPluginExtension
        project.delivery {
            signingProperties {
                all {
                    propertiesFile = project.file("signing.properties")
                }
                release {
                    propertiesFile = project.file("signing.properties")
                    keyAliasField = "keyAliasRelease"
                }
            }
        }
        assert project.android.signingConfigs.releaseSigning.keyAlias == "releaseAlias"
        assert project.android.signingConfigs.debugSigning.keyAlias == "delivery"

        project.evaluate()
    }

    /**
     * Testing Tasks
     */
    @Test
    public void testInitReleaseTask() {
        project.evaluate()
        Assert.assertNotNull(project.tasks['delivery'])

        def arrayOfTasks = this.retrieveArray(project.tasks['delivery'].getDependsOn())
        Assert.assertEquals(3, arrayOfTasks.size())
        Assert.assertTrue(arrayOfTasks.contains('runBuildTasks'))


        project.tasks['delivery'].execute()
    }

    @Test
    public void testUploadApks() {
        project.delivery {
            signingProperties {
                all {
                    propertiesFile = project.file("signing.properties")
                }
                release {
                    propertiesFile = project.file("signing.properties")
                    keyAliasField = "keyAliasRelease"
                }
            }
        }
        project.evaluate()
        Assert.assertNotNull(project.tasks['uploadTestQualifArtifacts'])


        Assert.assertEquals(9, project.configurations.testQualifConfig.allArtifacts.size())

        project.tasks['tasks'].execute()




    }

    @Test
    public void testValidateReleaseTask() {
        project.evaluate()
        Assert.assertNotNull(project.tasks['validateDelivery'])

        def arrayOfTasks = this.retrieveArray(project.tasks['validateDelivery'].getDependsOn())
        Assert.assertEquals(2, arrayOfTasks.size())
        Assert.assertTrue(arrayOfTasks.contains('prepareNextVersion'));

        project.tasks['validateDelivery'].execute()
    }


    @Test
    public void testCheckSnapshotDependenciesTask() {
        project.evaluate()
        Assert.assertNotNull(project.tasks['checkSnapshotDependencies'])
    }


    @Test
    public void testCommitBranch() {
        project.evaluate()
        Assert.assertNotNull(project.tasks['commitBranch'])
    }

    @Test
    public void testCreateReleaseBranchTask() {
        project.evaluate()
        Assert.assertNotNull(project.tasks['createReleaseBranch'])
    }

    @Test
    public void testMergeOnDevelopTask() {
        project.evaluate()
        Assert.assertNotNull(project.tasks['mergeOnDevelop'])
    }


    @Test
    public void testMergeReleaseBranchTask() {
        project.evaluate()
        Assert.assertNotNull(project.tasks['mergeReleaseBranch'])
    }


    @Test
    public void testPrepareNextVersionTask() {
        project.evaluate()
        Assert.assertNotNull(project.tasks['prepareNextVersion'])
    }


    @Test
    public void testUpdateVersionsFileTask() {
        project.evaluate()
        Assert.assertNotNull(project.tasks['updateVersionsFile'])
    }


    @Test
    public void testUpdateVersionsDoingNothingTask() {
        project.evaluate()
        Assert.assertNotNull(project.tasks['updateVersionsFile'])
    }


    ArrayList<String> retrieveArray(def inputArray) {
        def arrayToReturn = new ArrayList<String>()
        inputArray.each { object ->
            if (object.class == ArrayList.class) {
                arrayToReturn = object
            }
        }
        return arrayToReturn;
    }
}
