package com.leroymerlin.plugins.core

import com.leroymerlin.plugins.DeliveryPluginExtension
import org.gradle.api.Project

/**
 * Created by florian on 30/01/2017.
 */
interface BaseScmAdapter {

    void setup(Project project, DeliveryPluginExtension extension)

    String addFiles(String[] files)

    String commit(String message)

    String deleteBranch(String branchName)

    String switchBranch(String branchName, boolean createIfNeeded)

    String tag(String annotation, String message)

    String merge(String from)

    String push(String branch, boolean tags)

    String pushTag(String tagName)

    List<String> generateGitCommand(List<String> command)

    String discardChange()
}
