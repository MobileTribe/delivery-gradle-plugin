package com.leroymerlin.plugins.entities

import com.leroymerlin.plugins.cli.DeliveryLogger

class RegistryProperty {

    final String name
    String url
    String password
    String user
    public final DeliveryLogger deliveryLogger = new DeliveryLogger()

    RegistryProperty(String name) {
        this.name = name
    }

    void setPropertiesFile(File propertiesFile) {
        if (!propertiesFile.exists()) {
            deliveryLogger.logError("Can't load ${propertiesFile.path} in $name registryProperties")
        } else {
            deliveryLogger.logInfo("$name registryProperty loaded from ${propertiesFile.path}")
            Properties fileProp = new Properties()
            propertiesFile.withInputStream {
                stream -> fileProp.load(stream)
            }
            fileProp.each {
                key, value ->
                    this[key] = value
            }
        }
    }
}
