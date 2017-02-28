package com.leroymerlin.plugins.utils

import com.leroymerlin.plugins.DeliveryPluginExtension
import groovy.text.SimpleTemplateEngine
import org.gradle.api.Project

public class Utils {

    public final static String tagName(Project project, DeliveryPluginExtension extension) {
        def tagName
        SimpleTemplateEngine engine = new SimpleTemplateEngine()

        def text = extension.releaseTagPattern
        def binding = ["version": project.version, "versionId": project.versionId, "projectName": project.projectName]

        //TODO Vérifier que le tag est correctement défini par l'utilisateur ?
        tagName = engine.createTemplate(text).make(binding)

        return tagName.toString().replace(" ", "")
    }

    public final static String releaseBranchName(Project project, DeliveryPluginExtension extension) {
        def branchName
        SimpleTemplateEngine engine = new SimpleTemplateEngine()

        def text = extension.releaseBranchPattern
        def binding = ["version": project.version, "versionId": project.versionId, "projectName": project.projectName]

        //TODO Vérifier que le tag est correctement défini par l'utilisateur ?
        branchName = engine.createTemplate(text).make(binding)
        branchName
    }

    public final static String newVersionCommitMessage(Project project, DeliveryPluginExtension extension) {
        def branchName
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        def text = extension.newVersionCommitPattern
        def binding = ["version": project.version, "versionId": project.versionId, "projectName": project.projectName]

        //TODO Vérifier que le tag est correctement défini par l'utilisateur ?
        branchName = engine.createTemplate(text).make(binding)

        branchName
    }


    public static String readProperty(File f, String key) {
        Properties properties = new Properties();
        def propStream = new FileInputStream(f.absolutePath);
        properties.load(propStream)
        return properties.getProperty(key)
    }

    public static void setPropertyInFile(File f, Map<String, String> datas) {
        if (!f.exists())
            f.createNewFile()
        Properties properties = new Properties();
        def propStream = new FileInputStream(f.absolutePath);
        properties.load(propStream);
        propStream.close();

        datas.each { key, value ->
            properties.setProperty(key, value);
        }
        def outStream = new FileOutputStream(f.absolutePath);
        properties.store(outStream, "");
        outStream.close();
    }
}
