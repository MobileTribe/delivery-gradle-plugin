/*
 * This file is part of the gradle-release plugin.
 *
 * (c) Eric Berry
 * (c) ResearchGate GmbH
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.leroymerlin.plugins.adapters

import com.leroymerlin.plugins.utils.Utils
import org.gradle.api.GradleException
import org.gradle.api.Project

import java.util.regex.Matcher

class GitAdapter extends BaseScmAdapter {

    private static
    final String LINE = '~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~'

    private static final String UNCOMMITTED = 'uncommitted'
    private static final String UNVERSIONED = 'unversioned'
    private static final String AHEAD = 'ahead'
    private static final String BEHIND = 'behind'

    URI baseUri

    class GitConfig {
        String requireBranch = System.getProperty('BRANCH', 'develop')
        def pushToRemote = 'origin' // needs to be def as can be boolean or string
        String password = System.getProperty('SCM_PASSWORD')
        String user = System.getProperty('SCM_USER')
    }

    GitAdapter(Project project) {
        super(project)
    }

    @Override
    Object createNewConfig() {
        return new GitConfig()
    }

    @Override
    boolean isSupported(File directory) {
        boolean supported = true;
        if (!directory.list().grep('.git')) {
            supported = supported &&( directory.parentFile ? isSupported(directory.parentFile) : false)
        }
        supported = supported && extension.hasProperty("gitConfig")

        return supported
    }

    @Override
    void init() {

        if (extension.gitConfig.user != null && !extension.gitConfig.user.isEmpty()) {

            baseUri = new URI(exec(['git', 'config', '--local', '--get', 'remote.origin.url'], errorMessage: "Fail to read origin url").readLines()[0]);

            String domain = baseUri.getHost()+baseUri.path
            String credential = "${baseUri.getScheme()}://${extension.gitConfig.user}:${extension.gitConfig.password}@$domain"

            exec(['git', 'remote', 'rm', 'origin'], errorMessage: "Fail to remove origin")
            exec(['git', 'remote', 'add', 'origin', credential], errorMessage: "Fail to add origin")


        }
        if (extension.gitConfig.requireBranch) {

            exec(['git', 'fetch'], errorMessage: "Fail to fetch")

            exec(['git', 'checkout', "$extension.gitConfig.requireBranch"], errorMessage: "Fail to checkout $extension.gitConfig.requireBranch")

            exec(['git', 'branch', "--set-upstream-to=origin/$extension.gitConfig.requireBranch", extension.gitConfig.requireBranch], errorMessage: "Fail to set upstream")
            exec(['git', 'pull'], errorMessage: "Fail to pull")

            def branch = gitCurrentBranch()
            if (branch != extension.gitConfig.requireBranch) {
                throw new GradleException("Current Git branch is \"$branch\" and not \"${extension.gitConfig.requireBranch}\".")
            }
        }
    }

    boolean isOnTheReleaseBranch(){
        return gitCurrentBranch().equals(Utils.releaseBranchName(this.project, this.extension))
    }

    boolean releaseBranchAlreadyExists(){
        boolean retour = false;
        exec(['git', 'branch']).readLines().each{ value ->
            retour |= value.contains(Utils.releaseBranchName(this.project, this.extension))
        }
        return retour
    }

    @Override
    void createNewReleaseBranchIfNeeded(){
        if( project.version != null ){
            def releaseBranchName = Utils.releaseBranchName(this.project, this.extension)
            if(!releaseBranchAlreadyExists()){
                exec(['git', 'checkout', "-b", releaseBranchName], errorMessage: "Failed to create the "  + releaseBranchName + " branch")
                exec(['git', 'branch', "--set-upstream-to=origin/$releaseBranchName", releaseBranchName], errorMessage: "Fail to set upstream")
                //TODO Should Push after creating the new branch ?
            }else if(!isOnTheReleaseBranch()){
                exec(['git', 'checkout', releaseBranchName], errorMessage: "Failed to switch to the " + releaseBranchName + " branch")
                exec(['git', 'branch', "--set-upstream-to=origin/$releaseBranchName", releaseBranchName], errorMessage: "Fail to set upstream")
            }
        }else{
            throw new GradleException("Project version not found")
        }
    }

    @Override
    void release() {
        if(baseUri!=null){
            exec(['git', 'remote', 'rm', 'origin'], errorMessage: "Fail to remove origin")
            exec(['git', 'remote', 'add', 'origin', baseUri.toURL().toString()], errorMessage: "Fail to add origin")
        }
    }

    @Override
    void checkCommitNeeded() {

        def status = gitStatus()

        if (status[UNVERSIONED]) {
       //     warnOrThrow(extension.failOnUnversionedFiles,
            warnOrThrow(true,
                    (['You have unversioned files:', LINE, *status[UNVERSIONED], LINE] as String[]).join('\n'))
        }

        if (status[UNCOMMITTED]) {
         //   warnOrThrow(extension.failOnCommitNeeded,
            warnOrThrow(true,
                    (['You have uncommitted files:', LINE, *status[UNCOMMITTED], LINE] as String[]).join('\n'))
        }
    }

    @Override
    void checkUpdateNeeded() {

        exec(['git', 'remote', 'update'], errorPatterns: ['error: ', 'fatal: '])

        def status = gitRemoteStatus()

        if (status[AHEAD]) {
        //    warnOrThrow(extension.failOnPublishNeeded, "You have ${status[AHEAD]} local change(s) to push.")
            warnOrThrow(true, "You have ${status[AHEAD]} local change(s) to push.")
        }

        if (status[BEHIND]) {
        //    warnOrThrow(extension.failOnUpdateNeeded, "You have ${status[BEHIND]} remote change(s) to pull.")
            warnOrThrow(true, "You have ${status[BEHIND]} remote change(s) to pull.")
        }
    }

    @Override
    void createReleaseTag(String tagName) {
        exec(['git', 'tag', tagName], errorMessage: "Duplicate tag [$tagName]", errorPatterns: ['already exists'])
        //if (shouldPush()) {
        exec(['git', 'push', '--porcelain', 'origin', tagName], errorMessage: "Failed to push tag [$tagName] to remote", errorPatterns: ['[rejected]', 'error: ', 'fatal: '])
       // }
    }

    @Override
    void commit(String message) {
        exec(['git', 'add', '.'])
        exec(['git', 'commit', '-a', '-m', message])
        exec(['git', 'pull'], errorMessage: "Fail to pull")

        /*  if (shouldPush()) {
              def branch
              if (extension.gitConfig.pushToCurrentBranch) {
                  branch = gitCurrentBranch()
              } else {
                  branch = extension.gitConfig.requireBranch ? extension.gitConfig.requireBranch : 'master'
              }*/
        exec(['git', 'push', '--porcelain', 'origin', gitCurrentBranch()], errorMessage: 'Failed to push to remote', errorPatterns: ['[rejected]', 'error: ', 'fatal: '])
       // }
    }

    @Override
    void merge(String branchFrom, String branchTo, boolean shouldPush){
        this.checkCommitNeeded()
        this.checkUpdateNeeded()

        if(branchTo.equals("develop")){
            branchTo = extension.gitConfig.requireBranch
        }

        exec(['git', 'checkout', branchTo], errorMessage: "Failed to checkout " + branchTo)
        exec(['git', 'merge', branchFrom], errorMessage: "Failed to merge " + branchFrom + " into " + branchTo)
        if(shouldPush){
            exec(['git', 'push', '--porcelain', 'origin', branchTo], errorMessage: "Failed to push branch [$branchTo] to remote", errorPatterns: ['[rejected]', 'error: ', 'fatal: '])
        }
    }

    @Override
    void commitBranch(String branch, String message){
        exec(['git', 'commit', '-a', '-m', message], errorMessage: "Failed to commit changed to " + branch + " branch")
        exec(['git', 'push', "-u", "origin", branch], errorMessage: "Failed to push the " + branch + " branch")
    }

    void revert() {
        //exec(['git', 'checkout', "*"+findPropertiesFile().name], errorMessage: 'Error reverting changes made by the release plugin.')
        exec(['git', 'reset', '--hard', extension.gitConfig.pushToRemote+"/"+extension.gitConfig.requireBranch], errorMessage: 'Error reverting changes made by the release plugin.')
    }

    private boolean shouldPush() {
        def shouldPush = false
        if (extension.gitConfig.pushToRemote) {
            exec(['git', 'remote']).eachLine { line ->
                Matcher matcher = line =~ ~/^\s*(.*)\s*$/
                if (matcher.matches() && matcher.group(1) == extension.gitConfig.pushToRemote) {
                    shouldPush = true
                }
            }
            if (!shouldPush && extension.gitConfig.pushToRemote != 'origin') {
                throw new GradleException("Could not push to remote ${extension.gitConfig.pushToRemote} as repository has no such remote")
            }
        }

        shouldPush
    }

    private String gitCurrentBranch() {
        def matches = exec(['git', 'branch']).readLines().grep(~/\s*\*.*/)
        matches[0].trim() - (~/^\*\s+/)
    }

    private Map<String, List<String>> gitStatus() {
        exec(['git', 'status', '--porcelain']).readLines().groupBy {
            if (it ==~ /^\s*\?{2}.*/) {
                UNVERSIONED
            } else {
                UNCOMMITTED
            }
        }
    }

    private Map<String, Integer> gitRemoteStatus() {
        def branchStatus = exec(['git', 'status', '-sb']).readLines()[0]
        def aheadMatcher = branchStatus =~ /.*ahead (\d+).*/
        def behindMatcher = branchStatus =~ /.*behind (\d+).*/

        def remoteStatus = [:]

        if (aheadMatcher.matches()) {
            remoteStatus[AHEAD] = aheadMatcher[0][1]
        }
        if (behindMatcher.matches()) {
            remoteStatus[BEHIND] = behindMatcher[0][1]
        }
        remoteStatus
    }
}
