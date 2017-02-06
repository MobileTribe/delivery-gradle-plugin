package com.leroymerlin.plugins.tasks

import org.gradle.api.DefaultTask

/**
 * Created by alexandre on 06/02/2017.
 */
abstract class ScmBranchTask extends DefaultTask {

    def branchName
    def create

    abstract initGit()

    abstract addAllFiles()

    abstract tag(String annotation, String message)

    abstract merge(String branchToBeMerged)

    abstract merge(String branchToBeMerged, String mergeInto)

    abstract goToBranch(String branch)

    abstract deleteBranch(String branch)

    abstract createBranch(String branch)

    abstract push()

    abstract commit(String message)
}

/*

delivery{

    scmAdapter Git

}



class Git{

    Class<com.leroymerlin.plugins.tasks.ScmBranchTask> getScmBranchTaskType(){
        return GitBranchTask.class
    }


}*/