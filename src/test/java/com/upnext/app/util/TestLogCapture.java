package com.upnext.app.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

/**
 * Utility class for capturing and validating log messages in tests.
 * Provides methods to capture logs, verify specific markers, and analyze telemetry data.
 */
public class TestLogCapture {
    
    private final List<LogEntry> capturedLogs = new CopyOnWriteArrayList<>();
    private boolean isCapturing = false;
    
    /**
     * Starts capturing log messages.
     */
    public void startCapture() {
        capturedLogs.clear();
        isCapturing = true;
        
        // Note: This is a simplified approach. In a real implementation,
        // you would typically use a test-specific logger or mock the Logger class.
        // For now, we'll rely on manual verification in tests.
    }
    
    /**
     * Stops capturing log messages.
     */
    public void stopCapture() {
        isCapturing = false;
    }
    
    /**
     * Manually adds a log entry (for testing purposes).
     * 
     * @param level The log level (INFO, WARNING, ERROR)
     * @param message The log message
     */
    public void addLogEntry(String level, String message) {
        if (isCapturing) {
            capturedLogs.add(new LogEntry(level, message, System.currentTimeMillis()));
        }
    }
    
    /**
     * Clears all captured logs.
     */
    public void clear() {
        capturedLogs.clear();
    }
    
    /**
     * Gets all captured log entries.
     * 
     * @return List of captured log entries
     */
    public List<LogEntry> getAllLogs() {
        return new ArrayList<>(capturedLogs);
    }
    
    /**
     * Filters logs by level.
     * 
     * @param level The log level to filter by (INFO, WARNING, ERROR)
     * @return List of matching log entries
     */
    public List<LogEntry> getLogsByLevel(String level) {
        return capturedLogs.stream()
                .filter(entry -> level.equalsIgnoreCase(entry.getLevel()))
                .toList();
    }
    
    /**
     * Finds logs containing a specific marker pattern.
     * 
     * @param marker The marker pattern to search for (e.g., "[QUESTION_CREATE_SUCCESS]")
     * @return List of matching log entries
     */
    public List<LogEntry> getLogsByMarker(String marker) {
        return capturedLogs.stream()
                .filter(entry -> entry.getMessage().contains(marker))
                .toList();
    }
    
    /**
     * Finds logs matching a regex pattern.
     * 
     * @param pattern The regex pattern to match
     * @return List of matching log entries
     */
    public List<LogEntry> getLogsByPattern(String pattern) {
        Pattern regex = Pattern.compile(pattern);
        return capturedLogs.stream()
                .filter(entry -> regex.matcher(entry.getMessage()).find())
                .toList();
    }
    
    /**
     * Verifies that a specific telemetry marker exists in the logs.
     * 
     * @param marker The marker to verify (e.g., "[QUESTION_CREATE_START]")
     * @return true if the marker exists, false otherwise
     */
    public boolean hasMarker(String marker) {
        return capturedLogs.stream()
                .anyMatch(entry -> entry.getMessage().contains(marker));
    }
    
    /**
     * Verifies that a complete operation flow exists in the logs.
     * Checks for START, SUCCESS, and optionally FAILED markers for an operation.
     * 
     * @param operation The operation name (e.g., "QUESTION_CREATE")
     * @return TelemetryValidationResult with detailed information
     */
    public TelemetryValidationResult validateOperationFlow(String operation) {
        String startMarker = "[" + operation + "_START]";
        String successMarker = "[" + operation + "_SUCCESS]";
        
        boolean hasStart = hasMarker(startMarker);
        boolean hasSuccess = hasMarker(successMarker);
        
        // Check for any failure marker (AUTH_FAILED, VALIDATION_FAILED, FAILED, etc.)
        boolean hasFailed = capturedLogs.stream()
                .anyMatch(entry -> entry.getMessage().contains("[" + operation + "_") && 
                                   entry.getMessage().contains("FAILED]"));
        
        List<LogEntry> operationLogs = getLogsByPattern("\\[" + operation + "_[A-Z_]+\\]");
        
        return new TelemetryValidationResult(
            operation,
            hasStart,
            hasSuccess,
            hasFailed,
            operationLogs
        );
    }
    
    /**
     * Validates UI action telemetry markers.
     * 
     * @param action The UI action (e.g., "TAG_ADD", "FORM_VALIDATION")
     * @return TelemetryValidationResult with UI-specific validation
     */
    public TelemetryValidationResult validateUIActionFlow(String action) {
        String uiMarker = "[UI_" + action + "_";
        
        List<LogEntry> uiLogs = capturedLogs.stream()
                .filter(entry -> entry.getMessage().contains(uiMarker))
                .toList();
        
        boolean hasUIAction = !uiLogs.isEmpty();
        boolean hasSuccess = uiLogs.stream()
                .anyMatch(entry -> entry.getMessage().contains("_SUCCESS]"));
        boolean hasFailed = uiLogs.stream()
                .anyMatch(entry -> entry.getMessage().contains("_FAILED]"));
        
        return new TelemetryValidationResult(
            "UI_" + action,
            hasUIAction,
            hasSuccess,
            hasFailed,
            uiLogs
        );
    }
    
    /**
     * Validates user metrics telemetry markers.
     * 
     * @return TelemetryValidationResult for user metrics operations
     */
    public TelemetryValidationResult validateUserMetricsFlow() {
        String metricsMarker = "[USER_METRICS_UPDATE";
        
        List<LogEntry> metricsLogs = capturedLogs.stream()
                .filter(entry -> entry.getMessage().contains(metricsMarker))
                .toList();
        
        boolean hasMetricsUpdate = !metricsLogs.isEmpty();
        boolean hasSuccess = metricsLogs.stream()
                .anyMatch(entry -> entry.getMessage().contains("_SUCCESS]"));
        boolean hasFailed = metricsLogs.stream()
                .anyMatch(entry -> entry.getMessage().contains("_FAILED]"));
        
        return new TelemetryValidationResult(
            "USER_METRICS_UPDATE",
            hasMetricsUpdate,
            hasSuccess,
            hasFailed,
            metricsLogs
        );
    }
    
    /**
     * Gets a count of logs at each level.
     * 
     * @return LogLevelSummary with counts by level
     */
    public LogLevelSummary getLogSummary() {
        int infoCount = (int) capturedLogs.stream().filter(e -> "INFO".equalsIgnoreCase(e.getLevel())).count();
        int warningCount = (int) capturedLogs.stream().filter(e -> "WARNING".equalsIgnoreCase(e.getLevel())).count();
        int errorCount = (int) capturedLogs.stream().filter(e -> "ERROR".equalsIgnoreCase(e.getLevel())).count();
        
        return new LogLevelSummary(infoCount, warningCount, errorCount);
    }
    
    /**
     * Represents a captured log entry.
     */
    public static class LogEntry {
        private final String level;
        private final String message;
        private final long timestamp;
        
        public LogEntry(String level, String message, long timestamp) {
            this.level = level;
            this.message = message;
            this.timestamp = timestamp;
        }
        
        public String getLevel() {
            return level;
        }
        
        public String getMessage() {
            return message;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        @Override
        public String toString() {
            return String.format("[%s] %s (timestamp: %d)", level, message, timestamp);
        }
    }
    
    /**
     * Result of telemetry validation for an operation.
     */
    public static class TelemetryValidationResult {
        private final String operation;
        private final boolean hasStart;
        private final boolean hasSuccess;
        private final boolean hasFailed;
        private final List<LogEntry> operationLogs;
        
        public TelemetryValidationResult(String operation, boolean hasStart, boolean hasSuccess, 
                                       boolean hasFailed, List<LogEntry> operationLogs) {
            this.operation = operation;
            this.hasStart = hasStart;
            this.hasSuccess = hasSuccess;
            this.hasFailed = hasFailed;
            this.operationLogs = new ArrayList<>(operationLogs);
        }
        
        public String getOperation() {
            return operation;
        }
        
        public boolean hasStart() {
            return hasStart;
        }
        
        public boolean hasSuccess() {
            return hasSuccess;
        }
        
        public boolean hasFailed() {
            return hasFailed;
        }
        
        public List<LogEntry> getOperationLogs() {
            return new ArrayList<>(operationLogs);
        }
        
        public boolean isCompleteFlow() {
            return hasStart && (hasSuccess || hasFailed);
        }
        
        public boolean isSuccessfulFlow() {
            return hasStart && hasSuccess && !hasFailed;
        }
        
        @Override
        public String toString() {
            return String.format("TelemetryValidation[%s]: start=%s, success=%s, failed=%s, logs=%d",
                operation, hasStart, hasSuccess, hasFailed, operationLogs.size());
        }
    }
    
    /**
     * Summary of log levels.
     */
    public static class LogLevelSummary {
        private final int infoCount;
        private final int warningCount;
        private final int errorCount;
        
        public LogLevelSummary(int infoCount, int warningCount, int errorCount) {
            this.infoCount = infoCount;
            this.warningCount = warningCount;
            this.errorCount = errorCount;
        }
        
        public int getInfoCount() {
            return infoCount;
        }
        
        public int getWarningCount() {
            return warningCount;
        }
        
        public int getErrorCount() {
            return errorCount;
        }
        
        public int getTotalCount() {
            return infoCount + warningCount + errorCount;
        }
        
        @Override
        public String toString() {
            return String.format("LogSummary: INFO=%d, WARNING=%d, ERROR=%d, Total=%d",
                infoCount, warningCount, errorCount, getTotalCount());
        }
    }
}