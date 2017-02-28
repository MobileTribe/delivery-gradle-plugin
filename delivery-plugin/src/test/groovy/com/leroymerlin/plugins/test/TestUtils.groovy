package com.leroymerlin.plugins.test

/**
 * Created by alexandre on 28/02/2017.
 */
class TestUtils {
    static File getPluginBaseDir() {
        def file = new File("delivery-plugin")
        if (file.exists()) {
            return file
        }
        return new File("").absoluteFile
    }
}