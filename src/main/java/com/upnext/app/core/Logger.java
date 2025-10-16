package com.upnext.app.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple logger utility for the UpNext application.
 * Logs messages to both console and a log file, with timestamp and severity level.
 */
public final class Logger {
    // Singleton instance
    private static Logger instance;
    
    // Log levels
    public enum Level {
        INFO("INFO"),
        WARNING("WARNING"),
        ERROR("ERROR"),
        DEBUG("DEBUG");
        
        private final String label;
        
        Level(String label) {
            this.label = label;
        }
        
        @Override
        public String toString() {
            return label;
        }
    }
    
    // Log file path
    private static final String LOG_FILE_PATH = "logs/upnext.log";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private PrintWriter fileWriter;
    private boolean consoleOutput;
    
    /**
     * Private constructor to initialize the logger.
     */
    private Logger() {
        this.consoleOutput = true;
        initializeLogFile();
    }
    
    /**
     * Initializes the log file, creating directories if needed.
     */
    private void initializeLogFile() {
        try {
            File logFile = new File(LOG_FILE_PATH);
            File parentDir = logFile.getParentFile();
            
            // Create log directory if it doesn't exist
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            // Append to existing file or create a new one
            this.fileWriter = new PrintWriter(new FileWriter(logFile, true), true);
            log(Level.INFO, "Logger initialized");
        } catch (IOException e) {
            System.err.println("Failed to initialize log file: " + e.getMessage());
        }
    }
    
    /**
     * Gets the singleton instance of the logger.
     * 
     * @return The logger instance
     */
    public static synchronized Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }
    
    /**
     * Logs a message with the specified level.
     * 
     * @param level The severity level
     * @param message The message to log
     */
    public void log(Level level, String message) {
        String timestamp = DATE_FORMAT.format(new Date());
        String formattedMessage = String.format("[%s] [%s] %s", timestamp, level, message);
        
        // Log to console if enabled
        if (consoleOutput) {
            if (level == Level.ERROR) {
                System.err.println(formattedMessage);
            } else {
                System.out.println(formattedMessage);
            }
        }
        
        // Log to file if writer is available
        if (fileWriter != null) {
            fileWriter.println(formattedMessage);
            fileWriter.flush();
        }
    }
    
    /**
     * Logs an exception with ERROR level.
     * 
     * @param message A message describing the context of the exception
     * @param exception The exception to log
     */
    public void logException(String message, Throwable exception) {
        log(Level.ERROR, message + ": " + exception.getClass().getName() + " - " + exception.getMessage());
        
        // Print stack trace to file
        if (fileWriter != null) {
            exception.printStackTrace(fileWriter);
            fileWriter.flush();
        }
        
        // Print stack trace to console if enabled
        if (consoleOutput) {
            // Use System.err.println for stack trace to avoid lint warnings
            System.err.println("Stack trace for " + exception.getClass().getName() + ":");
            StackTraceElement[] stackTrace = exception.getStackTrace();
            for (StackTraceElement element : stackTrace) {
                System.err.println("\tat " + element);
            }
        }
    }
    
    /**
     * Logs an info message.
     * 
     * @param message The message to log
     */
    public void info(String message) {
        log(Level.INFO, message);
    }
    
    /**
     * Logs a warning message.
     * 
     * @param message The message to log
     */
    public void warning(String message) {
        log(Level.WARNING, message);
    }
    
    /**
     * Logs an error message.
     * 
     * @param message The message to log
     */
    public void error(String message) {
        log(Level.ERROR, message);
    }
    
    /**
     * Logs a debug message.
     * 
     * @param message The message to log
     */
    public void debug(String message) {
        log(Level.DEBUG, message);
    }
    
    /**
     * Enables or disables console output.
     * 
     * @param enabled True to enable console output, false to disable
     */
    public void setConsoleOutput(boolean enabled) {
        this.consoleOutput = enabled;
    }
    
    /**
     * Closes the logger resources.
     * Should be called when the application is shutting down.
     */
    public void close() {
        if (fileWriter != null) {
            fileWriter.close();
        }
    }
}