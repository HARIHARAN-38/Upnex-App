package com.upnext.app.ui.components.questions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.upnext.app.ui.theme.AppTheme;

/**
 * Enhanced tag input field component that works with ViewModel for better
 * tag entry control including lowercase normalization, duplicate prevention,
 * limit enforcement, and proper validation feedback.
 */
public class TagInputField extends JPanel {
    private static final int PREFERRED_HEIGHT = 36;
    private static final String PLACEHOLDER_TEXT = "Type to add tags...";
    
    // UI Components
    private final JTextField inputField;
    
    // Callback for tag addition attempts
    private final Function<String, Boolean> onTagAddAttempt;
    
    /**
     * Creates a new TagInputField with the specified callback for tag addition attempts.
     * The callback should return true if the tag was successfully added, false otherwise.
     *
     * @param onTagAddAttempt Callback function called when a user attempts to add a tag
     */
    public TagInputField(Function<String, Boolean> onTagAddAttempt) {
        this.onTagAddAttempt = onTagAddAttempt;
        
        // Setup layout
        setLayout(new BorderLayout());
        setOpaque(false);
        
        // Create input field
        inputField = createInputField();
        
        // Add components
        add(inputField, BorderLayout.CENTER);
        
        // Apply styling
        applyThemeStyles();
    }
    
    /**
     * Creates and configures the input text field.
     */
    private JTextField createInputField() {
        JTextField field = new JTextField();
        field.setFont(AppTheme.PRIMARY_FONT);
        field.setPreferredSize(new Dimension(0, PREFERRED_HEIGHT));
        
        // Add placeholder behavior
        setPlaceholderText(field, PLACEHOLDER_TEXT);
        
        // Add key listener for Enter key
        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_COMMA) {
                    e.consume();
                    handleTagInput();
                }
            }
        });
        
        // Add focus listeners for placeholder behavior
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(PLACEHOLDER_TEXT)) {
                    field.setText("");
                    field.setForeground(AppTheme.TEXT_PRIMARY);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    setPlaceholderText(field, PLACEHOLDER_TEXT);
                } else {
                    handleTagInput();
                }
            }
        });
        
        return field;
    }
    
    /**
     * Sets placeholder text with appropriate styling.
     */
    private void setPlaceholderText(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(AppTheme.TEXT_SECONDARY);
    }
    
    /**
     * Applies AppTheme styling to the component.
     */
    private void applyThemeStyles() {
        // Style the input field
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xD1D5DB), 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        inputField.setBackground(Color.WHITE);
        
        // Add hover and focus effects
        inputField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                inputField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppTheme.PRIMARY, 2),
                    new EmptyBorder(7, 11, 7, 11) // Adjust padding for thicker border
                ));
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                inputField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xD1D5DB), 1),
                    new EmptyBorder(8, 12, 8, 12)
                ));
            }
        });
    }
    
    /**
     * Handles tag input when Enter is pressed or other tag addition events.
     */
    private void handleTagInput() {
        String input = inputField.getText().trim();
        
        // Skip if empty or is placeholder text
        if (input.isEmpty() || input.equals(PLACEHOLDER_TEXT)) {
            return;
        }
        
        // Call the callback to attempt tag addition
        if (onTagAddAttempt != null) {
            boolean success = onTagAddAttempt.apply(input);
            
            // Clear the input field only if tag was successfully added
            if (success) {
                inputField.setText("");
                // Reset placeholder if needed
                if (inputField.getText().trim().isEmpty()) {
                    setPlaceholderText(inputField, PLACEHOLDER_TEXT);
                }
            }
        }
    }
    
    /**
     * Sets focus to the input field.
     */
    @Override
    public void requestFocus() {
        inputField.requestFocus();
    }
    
    /**
     * Gets the underlying text field component.
     *
     * @return The JTextField component
     */
    public JTextField getTextField() {
        return inputField;
    }
    
    /**
     * Gets the current text in the input field.
     *
     * @return The current text, or empty string if placeholder is shown
     */
    public String getText() {
        String text = inputField.getText();
        return PLACEHOLDER_TEXT.equals(text) ? "" : text;
    }
    
    /**
     * Sets the text in the input field.
     *
     * @param text The text to set
     */
    public void setText(String text) {
        if (text == null || text.trim().isEmpty()) {
            setPlaceholderText(inputField, PLACEHOLDER_TEXT);
        } else {
            inputField.setText(text);
            inputField.setForeground(AppTheme.TEXT_PRIMARY);
        }
    }
    
    /**
     * Clears the input field.
     */
    public void clear() {
        setText("");
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        inputField.setEnabled(enabled);
        inputField.setBackground(enabled ? Color.WHITE : new Color(0xF3F4F6));
    }
}