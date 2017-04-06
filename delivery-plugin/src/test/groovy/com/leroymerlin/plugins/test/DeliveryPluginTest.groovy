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


    @Test
    void testFlowMethods() {
        project.delivery {
            //tag::flowExample[]
            //...
            flows {
                release { //will generate releaseFlow task
                    /**
                     * change and create scm branch
                     * @param name
                     * @param @optional create (default: false)
                     */
                    branch 'release', false
                    /**
                     * add files to be committed
                     * if files is not set, all files will be added
                     * @param files
                     */
                    add 'test.txt'
                    /**
                     * commit files
                     * if addAll is true, will add all files before commit
                     * @param message
                     * @param @optional addAll (default: false)
                     */
                    commit 'first commit', false
                    /**
                     * tag a commit
                     * @param @optional message (default: "")
                     * @param @optional annotation (default: "")
                     */
                    tag 'a message', 'an annotation'
                    /**
                     * merge a branch
                     * @param branch
                     */
                    merge 'develop'
                    /**
                     * push changes
                     */
                    push
                    /**
                     * delete a branch
                     * @param branchName
                     */
                    delete 'develop'
                    /**
                     * change the properties of the version.properties
                     * @param @optional version (default: null)
                     * @param @optional versionId (default: null)
                     * @param @optional projectName (default: null)
                     */
                    changeProperties '1.0.0', '3'
                    /**
                     * build the project
                     */
                    build
                    /**
                     * execute a command in the folder
                     * @param cmd
                     */
                    cmd 'a command'
                    /**
                     * cancel all changes not committed
                     */
                    discardChange
                    /**
                     * call a task by its name
                     * if newBuild is true, executes a Gradle build
                     * @param taskName
                     * @param @optional newBuild (default: false)
                     */
                    task 'customTask'
                }
            }
            //...
            //end::flowExample[]
        }
        project.evaluate()
        Asserts.notNull(project.tasks.findByPath('releaseFlow'), "Flow release")
    }

    void testSigningMethods() {
        project.delivery {
            //tag::signingAndroidExample[]
            //...
            signingProperties {
                // the name you set here will be the variant name
                variantName {
                    propertiesFile = file('path/to/my/signing.properties')
                    storeFile='path/to/my/store.jks'
                    storePassword='myStorePassword'
                    keyAlias='myAlias'
                    keyPassword='myPass'
                }
            }
            //...
            //end::signingAndroidExample[]
            //tag::signingiOSExample[]
            //...
            signingProperties {
                // you can set the name you want for iOS
                nameYouWant {
                    /**
                     * For iOS you need to set a target and a scheme
                     */
                    target = "delivery"
                    scheme = "delivery"
                    propertiesFile = file("path/to/my/signing_ios.properties")
                    certificateURI='path/to/my/certificat.p12'
                    certificatePassword='myPass'
                    mobileProvisionURI='path/to/my/Provisioning_Profile.mobileprovision'
                }
            }
            //...
            //end::signingiOSExample[]
            //tag::signingIonicExample[]
            //...
            signingProperties {
                // you have to use android or ios name for ionic signing configs
                android {
                    propertiesFile = file("path/to/my/signing.properties")
                }
                ios {
                    propertiesFile = file("path/to/my/signing_ios.properties")
                }
            }
            //...
            //end::signingIonicExample[]
        }
        project.evaluate()
    }
}
