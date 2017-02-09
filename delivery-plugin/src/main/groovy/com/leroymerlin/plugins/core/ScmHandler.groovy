package com.leroymerlin.plugins.core

import com.leroymerlin.plugins.cli.Executor

/**
 * Created by alexandre on 06/02/2017.
 */
class ScmHandler extends Executor {
    Map params = ['directory': 'delivery-test', 'errorMessage': 'An error occured']

    String init() {
        return println(exec(params, ['git', 'init']))
    }

    String addAllFiles() {
        return exec(params, ['git', 'add', '.'])
    }

    String commit(String comment) {
        return exec(params, ['git', 'commit', '-m', "\'" + comment + "\'"])
    }

    String deleteBranch(String branchName) {
        return exec(params, ['git', 'branch', '-d', branchName])
    }

    String createBranch(String branchName) {
        return exec(params, ['git', 'checkout', '-b', branchName])
    }

    String goToBranch(String branchName) {
        return println(exec(params, ['git', 'checkout', branchName]))
    }

    String tag(String annotation, String message) {
        return exec(params, ['git', 'tag', '-a', annotation, '-m', '\'' + message + '\''])
    }

    String merge(String branchToBeMerged, String mergeInto) {
        exec(params, ['git', 'checkout', mergeInto])
        return exec(params, ['git', 'merge', '--no-ff', branchToBeMerged])
    }

    String push() {
        return exec(params, ['git', 'push'])
    }
}

/*

delivery{

    scmAdapter Git

}



class Git{

    Class<com.leroymerlin.plugins.core.ScmHandler> getScmBranchTaskType(){
        return ScmBranchTask.class
    }


}*/