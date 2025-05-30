package com.game.util;

import java.io.IOException;
import java.util.logging.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogManager {
    private static final Logger LOGGER = Logger.getLogger(LogManager.class.getName());
    private static FileHandler fileHandler;
    private static ConsoleHandler consoleHandler;
    private static boolean isInitialized = false;

    private LogManager() {} // Private constructor to prevent instantiation

    public static synchronized void initialize() throws IOException {
        if (isInitialized) {
            LOGGER.warning("LogManager is already initialized");
            return;
        }

        try {
            setupLogDirectory();
            setupFileHandler();
            setupConsoleHandler();
            configureRootLogger();

            isInitialized = true;
            LOGGER.info("Logging system initialized successfully");
        } catch (IOException e) {
            LOGGER.severe("Failed to initialize logging system: " + e.getMessage());
            throw e; // Rethrow to let the application handle the error
        }
    }

    private static void setupLogDirectory() throws IOException {
        java.nio.file.Files.createDirectories(java.nio.file.Paths.get("logs"));
    }

    private static void setupFileHandler() throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        fileHandler = new FileHandler("logs/game_" + timestamp + ".log");
        fileHandler.setFormatter(new SimpleFormatter() {
            @Override
            public String format(LogRecord record) {
                return String.format("[%1$tF %1$tT] [%2$s] %3$s: %4$s%n",
                        record.getMillis(),
                        record.getLevel(),
                        record.getLoggerName(),
                        record.getMessage());
            }
        });
    }

    private static void setupConsoleHandler() {
        consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter() {
            @Override
            public String format(LogRecord record) {
                return String.format("%s%n", record.getMessage());
            }
        });
    }

    private static void configureRootLogger() {
        Logger rootLogger = Logger.getLogger("");

        // Remove default handlers
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        // Add our custom handlers
        rootLogger.addHandler(fileHandler);
        rootLogger.addHandler(consoleHandler);
        rootLogger.setLevel(Level.INFO);
    }

    public static Logger getLogger(String name) {
        if (!isInitialized) {
            throw new IllegalStateException("LogManager must be initialized before getting a logger");
        }
        return Logger.getLogger(name);
    }

    public static void setLogLevel(Level level) {
        if (!isInitialized) {
            throw new IllegalStateException("LogManager must be initialized before setting log level");
        }
        Logger.getLogger("").setLevel(level);
    }
}