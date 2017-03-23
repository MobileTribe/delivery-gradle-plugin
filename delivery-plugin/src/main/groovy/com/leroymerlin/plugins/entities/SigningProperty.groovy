package com.leroymerlin.plugins.entities

class SigningProperty {

    final String name

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

    def get(String name) {
        properties.get(name, null)
    }
}
