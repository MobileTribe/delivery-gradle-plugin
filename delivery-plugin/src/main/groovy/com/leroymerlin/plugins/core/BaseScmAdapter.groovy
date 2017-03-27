package com.leroymerlin.plugins.core

import com.leroymerlin.plugins.DeliveryPluginExtension
import org.gradle.api.Project

/**
 * Created by florian on 30/01/2017.
 */
interface BaseScmAdapter {

    void setup(Project project, DeliveryPluginExtension extension)

    String addFiles(String[] files)

    String commit(String comment)

    String deleteBranch(String branchName)

    String switchBranch(String branchName, boolean createIfNeeded)

    String tag(String annotation, String message)

    String merge(String from)

    String push()

    List<String> generateGitCommand(List<String> command)

    String discardChange()
}
