package com.leroymerlin.plugins.tasks

import com.leroymerlin.plugins.core.GitHelper

/**
 * Created by alexandre on 06/02/2017.
 */
class GitBranchTask extends ScmBranchTask {

    @Override
    initGit() {
        return GitHelper.initGit()
    }

    @Override
    addAllFiles() {
        return GitHelper.addAllFiles()
    }

    @Override
    tag(String annotation, String message) {
        return GitHelper.tag(annotation, message)
    }

    @Override
    merge(String branchToBeMerged) {
        return GitHelper.merge(branchToBeMerged)
    }

    @Override
    merge(String branchToBeMerged, String mergeInto) {
        return GitHelper.merge(branchToBeMerged, mergeInto)
    }

    @Override
    goToBranch(String branch) {
        return GitHelper.checkout(branch, false)
    }

    @Override
    deleteBranch(String branch) {
        return GitHelper.deleteBranch(branch)
    }

    @Override
    createBranch(String branch) {
        return GitHelper.checkout(branch, true)
    }

    @Override
    push() {
        return GitHelper.push()
    }

    @Override
    commit(String message) {
        return GitHelper.commit(message)
    }
}
