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

import com.leroymerlin.plugins.DeliveryPluginExtension
import com.leroymerlin.plugins.adapters.cli.Executor
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PluginHelper {

    private static final String LINE_SEP = System.getProperty('line.separator')
    private static final String PROMPT = "${LINE_SEP}??>"

    protected Project project

    protected DeliveryPluginExtension extension

    protected Executor executor

    /**
     * Retrieves SLF4J {@link Logger} instance.
     *
     * The logger is taken from the {@link Project} instance if it's initialized already
     * or from SLF4J {@link LoggerFactory} if it's not.
     *
     * @return SLF4J {@link Logger} instance
     */
    Logger getLog() { project?.logger ?: LoggerFactory.getLogger(this.class) }

    boolean useAutomaticVersion() {
        project.hasProperty('release.useAutomaticVersion') && project.getProperty('release.useAutomaticVersion') == "true" ||
                project.hasProperty('gradle.release.useAutomaticVersion') && project.getProperty('gradle.release.useAutomaticVersion') == "true" ||
                extension.useAutomaticVersion;
    }

    /**
     * Executes command specified and retrieves its "stdout" output.
     *
     * @param failOnStderr whether execution should fail if there's any "stderr" output produced, "true" by default.
     * @param commands commands to execute
     * @return command "stdout" output
     */
    String exec(
            Map options = [:],
            List<String> commands
    ) {
        initExecutor()
        options['directory'] = options['directory'] ?: project.rootDir
        executor.exec(options, commands)
    }

    private void initExecutor() {
        if (!executor) {
            executor = new Executor(log)
        }
    }

    void warnOrThrow(boolean doThrow, String message) {
        if (doThrow) {
            throw new GradleException(message)
        } else {
            log.warn("!!WARNING!! $message")
        }
    }


    String findProperty(String key, String defaultVal = "") {
        System.properties[key] ?: project.properties[key] ?: defaultVal
    }

    /**
     * Reads user input from the console.
     *
     * @param message Message to display
     * @param defaultValue (optional) default value to display
     * @return User input entered or default value if user enters no data
     */
    protected static String readLine(String message, String defaultValue = null) {
        String _message = "$PROMPT $message" + (defaultValue ? " [$defaultValue] " : "")
        if (System.console()) {
            return System.console().readLine(_message) ?: defaultValue
        }
        println "$_message (WAITING FOR INPUT BELOW)"

        return System.in.newReader().readLine() ?: defaultValue
    }

    private static boolean promptYesOrNo(String message, boolean defaultValue = false) {
        def defaultStr = defaultValue ? 'Y' : 'n'
        String consoleVal = readLine("${message} (Y|n)", defaultStr)
        if (consoleVal) {
            return consoleVal.toLowerCase().startsWith('y')
        }
        defaultValue
    }
}
