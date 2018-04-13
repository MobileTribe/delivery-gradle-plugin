package com.leroymerlin.plugins.cli

import java.util.logging.Level
import java.util.logging.Logger

class DeliveryLogger {

    Logger logger = Logger.getGlobal()

    private void logMessage(String message, Ansi color = null, Level logLevel = Level.INFO) {
        logger.log(logLevel, color != null ? color.colorize(message) : message)
    }

    void logError(String message) {
        logMessage(message, new Ansi(Ansi.HIGH_INTENSITY, Ansi.RED), Level.WARNING)
    }

    void logWarning(String message) {
        logMessage(message, new Ansi(Ansi.HIGH_INTENSITY, Ansi.YELLOW), Level.WARNING)
    }

    void logOutput(String message) {
        logMessage(message, Ansi.Green)
    }

    void logInfo(String message) {
        logMessage(message, Ansi.Blue)
    }
}
