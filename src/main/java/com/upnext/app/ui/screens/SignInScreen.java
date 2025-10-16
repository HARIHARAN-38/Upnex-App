package com.upnext.app.ui.screens;

import com.upnext.app.ui.components.forms.*;
import com.upnext.app.ui.theme.AppTheme;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Sign in screen for the UpNext application.
 * Allows users to authenticate with their credentials.
 */
public class SignInScreen extends JPanel {
    private final FormField emailField;
    private final PasswordField passwordField;
    private final SubmitButton signInButton;
    private final JButton createAccountLink;
    
    /**
     * Creates a new sign in screen.
     */
    public SignInScreen() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BACKGROUND);
        setBorder(new EmptyBorder(40, 40, 40, 40));
        
        // Header
        JLabel titleLabel = new JLabel("Sign In to UpNext");
        titleLabel.setFont(AppTheme.HEADING_FONT.deriveFont(24f));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 30, 0));
        
        // Form fields
        FormPanel formPanel = new FormPanel();
        
        emailField = new FormField("Email");
        passwordField = new PasswordField("Password");
        signInButton = new SubmitButton("Sign In");
        
        formPanel.addField(emailField);
        formPanel.addField(passwordField);
        formPanel.addSubmitButton(signInButton);
        
        // Footer with create account link
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(AppTheme.BACKGROUND);
        footerPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        JLabel noAccountLabel = new JLabel("Don't have an account?");
        noAccountLabel.setForeground(AppTheme.TEXT_SECONDARY);
        
        createAccountLink = new JButton("Create Account");
        createAccountLink.setBorderPainted(false);
        createAccountLink.setContentAreaFilled(false);
        createAccountLink.setForeground(AppTheme.PRIMARY);
        createAccountLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createAccountLink.setFont(AppTheme.PRIMARY_FONT);
        
        footerPanel.add(noAccountLabel);
        footerPanel.add(createAccountLink);
        
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
     * Gets the sign in button.
     *
     * @return The sign in button
     */
    public SubmitButton getSignInButton() {
        return signInButton;
    }

    /**
     * Gets the create account link button.
     *
     * @return The create account button
     */
    public JButton getCreateAccountLink() {
        return createAccountLink;
    }
}