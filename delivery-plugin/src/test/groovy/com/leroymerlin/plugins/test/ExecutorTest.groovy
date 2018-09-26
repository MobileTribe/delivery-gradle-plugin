package com.leroymerlin.plugins.test

import com.leroymerlin.plugins.cli.Executor
import org.junit.After
import org.junit.Before
import org.junit.Test

class ExecutorTest {

    @Before
    void setUp() {
    }

    @After
    void tearDown() {
    }

    @Test
    void testUnknownCommand() {
        def result = Executor.exec(["unknownCommand"]) {
            needSuccessExitCode = false
            directory = new File("")
        }
        assert result.exitValue == 127
    }

}
