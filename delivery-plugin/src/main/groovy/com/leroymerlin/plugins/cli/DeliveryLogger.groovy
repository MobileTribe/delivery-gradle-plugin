package com.leroymerlin.plugins.cli

import com.leroymerlin.plugins.utils.SystemUtils

import java.util.logging.*

class DeliveryLogger {


    private static class DeliveryFormatter extends Formatter {

        @Override
        synchronized String format(LogRecord record) {
            String message = formatMessage(record)
            if (SystemUtils.getEnvProperty("ugly") == null) {
                return "$message\n"
            }
            return String.format('%1$s: %2$s\n',
                    record.getLevel().getLocalizedName(),
                    message)
        }
    }
    static Logger logger = Logger.getLogger("DeliveryLogger")

    static {
        Handler consoleHandler = new ConsoleHandler()
        def formatter = new DeliveryFormatter()
        consoleHandler.setFormatter(formatter)
        logger.setUseParentHandlers(false)
        if (logger.handlers.length == 0) logger.addHandler(consoleHandler)
    }

    private static void logMessage(String message, Ansi color = null, Level logLevel = Level.INFO) {
        logger.log(logLevel, color != null ? color.colorize(message) : message)
    }

    static void logError(String message) {
        logMessage(message, (SystemUtils.getEnvProperty("ugly") != null)
                ? null : new Ansi(Ansi.HIGH_INTENSITY, Ansi.RED), Level.SEVERE)
    }

    static void logWarning(String message) {
        logMessage(message, (SystemUtils.getEnvProperty("ugly") != null)
                ? null : new Ansi(Ansi.HIGH_INTENSITY, Ansi.YELLOW), Level.WARNING)
    }

    static void logOutput(String message) {
        logMessage(message, (SystemUtils.getEnvProperty("ugly") != null)
                ? null : Ansi.Green, Level.WARNING)
    }

    static void logInfo(String message) {
        logMessage(message, (SystemUtils.getEnvProperty("ugly") != null)
                ? null : Ansi.Cyan, Level.WARNING)
    }
}
