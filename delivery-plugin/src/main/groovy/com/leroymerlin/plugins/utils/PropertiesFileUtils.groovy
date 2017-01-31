package com.leroymerlin.plugins.utils

import org.gradle.api.Project

/**
 * Created by florian on 30/01/2017.
 */
class PropertiesFileUtils {

    static void setDefaultProperty(File file, String key, String value) {
        Properties properties = readPropertiesFile(file);
        if (!properties.hasProperty(key)) {
            properties.setProperty(key, value)
            writePropertiesFile(file, properties)
        }
    }

    static void writePropertiesFile(File file, Properties properties) {
        if (!file.exists()) {
            file.createNewFile()
        }
        def stream = new FileOutputStream(file)
        properties.store(stream, "")
        stream.close()
    }

    static Properties readAndApplyPropertiesFile(Project project, File file) {
        Properties properties = readPropertiesFile(file)
        applyPropertiesOnProject(project, properties);
        return properties;
    }

    static Properties readPropertiesFile(File file) {
        Properties properties = new Properties();
        if (file.exists()) {
            properties.load(new FileInputStream(file))
        }
        return properties;
    }

    static void applyPropertiesOnProject(Project project, Properties properties) {
        properties.each { prop ->
            project.ext.set(prop.key, prop.value);
        }
    }
}
