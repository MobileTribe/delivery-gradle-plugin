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
    public static LogLevel level

    static String exec(
            Map options = [:],
            List<String> commands
    ) {
        optionsMap = options
        commandsList = commands

        StringBuffer out = new StringBuffer()

        File directory = options['directory'] ? options['directory'] as File : null
        level = options['logLevel'] ? options['logLevel'] as LogLevel : null
        List processEnv = options['env'] ? ((options['env'] as Map) << System.getenv()).collect {
            "$it.key=$it.value"
        } : null
        if (level != null)
            logger?.log(level, "Running $commands in [$directory]")
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
        Thread tout = new Thread(new TextDumper(process.getInputStream(), false, output))
        Thread terr = new Thread(new TextDumper(process.getErrorStream(), true, output))

        tout.start()
        terr.start()
        tout.join()
        terr.join()

        process.waitFor()
        process.closeStreams()
    }

    private static class TextDumper implements Runnable {
        InputStream input
        boolean catchError
        Appendable app

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
                            throw new GradleException("Running $commandsList produced an error: ${next}")
                        } else {
                            if (level != null)
                                logger?.log(level, next)
                        }
                    } else {
                        if (level != null)
                            logger?.log(level, next)
                    }

                    if (optionsMap['errorPatterns'] && [next]*.toString().any { String s ->
                        (optionsMap['errorPatterns'] as List<String>).any {
                            s.contains(it)
                        }
                    }) {
                        throw new GradleException("${optionsMap['errorMessage'] ? optionsMap['errorMessage'] as String : 'Failed to run [' + commandsList.join(' ') + ']'} - [$next]")
                    }
                }
            } catch (IOException var5) {
                throw new GroovyRuntimeException("exception while reading process stream", var5)
            }
        }
    }
}
