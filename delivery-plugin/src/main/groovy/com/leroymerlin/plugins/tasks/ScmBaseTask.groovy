package com.leroymerlin.plugins.tasks

import com.leroymerlin.plugins.core.BaseScmAdapter
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input

/**
 * Created by alexandre on 07/02/2017.
 */
class ScmBaseTask extends DefaultTask {
    @Input
    BaseScmAdapter scmAdapter
}
