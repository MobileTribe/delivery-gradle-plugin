package com.leroymerlin.plugins.tasks

import org.gradle.api.DefaultTask

/**
 * Created by florian on 30/01/2017.
 */
abstract class ProjectTypeTask extends DefaultTask {

    abstract String[] getDependencies();


    ProjectTypeTask(){
        dependsOn("")
    }



}
