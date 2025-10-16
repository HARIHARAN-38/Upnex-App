package com.upnext.app.ui.components.forms;

import com.upnext.app.ui.theme.AppTheme;
import java.awt.*;
import javax.swing.*;

/**
 * A reusable form field component that includes a label and a text field.
 */
public class FormField extends JPanel {
    private final JLabel label;
    private final JTextField textField;

    /**
     * Creates a new form field with the given label.
     *
     * @param labelText The text to display as the field's label
     * @param columns   The width of the text field in columns
     */
    public FormField(String labelText, int columns) {
        setLayout(new BorderLayout(5, 5));
        setOpaque(false);

        label = new JLabel(labelText);
        label.setForeground(AppTheme.TEXT_PRIMARY);
        
        textField = new JTextField(columns);
        textField.setMargin(new Insets(5, 5, 5, 5));
        
        add(label, BorderLayout.NORTH);
        add(textField, BorderLayout.CENTER);
    }

    /**
     * Creates a new form field with the given label and default width of 20 columns.
     *
     * @param labelText The text to display as the field's label
     */
    public FormField(String labelText) {
        this(labelText, 20);
    }

    /**
     * Gets the text currently entered in the field.
     *
     * @return The text in the text field
     */
    public String getText() {
        return textField.getText();
    }

    /**
     * Sets the text in the field.
     *
     * @param text The text to set in the field
     */
    public void setText(String text) {
        textField.setText(text);
    }

    /**
     * Sets whether the field is editable.
     *
     * @param editable Whether the field should be editable
     */
    public void setEditable(boolean editable) {
        textField.setEditable(editable);
    }

    /**
     * Gets the underlying JTextField component.
     *
     * @return The JTextField used in this form field
     */
    public JTextField getTextField() {
        return textField;
    }

    /**
     * Gets the underlying JLabel component.
     *
     * @return The JLabel used in this form field
     */
    public JLabel getLabel() {
        return label;
    }
}