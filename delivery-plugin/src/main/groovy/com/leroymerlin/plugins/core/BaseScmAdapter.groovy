package com.leroymerlin.plugins.core

import com.leroymerlin.plugins.DeliveryPluginExtension
import org.gradle.api.Project

/**
 * Created by florian on 30/01/2017.
 */
interface BaseScmAdapter {

    void setup(Project project, DeliveryPluginExtension extension)

    void release()

    String addAllFiles() throws ScmException

    String commit(String comment) throws ScmException

    String deleteBranch(String branchName) throws ScmException

    String switchBranch(String branchName, boolean createIfNeeded) throws ScmException

    String tag(String annotation, String message) throws ScmException

    String merge(String from) throws ScmException

    String push() throws ScmException
}
