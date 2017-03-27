package com.leroymerlin.plugins.test.integration

import com.leroymerlin.plugins.cli.Executor
import org.junit.Test

/**
 * Created by alexandre on 27/03/17.
 */
class IonicTest extends AbstractIntegrationTest {

    @Override
    String getProjectName() {
        return "ionic"
    }

    @Test
    void testBuildTaskGeneration() {

        "chmod -R a+rwx ${workingDirectory.absolutePath}".execute()
        println(Executor.exec(["ionic", "build", 'android'], directory: workingDirectory))
        /*testTask('buildFlow')
        def list = []
        archiveDirectory.eachFileRecurse(FileType.FILES, {
            f ->
                list << f
        })
        Assert.assertEquals("archive folder should contain 16 files", 16, list.size());*/
    }
}
