package com.leroymerlin.plugins.test.integration

import groovy.io.FileType
import org.junit.Assert
import org.junit.Test

/**
 * Created by alexandre on 17/12/15.
 */
class UnknowTest extends AbstractIntegrationTest {

    @Override
    String getProjectName() {
        return "unknow"
    }

    @Test
    void testBuildTaskGeneration() {

        def archiveDirectory = new File(workingDirectory, "build/archive")
        applyExtraGradle('''

task ('buildVariant1', type: DeliveryBuild, group: 'delivery'){
    variantName = 'variant1'
    outputFiles = ["release": file('build/variant1.txt')]
}.doLast{
    cmd('java -jar delivery-test.jar variant1')
}

task('buildVariant2', type: DeliveryBuild, group: 'delivery'){
    variantName = 'variant2'
    outputFiles = ["release": file('build/variant2.txt')]
}.doLast{
    cmd('java -jar delivery-test.jar variant2')
}


delivery{

    configurator = [ applyProperties: {
        def f = file('version.txt')
        f.delete()
        f.createNewFile()
        f << project.version
    }]
    archiveRepositories = {
        maven {
            url uri("''' + archiveDirectory.absolutePath + '''")
        }
    }
    flows{
        projectBuild{
            build
        }
    }
}
''')

        testTask('projectBuildFlow')
        def list = []
        archiveDirectory.eachFileRecurse(FileType.FILES, {
            f ->
                list << f
        })
        Assert.assertEquals("archive folder should contain 4 files", 4, list.size())
    }
}