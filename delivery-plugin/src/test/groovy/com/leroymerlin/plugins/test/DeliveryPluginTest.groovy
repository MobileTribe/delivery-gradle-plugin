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
                     *
                     * @param name
                     * @param create (default false)
                     */
                    branch 'branchName', false

                    /**
                     * add files to be committed, if files is not set, all files will be added
                     *
                     * @param files
                     */
                    add 'test.txt'

                    /**
                     * commit files, if addAll is true, will add files before commit
                     *
                     * @param message
                     * @param addAll (default false)
                     */
                    commit 'first commit', false

                    /**
                     * tag a commit
                     *
                     * @param message (default "")
                     * @param annotation (default "")
                     */
                    tag 'a message', 'an annotation'

                    /**
                     * merge a branch
                     *
                     * @param branch
                     */
                    merge 'branchName'

                    /**
                     * push changes
                     */
                    push

                    /**
                     * delete a branch
                     *
                     * @param branchName
                     */
                    delete 'branchName'

                    /**
                     * change the properties of the version.properties
                     *
                     * @param version (default null)
                     * @param versionId (default null)
                     * @param projectName (default null)
                     */
                    changeProperties '1.0.0', '3'

                    /**
                     * build the project
                     */
                    build

                    /**
                     * execute a command in the folder
                     *
                     * @param cmd
                     */
                    cmd 'a command'

                    /**
                     * cancel all changes not committed
                     */
                    discardChange
                }
            }
            //...
            //end::flowExample[]
        }
        project.evaluate()
        Asserts.notNull(project.tasks.findByPath('releaseFlow'), "Flow release")
    }
}
