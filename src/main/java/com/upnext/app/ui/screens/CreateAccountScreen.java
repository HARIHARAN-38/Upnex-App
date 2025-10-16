package com.upnext.app.ui.screens;

import com.upnext.app.ui.components.forms.*;
import com.upnext.app.ui.theme.AppTheme;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Create account screen for the UpNext application.
 * Allows new users to register by providing their details.
 */
public class CreateAccountScreen extends JPanel {
    private final FormField nameField;
    private final FormField emailField;
    private final PasswordField passwordField;
    private final PasswordField confirmPasswordField;
    private final SubmitButton createAccountButton;
    private final JButton signInLink;
    
    /**
     * Creates a new account creation screen.
     */
    public CreateAccountScreen() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BACKGROUND);
        setBorder(new EmptyBorder(40, 40, 40, 40));
        
        // Header
        JLabel titleLabel = new JLabel("Create an Account");
        titleLabel.setFont(AppTheme.HEADING_FONT.deriveFont(24f));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 30, 0));
        
        // Form fields
        FormPanel formPanel = new FormPanel();
        
        nameField = new FormField("Full Name");
        emailField = new FormField("Email");
        passwordField = new PasswordField("Password");
        confirmPasswordField = new PasswordField("Confirm Password");
        createAccountButton = new SubmitButton("Create Account");
        
        formPanel.addField(nameField);
        formPanel.addField(emailField);
        formPanel.addField(passwordField);
        formPanel.addField(confirmPasswordField);
        formPanel.addSubmitButton(createAccountButton);
        
        // Footer with sign in link
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(AppTheme.BACKGROUND);
        footerPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        JLabel haveAccountLabel = new JLabel("Already have an account?");
        haveAccountLabel.setForeground(AppTheme.TEXT_SECONDARY);
        
        signInLink = new JButton("Sign In");
        signInLink.setBorderPainted(false);
        signInLink.setContentAreaFilled(false);
        signInLink.setForeground(AppTheme.PRIMARY);
        signInLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signInLink.setFont(AppTheme.PRIMARY_FONT);
        
        footerPanel.add(haveAccountLabel);
        footerPanel.add(signInLink);
        
        // Main content panel to center the form
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.add(footerPanel, BorderLayout.SOUTH);
        
        // Add to panel with constraints to center it
        JPanel centeringPanel = new JPanel(new GridBagLayout());
        centeringPanel.setBackground(AppTheme.BACKGROUND);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        
        centeringPanel.add(contentPanel, gbc);
        add(centeringPanel, BorderLayout.CENTER);
    }

    /**
     * Gets the name field component.
     *
     * @return The name field
     */
    public FormField getNameField() {
        return nameField;
    }

    /**
     * Gets the email field component.
     *
     * @return The email field
     */
    public FormField getEmailField() {
        return emailField;
    }

    /**
     * Gets the password field component.
     *
     * @return The password field
     */
    public PasswordField getPasswordField() {
        return passwordField;
    }

    /**
     * Gets the confirm password field component.
     *
     * @return The confirm password field
     */
    public PasswordField getConfirmPasswordField() {
        return confirmPasswordField;
    }

    /**
     * Gets the create account button.
     *
     * @return The create account button
     */
    public SubmitButton getCreateAccountButton() {
        return createAccountButton;
    }

    /**
     * Gets the sign in link button.
     *
     * @return The sign in button
     */
    public JButton getSignInLink() {
        return signInLink;
    }
}