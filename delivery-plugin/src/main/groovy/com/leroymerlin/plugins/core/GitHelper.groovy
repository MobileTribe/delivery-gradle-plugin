package com.leroymerlin.plugins.core

import com.leroymerlin.plugins.cli.Executor

/**
 * Created by alexandre on 06/02/2017.
 */
class GitHelper extends Executor {
    static Map params = ['directory': 'delivery-test', 'errorMessage': 'An error occured']

    static String initGit() {
        return exec(params, ['git', 'init'])
    }

    static String addAllFiles() {
        return exec(params, ['git', 'add', '.'])
    }

    static String commit(String comment) {
        return exec(params, ['git', 'commit', '-m', "\'" + comment + "\'"])
    }

    static String deleteBranch(String branchName) {
        return exec(params, ['git', 'branch', '-d', branchName])
    }

    static String checkout(String branchName, boolean createNewBranch) {
        if (createNewBranch)
            return exec(params, ['git', 'checkout', '-b', branchName])
        else
            return exec(params, ['git', 'checkout', branchName])
    }

    static String tag(String annotation, String message) {
        return exec(params, ['git', 'tag', '-a', annotation, '-m', '\'' + message + '\''])
    }

    static String merge(String branchToBeMerged) {
        return exec(params, ['git', 'merge', '--no-ff', branchToBeMerged])
    }

    static String merge(String branchToBeMerged, String mergeInto) {
        exec(params, ['git', 'checkout', mergeInto])
        return exec(params, ['git', 'merge', '--no-ff', branchToBeMerged])
    }

    static String push() {
        return exec(params, ['git', 'push'])
    }
}
