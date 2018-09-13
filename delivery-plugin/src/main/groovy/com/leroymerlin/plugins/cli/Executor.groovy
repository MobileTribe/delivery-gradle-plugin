package com.leroymerlin.plugins.cli

import org.gradle.api.GradleException

/**
 * Created by alexandre on 31/01/2017.
 */

class Executor {

    public static DeliveryLogger deliveryLogger = new DeliveryLogger()
    public Map options
    public List<String> commands
    public boolean warning

    static String exec(List<String> commands, Map options = [:], boolean warning = false) {
        Executor executor = new Executor(commands, options, warning)
        executor.run()
    }

    static List<String> convertToCommandLine(String cmd) {
        StringTokenizer st = new StringTokenizer(cmd)
        List<String> cmdList = new ArrayList<>()
        for (int i = 0; st.hasMoreTokens(); i++)
            cmdList.add(st.nextToken())
        cmdList
    }

    private Executor(List<String> commands, Map options = [:], boolean warning = false){
        this.options = options
        this.commands = commands
        this.warning = warning
    }

    private String run(){
        StringBuffer out = new StringBuffer()
        File directory = options['directory'] ? options['directory'] as File : null
        List processEnv = options['env'] ? ((options['env'] as Map) << System.getenv()).collect {
            "$it.key=$it.value"
        } : null

        if (!(options['hideCommand'] as boolean)) {
            deliveryLogger.logInfo("Running $commands in [${directory != null ? directory : System.getProperty("os.name")}]")
        }
        try {
            Process process = commands.execute(processEnv, directory)
            waitForProcessOutput(process, out)
        } catch (Exception e) {
            if (options['failOnStderr'] as boolean && options['failOnStderrMessage'] != null) {
                throw new Exception(options['failOnStderrMessage'] as String)
            } else {
                e.printStackTrace()
            }
        }
        return out.toString()
    }



     private void waitForProcessOutput(Process process, Appendable output) {
        def dumperOut = new TextDumper(process.getOutputStream(), process.getInputStream(), false, output)
        def dumperErr = new TextDumper(process.getOutputStream(), process.getErrorStream(), true, output)

        Thread tout = new Thread(dumperOut)
        Thread terr = new Thread(dumperErr)

        tout.start()
        terr.start()
        tout.join()
        terr.join()

        process.waitFor()
        process.closeStreams()

        if (dumperOut.exception) {
            throw dumperOut.exception
        }
        if (dumperErr.exception) {
            throw dumperErr.exception
        }

    }

    private class TextDumper implements Runnable {
        InputStream input
        OutputStream output
        boolean catchError
        Appendable app

        Exception exception

        TextDumper(OutputStream outputStream, InputStream inputStream, boolean catchError, Appendable app) {
            this.input = inputStream
            this.output = outputStream
            this.catchError = catchError
            this.app = app
        }

        void run() {
            BufferedReader br = new BufferedReader(new InputStreamReader(this.input))
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(this.output))

            try {
                String next
                while ((next = br.readLine()) != null) {
                    if (next.contains("?")) {
                        writer.write("Yes")
                        writer.newLine()
                        writer.flush()
                    }
                    if (this.app != null) {
                        this.app.append(next)
                        this.app.append("\n")
                    }
                    if (catchError) {
                        if (options['failOnStderr'] as boolean) {
                            if (options['failOnStderrMessage'] != null) {
                                throw new Exception(options['failOnStderrMessage'] as String)
                            }
                            throw new Exception("Running '${commands.join(' ')}' produced an error: ${next}")
                        } else {
                            if (warning)
                                deliveryLogger.logWarning(next)
                            else
                                deliveryLogger.logOutput(next)
                        }
                    } else {
                        if (warning)
                            deliveryLogger.logWarning(next)
                        else
                            deliveryLogger.logOutput(next)
                    }

                    if (options['errorPatterns'] && [next]*.toString().any { String s ->
                        (options['errorPatterns'] as List<String>).any {
                            s.contains(it)
                        }
                    }) {
                        throw new GradleException(options['errorMessage'] ? options['errorMessage'] as String : "Failed to run '${commands.join(' ')}' - $next")
                    }
                }
            } catch (IOException var5) {
                throw new GroovyRuntimeException("exception while reading process stream", var5)
            } catch (GradleException ex) {
                exception = ex
            }
        }
    }
}
