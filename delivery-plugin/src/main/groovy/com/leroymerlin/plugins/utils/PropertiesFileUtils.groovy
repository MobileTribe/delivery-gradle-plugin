package com.leroymerlin.plugins.utils

import com.leroymerlin.plugins.cli.Executor
import org.gradle.api.Project

/**
 * Created by florian on 30/01/2017.
 */
class PropertiesFileUtils extends Executor {

    static void setDefaultProperty(File file, String key, String value) {
        Properties properties = readPropertiesFile(file)
        if (properties.getProperty(key) == null) {
            properties.setProperty(key, value)
            writePropertiesFile(file, properties)
        }
    }

    static void setProperty(File file, String key, String value) {
        Properties properties = readPropertiesFile(file)
        properties.setProperty(key, value)
        writePropertiesFile(file, properties)
    }

    static void writePropertiesFile(File file, Properties properties) {
        if (!file.exists()) {
            file.createNewFile()
        }
        FileOutputStream out = new FileOutputStream(file)
        ByteArrayOutputStream arrayOut = new ByteArrayOutputStream()
        properties.store(arrayOut, null)
        String string = new String(arrayOut.toByteArray(), "8859_1")
        String sep = System.getProperty("line.separator")
        String content = string.substring(string.indexOf(sep) + sep.length())
        out.write(content.getBytes("8859_1"))
        out.close()
    }

    static Properties readAndApplyPropertiesFile(Project project, File file) {
        Properties properties = readPropertiesFile(file)
        applyPropertiesOnProject(project, properties)
        return properties
    }

    static Properties readPropertiesFile(File file) {
        Properties properties = new Properties()
        if (file.exists()) {
            properties.load(new FileInputStream(file))
        }
        return properties
    }

    static void applyPropertiesOnProject(Project project, Properties properties) {
        properties.each { prop ->
            project.ext.set(prop.key, prop.value)
        }
    }
}
