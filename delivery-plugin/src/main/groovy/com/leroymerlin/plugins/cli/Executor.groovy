package com.leroymerlin.plugins.cli

import groovy.transform.TypeChecked
import org.gradle.api.GradleException

import java.util.logging.Level

/**
 * Created by alexandre on 31/01/2017.
 */


class Executor {
    static DeliveryLogger deliveryLogger = new DeliveryLogger()

    static ExecutorResult exec(List<String> commands, @DelegatesTo(ExecutorParams) Closure closure) {
        def params = new ExecutorParams()
        def code = closure.rehydrate(params, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

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

        if (!params.hideCommand) {
            deliveryLogger.logInfo("Running $commands in [${directory != null ? directory : System.getProperty("user.dir")}]")
        }
        int exitValue = 127
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
            if (!params.hideCommand) {
                deliveryLogger.logError("Running $commands in [${directory != null ? directory : System.getProperty("user.dir")}] failed")
            }
            exception = e
        }

        def result = new ExecutorResult()
        result.error = exception
        result.logs = out.toString()
        result.exitValue = exitValue

        if (params.needSuccessExitCode && exitValue != 0) {
            throw new GradleException("Running '${commands.join(' ')}' produced an error exit code: ${exitValue}")
        }
        return result
    }

    private class TextDumper implements Runnable {
        InputStream input
        OutputStream output
        boolean catchError
        Appendable out


        TextDumper(OutputStream outputStream, InputStream inputStream, boolean catchError, Appendable out) {
            this.input = inputStream
            this.output = outputStream
            this.catchError = catchError
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
                deliveryLogger.log(next, params.logLevel)
            }

        }

    }

    private static class ExecutorParams {
        boolean needSuccessExitCode = true
        File directory
        Map<String, String> env = System.getenv()
        Boolean hideCommand = false
        Map<String, String> inputPatterns = [:]
        Map<String, ExecutorError> errorPatterns = [:]
        Level logLevel = Level.INFO

        void handleError(String pattern, @DelegatesTo(ExecutorError) Closure closure) {
            def error = new ExecutorError()
            def code = closure.rehydrate(error, this, this)
            code.resolveStrategy = Closure.DELEGATE_ONLY
            code()
            errorPatterns.put(pattern, error)
        }
    }


    private static class ExecutorError {
        String message
        Exception error
        Boolean fatal = true
    }

    private static class ExecutorResult {
        Exception error
        String logs
        int exitValue
    }

}

//
//class Executor {
//
//
//    private String run() {
//        StringBuffer out = new StringBuffer()
//        File directory = options['directory'] ? options['directory'] as File : null
//        List processEnv = options['env'] ? ((options['env'] as Map) << System.getenv()).collect {
//            "$it.key=$it.value"
//        } : null
//
//        if (!(options['hideCommand'] as boolean)) {
//            deliveryLogger.logInfo("Running $commands in [${directory != null ? directory : System.getProperty("os.name")}]")
//        }
//        try {
//            Process process = commands.execute(processEnv, directory)
//            waitForProcessOutput(process, out)
//        } catch (Exception e) {
//            if (options['failOnStderr'] as boolean && options['failOnStderrMessage'] != null) {
//                throw new Exception(options['failOnStderrMessage'] as String)
//            } else {
//                e.printStackTrace()
//            }
//        }
//        return out.toString()
//    }
//
//
//    private void waitForProcessOutput(Process process, Appendable output) {
//        def dumperOut = new TextDumper(process.getOutputStream(), process.getInputStream(), false, output)
//        def dumperErr = new TextDumper(process.getOutputStream(), process.getErrorStream(), true, output)
//
//        Thread tout = new Thread(dumperOut)
//        Thread terr = new Thread(dumperErr)
//
//        tout.start()
//        terr.start()
//        tout.join()
//        terr.join()
//
//        process.waitFor()
//        process.closeStreams()
//
//        if (dumperOut.exception) {
//            throw dumperOut.exception
//        }
//        if (dumperErr.exception) {
//            throw dumperErr.exception
//        }
//
//    }
//
//    private class TextDumper implements Runnable {
//        InputStream input
//        OutputStream output
//        boolean catchError
//        Appendable app
//
//        Exception exception
//
//        TextDumper(OutputStream outputStream, InputStream inputStream, boolean catchError, Appendable app) {
//            this.input = inputStream
//            this.output = outputStream
//            this.catchError = catchError
//            this.app = app
//        }
//
//        void run() {
//            BufferedReader br = new BufferedReader(new InputStreamReader(this.input))
//            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(this.output))
//
//            try {
//                String next
//                while ((next = br.readLine()) != null) {
//                    if (next.contains("?")) {
//                        writer.write("Yes")
//                        writer.newLine()
//                        writer.flush()
//                    }
//                    if (this.app != null) {
//                        this.app.append(next)
//                        this.app.append("\n")
//                    }
//                    if (catchError) {
//                        if (options['failOnStderr'] as boolean) {
//                            if (options['failOnStderrMessage'] != null) {
//                                throw new Exception(options['failOnStderrMessage'] as String)
//                            }
//                            throw new Exception("Running '${commands.join(' ')}' produced an error: ${next}")
//                        } else {
//                            if (warning)
//                                deliveryLogger.logWarning(next)
//                            else
//                                deliveryLogger.logOutput(next)
//                        }
//                    } else {
//                        if (warning)
//                            deliveryLogger.logWarning(next)
//                        else
//                            deliveryLogger.logOutput(next)
//                    }
//
//                    if (options['errorPatterns'] && [next]*.toString().any { String s ->
//                        (options['errorPatterns'] as List<String>).any {
//                            s.contains(it)
//                        }
//                    }) {
//                        throw new GradleException(options['errorMessage'] ? options['errorMessage'] as String : "Failed to run '${commands.join(' ')}' - $next")
//                    }
//                }
//            } catch (IOException var5) {
//                throw new GroovyRuntimeException("exception while reading process stream", var5)
//            } catch (GradleException ex) {
//                exception = ex
//            }
//        }
//    }
//}
