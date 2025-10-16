package com.upnext.app.ui.components;

import com.upnext.app.ui.theme.AppTheme;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * A visual component to display and edit skill proficiency levels.
 * The bar displays the proficiency level from 1 to 10 with appropriate color coding.
 */
public class ProficiencyBar extends JPanel {
    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 10;
    private static final int DEFAULT_WIDTH = 200;
    private static final int DEFAULT_HEIGHT = 20;
    private static final int LEVEL_MARKER_WIDTH = 10;
    
    private int proficiencyLevel;
    private boolean editable;
    private boolean isDragging;
    private Color[] levelColors;
    private final JLabel levelLabel;
    
    /**
     * Creates a new proficiency bar with the specified initial level.
     * 
     * @param initialLevel The initial proficiency level (1-10)
     * @param editable Whether the bar is editable by the user
     */
    public ProficiencyBar(int initialLevel, boolean editable) {
        this.proficiencyLevel = constrainLevel(initialLevel);
        this.editable = editable;
        this.isDragging = false;
        
        initializeLevelColors();
        
        setLayout(new BorderLayout(10, 0));
        setOpaque(false);
        setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        setBorder(new EmptyBorder(5, 0, 5, 0));
        
        levelLabel = new JLabel(String.valueOf(proficiencyLevel));
        levelLabel.setFont(AppTheme.PRIMARY_FONT);
        levelLabel.setForeground(AppTheme.TEXT_PRIMARY);
        levelLabel.setPreferredSize(new Dimension(30, DEFAULT_HEIGHT));
        levelLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        add(levelLabel, BorderLayout.EAST);
        
        if (editable) {
            setupMouseListeners();
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
    }
    
    /**
     * Creates a new proficiency bar with default level 1 and not editable.
     */
    public ProficiencyBar() {
        this(1, false);
    }
    
    /**
     * Creates a new proficiency bar with the specified initial level and not editable.
     * 
     * @param initialLevel The initial proficiency level (1-10)
     */
    public ProficiencyBar(int initialLevel) {
        this(initialLevel, false);
    }
    
    /**
     * Sets up the color gradient for different proficiency levels.
     */
    private void initializeLevelColors() {
        levelColors = new Color[MAX_LEVEL + 1];
        
        // Beginner levels (1-3): lighter shades
        levelColors[1] = new Color(0xCBD5E0); // Light gray
        levelColors[2] = new Color(0xA0CFCF); // Light teal
        levelColors[3] = new Color(0x90CDF4); // Light blue
        
        // Intermediate levels (4-7): medium shades
        levelColors[4] = new Color(0x63B3ED); // Medium blue
        levelColors[5] = new Color(0x4299E1); // Standard blue
        levelColors[6] = new Color(0x3182CE); // Deeper blue
        levelColors[7] = new Color(0x2B6CB0); // Navy blue
        
        // Advanced levels (8-10): intense shades
        levelColors[8] = new Color(0x2C5282); // Dark blue
        levelColors[9] = new Color(0x1A365D); // Very dark blue
        levelColors[10] = new Color(0x0F172A); // Almost black blue
    }
    
    /**
     * Sets up mouse listeners for editing the proficiency level.
     */
    private void setupMouseListeners() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (editable) {
                    isDragging = true;
                    updateLevelFromPosition(e.getX());
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                isDragging = false;
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                if (editable && isDragging) {
                    updateLevelFromPosition(e.getX());
                }
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                if (editable) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
            }
        };
        
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }
    
    /**
     * Updates the proficiency level based on the mouse position.
     * 
     * @param x The x-coordinate of the mouse position
     */
    private void updateLevelFromPosition(int x) {
        int barWidth = getWidth() - levelLabel.getWidth();
        int newLevel = Math.max(1, Math.min(MAX_LEVEL, (int) (x * MAX_LEVEL / barWidth) + 1));
        setProficiencyLevel(newLevel);
    }
    
    /**
     * Gets the current proficiency level.
     * 
     * @return The proficiency level (1-10)
     */
    public int getProficiencyLevel() {
        return proficiencyLevel;
    }
    
    /**
     * Sets the proficiency level.
     * 
     * @param level The new proficiency level (1-10)
     */
    public void setProficiencyLevel(int level) {
        int oldLevel = this.proficiencyLevel;
        this.proficiencyLevel = constrainLevel(level);
        
        if (oldLevel != this.proficiencyLevel) {
            levelLabel.setText(String.valueOf(this.proficiencyLevel));
            firePropertyChange("proficiencyLevel", oldLevel, this.proficiencyLevel);
            repaint();
        }
    }
    
    /**
     * Constrains the level to the valid range (1-10).
     * 
     * @param level The level to constrain
     * @return The constrained level
     */
    private int constrainLevel(int level) {
        return Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, level));
    }
    
    /**
     * Sets whether the bar is editable.
     * 
     * @param editable Whether the bar is editable
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
        
        if (editable && getMouseListeners().length == 0) {
            setupMouseListeners();
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        } else if (!editable) {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }
    
    /**
     * Checks if the bar is editable.
     * 
     * @return Whether the bar is editable
     */
    public boolean isEditable() {
        return editable;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int barWidth = getWidth() - levelLabel.getWidth() - 10; // Width minus label width and spacing
        int barHeight = getHeight() - 10; // Height with some padding
        
        // Draw background
        g2d.setColor(new Color(0xE2E8F0)); // Light gray background
        g2d.fillRoundRect(0, 5, barWidth, barHeight, 5, 5);
        
        // Draw filled section
        int filledWidth = (int) (barWidth * ((double) proficiencyLevel / MAX_LEVEL));
        g2d.setColor(levelColors[proficiencyLevel]);
        g2d.fillRoundRect(0, 5, filledWidth, barHeight, 5, 5);
        
        // Draw level markers
        g2d.setColor(new Color(0xCBD5E0, true)); // Slightly transparent gray
        float segmentWidth = (float) barWidth / MAX_LEVEL;
        
        for (int i = 1; i < MAX_LEVEL; i++) {
            int x = (int) (i * segmentWidth);
            g2d.drawLine(x, 5, x, barHeight + 5);
        }
        
        g2d.dispose();
    }
}