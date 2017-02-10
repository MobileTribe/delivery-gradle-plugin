package com.leroymerlin.plugins.core

import com.leroymerlin.plugins.cli.Executor

/**
 * Created by alexandre on 06/02/2017.
 */
class GitHandler extends Executor {
    Map params = ['directory': 'delivery-test', 'errorMessage': 'An error occured']

    String init() {
        return println(exec(params, ['git', 'init']))
    }

    String addAllFiles() {
        return println(exec(params, ['git', 'add', '.']))
    }

    String commit(String comment) {
        return println(exec(params, ['git', 'commit', '-am', "\'" + comment + "\'"]))
    }

    String deleteBranch(String branchName) {
        return println(exec(params, ['git', 'branch', '-d', branchName]))
    }

    String createBranch(String branchName) {
        return println(exec(params, ['git', 'checkout', '-B', branchName]))
    }

    String goToBranch(String branchName) {
        return println(exec(params, ['git', 'checkout', branchName]))
    }

    String tag(String annotation, String message) {
        return println(exec(params, ['git', 'tag', '-a', annotation, '-m', '\'' + message + '\'']))
    }

    String merge(String branchToBeMerged, String mergeInto) {
        println(exec(params, ['git', 'checkout', mergeInto]))
        return println(exec(params, ['git', 'merge', '--no-ff', branchToBeMerged]))
    }

    String push() {
        return println(exec(params, ['git', 'push']))
    }
}