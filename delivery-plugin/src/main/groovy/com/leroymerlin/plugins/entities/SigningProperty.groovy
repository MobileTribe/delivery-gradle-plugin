package com.leroymerlin.plugins.entities

import com.leroymerlin.plugins.cli.Executor

class SigningProperty {

    final String name

    def properties = [:]

    SigningProperty(String name) {
        this.name = name
    }

    void setPropertiesFile(File propertiesFile) {
        if (!propertiesFile.exists()) {
            Executor.logger?.warn("Can't load ${propertiesFile.path} in $name signingProperty")
        } else {
            Executor.logger?.warn("$name signingProperty loaded from ${propertiesFile.path}")
            Properties fileProp = new Properties()
            propertiesFile.withInputStream {
                stream -> fileProp.load(stream)
            }
            fileProp.each {
                key, value ->
                    if (!properties.containsKey(key)) {
                        properties.put(key, value)
                    }
            }
        }
    }

    @Override
    void setProperty(String property, Object newValue) {
        properties.put(property, newValue)
    }

    def get(String name) {
        if (properties.containsKey(name)) {
            return properties.get(name)
        }
        return null
    }
}
