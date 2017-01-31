package com.leroymerlin.plugins.internal

class SigningProperty {

    final String name
    File propertiesFile
    String storeFileField = "storeFile";
    String storePasswordField = "storePassword";
    String keyAliasField = "keyAlias";
    String keyAliasPasswordField = "keyAliasPassword";


    SigningProperty(String name) {
        this.name = name
    }
}
