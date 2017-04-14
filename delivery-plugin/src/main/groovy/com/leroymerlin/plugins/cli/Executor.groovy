package com.leroymerlin.plugins.cli

import org.gradle.api.GradleException
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger

/**
 * Created by alexandre on 31/01/2017.
 */

class Executor {

    public static Logger logger
    public static Map optionsMap
    public static List<String> commandsList
    public static LogLevel levelError = LogLevel.ERROR, levelLog = LogLevel.INFO

    static String exec(
            Map options = [:],
            List<String> commands
    ) {
        optionsMap = options
        commandsList = commands

        StringBuffer out = new StringBuffer()

        File directory = options['directory'] ? options['directory'] as File : null
        if (options.hasProperty('logError')) {
            levelError = options['logError'] ? LogLevel.ERROR : null
        }
        if (options.hasProperty('logLevel')) {
            levelLog = options['logLevel'] as LogLevel
        }
        List processEnv = options['env'] ? ((options['env'] as Map) << System.getenv()).collect {
            "$it.key=$it.value"
        } : null
        if (levelLog != null) {
            logger?.log(levelLog, "Running $commands in [$directory]")
        }
        Process process = commands.execute(processEnv, directory)
        waitForProcessOutput(process, out)

        return out.toString()
    }

    static List<String> convertToCommandLine(String cmd) {
        StringTokenizer st = new StringTokenizer(cmd)
        List<String> cmdList = new ArrayList<>()
        for (int i = 0; st.hasMoreTokens(); i++)
            cmdList.add(st.nextToken())
        cmdList
    }

    static void waitForProcessOutput(Process process, Appendable output) {
        def dumperOut = new TextDumper(process.getInputStream(), false, output)
        def dumperErr = new TextDumper(process.getErrorStream(), true, output)

        Thread tout = new Thread(dumperOut)
        Thread terr = new Thread(dumperErr)

        tout.start()
        terr.start()
        tout.join()
        terr.join()



        process.waitFor()
        process.closeStreams()

        if (dumperOut.exception) {
            throw dumperOut.exception;
        }
        if (dumperErr.exception) {
            throw dumperErr.exception;
        }

    }

    private static class TextDumper implements Runnable {
        InputStream input
        boolean catchError
        Appendable app

        Exception exception;

        TextDumper(InputStream inputStream, boolean catchError, Appendable app) {
            this.input = inputStream
            this.catchError = catchError
            this.app = app
        }

        void run() {
            InputStreamReader isr = new InputStreamReader(this.input)
            BufferedReader br = new BufferedReader(isr)

            try {
                String next
                while ((next = br.readLine()) != null) {
                    if (this.app != null) {
                        this.app.append(next)
                        this.app.append("\n")
                    }
                    if (catchError) {
                        if (optionsMap['failOnStderr'] as boolean) {
                            throw new GradleException("Running '${commandsList.join(' ')}' produced an error: ${next}")
                        } else {
                            if (levelError != null)
                                logger?.log(levelError, next)
                        }
                    } else {
                        if (levelLog != null)
                            logger?.log(levelLog, next)
                    }

                    if (optionsMap['errorPatterns'] && [next]*.toString().any { String s ->
                        (optionsMap['errorPatterns'] as List<String>).any {
                            s.contains(it)
                        }
                    }) {
                        throw new GradleException(optionsMap['errorMessage'] ? optionsMap['errorMessage'] as String : "Failed to run '${commandsList.join(' ')}' - $next")
                    }
                }
            } catch (IOException var5) {
                throw new GroovyRuntimeException("exception while reading process stream", var5)
            } catch (GradleException ex) {
                exception = ex;
            }
        }
    }
}
