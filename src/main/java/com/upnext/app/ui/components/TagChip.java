package com.upnext.app.ui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import com.upnext.app.ui.theme.AppTheme;

/**
 * A clickable tag chip component that displays a tag name with hover effects
 * and supports click actions for navigation and filtering.
 */
public class TagChip extends JLabel {
    private static final int PADDING_HORIZONTAL = 8;
    private static final int PADDING_VERTICAL = 4;
    private static final int BORDER_RADIUS = 12;
    
    private final String tagName;
    private Consumer<String> onClickCallback;
    private boolean isHovered = false;
    private boolean isSelected = false;
    
    // Color scheme
    private final Color backgroundColor = new Color(0xF1F5F9);
    private final Color hoverBackgroundColor = new Color(0xE2E8F0);
    private final Color selectedBackgroundColor = AppTheme.ACCENT;
    private final Color textColor = new Color(0x475569);
    private final Color hoverTextColor = new Color(0x334155);
    private final Color selectedTextColor = Color.WHITE;
    
    /**
     * Creates a new TagChip with the specified tag name.
     * 
     * @param tagName The name of the tag to display
     */
    public TagChip(String tagName) {
        super(tagName);
        this.tagName = tagName;
        initializeComponent();
    }
    
    /**
     * Initializes the component with styling and event handlers.
     */
    private void initializeComponent() {
        // Basic styling
        setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.PLAIN, 12f));
        setForeground(textColor);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Padding
        setBorder(new EmptyBorder(PADDING_VERTICAL, PADDING_HORIZONTAL, PADDING_VERTICAL, PADDING_HORIZONTAL));
        
        // Calculate preferred size based on text
        setPreferredSize(calculatePreferredSize());
        
        // Mouse event handlers
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                updateAppearance();
                repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                updateAppearance();
                repaint();
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onClickCallback != null) {
                    onClickCallback.accept(tagName);
                }
            }
        });
        
        updateAppearance();
    }
    
    /**
     * Calculates the preferred size based on the text content.
     * 
     * @return The preferred size
     */
    private Dimension calculatePreferredSize() {
        if (getFont() == null) {
            return new Dimension(50, 24);
        }
        
        int textWidth = getFontMetrics(getFont()).stringWidth(tagName);
        int width = textWidth + (2 * PADDING_HORIZONTAL) + 4; // Extra padding for rounded corners
        int height = getFontMetrics(getFont()).getHeight() + (2 * PADDING_VERTICAL);
        
        return new Dimension(width, Math.max(height, 24));
    }
    
    /**
     * Updates the appearance based on current state.
     */
    private void updateAppearance() {
        if (isSelected) {
            setForeground(selectedTextColor);
        } else if (isHovered) {
            setForeground(hoverTextColor);
        } else {
            setForeground(textColor);
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        
        // Draw rounded background
        Color bgColor;
        if (isSelected) {
            bgColor = selectedBackgroundColor;
        } else if (isHovered) {
            bgColor = hoverBackgroundColor;
        } else {
            bgColor = backgroundColor;
        }
        
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, width - 1, height - 1, BORDER_RADIUS, BORDER_RADIUS);
        
        // Draw subtle border
        g2.setColor(new Color(0xCBD5E1));
        g2.drawRoundRect(0, 0, width - 1, height - 1, BORDER_RADIUS, BORDER_RADIUS);
        
        g2.dispose();
        
        // Draw the text
        super.paintComponent(g);
    }
    
    /**
     * Sets the click callback for when the tag is clicked.
     * 
     * @param callback Consumer that receives the tag name when clicked
     */
    public void setOnClickCallback(Consumer<String> callback) {
        this.onClickCallback = callback;
    }
    
    /**
     * Gets the tag name.
     * 
     * @return The tag name
     */
    public String getTagName() {
        return tagName;
    }
    
    /**
     * Sets the selected state of the tag chip.
     * 
     * @param selected true if selected, false otherwise
     */
    public void setSelected(boolean selected) {
        this.isSelected = selected;
        updateAppearance();
        repaint();
    }
    
    /**
     * Gets the selected state of the tag chip.
     * 
     * @return true if selected, false otherwise
     */
    public boolean isSelected() {
        return isSelected;
    }
    
    /**
     * Creates a tag chip with the specified styling variant.
     * 
     * @param tagName The tag name
     * @param variant The styling variant ("primary", "secondary", "outline")
     * @return A configured TagChip
     */
    public static TagChip createWithVariant(String tagName, String variant) {
        TagChip chip = new TagChip(tagName);
        
        switch (variant.toLowerCase()) {
            case "primary":
                chip.setSelected(true);
                break;
            case "outline":
                // Custom outline styling could be added here
                break;
            case "secondary":
            default:
                // Default styling
                break;
        }
        
        return chip;
    }
    
    /**
     * Creates a compact tag chip with smaller padding.
     * 
     * @param tagName The tag name
     * @return A compact TagChip
     */
    public static TagChip createCompact(String tagName) {
        TagChip chip = new TagChip(tagName);
        
        // Smaller font and padding for compact version
        chip.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.PLAIN, 10f));
        chip.setBorder(new EmptyBorder(2, 6, 2, 6));
        
        return chip;
    }
}