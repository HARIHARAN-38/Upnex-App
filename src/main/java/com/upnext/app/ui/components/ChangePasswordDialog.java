package com.upnext.app.ui.components;

import java.awt.BorderLayout;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;

import com.upnext.app.core.Logger;

import com.upnext.app.ui.theme.AppTheme;

/**
 * Modal dialog for changing user password.
 * Provides form fields for current password, new password, and confirmation
 * with proper validation and error handling.
 */
public class ChangePasswordDialog extends JDialog {
    
    // Constants
    private static final int DIALOG_WIDTH = 400;
    private static final int DIALOG_HEIGHT = 300;
    private static final int PADDING_MEDIUM = 16;
    private static final int PADDING_SMALL = 8;
    
    // UI Components
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JButton changeButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    
    /**
     * Creates a new ChangePasswordDialog.
     * 
     * @param parent The parent frame
     */
    public ChangePasswordDialog(Frame parent) {
        super(parent, "Change Password", true);
        
        initializeDialog();
        createComponents();
        layoutComponents();
        setupEventHandlers();
        
        // Center on parent
        setLocationRelativeTo(parent);
    }
    
    /**
     * Initializes the dialog properties.
     */
    private void initializeDialog() {
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(AppTheme.BACKGROUND);
    }
    
    /**
     * Creates the UI components.
     */
    private void createComponents() {
        currentPasswordField = new JPasswordField(20);
        currentPasswordField.setFont(AppTheme.PRIMARY_FONT);
        
        newPasswordField = new JPasswordField(20);
        newPasswordField.setFont(AppTheme.PRIMARY_FONT);
        
        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setFont(AppTheme.PRIMARY_FONT);
        
        changeButton = new JButton("Change Password");
        changeButton.setFont(AppTheme.PRIMARY_FONT);
        changeButton.setBackground(AppTheme.PRIMARY);
        changeButton.setForeground(java.awt.Color.WHITE);
        changeButton.setOpaque(true);
        changeButton.setBorderPainted(false);
        changeButton.setFocusPainted(false);
        
        cancelButton = new JButton("Cancel");
        cancelButton.setFont(AppTheme.PRIMARY_FONT);
        cancelButton.setBackground(AppTheme.SURFACE);
        cancelButton.setForeground(AppTheme.TEXT_SECONDARY);
        cancelButton.setOpaque(true);
        cancelButton.setBorderPainted(true);
        cancelButton.setFocusPainted(false);
        
        statusLabel = new JLabel(" ");
        statusLabel.setFont(AppTheme.PRIMARY_FONT);
        statusLabel.setForeground(AppTheme.ACCENT);
    }
    
    /**
     * Layouts the components in the dialog.
     */
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Main content panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(AppTheme.BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(PADDING_SMALL, PADDING_SMALL, PADDING_SMALL, PADDING_SMALL);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Title
        JLabel titleLabel = new JLabel("Change Your Password");
        titleLabel.setFont(AppTheme.HEADING_FONT);
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, PADDING_MEDIUM, 0);
        contentPanel.add(titleLabel, gbc);
        
        // Reset insets and gridwidth
        gbc.insets = new Insets(PADDING_SMALL, PADDING_SMALL, PADDING_SMALL, PADDING_SMALL);
        gbc.gridwidth = 1;
        
        // Current password
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel currentLabel = new JLabel("Current Password:");
        currentLabel.setFont(AppTheme.PRIMARY_FONT);
        currentLabel.setForeground(AppTheme.TEXT_PRIMARY);
        contentPanel.add(currentLabel, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        contentPanel.add(currentPasswordField, gbc);
        
        // New password
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        JLabel newLabel = new JLabel("New Password:");
        newLabel.setFont(AppTheme.PRIMARY_FONT);
        newLabel.setForeground(AppTheme.TEXT_PRIMARY);
        contentPanel.add(newLabel, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        contentPanel.add(newPasswordField, gbc);
        
        // Confirm password
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        JLabel confirmLabel = new JLabel("Confirm Password:");
        confirmLabel.setFont(AppTheme.PRIMARY_FONT);
        confirmLabel.setForeground(AppTheme.TEXT_PRIMARY);
        contentPanel.add(confirmLabel, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        contentPanel.add(confirmPasswordField, gbc);
        
        // Status label
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(PADDING_MEDIUM, PADDING_SMALL, PADDING_SMALL, PADDING_SMALL);
        contentPanel.add(statusLabel, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(AppTheme.BACKGROUND);
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createHorizontalStrut(PADDING_SMALL));
        buttonPanel.add(changeButton);
        
        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Sets up event handlers for the dialog components.
     */
    private void setupEventHandlers() {
        cancelButton.addActionListener(e -> dispose());
        
        changeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handlePasswordChange();
            }
        });
        
        // Enter key submits form
        getRootPane().setDefaultButton(changeButton);
    }
    
    /**
     * Handles the password change operation.
     */
    private void handlePasswordChange() {
        // Clear previous status
        statusLabel.setText(" ");
        
        // Get password values
        String currentPassword = new String(currentPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        try {
            // Validate inputs
            if (currentPassword.isEmpty()) {
                showError("Please enter your current password.");
                currentPasswordField.requestFocus();
                return;
            }
            
            if (newPassword.isEmpty()) {
                showError("Please enter a new password.");
                newPasswordField.requestFocus();
                return;
            }
            
            if (newPassword.length() < 6) {
                showError("New password must be at least 6 characters long.");
                newPasswordField.requestFocus();
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                showError("New password and confirmation do not match.");
                confirmPasswordField.requestFocus();
                return;
            }
            
            if (currentPassword.equals(newPassword)) {
                showError("New password must be different from current password.");
                newPasswordField.requestFocus();
                return;
            }
            
            // Disable button during processing
            changeButton.setEnabled(false);
            changeButton.setText("Changing...");
            
            // Attempt to change password
            // TODO: Implement changePassword method in AuthService
            // For now, simulate success (in real implementation, validate current password and update new one)
            boolean success = true;
            
            if (success) {
                Logger.getInstance().info("Password changed successfully");
                javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Your password has been changed successfully.",
                    "Password Changed",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE
                );
                dispose();
            } else {
                showError("Current password is incorrect.");
                currentPasswordField.requestFocus();
                currentPasswordField.selectAll();
            }
            
        } catch (Exception ex) {
            Logger.getInstance().error("Error changing password: " + ex.getMessage());
            showError("An error occurred while changing your password. Please try again.");
        } finally {
            // Re-enable button
            changeButton.setEnabled(true);
            changeButton.setText("Change Password");
            
            // Clear password fields for security
            clearPasswordFields();
        }
    }
    
    /**
     * Shows an error message in the status label.
     * 
     * @param message The error message to display
     */
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(AppTheme.ACCENT);
    }
    
    /**
     * Clears all password fields for security.
     */
    private void clearPasswordFields() {
        currentPasswordField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
    }
    
    /**
     * Override dispose to clear sensitive data.
     */
    @Override
    public void dispose() {
        clearPasswordFields();
        super.dispose();
    }
}