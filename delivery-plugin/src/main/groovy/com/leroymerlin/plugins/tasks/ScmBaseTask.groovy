package com.leroymerlin.plugins.tasks

import com.leroymerlin.plugins.core.ScmHandler
import org.gradle.api.DefaultTask

/**
 * Created by alexandre on 07/02/2017.
 */
class ScmBaseTask extends DefaultTask {
    ScmHandler scmHandler = new ScmHandler()

}
