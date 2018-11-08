package com.leroymerlin.plugins.utils

import com.leroymerlin.plugins.DeliveryPlugin
import org.gradle.api.Project

/**
 * Created by florian on 30/01/2017.
 */
class PropertiesUtils {

    static String getSystemProperty(String key, String defaultValue = null) {
        def property = System.getProperty(key)
        if (property == null || property.isEmpty()) {
            return defaultValue
        }
        return property
    }

    static void setProperty(Project project, String key, String value) {
        project.ext.set(key, value)
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


    static Project findParentProjectWithDelivery(Project project) {
        if (project.parent != null) {
            if (project.parent.plugins.hasPlugin(DeliveryPlugin.class)) {
                return project.parent
            }
            return findParentProjectWithDelivery(project.parent)
        }
        return null
    }

    static boolean userHasDefineProperty(Project project, String propertyName) {
        if (project == null) {
            return false
        } else if (project.ext.has(propertyName)) {
            return true
        } else {
            return userHasDefineProperty(project.parent, propertyName)
        }
    }
}
