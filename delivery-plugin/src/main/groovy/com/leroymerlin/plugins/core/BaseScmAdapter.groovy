package com.leroymerlin.plugins.core

import com.leroymerlin.plugins.DeliveryPluginExtension
import org.gradle.api.Project

/**
 * Created by florian on 30/01/2017.
 */
interface BaseScmAdapter {

    void setup(Project project, DeliveryPluginExtension extension)

    void addFiles(String[] files)

    void commit(String message)

    void deleteBranch(String branchName)

    void switchBranch(String branchName, boolean createIfNeeded)

    void tag(String annotation, String message)

    void merge(String from)

    void push(String branch, boolean tags)

    void pushTag(String tagName)

    void pull(String branchName)

    void discardChange()
}
