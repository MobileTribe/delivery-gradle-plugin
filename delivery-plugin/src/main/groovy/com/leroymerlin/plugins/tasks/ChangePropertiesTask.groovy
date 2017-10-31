package com.leroymerlin.plugins.tasks

import com.leroymerlin.plugins.DeliveryPlugin
import com.leroymerlin.plugins.utils.PropertiesUtils
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

/**
 * Created by alexandre on 15/02/2017.
 */
class ChangePropertiesTask extends DefaultTask {

    String version, versionId, artifact
    Project project

    @TaskAction
    changeProperties() {
        File[] versionFiles = DeliveryPlugin.getVersionFiles(project).reverse()

        Map<String, String> values = new ConcurrentHashMap<>()
        if (version != null) {
            values.put(project.versionKey as String, version)
        }
        if (versionId != null) {
            values.put(project.versionIdKey as String, versionId)
        }
        if (artifact != null) {
            values.put(project.artifactKey as String, artifact)
        }

        versionFiles.each {
            Properties prop = PropertiesUtils.readPropertiesFile(it)
            values.entrySet().each { map ->
                if (prop.getProperty(map.key) != null) {
                    prop.setProperty(map.key, values.remove(map.key))
                    PropertiesUtils.writePropertiesFile(it, prop)
                }
            }
        }

        values.each {
            Logger.global.warning("Could not set ${it.key}. Not found in version.properties files")
        }

        project.delivery.plugin.setupProperties()
    }
}
