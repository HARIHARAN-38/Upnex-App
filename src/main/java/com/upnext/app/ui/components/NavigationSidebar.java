package com.upnext.app.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import com.upnext.app.core.Logger;
import com.upnext.app.service.AuthService;
import com.upnext.app.ui.theme.AppTheme;

/**
 * Left sidebar navigation component for the profile system.
 * Contains navigation buttons for Profile, Help, About screens
 * and logout functionality at the bottom.
 */
public class NavigationSidebar extends JPanel {
    
    // Constants
    private static final int SIDEBAR_WIDTH = 200;
    private static final int PADDING_MEDIUM = 16;
    private static final int PADDING_SMALL = 8;
    private static final int BUTTON_HEIGHT = 44;
    
    // Screen identifiers for navigation
    public static final String PROFILE_PAGE = "profile";
    public static final String HELP_PAGE = "help";
    public static final String ABOUT_PAGE = "about";
    
    // UI Components
    private final JButton profileButton;
    private final JButton helpButton;
    private final JButton aboutButton;
    private final JButton logoutButton;
    
    // Current active page tracking
    private String activePage = PROFILE_PAGE;
    
    // Callbacks
    private Consumer<String> navigationCallback;
    private Runnable logoutCallback;
    
    /**
     * Creates a new NavigationSidebar with profile system navigation.
     */
    public NavigationSidebar() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.SURFACE);
        setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));
        
        // Add border to separate from main content
        setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 0, 1, new Color(0xE2E8F0)),
            new EmptyBorder(PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM)
        ));
        
        // Create navigation buttons panel
        JPanel navigationPanel = new JPanel();
        navigationPanel.setLayout(new BoxLayout(navigationPanel, BoxLayout.Y_AXIS));
        navigationPanel.setOpaque(false);
        
        // Create navigation buttons
        profileButton = createNavigationButton("Profile", PROFILE_PAGE);
        helpButton = createNavigationButton("Help", HELP_PAGE);
        aboutButton = createNavigationButton("About", ABOUT_PAGE);
        
        // Add buttons to navigation panel with spacing
        navigationPanel.add(profileButton);
        navigationPanel.add(Box.createVerticalStrut(PADDING_SMALL));
        navigationPanel.add(helpButton);
        navigationPanel.add(Box.createVerticalStrut(PADDING_SMALL));
        navigationPanel.add(aboutButton);
        
        // Create logout section at bottom
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoutPanel.setOpaque(false);
        
        logoutButton = createLogoutButton();
        logoutPanel.add(logoutButton);
        
        // Add panels to main layout
        add(navigationPanel, BorderLayout.NORTH);
        add(logoutPanel, BorderLayout.SOUTH);
        
        // Set initial active button (inline to avoid overridable method call in constructor)
        activePage = PROFILE_PAGE;
        profileButton.setBackground(AppTheme.PRIMARY);
        profileButton.setForeground(Color.WHITE);
        profileButton.setOpaque(true);
        profileButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.PRIMARY, 1, true),
            new EmptyBorder(PADDING_SMALL, PADDING_MEDIUM, PADDING_SMALL, PADDING_MEDIUM)
        ));
    }
    
    /**
     * Creates a navigation button with proper styling and event handling.
     * 
     * @param text The button text
     * @param pageId The page identifier for navigation
     * @return Configured navigation button
     */
    private JButton createNavigationButton(String text, String pageId) {
        JButton button = new JButton(text);
        button.setFont(AppTheme.PRIMARY_FONT);
        button.setPreferredSize(new Dimension(SIDEBAR_WIDTH - 32, BUTTON_HEIGHT));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        
        // Set default styling (inactive state)
        setInactiveButtonStyle(button);
        
        // Add click handler
        button.addActionListener(e -> {
            if (navigationCallback != null) {
                navigationCallback.accept(pageId);
            }
            setActiveButton(pageId);
        });
        
        // Add hover effects
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!pageId.equals(activePage)) {
                    button.setBackground(new Color(0xF1F5F9));
                    button.setOpaque(true);
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (!pageId.equals(activePage)) {
                    setInactiveButtonStyle(button);
                }
            }
        });
        
        return button;
    }
    
    /**
     * Creates the logout button with special styling.
     * 
     * @return Configured logout button
     */
    private JButton createLogoutButton() {
        JButton button = new JButton("Logout");
        button.setFont(AppTheme.PRIMARY_FONT);
        button.setPreferredSize(new Dimension(SIDEBAR_WIDTH - 32, BUTTON_HEIGHT));
        button.setBackground(AppTheme.ACCENT);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add rounded border
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.ACCENT, 1, true),
            new EmptyBorder(PADDING_SMALL, PADDING_MEDIUM, PADDING_SMALL, PADDING_MEDIUM)
        ));
        
        // Add click handler
        button.addActionListener(e -> handleLogout());
        
        // Add hover effects
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(AppTheme.ACCENT.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(AppTheme.ACCENT);
            }
        });
        
        return button;
    }
    
    /**
     * Sets the inactive button styling.
     * 
     * @param button The button to style
     */
    private void setInactiveButtonStyle(JButton button) {
        button.setBackground(AppTheme.SURFACE);
        button.setForeground(AppTheme.TEXT_SECONDARY);
        button.setOpaque(false);
        button.setBorder(new EmptyBorder(PADDING_SMALL, PADDING_MEDIUM, PADDING_SMALL, PADDING_MEDIUM));
    }
    
    /**
     * Sets the active button styling.
     * 
     * @param button The button to style
     */
    private void setActiveButtonStyle(JButton button) {
        button.setBackground(AppTheme.PRIMARY);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.PRIMARY, 1, true),
            new EmptyBorder(PADDING_SMALL, PADDING_MEDIUM, PADDING_SMALL, PADDING_MEDIUM)
        ));
    }
    
    /**
     * Sets the active navigation button based on the current page.
     * 
     * @param pageId The currently active page identifier
     */
    public void setActiveButton(String pageId) {
        activePage = pageId;
        
        // Reset all buttons to inactive state
        setInactiveButtonStyle(profileButton);
        setInactiveButtonStyle(helpButton);
        setInactiveButtonStyle(aboutButton);
        
        // Set the active button
        switch (pageId) {
            case PROFILE_PAGE -> setActiveButtonStyle(profileButton);
            case HELP_PAGE -> setActiveButtonStyle(helpButton);
            case ABOUT_PAGE -> setActiveButtonStyle(aboutButton);
        }
        
        repaint();
    }
    
    /**
     * Handles logout functionality.
     */
    private void handleLogout() {
        try {
            Logger.getInstance().info("User initiated logout from profile sidebar");
            
            // Confirm logout with user
            boolean confirmed = FeedbackManager.showConfirmation(
                this,
                "Are you sure you want to sign out?",
                "Confirm Logout"
            );
            
            if (confirmed) {
                // Sign out through AuthService
                AuthService.getInstance().signOut();
                
                // Execute logout callback (should navigate to sign-in screen)
                if (logoutCallback != null) {
                    logoutCallback.run();
                }
                
                Logger.getInstance().info("User successfully logged out");
            }
        } catch (Exception e) {
            Logger.getInstance().error("Error during logout: " + e.getMessage());
            FeedbackManager.showError(
                this,
                "An error occurred while signing out. Please try again.",
                "Logout Error"
            );
        }
    }
    
    /**
     * Sets the navigation callback for handling page changes.
     * 
     * @param callback Callback function that receives the page identifier
     */
    public void setNavigationCallback(Consumer<String> callback) {
        this.navigationCallback = callback;
    }
    
    /**
     * Sets the logout callback for handling logout navigation.
     * 
     * @param callback Callback function to execute on logout
     */
    public void setLogoutCallback(Runnable callback) {
        this.logoutCallback = callback;
    }
    
    /**
     * Gets the currently active page identifier.
     * 
     * @return The active page identifier
     */
    public String getActivePage() {
        return activePage;
    }
}