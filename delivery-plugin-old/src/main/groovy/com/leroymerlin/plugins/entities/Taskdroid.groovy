package com.leroymerlin.plugins.entities

import org.gradle.api.Project

class Taskdroid {
    String name
    String description
    String group
    def function
    def dependOn

    Taskdroid(String name, String description, String group, def function, def dependOn = []) {
        this.name = name
        this.description = description
        this.group = group
        this.function = function
        this.dependOn = dependOn;
    }


    String createTask(Project project, String previousTaskName){
        def dep = [];
        if(previousTaskName)
            dep.add(previousTaskName);
        dep.addAll(dependOn)
        project.task(name, description: description, group: group, dependsOn: dep) << function
        return name;
    }
}
