package com.leroymerlin.plugins.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

class PropertiesTask extends DefaultTask {

    @Input
    @Optional
    String propertiesName
    @Input
    def values

    @Input
    @Optional
    def Map mapVersion

    PropertiesTask() {
        propertiesName = 'version.properties'

    }

    @TaskAction
    void removeProperties() {
        File f = project.file(propertiesName);
        if (!f.exists())
            f.createNewFile()
        Properties properties = new Properties();
        def propStream = new FileInputStream(f.absolutePath);
        properties.load(propStream);
        propStream.close();

        values.each { key, value ->
            if (mapVersion == null || !mapVersion.containsKey(value))
                handleValue(key, value, properties)
            else
                handleValue(key, mapVersion.get(value), properties)

        }
        def outStream = new FileOutputStream(f.absolutePath);
        properties.store(outStream, "");
        outStream.close();
    }

    void handleValue(String key, String value, Properties properties) {
        //Méthode overridée dans l'enfant
    }

}