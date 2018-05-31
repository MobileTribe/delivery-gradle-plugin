package com.leroymerlin.plugins.entities

import com.leroymerlin.plugins.cli.DeliveryLogger

class SigningProperty {

    final String name
    def properties = [:]
    public final DeliveryLogger deliveryLogger = new DeliveryLogger()

    SigningProperty(String name) {
        this.name = name
    }

    void setPropertiesFile(File propertiesFile) {
        if (!propertiesFile.exists()) {
            deliveryLogger.logError("Can't load ${propertiesFile.path} in $name signingProperty")
        } else {
            deliveryLogger.logInfo("$name signingProperty loaded from ${propertiesFile.path}")
            Properties fileProp = new Properties()
            propertiesFile.withInputStream {
                stream -> fileProp.load(stream)
            }
            fileProp.each {
                key, String value ->
                    //resolve relative path
                    if ("mobileProvisionURI" == key) {
                        value = value.split(",").collect {
                            path ->
                                def file = new File(propertiesFile.parentFile, path)
                                if (file.exists()) {
                                    return file.path
                                }
                                return path
                        }.join(",")
                    } else if ("certificateURI" == key || "storeFile" == key) {
                        def file = new File(propertiesFile.parentFile, value)
                        if (file.exists()) {
                            value = file.path
                        }
                    }
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
