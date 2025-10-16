package com.upnext.app.ui.components.forms;

import com.upnext.app.ui.theme.AppTheme;
import java.awt.*;
import javax.swing.*;

/**
 * A customized button with consistent styling for form submission.
 */
public class SubmitButton extends JButton {
    
    /**
     * Creates a new submit button with the given text.
     *
     * @param text The button text
     */
    public SubmitButton(String text) {
        super(text);
        setBackground(AppTheme.PRIMARY);
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(8, 15, 8, 15));
        setFont(AppTheme.PRIMARY_FONT);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (getModel().isPressed()) {
            g2.setColor(AppTheme.PRIMARY_DARK);
        } else if (getModel().isRollover()) {
            g2.setColor(AppTheme.PRIMARY.darker());
        } else {
            g2.setColor(AppTheme.PRIMARY);
        }
        
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
        g2.dispose();
        
        super.paintComponent(g);
    }
}