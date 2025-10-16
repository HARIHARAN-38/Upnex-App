package com.upnext.app.ui.navigation;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

/**
 * Manages navigation between different screens/views in the application.
 * Provides methods to register screens and navigate between them.
 */
public class ViewNavigator {
    // Singleton instance
    private static ViewNavigator instance;
    
    // The container where screens will be swapped
    private final JPanel container;
    
    // CardLayout for managing screen transitions
    private final CardLayout cardLayout;
    
    // Map to store registered screens
    private final Map<String, JPanel> screens;
    
    // Current screen id
    private String currentScreen;
    
    /**
     * Private constructor to enforce singleton pattern.
     * 
     * @param container The container where screens will be swapped
     */
    private ViewNavigator(JPanel container) {
        this.container = container;
        this.cardLayout = new CardLayout();
        this.container.setLayout(cardLayout);
        this.screens = new HashMap<>();
        this.currentScreen = null;
    }
    
    /**
     * Initializes the navigator with the container where screens will be swapped.
     * 
     * @param container The container panel
     * @return The ViewNavigator singleton instance
     */
    public static ViewNavigator initialize(JPanel container) {
        if (instance == null) {
            instance = new ViewNavigator(container);
        } else {
            throw new IllegalStateException("ViewNavigator is already initialized");
        }
        return instance;
    }
    
    /**
     * Gets the singleton instance of the ViewNavigator.
     * 
     * @return The ViewNavigator singleton instance
     * @throws IllegalStateException if the navigator has not been initialized
     */
    public static ViewNavigator getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ViewNavigator is not initialized. Call initialize() first.");
        }
        return instance;
    }
    
    /**
     * Registers a screen with the navigator.
     * 
     * @param screenId The unique identifier for the screen
     * @param screen The screen panel to register
     */
    public void registerScreen(String screenId, JPanel screen) {
        screens.put(screenId, screen);
        container.add(screen, screenId);
    }
    
    /**
     * Navigates to the specified screen.
     * 
     * @param screenId The identifier of the screen to navigate to
     * @throws IllegalArgumentException if the screen ID is not registered
     */
    public void navigateTo(String screenId) {
        if (!screens.containsKey(screenId)) {
            throw new IllegalArgumentException("Screen not registered: " + screenId);
        }
        
        currentScreen = screenId;
        cardLayout.show(container, screenId);
    }
    
    /**
     * Gets the current screen ID.
     * 
     * @return The current screen ID, or null if no screen is showing
     */
    public String getCurrentScreen() {
        return currentScreen;
    }
    
    /**
     * Checks if a screen with the specified ID is registered.
     * 
     * @param screenId The screen ID to check
     * @return True if the screen is registered, false otherwise
     */
    public boolean hasScreen(String screenId) {
        return screens.containsKey(screenId);
    }
    
    /**
     * Returns the screen panel for the given ID.
     * 
     * @param screenId The ID of the screen to retrieve
     * @return The screen panel, or null if not found
     */
    public JPanel getScreen(String screenId) {
        return screens.get(screenId);
    }
}