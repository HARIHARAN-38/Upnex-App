package com.upnext.app.ui.components.forms;

import com.upnext.app.ui.theme.AppTheme;
import java.awt.*;
import javax.swing.*;

/**
 * A reusable password field component that includes a label and a password field.
 */
public class PasswordField extends JPanel {
    private final JLabel label;
    private final JPasswordField passwordField;

    /**
     * Creates a new password field with the given label.
     *
     * @param labelText The text to display as the field's label
     * @param columns   The width of the password field in columns
     */
    public PasswordField(String labelText, int columns) {
        setLayout(new BorderLayout(5, 5));
        setOpaque(false);

        label = new JLabel(labelText);
        label.setForeground(AppTheme.TEXT_PRIMARY);
        
        passwordField = new JPasswordField(columns);
        passwordField.setMargin(new Insets(5, 5, 5, 5));
        
        add(label, BorderLayout.NORTH);
        add(passwordField, BorderLayout.CENTER);
    }

    /**
     * Creates a new password field with the given label and default width of 20 columns.
     *
     * @param labelText The text to display as the field's label
     */
    public PasswordField(String labelText) {
        this(labelText, 20);
    }

    /**
     * Gets the password currently entered in the field.
     *
     * @return The password as a char array
     */
    public char[] getPassword() {
        return passwordField.getPassword();
    }

    /**
     * Sets the password in the field.
     *
     * @param password The password to set in the field
     */
    public void setPassword(char[] password) {
        passwordField.setText(new String(password));
    }

    /**
     * Clears the password field.
     */
    public void clear() {
        passwordField.setText("");
    }

    /**
     * Gets the underlying JPasswordField component.
     *
     * @return The JPasswordField used in this password field
     */
    public JPasswordField getPasswordField() {
        return passwordField;
    }

    /**
     * Gets the underlying JLabel component.
     *
     * @return The JLabel used in this password field
     */
    public JLabel getLabel() {
        return label;
    }
}