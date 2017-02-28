package com.leroymerlin.plugins.tasks

import org.gradle.api.tasks.Input

import javax.inject.Inject

class CreatePropertiesTask extends PropertiesTask {
    @Input
    @Optional
    boolean forceUpdate

    @Inject
    public CreatePropertiesTask() {
        super()
        forceUpdate = false;
    }


    @Override
    void handleValue(String key, String value, Properties properties) {
        if (!properties.containsKey(key) || forceUpdate) {
            println "property updated: ${key}=${value}"
            properties.put(key, value);
        }
    }
}