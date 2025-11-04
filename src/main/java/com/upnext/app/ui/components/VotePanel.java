package com.upnext.app.ui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.upnext.app.ui.animations.AnimationUtils;
import com.upnext.app.ui.theme.AppTheme;

/**
 * Reusable voting panel component for questions and answers.
 * Provides upvote/downvote buttons with real-time vote count updates.
 */
public class VotePanel extends JPanel {
    
    // UI components
    private final JButton upvoteButton;
    private final JButton downvoteButton;
    private final JLabel voteCountLabel;
    
    // State
    private Long itemId;
    private int currentVoteCount;
    private boolean isUpvoteActive = false;
    private boolean isDownvoteActive = false;
    private boolean isEnabled = true;
    
    // Callbacks
    private BiConsumer<Long, Boolean> onVoteAction; // itemId, isUpvote
    private Consumer<String> onError;
    
    /**
     * Creates a new vote panel.
     */
    public VotePanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setAlignmentY(Component.TOP_ALIGNMENT);
        
        // Create upvote button
    upvoteButton = new JButton("▲");
    // Use the same font as Home screen (Dialog) to ensure triangle glyphs render consistently
    upvoteButton.setFont(new Font("Dialog", Font.PLAIN, 16));
        upvoteButton.setForeground(AppTheme.ACCENT);
        upvoteButton.setBorderPainted(false);
        upvoteButton.setContentAreaFilled(false);
        upvoteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        upvoteButton.setPreferredSize(new Dimension(40, 30));
        upvoteButton.setMaximumSize(new Dimension(40, 30));
        upvoteButton.setMinimumSize(new Dimension(40, 30));
        upvoteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        upvoteButton.addActionListener(e -> handleUpvote());
        
        // Add hover and active state effects for upvote with animations
        upvoteButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (isEnabled) {
                    Color targetColor = new Color(0x28a745);
                    Color currentColor = upvoteButton.getForeground();
                    AnimationUtils.animateForegroundTransition(upvoteButton, currentColor, targetColor, 150);
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (isEnabled) {
                    Color targetColor = isUpvoteActive ? new Color(0x28a745) : AppTheme.ACCENT;
                    Color currentColor = upvoteButton.getForeground();
                    AnimationUtils.animateForegroundTransition(upvoteButton, currentColor, targetColor, 150);
                }
            }
        });
        
        // Create vote count label
        voteCountLabel = new JLabel("0", JLabel.CENTER);
        voteCountLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.BOLD, 14f));
        voteCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        voteCountLabel.setPreferredSize(new Dimension(40, 20));
        voteCountLabel.setMaximumSize(new Dimension(40, 20));
        voteCountLabel.setMinimumSize(new Dimension(40, 20));
        updateVoteCountColor(0);
        
        // Create downvote button
    downvoteButton = new JButton("▼");
    downvoteButton.setFont(new Font("Dialog", Font.PLAIN, 16));
        downvoteButton.setForeground(AppTheme.ACCENT);
        downvoteButton.setBorderPainted(false);
        downvoteButton.setContentAreaFilled(false);
        downvoteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        downvoteButton.setPreferredSize(new Dimension(40, 30));
        downvoteButton.setMaximumSize(new Dimension(40, 30));
        downvoteButton.setMinimumSize(new Dimension(40, 30));
        downvoteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        downvoteButton.addActionListener(e -> handleDownvote());
        
        // Add hover and active state effects for downvote with animations
        downvoteButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (isEnabled) {
                    Color targetColor = new Color(0xdc3545);
                    Color currentColor = downvoteButton.getForeground();
                    AnimationUtils.animateForegroundTransition(downvoteButton, currentColor, targetColor, 150);
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (isEnabled) {
                    Color targetColor = isDownvoteActive ? new Color(0xdc3545) : AppTheme.ACCENT;
                    Color currentColor = downvoteButton.getForeground();
                    AnimationUtils.animateForegroundTransition(downvoteButton, currentColor, targetColor, 150);
                }
            }
        });
        
        // Add components to panel
        add(upvoteButton);
        add(Box.createRigidArea(new Dimension(0, 2)));
        add(voteCountLabel);
        add(Box.createRigidArea(new Dimension(0, 2)));
        add(downvoteButton);
        
        // Set initial size
        setPreferredSize(new Dimension(50, 100));
        setMaximumSize(new Dimension(50, 100));
        setMinimumSize(new Dimension(50, 100));
    }
    
    /**
     * Sets the item ID that this vote panel controls.
     * 
     * @param itemId The ID of the question or answer
     */
    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }
    
    /**
     * Gets the current item ID.
     * 
     * @return The item ID
     */
    public Long getItemId() {
        return itemId;
    }
    
    /**
     * Sets the vote action callback.
     * 
     * @param onVoteAction Callback that receives (itemId, isUpvote)
     */
    public void setOnVoteAction(BiConsumer<Long, Boolean> onVoteAction) {
        this.onVoteAction = onVoteAction;
    }
    
    /**
     * Sets the error callback.
     * 
     * @param onError Callback that receives error messages
     */
    public void setOnError(Consumer<String> onError) {
        this.onError = onError;
    }
    
    /**
     * Updates the vote count display.
     * 
     * @param voteCount The new vote count (net score)
     */
    public void setVoteCount(int voteCount) {
        this.currentVoteCount = voteCount;
        voteCountLabel.setText(String.valueOf(voteCount));
        updateVoteCountColor(voteCount);
    }
    
    /**
     * Gets the current vote count.
     * 
     * @return The current vote count
     */
    public int getVoteCount() {
        return currentVoteCount;
    }
    
    /**
     * Sets the active vote state (for showing which vote the user has cast).
     * 
     * @param isUpvoteActive Whether the upvote is active
     * @param isDownvoteActive Whether the downvote is active
     */
    public void setVoteState(boolean isUpvoteActive, boolean isDownvoteActive) {
        this.isUpvoteActive = isUpvoteActive;
        this.isDownvoteActive = isDownvoteActive;
        
        // Update button colors based on active state
        upvoteButton.setForeground(isUpvoteActive ? new Color(0x28a745) : AppTheme.ACCENT);
        downvoteButton.setForeground(isDownvoteActive ? new Color(0xdc3545) : AppTheme.ACCENT);
        
        // Update button background for active state
        if (isUpvoteActive) {
            upvoteButton.setOpaque(true);
            upvoteButton.setBackground(new Color(40, 167, 69, 50)); // Light green background with alpha
        } else {
            upvoteButton.setOpaque(false);
        }
        
        if (isDownvoteActive) {
            downvoteButton.setOpaque(true);
            downvoteButton.setBackground(new Color(220, 53, 69, 50)); // Light red background with alpha
        } else {
            downvoteButton.setOpaque(false);
        }
    }
    
    /**
     * Enables or disables the vote panel.
     * 
     * @param enabled Whether the panel should be enabled
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.isEnabled = enabled;
        
        upvoteButton.setEnabled(enabled);
        downvoteButton.setEnabled(enabled);
        
        if (enabled) {
            upvoteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            downvoteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        } else {
            upvoteButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            downvoteButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            upvoteButton.setForeground(AppTheme.TEXT_SECONDARY);
            downvoteButton.setForeground(AppTheme.TEXT_SECONDARY);
        }
    }
    
    /**
     * Sets loading state for the vote panel.
     * 
     * @param isLoading Whether the panel is in loading state
     */
    public void setLoading(boolean isLoading) {
        setEnabled(!isLoading);
        
        if (isLoading) {
            voteCountLabel.setText("...");
            voteCountLabel.setForeground(AppTheme.TEXT_SECONDARY);
        } else {
            voteCountLabel.setText(String.valueOf(currentVoteCount));
            updateVoteCountColor(currentVoteCount);
        }
    }
    
    /**
     * Updates the vote count label color based on the score.
     * 
     * @param voteCount The vote count
     */
    private void updateVoteCountColor(int voteCount) {
        if (voteCount > 0) {
            voteCountLabel.setForeground(new Color(0x28a745)); // Green for positive
        } else if (voteCount < 0) {
            voteCountLabel.setForeground(new Color(0xdc3545)); // Red for negative
        } else {
            voteCountLabel.setForeground(AppTheme.TEXT_SECONDARY); // Gray for neutral
        }
    }
    
    /**
     * Handles upvote button click.
     */
    private void handleUpvote() {
        if (!isEnabled || itemId == null) {
            return;
        }
        
        if (onVoteAction != null) {
            setLoading(true);
            try {
                onVoteAction.accept(itemId, true);
            } catch (Exception e) {
                if (onError != null) {
                    onError.accept("Failed to process upvote: " + e.getMessage());
                }
            } finally {
                resetLoadingState();
            }
        }
    }
    
    /**
     * Handles downvote button click.
     */
    private void handleDownvote() {
        if (!isEnabled || itemId == null) {
            return;
        }
        
        if (onVoteAction != null) {
            setLoading(true);
            try {
                onVoteAction.accept(itemId, false);
            } catch (Exception e) {
                if (onError != null) {
                    onError.accept("Failed to process downvote: " + e.getMessage());
                }
            } finally {
                resetLoadingState();
            }
        }
    }
    
    /**
     * Resets the loading state (to be called after vote processing is complete).
     */
    public void resetLoadingState() {
        setLoading(false);
    }
    
    /**
     * Creates a vote panel with compact styling for use in lists.
     * 
     * @return A compact vote panel
     */
    public static VotePanel createCompact() {
        VotePanel panel = new VotePanel();
        
        // Make buttons smaller for compact layout
        panel.upvoteButton.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.BOLD, 14f));
        panel.downvoteButton.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.BOLD, 14f));
        panel.voteCountLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.BOLD, 12f));
        
        Dimension compactButtonSize = new Dimension(35, 25);
        panel.upvoteButton.setPreferredSize(compactButtonSize);
        panel.upvoteButton.setMaximumSize(compactButtonSize);
        panel.upvoteButton.setMinimumSize(compactButtonSize);
        
        panel.downvoteButton.setPreferredSize(compactButtonSize);
        panel.downvoteButton.setMaximumSize(compactButtonSize);
        panel.downvoteButton.setMinimumSize(compactButtonSize);
        
        Dimension compactLabelSize = new Dimension(35, 18);
        panel.voteCountLabel.setPreferredSize(compactLabelSize);
        panel.voteCountLabel.setMaximumSize(compactLabelSize);
        panel.voteCountLabel.setMinimumSize(compactLabelSize);
        
        // Adjust panel size
        panel.setPreferredSize(new Dimension(45, 85));
        panel.setMaximumSize(new Dimension(45, 85));
        panel.setMinimumSize(new Dimension(45, 85));
        
        return panel;
    }
    
    /**
     * Creates a vote panel with horizontal layout for use in toolbars.
     * 
     * @return A horizontal vote panel
     */
    public static VotePanel createHorizontal() {
        VotePanel panel = new VotePanel();
        
        // Remove existing components
        panel.removeAll();
        
        // Change to horizontal layout
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        
        // Re-add components horizontally
        panel.add(panel.upvoteButton);
        panel.add(Box.createRigidArea(new Dimension(4, 0)));
        panel.add(panel.voteCountLabel);
        panel.add(Box.createRigidArea(new Dimension(4, 0)));
        panel.add(panel.downvoteButton);
        
        // Adjust sizes for horizontal layout
        panel.setPreferredSize(new Dimension(120, 35));
        panel.setMaximumSize(new Dimension(120, 35));
        panel.setMinimumSize(new Dimension(120, 35));
        
        return panel;
    }
}