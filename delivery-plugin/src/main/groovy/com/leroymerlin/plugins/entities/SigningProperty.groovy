package com.leroymerlin.plugins.entities

import java.util.logging.Logger

class SigningProperty {

    final String name

    def properties = [:]

    SigningProperty(String name) {
        this.name = name
    }

    void setPropertiesFile(File propertiesFile) {
        if (!propertiesFile.exists()) {
            Logger.global.warning("Can't load ${propertiesFile.path} in $name signingProperty")
        } else {
            Logger.global.info("$name signingProperty loaded from ${propertiesFile.path}")
            Properties fileProp = new Properties()
            propertiesFile.withInputStream {
                stream -> fileProp.load(stream)
            }
            fileProp.each {
                key, value ->
                    properties.put(key, value)
            }
        }
    }

    @Override
    void setProperty(String property, Object newValue) {
        if (property == "propertiesFile") {
            setPropertiesFile(newValue as File)
        } else {
            properties.put(property, newValue)
        }
    }

    def get(String name) {
        if (properties.containsKey(name)) {
            return properties.get(name)
        }
        return null
    }
}
