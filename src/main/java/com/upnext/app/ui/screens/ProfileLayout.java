package com.upnext.app.ui.screens;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

import com.upnext.app.core.Logger;
import com.upnext.app.ui.components.HeroBar;
import com.upnext.app.ui.components.NavigationSidebar;
import com.upnext.app.ui.navigation.ViewNavigator;
import com.upnext.app.ui.theme.AppTheme;

/**
 * Main layout container for the profile system that combines the top navigation bar,
 * left sidebar navigation, and dynamic content area. This layout provides the
 * consistent structure for Profile, Help, and About pages.
 */
public class ProfileLayout extends JPanel {
    
    // Constants
    private static final int MIN_CONTENT_WIDTH = 400;
    
    // UI Components
    private final HeroBar heroBar;
    private final NavigationSidebar navigationSidebar;
    private final JPanel contentPanel;
    private final CardLayout contentCardLayout;
    
    // Content screens
    private final ProfileScreen profileScreen;
    private final HelpScreen helpScreen;
    private final AboutScreen aboutScreen;
    
    /**
     * Creates a new ProfileLayout with navigation and content areas.
     */
    public ProfileLayout() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BACKGROUND);
        
        // Create hero bar (top navigation)
        heroBar = new HeroBar();
        
        // Create navigation sidebar
        navigationSidebar = new NavigationSidebar();
        
        // Create content area with card layout for switching between screens
        contentCardLayout = new CardLayout();
        contentPanel = new JPanel(contentCardLayout);
        contentPanel.setBackground(AppTheme.BACKGROUND);
        contentPanel.setPreferredSize(new Dimension(MIN_CONTENT_WIDTH, 0));
        
        // Create content screens
        profileScreen = new ProfileScreen();
        helpScreen = new HelpScreen();
        aboutScreen = new AboutScreen();
        
        // Add screens to content panel
        contentPanel.add(profileScreen, NavigationSidebar.PROFILE_PAGE);
        contentPanel.add(helpScreen, NavigationSidebar.HELP_PAGE);
        contentPanel.add(aboutScreen, NavigationSidebar.ABOUT_PAGE);
        
        // Set up navigation callbacks
        setupNavigationCallbacks();
        
        // Create main content area (sidebar + content)
        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBackground(AppTheme.BACKGROUND);
        mainContentPanel.add(navigationSidebar, BorderLayout.WEST);
        mainContentPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Add components to main layout
        add(heroBar, BorderLayout.NORTH);
        add(mainContentPanel, BorderLayout.CENTER);
        
        // Show profile screen by default
        showProfilePage();
    }
    
    /**
     * Sets up navigation callbacks for the sidebar and hero bar.
     */
    private void setupNavigationCallbacks() {
        // Set up sidebar navigation callback
        navigationSidebar.setNavigationCallback(pageId -> {
            Logger.getInstance().info("Navigating to profile system page: " + pageId);
            
            switch (pageId) {
                case NavigationSidebar.PROFILE_PAGE -> showProfilePage();
                case NavigationSidebar.HELP_PAGE -> showHelpPage();
                case NavigationSidebar.ABOUT_PAGE -> showAboutPage();
                default -> {
                    Logger.getInstance().warning("Unknown profile page requested: " + pageId);
                    showProfilePage(); // Default to profile page
                }
            }
        });
        
        // Set up logout callback
        navigationSidebar.setLogoutCallback(() -> {
            Logger.getInstance().info("User logging out from profile system");
            
            // Navigate back to sign-in screen
            try {
                ViewNavigator.getInstance().navigateTo("signIn");
            } catch (Exception e) {
                Logger.getInstance().error("Error navigating to sign-in screen: " + e.getMessage());
            }
        });
        
        // Set up hero bar search callback (if needed)
        heroBar.setSearchCallback(query -> {
            // For now, log the search - could be enhanced to search within profile content
            Logger.getInstance().info("Search performed from profile system: " + query);
        });
    }
    
    /**
     * Shows the profile page.
     */
    public void showProfilePage() {
        contentCardLayout.show(contentPanel, NavigationSidebar.PROFILE_PAGE);
        navigationSidebar.setActiveButton(NavigationSidebar.PROFILE_PAGE);
        profileScreen.refresh();
        Logger.getInstance().info("Showing profile page");
    }
    
    /**
     * Shows the help page.
     */
    public void showHelpPage() {
        contentCardLayout.show(contentPanel, NavigationSidebar.HELP_PAGE);
        navigationSidebar.setActiveButton(NavigationSidebar.HELP_PAGE);
        helpScreen.refresh();
        Logger.getInstance().info("Showing help page");
    }
    
    /**
     * Shows the about page.
     */
    public void showAboutPage() {
        contentCardLayout.show(contentPanel, NavigationSidebar.ABOUT_PAGE);
        navigationSidebar.setActiveButton(NavigationSidebar.ABOUT_PAGE);
        aboutScreen.refresh();
        Logger.getInstance().info("Showing about page");
    }
    
    /**
     * Gets the currently active page identifier.
     * 
     * @return The active page identifier
     */
    public String getActivePage() {
        return navigationSidebar.getActivePage();
    }
    
    /**
     * Refreshes the content of the currently active page.
     */
    public void refreshCurrentPage() {
        String activePage = getActivePage();
        
        switch (activePage) {
            case NavigationSidebar.PROFILE_PAGE -> profileScreen.refresh();
            case NavigationSidebar.HELP_PAGE -> helpScreen.refresh();
            case NavigationSidebar.ABOUT_PAGE -> aboutScreen.refresh();
            default -> Logger.getInstance().warning("Cannot refresh unknown page: " + activePage);
        }
    }
    
    /**
     * Gets the profile screen instance.
     * 
     * @return The profile screen
     */
    public ProfileScreen getProfileScreen() {
        return profileScreen;
    }
    
    /**
     * Gets the help screen instance.
     * 
     * @return The help screen
     */
    public HelpScreen getHelpScreen() {
        return helpScreen;
    }
    
    /**
     * Gets the about screen instance.
     * 
     * @return The about screen
     */
    public AboutScreen getAboutScreen() {
        return aboutScreen;
    }
    
    /**
     * Gets the navigation sidebar instance.
     * 
     * @return The navigation sidebar
     */
    public NavigationSidebar getNavigationSidebar() {
        return navigationSidebar;
    }
    
    /**
     * Gets the hero bar instance.
     * 
     * @return The hero bar
     */
    public HeroBar getHeroBar() {
        return heroBar;
    }
}