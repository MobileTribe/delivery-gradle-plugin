package com.leroymerlin.plugins.core

/**
 * Created by alexandre on 31/01/2017.
 */
interface BaseScmInterface {
    void initReleaseBranch()

    void prepareReleaseFiles()

    void commitChanges()

    void runBuild()

    void makeRelease()
}
