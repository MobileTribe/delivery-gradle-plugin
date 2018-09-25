package com.leroymerlin.plugins.test.integration

import com.leroymerlin.plugins.cli.Executor
import org.junit.*

/**
 * Created by alexandre on 17/12/15.
 */
class DockerTest extends AbstractIntegrationTest {

    @Override
    String getProjectName() {
        return "docker"
    }

    @Before
    void beforeMethod() {
        //Executor.exec(Executor.convertToCommandLine("docker images"))
        def exec = Executor.exec(Executor.convertToCommandLine("docker ps")) {
            needSuccessExitCode = false
        }
        Assume.assumeTrue("docker is not installed or running", exec.exitValue == Executor.EXIT_CODE_OK)
        // rest of setup.
    }

    @After
    void cleanImage() {
        Executor.exec(Executor.convertToCommandLine("docker rmi delivery-test:1.0.0-SNAPSHOT"))
    }

    @Test
    void testBuildDocker() {
        applyExtraGradle('''


delivery{
    registryProperties {
        main {
            url 'docker.registry.com'
        }
    }
}

task('buildDockerImage', type: DockerBuild, group: 'delivery'){
    registry 'main'
    imageName 'delivery-test'
}

''')
        testTask('listDockerImages', 'install')
//        def list = []
//        archiveDirectory.eachFileRecurse(FileType.FILES, {
//            f ->
//                list << f
//        })


        def lineCount = Executor.exec(Executor.convertToCommandLine("docker images delivery-test")) {
            directory = project.projectDir
        }.logs.readLines().size()
        Assert.assertEquals("image delivery-test not found", 2, lineCount)
    }
}
