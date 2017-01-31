package com.leroymerlin.plugins.test

import com.leroymerlin.plugins.DeliveryPlugin
import com.leroymerlin.plugins.adapters.GitAdapter
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Before
/**
 * Created by florian on 17/12/15.
 */
class BasePluginTest {

    Project project

    private def versionFile = 'version.properties'
    private def tmpVersionFile = versionFile + ".tmp"
    private def deliveryFile = 'delivery.properties'
    private def tmpDeliveryFile = deliveryFile + ".tmp"

    GitAdapter adapter;

    @Before
    public void setUp() {
        project = ProjectBuilder.builder().withProjectDir(new File("delivery-plugin-old/src/test/resources/android-app")).build()
        project.buildDir = new File("../../../../build")
        project.ant.copy(
                file: versionFile,
                tofile: tmpVersionFile
        )
        project.ant.copy(
                file: deliveryFile,
                tofile: tmpDeliveryFile
        )


    }

    @After
    public void tearDown() {
        project.file(".git").deleteDir()
        adapter = null;

        project.file(versionFile).delete();
        project.ant.copy(
                file: tmpVersionFile,
                tofile: versionFile
        )
        project.file(tmpVersionFile).delete()
        project.file(deliveryFile).delete();
        project.ant.copy(
                file: tmpDeliveryFile,
                tofile: deliveryFile
        )
        project.file(tmpDeliveryFile).delete()

        project = null;

        System.clearProperty("VERSION")
        System.clearProperty("VERSION_ID")
    }




    public void setupProject() {
        def manager = project.pluginManager

        project.buildscript {
            repositories {
                jcenter()
                mavenCentral()
                maven { url 'http://dl.bintray.com/content/noamt/gradle-plugins' }
            }
            dependencies {
                //classpath 'com.android.tools.build:gradle:1.5.0'
            }
        }

        project.repositories {
            jcenter()
            mavenCentral()
        }

        manager.apply('com.android.application')
        manager.apply(DeliveryPlugin.class)


        //project.apply from: 'local.properties'
        project.android {
            compileSdkVersion 23
            buildToolsVersion "23.0.1"

            defaultConfig {
                minSdkVersion 15
                targetSdkVersion 23
                //  multiDexEnabled true
                applicationId 'com.leroymerlin.plugin.testapp'
                versionName  project.version
                versionCode  Integer.parseInt(project.versionId)

            }

            productFlavors {
                qualif {}
                preprod {}
                prod {}
            }

            buildTypes {
                debug {
                    minifyEnabled true
                    proguardFiles getDefaultProguardFile('proguard-android.txt')
                }
                minify{
                    minifyEnabled true
                    proguardFiles getDefaultProguardFile('proguard-android.txt')
                    debuggable true
                }
                release {}
            }
        }

        project.file(".git").deleteDir()
        adapter = new GitAdapter(project)
        adapter.exec(['git', 'init'], errorMessage: "Fail to init origin")
        adapter.exec(['git', 'add', '.'])
        adapter.exec(['git', 'commit', '-a', '-m', 'init commit'])
        adapter.exec(['git', 'branch', 'develop'])
        adapter.exec(['git', 'checkout', 'develop'], errorMessage: "Fail to checkout develop")
    }



}
