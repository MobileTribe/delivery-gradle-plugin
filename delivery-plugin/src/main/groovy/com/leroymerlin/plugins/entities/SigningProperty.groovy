package com.leroymerlin.plugins.entities

class SigningProperty {

    String name

    def properties = [:]

    SigningProperty(String name) {
        this.name = name
    }


    void setPropertiesFile(File propertiesFile) {
        Properties fileProp = new Properties();
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
