package com.upnext.app.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import com.upnext.app.ui.theme.AppTheme;

/**
 * Loading state component with animated spinner and message.
 * Provides visual feedback during long-running operations.
 */
public class LoadingPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private final JPanel spinnerPanel;
    private final JLabel messageLabel;
    private final Timer animationTimer;
    private int rotation = 0;
    private boolean isVisible = false;
    
    public LoadingPanel() {
        this("Loading...");
    }
    
    public LoadingPanel(String message) {
        setLayout(new BorderLayout());
        setBackground(AppTheme.SURFACE);
        setOpaque(true);
        
        // Create spinner panel
        spinnerPanel = new JPanel() {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (isVisible) {
                    drawSpinner(g);
                }
            }
        };
        spinnerPanel.setPreferredSize(new Dimension(50, 50));
        spinnerPanel.setBackground(AppTheme.SURFACE);
        
        // Create message label
        messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setFont(AppTheme.PRIMARY_FONT);
        messageLabel.setForeground(AppTheme.TEXT_SECONDARY);
        
        // Layout components
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
        centerPanel.setBackground(AppTheme.SURFACE);
        centerPanel.add(spinnerPanel);
        centerPanel.add(messageLabel);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // Create animation timer
        animationTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rotation = (rotation + 15) % 360;
                spinnerPanel.repaint();
            }
        });
        
        setVisible(false);
    }
    
    private void drawSpinner(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = spinnerPanel.getWidth();
        int height = spinnerPanel.getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = Math.min(width, height) / 4;
        
        // Draw spinning dots
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(rotation + i * 45);
            int x = centerX + (int) (radius * Math.cos(angle));
            int y = centerY + (int) (radius * Math.sin(angle));
            
            // Fade effect - dots at the front are more opaque
            float alpha = 1.0f - (i * 0.1f);
            Color dotColor = new Color(
                AppTheme.PRIMARY.getRed() / 255f,
                AppTheme.PRIMARY.getGreen() / 255f,
                AppTheme.PRIMARY.getBlue() / 255f,
                alpha
            );
            
            g2d.setColor(dotColor);
            g2d.fillOval(x - 3, y - 3, 6, 6);
        }
        
        g2d.dispose();
    }
    
    /**
     * Shows the loading panel with animation.
     */
    public void showLoading() {
        showLoading("Loading...");
    }
    
    /**
     * Shows the loading panel with a custom message.
     * 
     * @param message The loading message to display
     */
    public void showLoading(String message) {
        messageLabel.setText(message);
        isVisible = true;
        setVisible(true);
        animationTimer.start();
        repaint();
    }
    
    /**
     * Hides the loading panel and stops animation.
     */
    public void hideLoading() {
        isVisible = false;
        animationTimer.stop();
        setVisible(false);
    }
    
    /**
     * Checks if the loading panel is currently showing.
     */
    public boolean isLoadingVisible() {
        return isVisible;
    }
    
    /**
     * Updates the loading message without affecting the animation state.
     * 
     * @param message The new message to display
     */
    public void updateMessage(String message) {
        messageLabel.setText(message);
        repaint();
    }
    
    /**
     * Creates a lightweight loading overlay that can be placed over other components.
     * 
     * @param message The loading message
     * @return A semi-transparent loading panel
     */
    public static LoadingPanel createOverlay(String message) {
        LoadingPanel overlay = new LoadingPanel(message);
        overlay.setBackground(new Color(255, 255, 255, 200)); // Semi-transparent white
        return overlay;
    }
    
    /**
     * Creates a loading panel optimized for specific operations.
     */
    public static class Operations {
        public static LoadingPanel loadingQuestions() {
            return new LoadingPanel("Loading questions...");
        }
        
        public static LoadingPanel loadingAnswers() {
            return new LoadingPanel("Loading answers...");
        }
        
        public static LoadingPanel posting() {
            return new LoadingPanel("Posting...");
        }
        
        public static LoadingPanel voting() {
            return new LoadingPanel("Processing vote...");
        }
        
        public static LoadingPanel searching() {
            return new LoadingPanel("Searching...");
        }
    }
}