package com.leroymerlin.plugins.test

import com.leroymerlin.plugins.DeliveryPlugin
import com.leroymerlin.plugins.tasks.build.DeliveryBuild
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
        project.file("version.properties").write("version=1.0.0-SNAPSHOT\n" +
                "artifact=test-app\n" +
                "versionId=2")
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
                     * @param @optional message
                     * @param @optional annotation
                     */
                    tag 'a message', 'an annotation'
                    /**
                     * merge a branch
                     * @param branch
                     */
                    merge 'develop'
                    /**
                     * pull changes from origin
                     */
                    pull
                    /**
                     * push changes to branch
                     * if branch is not set, push changes to actual branch
                     * if tags is true push all tags to remote
                     * @param @optional branch (default: current branch)
                     * @param @optional tags (default: true)
                     */
                    push
                    /**
                     * push tag
                     * if tagName is not set, push sane tags
                     * @param @optional tagName (default = all commit)
                     */
                    pushTag
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
                    /**
                     * display a title in log
                     * @param stepName
                     * @param @optional title (default: stepName)
                     * @param @optional taskName (default: flowName+Step+stepName)
                     */
                    step 'name', 'hello world'
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
            //tag::signingFileExample[]
            //...
            signingProperties {
                all {
                    // load file properties into all the signing property
                    propertiesFile = file("path/to/my/signing.properties")
                }
                specificBuildType {
                    // override singing properties for specific buildtype
                    propertiesFile = file("path/to/my/specific.properties")
                }
            }
            //...
            //end::signingFileExample[]
            //tag::signingAndroidExample[]
            //...
            signingProperties {
                // the name you set here has to match a buildtype
                buildTypeName {
                    storeFile = 'path/to/my/store.jks'
                    storePassword = 'myStorePassword'
                    keyAlias = 'myAlias'
                    keyPassword = 'myPass'
                }
            }
            //...
            //end::signingAndroidExample[]
            //tag::signingiOSExample[]
            //...
            signingProperties {
                // you can set the name you want
                // for each signingProperty, you'll archive an IPA
                nameYouWant {
                    target = "delivery"
                    scheme = "delivery"
                    certificateURI = 'path/to/my/certificat.p12'
                    certificatePassword = 'myPass'
                    // you can specify multiple files separated by a comma
                    mobileProvisionURI = 'path/to/my/Provisioning_Profile.mobileprovision,path/to/my/Provisioning_Profile2.mobileprovision'
                }
            }
            //...
            //end::signingiOSExample[]
            //tag::signingIonicExample[]
            //...
            signingProperties {
                // you have to use android or ios name for ionic signing configs
                android {
                    //See android needed properties
                }
                ios {
                    //See ios needed properties
                    //target and scheme are optionals for ionic
                }
            }
            //...
            //end::signingIonicExample[]
            //tag::signingReactExample[]
            //...
            signingProperties {
                // you have to use android or ios name for react signing configs
                android {
                    //See android needed properties
                }
                ios {
                    //See ios needed properties
                    //target and scheme are optionals for ionic
                }
            }
            //...
            //end::signingReactExample[]
            //tag::deliveryBuildExample[]
            //...
            task buildVariant(type: DeliveryBuild) {
                variantName = 'variant'
                outputFiles = ["release": file('build/variant.txt')]
            } << {
                cmd('java -jar delivery-test.jar variant')
            }
            //...
            //end::deliveryBuildExample[]
            //tag::configuratorExample[]
            //...
            delivery {
                configurator = [
                        /**
                         * Configure your project
                         */
                        configure           : {/*...*/ },
                        /**
                         * Apply some properties to your project
                         */
                        applyProperties     : {/*...*/ },
                        /**
                         * Apply the signing properties to your project
                         * @param property
                         */
                        applySigningProperty: { property -> /*...*/ }
                ]
            }
            //...
            //end::configuratorExample[]
        }
        project.evaluate()
    }
}
