package com.upnext.app.ui.components.questions;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import com.upnext.app.domain.question.Tag;
import com.upnext.app.ui.theme.AppTheme;

/**
 * Component for displaying selected tags as removable chips with proper AppTheme styling.
 * Each tag is displayed as a rounded chip with hover effects and a remove button.
 */
public class TagChipList extends JPanel {
    private static final int CHIP_HEIGHT = 28;
    private static final int CHIP_PADDING = 8;
    private static final int REMOVE_BUTTON_SIZE = 16;
    
    // Callback for tag removal
    private final Consumer<Tag> onTagRemoved;
    
    // List of currently displayed tags
    private final List<Tag> tags;
    
    /**
     * Creates a new TagChipList with the specified callback for tag removal.
     *
     * @param onTagRemoved Callback function called when a tag is removed
     */
    public TagChipList(Consumer<Tag> onTagRemoved) {
        this.onTagRemoved = onTagRemoved;
        this.tags = new ArrayList<>();
        
        // Setup layout
        setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4));
        setOpaque(false);
        
        // Set preferred size to show placeholder when empty
        setPreferredSize(new Dimension(0, CHIP_HEIGHT + 8));
    }
    
    /**
     * Adds a tag to the chip list.
     *
     * @param tag The tag to add
     */
    public void addTag(Tag tag) {
        if (tag == null || tags.contains(tag)) {
            return;
        }
        
        tags.add(tag);
        JPanel chip = createTagChip(tag);
        add(chip);
        
        // Update layout
        revalidate();
        repaint();
    }
    
    /**
     * Removes a tag from the chip list.
     *
     * @param tag The tag to remove
     */
    public void removeTag(Tag tag) {
        if (tag == null || !tags.contains(tag)) {
            return;
        }
        
        tags.remove(tag);
        
        // Find and remove the chip component
        for (int i = 0; i < getComponentCount(); i++) {
            if (getComponent(i) instanceof JPanel) {
                JPanel chip = (JPanel) getComponent(i);
                if (tag.equals(chip.getClientProperty("tag"))) {
                    remove(chip);
                    break;
                }
            }
        }
        
        // Call the removal callback
        if (onTagRemoved != null) {
            onTagRemoved.accept(tag);
        }
        
        // Update layout
        revalidate();
        repaint();
    }
    
    /**
     * Clears all tags from the chip list.
     */
    public void clearTags() {
        tags.clear();
        removeAll();
        revalidate();
        repaint();
    }
    
    /**
     * Creates a styled chip for a tag.
     *
     * @param tag The tag to create a chip for
     * @return The chip panel
     */
    private JPanel createTagChip(Tag tag) {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        chip.putClientProperty("tag", tag);
        
        // Base styling
        chip.setBackground(new Color(0xF3F4F6));
        chip.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE5E7EB), 1),
            new EmptyBorder(4, CHIP_PADDING, 4, 4)
        ));
        chip.setPreferredSize(new Dimension(0, CHIP_HEIGHT));
        
        // Tag name label
        JLabel nameLabel = new JLabel(tag.getName());
        nameLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(12f));
        nameLabel.setForeground(AppTheme.TEXT_PRIMARY);
        
        // Remove button
        JButton removeButton = createRemoveButton(tag, chip);
        
        // Add components
        chip.add(nameLabel);
        chip.add(removeButton);
        
        // Add hover effects
        addHoverEffects(chip);
        
        return chip;
    }
    
    /**
     * Creates a remove button for a tag chip.
     *
     * @param tag The tag associated with this button
     * @param chip The chip panel containing this button
     * @return The remove button
     */
    private JButton createRemoveButton(Tag tag, JPanel chip) {
        JButton button = new JButton("×");
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        button.setForeground(AppTheme.TEXT_SECONDARY);
        button.setBackground(new Color(0, 0, 0, 0));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(REMOVE_BUTTON_SIZE, REMOVE_BUTTON_SIZE));
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
    button.getAccessibleContext().setAccessibleName("Remove tag " + tag.getName());
        
        // Add click handler
        button.addActionListener(e -> removeTag(tag));
        
        // Add hover effects for the button
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(AppTheme.ACCENT);
                button.setBackground(new Color(0xFEF2F2));
                button.setOpaque(true);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(AppTheme.TEXT_SECONDARY);
                button.setBackground(new Color(0, 0, 0, 0));
                button.setOpaque(false);
            }
        });
        
        return button;
    }
    
    /**
     * Adds hover effects to a chip.
     *
     * @param chip The chip to add effects to
     */
    private void addHoverEffects(JPanel chip) {
        chip.addMouseListener(new MouseAdapter() {
            private final Color originalBackground = chip.getBackground();
            private final Color hoverBackground = new Color(0xE5E7EB);
            
            @Override
            public void mouseEntered(MouseEvent e) {
                chip.setBackground(hoverBackground);
                chip.setBorder(new CompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xD1D5DB), 1),
                    new EmptyBorder(4, CHIP_PADDING, 4, 4)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                chip.setBackground(originalBackground);
                chip.setBorder(new CompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xE5E7EB), 1),
                    new EmptyBorder(4, CHIP_PADDING, 4, 4)
                ));
            }
        });
    }
    
    /**
     * Gets the list of currently displayed tags.
     *
     * @return A copy of the tags list
     */
    public List<Tag> getTags() {
        return new ArrayList<>(tags);
    }
    
    /**
     * Gets the number of tags currently displayed.
     *
     * @return The number of tags
     */
    public int getTagCount() {
        return tags.size();
    }
    
    /**
     * Checks if the chip list is empty.
     *
     * @return True if no tags are displayed
     */
    public boolean isEmpty() {
        return tags.isEmpty();
    }
    
    /**
     * Checks if the chip list contains a specific tag.
     *
     * @param tag The tag to check for
     * @return True if the tag is present
     */
    public boolean containsTag(Tag tag) {
        return tags.contains(tag);
    }
}