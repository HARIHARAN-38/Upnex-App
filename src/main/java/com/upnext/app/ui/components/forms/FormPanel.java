package com.upnext.app.ui.components.forms;

import com.upnext.app.ui.theme.AppTheme;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * A container for form elements with proper spacing and alignment.
 */
public class FormPanel extends JPanel {
    private final JPanel contentPanel;
    
    /**
     * Creates a new form panel with vertical layout.
     */
    public FormPanel() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BACKGROUND);
        
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(AppTheme.SURFACE);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(AppTheme.SURFACE);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Adds a component to the form with vertical spacing.
     *
     * @param component The component to add
     * @return The added component
     */
    public Component addField(Component component) {
        // Add vertical spacing before component (except for the first component)
        if (contentPanel.getComponentCount() > 0) {
            contentPanel.add(Box.createVerticalStrut(15));
        }
        
        // Make component left-aligned and full-width
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, component.getPreferredSize().height));
        if (component instanceof JComponent) {
            ((JComponent) component).setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        
        contentPanel.add(component);
        return component;
    }
    
    /**
     * Adds a submit button centered at the bottom of the form.
     *
     * @param button The button to add
     */
    public void addSubmitButton(JButton button) {
        // Add spacing before the button
        contentPanel.add(Box.createVerticalStrut(25));
        
        // Create a panel for the button to center it
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
        buttonPanel.add(button);
        
        contentPanel.add(buttonPanel);
    }
    
    /**
     * Gets the content panel containing all form elements.
     *
     * @return The content panel
     */
    public JPanel getContentPanel() {
        return contentPanel;
    }
}