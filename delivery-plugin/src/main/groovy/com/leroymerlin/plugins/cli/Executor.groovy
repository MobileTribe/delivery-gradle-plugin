package com.leroymerlin.plugins.cli

import groovy.transform.TypeChecked
import org.gradle.api.GradleException

/**
 * Created by alexandre on 31/01/2017.
 */


class Executor {
    public static final int EXIT_CODE_NOT_FOUND = 127
    public static final int EXIT_CODE_OK = 0

    static DeliveryLogger deliveryLogger = new DeliveryLogger()

    static ExecutorResult exec(List<String> commands, @DelegatesTo(ExecutorParams) Closure closure = {}) {
        def params = new ExecutorParams()
        closure.delegate = params
        //code.resolveStrategy = Closure.DELEGATE_ONLY
        closure()

        return new Executor(commands, params).run()
    }

    static List<String> convertToCommandLine(String cmd) {
        StringTokenizer st = new StringTokenizer(cmd)
        List<String> cmdList = new ArrayList<>()
        for (int i = 0; st.hasMoreTokens(); i++)
            cmdList.add(st.nextToken())
        cmdList
    }

    ExecutorParams params
    List<String> commands
    Exception exception


    private Executor(List<String> commands, ExecutorParams params) {
        this.params = params
        this.commands = commands
    }

    @TypeChecked
    private ExecutorResult run() {
        StringBuffer out = new StringBuffer()
        File directory = params.directory
        List processEnv = params.env.collect {
            "$it.key=$it.value"
        }

        if (!params.hideCommand && !params.silent) {
            deliveryLogger.logInfo("Running $commands in [${directory != null ? directory : System.getProperty("user.dir")}]")
        }
        int exitValue = EXIT_CODE_NOT_FOUND
        try {
            Process process = commands.execute(processEnv, directory)
            def dumperOut = new TextDumper(process.getOutputStream(), process.getInputStream(), false, out)
            def dumperErr = new TextDumper(process.getOutputStream(), process.getErrorStream(), true, out)
            Thread tout = new Thread(dumperOut)
            Thread terr = new Thread(dumperErr)
            tout.start()
            terr.start()
            tout.join()
            terr.join()
            exitValue = process.waitFor()
            process.closeStreams()
        } catch (IOException e) {
            if (!params.hideCommand && !params.silent) {
                deliveryLogger.logError("Running $commands in [${directory != null ? directory : System.getProperty("user.dir")}] failed")
            }
            exception = e
        }

        def result = new ExecutorResult()
        result.error = exception
        result.logs = out.toString()
        result.exitValue = exitValue

        if (params.needSuccessExitCode && exitValue != 0) {
            String command = commands.join(' ')
            if (params.hideCommand) {
                command = "*******"
            }
            throw new GradleException("Running '$command' produced an error exit code: ${exitValue}")
        }
        return result
    }

    private class TextDumper implements Runnable {
        InputStream input
        OutputStream output
        boolean errorOutput
        Appendable out


        TextDumper(OutputStream outputStream, InputStream inputStream, boolean errorOutput, Appendable out) {
            this.input = inputStream
            this.output = outputStream
            this.errorOutput = errorOutput
            this.out = out
        }

        void run() {
            BufferedReader br = new BufferedReader(new InputStreamReader(this.input))
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(this.output))
            String next
            while ((next = br.readLine()) != null) {
                def inputMatch = params.inputPatterns.find { next.contains(it.key) }
                if (inputMatch) {
                    writer.write(inputMatch.value)
                    writer.newLine()
                    writer.flush()
                } else {
                    def errorMatch = params.errorPatterns.find { next.contains(it.key) }
                    if (errorMatch) {
                        if (errorMatch.value.error) {
                            exception = errorMatch.value.error
                        } else {
                            exception = new GradleException("Running '${commands.join(' ')}' produced an error: ${errorMatch.value.message}")
                        }

                        if (errorMatch.value.fatal) {
                            throw exception
                        }
                    }
                }

                if (this.out != null) {
                    this.out.append(next)
                    this.out.append("\n")
                }
                if (!params.silent) {
                    if (errorOutput) deliveryLogger.logError(next)
                    else deliveryLogger.logInfo(next)
                }
            }
        }
    }

    static class ExecutorParams {
        private ExecutorParams() {}

        boolean needSuccessExitCode = true
        boolean silent = false
        File directory
        Map<String, String> env = System.getenv()
        Boolean hideCommand = false
        Map<String, String> inputPatterns = [:]
        Map<String, ExecutorError> errorPatterns = [:]

        void handleError(List<String> patterns, @DelegatesTo(ExecutorError) Closure closure) {
            def error = new ExecutorError()
            closure.delegate = error
            //code.resolveStrategy = Closure.DELEGATE_ONLY
            closure()
            patterns.forEach({
                pattern -> errorPatterns.put(pattern, error)
            })
        }
    }


    private static class ExecutorError {
        String message
        Exception error
        Boolean fatal = true
    }

    static class ExecutorResult {
        Exception error
        String logs
        int exitValue
    }
}