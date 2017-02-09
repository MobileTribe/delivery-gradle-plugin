package com.leroymerlin.plugins.tasks

import com.leroymerlin.plugins.core.GitHandler
import org.gradle.api.DefaultTask

/**
 * Created by alexandre on 07/02/2017.
 */
class ScmBaseTask extends DefaultTask {
    GitHandler scmHandler = new GitHandler()

}
