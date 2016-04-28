package com.leroymerlin.plugins.utils

import com.leroymerlin.plugins.DeliveryPlugin
import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.adapters.BaseScmAdapter
import org.gradle.api.GradleException
import org.gradle.api.Project

import java.text.SimpleDateFormat

/**
 * Created by root on 23/02/16.
 */
public class ScmFlowMethods {
    private BaseScmAdapter scmAdapter;

    Project project
    DeliveryPlugin parent
    DeliveryPluginExtension extension

    public ScmFlowMethods(Project project, DeliveryPlugin deliveryPlugin) {
        this.project = project
        this.parent = deliveryPlugin
        this.extension = parent.extension;
    }

    /**
     * Recursively look for the type of the SCM we are dealing with, if no match is found look in parent directory
     * @param directory the directory to start from
     */
    protected BaseScmAdapter findScmAdapter() {
        BaseScmAdapter adapter;
        File projectPath = project.rootProject.projectDir.canonicalFile

        this.project.delivery.scmAdapters.find {
            BaseScmAdapter instance = it.getValue().newInstance(project)
            if (instance.isSupported(projectPath)) {
                adapter = instance
                return true
            }

            return false
        }

        if (adapter == null) {
            throw new GradleException(
                    "No supported Adapter could be found. Are [${projectPath}] or its parents are valid scm directories?")
        }

        return adapter
    }

    public void prepareScmAdapter() {
        parent.logger.info("Preparing the ScmAdapter")
        try {
            scmAdapter = findScmAdapter()
        } catch (Exception e) {
            parent.logger.error("Error during the creation of the ScmAdapter " + e)
        }
        if (scmAdapter != null) {
            scmAdapter.init()
            scmAdapter.checkUpdateNeeded()
            scmAdapter.checkCommitNeeded()
        } else {
            parent.warnOrThrow(true, "No Scm adapter is properly configure")
        }
    }

    public void releaseScmAdapter() {
        parent.logger.info("Release the ScmAdapter")
        if (scmAdapter != null) {
            scmAdapter.release()
            scmAdapter = null;
        }

    }

    public void createReleaseBranch() {
        parent.logger.info("Creating the Release Branch ")

        parent.releaseMethods.unSnapVersion()

        scmAdapter.createNewReleaseBranchIfNeeded()
    }

    void mergeReleaseBranch() {
        parent.releaseMethods.unSnapVersion()

        parent.logger.info("Merge Release on Master and tags")
        //Se repositionne sur la bonne branche
        scmAdapter.createNewReleaseBranchIfNeeded()
        //Sauvegarde les infos de la release ds un fichier delivery.properties
        saveLatestReleaseInfos()
        //COmmit le fichier qui a été créé
        basicCommit("docs (infoRelease) : Updates the delivery.properties file with the date and version of the release")

        String releaseBranchName = Utils.releaseBranchName(this.project, this.extension)
        scmAdapter.merge(releaseBranchName, "master", true)
        scmAdapter.createReleaseTag(Utils.tagName(this.project, this.extension))
    }

    void saveLatestReleaseInfos() {
        def ant = new AntBuilder()
        def date = new Date()
        def sdf = new SimpleDateFormat("dd/MM/yyyy HH'h'mm")

        File f = project.file("delivery.properties");
        ant.propertyfile(file: f.absolutePath) {
            entry(key: "releaseVersion", value: project.version - '-SNAPSHOT')
            entry(key: "releaseVersionCode", value: project.versioncode)
            entry(key: "releaseDate", value: sdf.format(date))
        }
    }

    void commitReleaseBranch() {
        parent.logger.info("Commit the release branch")
        String branch = Utils.releaseBranchName(this.project, this.extension)
        String message = Utils.newVersionCommitMessage(this.project, this.extension)
        scmAdapter.commitBranch(branch, message)
    }

    void backOnDevelop() {
        parent.logger.info("Trying to merge on develop")
        scmAdapter.merge("master", "develop", false)
    }

    void basicCommit(String message) {
        parent.logger.info("Commit of the changes " + message)
        if (message != null) {
            scmAdapter.commit(message)
        }
    }

}
