package com.upnext.app.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.upnext.app.core.Logger;
import com.upnext.app.service.AuthService;
import com.upnext.app.ui.components.FeedbackManager;
import com.upnext.app.ui.theme.AppTheme;

/**
 * Dialog for confirming account deletion with password verification.
 * This dialog requires the user to enter their current password before
 * allowing account deletion to proceed.
 */
public class DeleteAccountDialog extends JDialog {
    
    private JPasswordField passwordField;
    private JButton deleteButton;
    private JButton cancelButton;
    private JLabel warningLabel;
    private JLabel instructionLabel;
    
    private boolean confirmed = false;
    private String enteredPassword = null;
    
    private static final Logger logger = Logger.getInstance();
    
    /**
     * Creates a new DeleteAccountDialog.
     * 
     * @param parent The parent frame
     */
    public DeleteAccountDialog(Frame parent) {
        super(parent, "Delete Account", true);
        initializeDialog();
        setupComponents();
        setupEventHandlers();
        setupKeyBindings();
    }
    
    /**
     * Initializes the dialog properties.
     */
    private void initializeDialog() {
        setSize(450, 350);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // Set dialog background
        getContentPane().setBackground(AppTheme.SURFACE);
    }
    
    /**
     * Sets up the dialog components.
     */
    private void setupComponents() {
        setLayout(new BorderLayout());
        
        // Create main content panel
        JPanel contentPanel = createContentPanel();
        add(contentPanel, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates the main content panel.
     * 
     * @return The content panel
     */
    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppTheme.SURFACE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Warning icon and title
        JLabel titleLabel = new JLabel("⚠️ Delete Account");
        titleLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(18f));
        titleLabel.setForeground(AppTheme.ACCENT);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 15, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(titleLabel, gbc);
        
        // Warning message
        warningLabel = new JLabel("<html><div style='text-align: center; width: 350px;'>" +
                "This action cannot be undone. Deleting your account will permanently remove:<br><br>" +
                "• All your questions and answers<br>" +
                "• Your skills and profile information<br>" +
                "• Your voting history<br>" +
                "• All associated account data" +
                "</div></html>");
        warningLabel.setFont(AppTheme.PRIMARY_FONT);
        warningLabel.setForeground(AppTheme.TEXT_PRIMARY);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(warningLabel, gbc);
        
        // Instruction label
        instructionLabel = new JLabel("Enter your current password to confirm deletion:");
        instructionLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(java.awt.Font.BOLD, 14f));
        instructionLabel.setForeground(AppTheme.TEXT_PRIMARY);
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 0, 8, 0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0; // Reset weight for label
        panel.add(instructionLabel, gbc);
        
        // Password field label
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(java.awt.Font.BOLD, 12f));
        passwordLabel.setForeground(AppTheme.TEXT_SECONDARY);
        gbc.gridy = 3;
        gbc.insets = new Insets(15, 0, 5, 0);
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(passwordLabel, gbc);
        
        // Password field
        passwordField = new JPasswordField();
        passwordField.setFont(AppTheme.PRIMARY_FONT.deriveFont(16f)); // Larger font for better visibility
        passwordField.setPreferredSize(new Dimension(350, 40));
        passwordField.setMinimumSize(new Dimension(350, 40));
        passwordField.setBackground(Color.WHITE);
        passwordField.setForeground(Color.BLACK); // Explicit black color for text
        passwordField.setCaretColor(Color.BLACK); // Black caret
        passwordField.setSelectionColor(new Color(59, 130, 246, 50)); // Light blue selection with alpha
        passwordField.setSelectedTextColor(Color.BLACK);
        passwordField.setEchoChar('●'); // Use filled circle for better visibility
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x3B82F6), 2), // Blue border for focus
            new EmptyBorder(8, 15, 8, 15) // Padding for better appearance
        ));
        passwordField.setOpaque(true); // Ensure the field is opaque
        passwordField.setToolTipText("Enter your account password to confirm deletion");
        
        // Add focus listeners to change border color
        passwordField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0x10B981), 2), // Green border when focused
                    new EmptyBorder(8, 15, 8, 15)
                ));
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0x3B82F6), 2), // Blue border when not focused
                    new EmptyBorder(8, 15, 8, 15)
                ));
            }
        });
        
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; // Allow field to expand horizontally
        panel.add(passwordField, gbc);
        
        return panel;
    }
    
    /**
     * Creates the button panel.
     * 
     * @return The button panel
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(AppTheme.SURFACE);
        panel.setBorder(new EmptyBorder(15, 20, 20, 20));
        
        // Cancel button
        cancelButton = new JButton("Cancel");
        cancelButton.setFont(AppTheme.PRIMARY_FONT);
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.setBackground(AppTheme.SURFACE);
        cancelButton.setForeground(AppTheme.TEXT_SECONDARY);
        cancelButton.setBorder(BorderFactory.createLineBorder(new Color(0xE2E8F0), 1));
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.setFocusPainted(false);
        
        // Delete button
        deleteButton = new JButton("Delete Account");
        deleteButton.setFont(AppTheme.PRIMARY_FONT);
        deleteButton.setPreferredSize(new Dimension(130, 35));
        deleteButton.setBackground(AppTheme.ACCENT);
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setBorder(BorderFactory.createLineBorder(AppTheme.ACCENT, 1));
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteButton.setFocusPainted(false);
        
        panel.add(cancelButton);
        panel.add(deleteButton);
        
        return panel;
    }
    
    /**
     * Sets up event handlers for buttons and fields.
     */
    private void setupEventHandlers() {
        // Cancel button
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
        
        // Delete button
        deleteButton.addActionListener(e -> handleDeleteAccount());
        
        // Password field - Enter key submits
        passwordField.addActionListener(e -> handleDeleteAccount());
        
        // Focus password field when dialog becomes visible
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                SwingUtilities.invokeLater(() -> passwordField.requestFocusInWindow());
            }
        });
    }
    
    /**
     * Sets up keyboard shortcuts.
     */
    private void setupKeyBindings() {
        // Escape key cancels
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        getRootPane().getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmed = false;
                dispose();
            }
        });
        
        // Enter key submits (when not in password field)
        getRootPane().setDefaultButton(deleteButton);
    }
    
    /**
     * Handles the delete account action.
     */
    private void handleDeleteAccount() {
        char[] passwordChars = passwordField.getPassword();
        
        if (passwordChars.length == 0) {
            FeedbackManager.showWarning(
                this,
                "Please enter your password to confirm account deletion.",
                "Password Required"
            );
            passwordField.requestFocusInWindow();
            return;
        }
        
        enteredPassword = new String(passwordChars);
        
        // Clear password from memory
        java.util.Arrays.fill(passwordChars, ' ');
        
        // Verify password with AuthService
        try {
            AuthService authService = AuthService.getInstance();
            if (authService.getCurrentUser() == null) {
                FeedbackManager.showError(
                    this,
                    "No user is currently signed in.",
                    "Authentication Error"
                );
                dispose();
                return;
            }
            
            String userEmail = authService.getCurrentUser().getEmail();
            
            // Try to sign in with the provided password to verify it's correct
            AuthService tempAuthService = AuthService.getInstance();
            try {
                // Create a temporary sign-in attempt to verify password
                tempAuthService.signIn(userEmail, enteredPassword);
                
                // If we get here, password is correct
                confirmed = true;
                logger.info("Password verification successful for account deletion");
                dispose();
                
            } catch (AuthService.AuthException e) {
                // Password is incorrect
                FeedbackManager.showError(
                    this,
                    "Incorrect password. Please try again.",
                    "Authentication Failed"
                );
                passwordField.setText("");
                passwordField.requestFocusInWindow();
                
                // Clear the password from memory
                if (enteredPassword != null) {
                    enteredPassword = null;
                }
            }
            
        } catch (Exception e) {
            logger.logException("Error during password verification for account deletion", e);
            FeedbackManager.showError(
                this,
                "An error occurred while verifying your password. Please try again.",
                "Verification Error"
            );
            passwordField.setText("");
            passwordField.requestFocusInWindow();
        }
    }
    
    /**
     * Shows the dialog and returns whether the user confirmed the deletion.
     * 
     * @return true if the user confirmed with correct password, false otherwise
     */
    public boolean showDialog() {
        setVisible(true);
        return confirmed;
    }
    
    /**
     * Gets the entered password (only valid if confirmed is true).
     * 
     * @return The entered password, or null if not confirmed
     */
    public String getEnteredPassword() {
        return confirmed ? enteredPassword : null;
    }
    
    /**
     * Clears sensitive data from memory.
     */
    public void clearSensitiveData() {
        if (enteredPassword != null) {
            enteredPassword = null;
        }
        if (passwordField != null) {
            passwordField.setText("");
        }
    }
}