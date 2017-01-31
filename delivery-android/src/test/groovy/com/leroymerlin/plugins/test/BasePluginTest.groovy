package com.leroymerlin.plugins.test

import com.leroymerlin.plugins.DeliveryPlugin
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Before

/**
 * Created by florian on 17/12/15.
 */
class BasePluginTest {

    Project project

    @Before
    public void setUp() {
        project = ProjectBuilder.builder().withProjectDir(new File("delivery-plugin/src/test/resources/android-app")).build()
        project.buildDir = new File("../../../../build")
    }

    @After
    public void tearDown() {
        project = null;
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
                versionName "1.0.0"
                versionCode 1

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
                minify {
                    minifyEnabled true
                    proguardFiles getDefaultProguardFile('proguard-android.txt')
                    debuggable true
                }
                release {}
            }
        }

    }


}
