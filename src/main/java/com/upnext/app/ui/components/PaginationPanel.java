package com.upnext.app.ui.components;

import java.awt.FlowLayout;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.upnext.app.ui.theme.AppTheme;

/**
 * Pagination component for handling large result sets.
 * Provides navigation controls and page information display.
 */
public class PaginationPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private int currentPage = 0;
    private int totalPages = 0;
    private int totalItems = 0;
    private int pageSize = 10;
    
    private final JButton firstButton = new JButton("« First");
    private final JButton prevButton = new JButton("‹ Previous");
    private final JButton nextButton = new JButton("Next ›");
    private final JButton lastButton = new JButton("Last »");
    private final JLabel pageInfoLabel = new JLabel();
    private final JLabel itemCountLabel = new JLabel();
    
    private Consumer<Integer> onPageChanged;
    
    public PaginationPanel() {
        initializeComponents();
        layoutComponents();
        updateButtonStates();
    }
    
    private void initializeComponents() {
        setBackground(AppTheme.SURFACE);
        
        // Style buttons
        styleButton(firstButton);
        styleButton(prevButton);
        styleButton(nextButton);
        styleButton(lastButton);
        
        // Style labels
        pageInfoLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(12f));
        pageInfoLabel.setForeground(AppTheme.TEXT_SECONDARY);
        
        itemCountLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(12f));
        itemCountLabel.setForeground(AppTheme.TEXT_SECONDARY);
        
        // Add action listeners
        firstButton.addActionListener(e -> goToPage(0));
        prevButton.addActionListener(e -> goToPage(currentPage - 1));
        nextButton.addActionListener(e -> goToPage(currentPage + 1));
        lastButton.addActionListener(e -> goToPage(totalPages - 1));
    }
    
    private void styleButton(JButton button) {
        button.setFont(AppTheme.PRIMARY_FONT.deriveFont(12f));
        button.setBackground(AppTheme.SURFACE);
        button.setForeground(AppTheme.PRIMARY);
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        button.setFocusPainted(false);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(AppTheme.BACKGROUND);
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(AppTheme.SURFACE);
            }
        });
    }
    
    private void layoutComponents() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        add(itemCountLabel);
        add(new JLabel("|")); // Separator
        add(firstButton);
        add(prevButton);
        add(pageInfoLabel);
        add(nextButton);
        add(lastButton);
    }
    
    /**
     * Updates the pagination state with new data.
     * 
     * @param currentPage The current page (0-based)
     * @param totalPages The total number of pages
     * @param totalItems The total number of items
     * @param pageSize The number of items per page
     */
    public void updatePagination(int currentPage, int totalPages, int totalItems, int pageSize) {
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
        this.pageSize = pageSize;
        
        updateLabels();
        updateButtonStates();
        repaint();
    }
    
    private void updateLabels() {
        // Update page info (e.g., \"Page 1 of 5\")
        if (totalPages > 0) {
            pageInfoLabel.setText(String.format("Page %d of %d", currentPage + 1, totalPages));
        } else {
            pageInfoLabel.setText("No pages");
        }
        
        // Update item count info (e.g., \"Showing 1-10 of 47 answers\")
        if (totalItems > 0) {
            int startItem = currentPage * pageSize + 1;
            int endItem = Math.min((currentPage + 1) * pageSize, totalItems);
            itemCountLabel.setText(String.format("Showing %d-%d of %d answers", startItem, endItem, totalItems));
        } else {
            itemCountLabel.setText("No answers");
        }
    }
    
    private void updateButtonStates() {
        boolean hasPrevious = currentPage > 0;
        boolean hasNext = currentPage < totalPages - 1;
        
        firstButton.setEnabled(hasPrevious);
        prevButton.setEnabled(hasPrevious);
        nextButton.setEnabled(hasNext);
        lastButton.setEnabled(hasNext);
        
        // Update button colors based on enabled state
        updateButtonAppearance(firstButton, hasPrevious);
        updateButtonAppearance(prevButton, hasPrevious);
        updateButtonAppearance(nextButton, hasNext);
        updateButtonAppearance(lastButton, hasNext);
    }
    
    private void updateButtonAppearance(JButton button, boolean enabled) {
        if (enabled) {
            button.setForeground(AppTheme.PRIMARY);
        } else {
            button.setForeground(AppTheme.TEXT_SECONDARY.darker());
        }
    }
    
    private void goToPage(int page) {
        if (page >= 0 && page < totalPages && page != currentPage) {
            if (onPageChanged != null) {
                onPageChanged.accept(page);
            }
        }
    }
    
    /**
     * Sets the callback to be invoked when the page changes.
     * 
     * @param onPageChanged Callback that receives the new page number (0-based)
     */
    public void setOnPageChanged(Consumer<Integer> onPageChanged) {
        this.onPageChanged = onPageChanged;
    }
    
    /**
     * Gets the current page number (0-based).
     */
    public int getCurrentPage() {
        return currentPage;
    }
    
    /**
     * Gets the total number of pages.
     */
    public int getTotalPages() {
        return totalPages;
    }
    
    /**
     * Gets the total number of items.
     */
    public int getTotalItems() {
        return totalItems;
    }
    
    /**
     * Checks if there are multiple pages.
     */
    public boolean hasMultiplePages() {
        return totalPages > 1;
    }
}