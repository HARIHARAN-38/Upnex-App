package com.upnext.app.ui.components;

import com.upnext.app.core.Logger;
import java.awt.Component;
import javax.swing.JOptionPane;

/**
 * Provides consistent feedback to users through dialogs and logging.
 * This utility class ensures uniform styling and behavior for all user-facing messages.
 */
public final class FeedbackManager {
    // Message types
    public enum MessageType {
        SUCCESS,
        INFO,
        WARNING,
        ERROR
    }
    
    private static final Logger logger = Logger.getInstance();
    
    private FeedbackManager() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Shows a message dialog with the appropriate styling and logs the message.
     * 
     * @param parentComponent The parent component for the dialog
     * @param message The message to display
     * @param title The dialog title
     * @param type The message type (SUCCESS, INFO, WARNING, ERROR)
     */
    public static void showMessage(Component parentComponent, String message, String title, MessageType type) {
        // Log the message with appropriate level
        switch (type) {
            case SUCCESS, INFO -> logger.info(title + ": " + message);
            case WARNING -> logger.warning(title + ": " + message);
            case ERROR -> logger.error(title + ": " + message);
        }
        
        // Convert to JOptionPane message type
        int optionPaneType = switch (type) {
            case SUCCESS -> JOptionPane.INFORMATION_MESSAGE;
            case WARNING -> JOptionPane.WARNING_MESSAGE;
            case ERROR -> JOptionPane.ERROR_MESSAGE;
            case INFO -> JOptionPane.INFORMATION_MESSAGE;
        };
        
        // Show the dialog
        JOptionPane.showMessageDialog(parentComponent, message, title, optionPaneType);
    }
    
    /**
     * Shows a success message dialog.
     * 
     * @param parentComponent The parent component for the dialog
     * @param message The success message to display
     * @param title The dialog title
     */
    public static void showSuccess(Component parentComponent, String message, String title) {
        showMessage(parentComponent, message, title, MessageType.SUCCESS);
    }
    
    /**
     * Shows an information message dialog.
     * 
     * @param parentComponent The parent component for the dialog
     * @param message The information message to display
     * @param title The dialog title
     */
    public static void showInfo(Component parentComponent, String message, String title) {
        showMessage(parentComponent, message, title, MessageType.INFO);
    }
    
    /**
     * Shows a warning message dialog.
     * 
     * @param parentComponent The parent component for the dialog
     * @param message The warning message to display
     * @param title The dialog title
     */
    public static void showWarning(Component parentComponent, String message, String title) {
        showMessage(parentComponent, message, title, MessageType.WARNING);
    }
    
    /**
     * Shows an error message dialog and logs the error.
     * 
     * @param parentComponent The parent component for the dialog
     * @param message The error message to display
     * @param title The dialog title
     */
    public static void showError(Component parentComponent, String message, String title) {
        showMessage(parentComponent, message, title, MessageType.ERROR);
    }
    
    /**
     * Shows an exception dialog and logs the exception.
     * 
     * @param parentComponent The parent component for the dialog
     * @param message A message describing the context of the exception
     * @param title The dialog title
     * @param exception The exception to display and log
     */
    public static void showException(Component parentComponent, String message, String title, Throwable exception) {
        // Log the exception
        logger.logException(title + ": " + message, exception);
        
        // Build a detailed message for the dialog
        String dialogMessage = message + "\n\n" + exception.getClass().getSimpleName() + ": " + exception.getMessage();
        
        // Show the dialog
        JOptionPane.showMessageDialog(parentComponent, dialogMessage, title, JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Shows a confirmation dialog and returns the user's response.
     * 
     * @param parentComponent The parent component for the dialog
     * @param message The message to display
     * @param title The dialog title
     * @return true if the user confirms, false otherwise
     */
    public static boolean showConfirmation(Component parentComponent, String message, String title) {
        int result = JOptionPane.showConfirmDialog(
            parentComponent,
            message,
            title,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        return result == JOptionPane.YES_OPTION;
    }
}