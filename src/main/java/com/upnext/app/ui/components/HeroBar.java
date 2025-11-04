package com.upnext.app.ui.components;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import com.upnext.app.core.Logger;
import com.upnext.app.domain.User;
import com.upnext.app.domain.question.Question;
import com.upnext.app.service.AuthService;
import com.upnext.app.service.SearchService;
import com.upnext.app.ui.navigation.ViewNavigator;
import com.upnext.app.ui.theme.AppTheme;

/**
 * Top navigation bar component that includes the app logo, search functionality,
 * and user profile controls. This component implements debounced search and
 * dropdown menu for user actions.
 */
public class HeroBar extends JPanel {
    // Constants
    private static final int PADDING_LARGE = 24;
    private static final int PADDING_MEDIUM = 16;
    private static final int PADDING_SMALL = 8;
    private static final int AVATAR_SIZE = 32;
    private static final int SEARCH_DEBOUNCE_MS = 300;
    
    // UI Components
    private final JLabel logoLabel;
    private final JTextField searchField;
    private final JLabel avatarLabel;
    private final JButton signOutButton;
    private final JPanel profilePanel;
    private final JPopupMenu profileMenu;
    
    // Search components
    private final ScheduledExecutorService searchExecutor;
    private java.util.concurrent.ScheduledFuture<?> searchFuture;
    private final SearchService searchService;
    private SearchResultsPanel searchResultPanel;
    private final JLayeredPane layeredPane;
    
    // Callbacks
    private Consumer<String> searchCallback;
    
    /**
     * Creates a new HeroBar component with search and profile functionality.
     */
    public HeroBar() {
        // Initialize services
        searchService = SearchService.getInstance();
        
        // Use layered pane for dropdown positioning
        layeredPane = new JLayeredPane();
        
        // Main layout setup
        setLayout(new BorderLayout(PADDING_MEDIUM, 0));
        setOpaque(false);
        setBorder(new EmptyBorder(0, 0, PADDING_MEDIUM, 0));
        
        // Initialize search executor for debouncing
        searchExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "Search-Debounce-Thread");
            thread.setDaemon(true); // Don't prevent app from exiting
            return thread;
        });
        
        // Logo/Title on the left
        logoLabel = new JLabel("UpNext");
        logoLabel.setFont(AppTheme.HEADING_FONT.deriveFont(24f));
        logoLabel.setForeground(AppTheme.PRIMARY);
        logoLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Navigate to home screen when logo is clicked
                ViewNavigator.getInstance().navigateTo(com.upnext.app.App.HOME_SCREEN);
            }
        });
        
        // Search box in the middle with placeholder text behavior
        searchField = createSearchField();
        
        // User profile and sign out on the right
        profilePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, PADDING_SMALL, 0));
        profilePanel.setOpaque(false);
        
        // Avatar with user initials (hidden for home page - profile accessed via profile card)
        avatarLabel = createAvatarLabel();
        avatarLabel.setVisible(false); // Hide avatar on home page
        
        // Sign out button
        signOutButton = new JButton("Sign Out");
        signOutButton.setForeground(AppTheme.ACCENT);
        signOutButton.setBorderPainted(false);
        signOutButton.setContentAreaFilled(false);
        signOutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Set up sign out action
        signOutButton.addActionListener(event -> {
            AuthService.getInstance().signOut();
            // Note: Navigation back to sign-in screen will be handled in App.java
        });
        
        // Create profile menu (kept for compatibility but hidden)
        profileMenu = createProfileMenu();
        
        // Avatar click listener disabled for home page
        // Profile navigation is now handled by the profile card "View Full Profile" button
        
        // Add components to profile panel (only sign out button visible)
        profilePanel.add(signOutButton);
        
        // Add components to the hero bar
        add(logoLabel, BorderLayout.WEST);
        add(searchField, BorderLayout.CENTER);
        add(profilePanel, BorderLayout.EAST);
        
        // Initialize search results panel
        initializeSearchResultsPanel();
        
        // Update UI with current user
        // Must call after fully initialized to avoid constructor issues
        SwingUtilities.invokeLater(this::updateProfileDisplay);
    }
    
    /**
     * Creates the search text field with placeholder text behavior
     * and debounced search functionality.
     *
     * @return Configured search field
     */
    private JTextField createSearchField() {
        JTextField field = new JTextField("Search questions...") {
            // Override to add a slight background color
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(0xF8F8F8));
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        
        field.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0)),
                BorderFactory.createEmptyBorder(PADDING_SMALL, PADDING_SMALL, PADDING_SMALL, PADDING_SMALL)));
        field.setForeground(AppTheme.TEXT_SECONDARY);
        
        // Focus listener for placeholder text behavior
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals("Search questions...")) {
                    field.setText("");
                    field.setForeground(AppTheme.TEXT_PRIMARY);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText("Search questions...");
                    field.setForeground(AppTheme.TEXT_SECONDARY);
                }
            }
        });
        
        // Key listener for debounced search
        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // Cancel previous search task if any
                if (searchFuture != null) {
                    searchFuture.cancel(false);
                }
                
                // Don't search for placeholder text
                if (field.getText().equals("Search questions...")) {
                    hideResults();
                    return;
                }
                
                // Create new search task
                final String query = field.getText();
                Runnable searchTask = () -> {
                    performSearch(query);
                    
                    // Also notify callback if provided (for external search handling)
                    if (searchCallback != null) {
                        searchCallback.accept(query);
                    }
                };
                
                // Schedule the search after debounce delay
                searchFuture = searchExecutor.schedule(searchTask, SEARCH_DEBOUNCE_MS, TimeUnit.MILLISECONDS);
            }
        });
        
        // Enter key for immediate search
        field.addActionListener(e -> {
            // Cancel debounced search
            if (searchFuture != null) {
                searchFuture.cancel(false);
            }
            
            // Don't search for placeholder text
            if (!field.getText().equals("Search questions...")) {
                performSearch(field.getText());
                
                // Also notify callback if provided
                if (searchCallback != null) {
                    searchCallback.accept(field.getText());
                }
            }
        });
        
        return field;
    }
    
    /**
     * Creates an avatar label with the user's initials
     * 
     * @return Configured avatar label
     */
    private JLabel createAvatarLabel() {
        JLabel label = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Get the current text (could be initials)
                String text = getText();
                
                // Draw circular background
                g2.setColor(AppTheme.PRIMARY);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                
                // Draw text if it exists
                if (text != null && !text.isEmpty()) {
                    g2.setColor(Color.WHITE);
                    g2.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.BOLD, 14f));
                    
                    FontMetrics fm = g2.getFontMetrics();
                    int textWidth = fm.stringWidth(text);
                    int textHeight = fm.getHeight();
                    
                    int x = (getWidth() - textWidth) / 2;
                    int y = (getHeight() - textHeight) / 2 + fm.getAscent();
                    
                    g2.drawString(text, x, y);
                }
                
                g2.dispose();
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(AVATAR_SIZE, AVATAR_SIZE);
            }
        };
        
        return label;
    }
    
    /**
     * Creates a popup menu for the profile avatar
     * 
     * @return Configured popup menu
     */
    private JPopupMenu createProfileMenu() {
        JPopupMenu menu = new JPopupMenu();
        menu.setBorder(BorderFactory.createLineBorder(new Color(0xE2E8F0)));
        
        // View Profile menu item
        JMenuItem profileMenuItem = new JMenuItem("View Profile");
        profileMenuItem.setFont(AppTheme.PRIMARY_FONT);
        profileMenuItem.addActionListener(e -> {
            try {
                Logger.getInstance().info("Navigating to profile layout from HeroBar");
                ViewNavigator.getInstance().navigateTo("profile-layout");
            } catch (Exception ex) {
                Logger.getInstance().error("Error navigating to profile layout: " + ex.getMessage());
                JOptionPane.showMessageDialog(
                    this,
                    "Error opening profile page. Please try again.",
                    "Navigation Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
        
        // Account Settings menu item
        JMenuItem settingsMenuItem = new JMenuItem("Account Settings");
        settingsMenuItem.setFont(AppTheme.PRIMARY_FONT);
        settingsMenuItem.addActionListener(e -> {
            // Will be implemented in future with proper navigation
            JOptionPane.showMessageDialog(
                this,
                "Account settings page coming soon!",
                "Feature Preview",
                JOptionPane.INFORMATION_MESSAGE
            );
        });
        
        // Sign Out menu item
        JMenuItem signOutMenuItem = new JMenuItem("Sign Out");
        signOutMenuItem.setFont(AppTheme.PRIMARY_FONT);
        signOutMenuItem.setForeground(AppTheme.ACCENT);
        signOutMenuItem.addActionListener(e -> {
            AuthService.getInstance().signOut();
            // Note: Navigation back to sign-in screen will be handled in App.java
        });
        
        // Add separator before sign out
        menu.add(profileMenuItem);
        menu.add(settingsMenuItem);
        menu.addSeparator();
        menu.add(signOutMenuItem);
        
        return menu;
    }
    
    /**
     * Updates the avatar label with user initials from name
     * Note: Avatar is hidden on home page - profile accessed via profile card
     */
    public void updateProfileDisplay() {
        User currentUser = AuthService.getInstance().getCurrentUser();
        if (currentUser != null) {
            avatarLabel.setText(getInitials(currentUser.getName()));
            // Keep avatar hidden - profile navigation is via profile card
            avatarLabel.setVisible(false);
        } else {
            avatarLabel.setText("?");
            avatarLabel.setVisible(false);
        }
    }
    
    /**
     * Gets the initials from a name (first letter of first and last name)
     * 
     * @param name The full name
     * @return Up to two initials
     */
    private String getInitials(String name) {
        if (name == null || name.isEmpty()) {
            return "?";
        }
        
        StringBuilder initials = new StringBuilder();
        String[] parts = name.trim().split("\\s+");
        
        // Add first letter of first name
        if (parts.length > 0 && !parts[0].isEmpty()) {
            initials.append(Character.toUpperCase(parts[0].charAt(0)));
        }
        
        // Add first letter of last name if available
        if (parts.length > 1 && !parts[parts.length - 1].isEmpty()) {
            initials.append(Character.toUpperCase(parts[parts.length - 1].charAt(0)));
        }
        
        return initials.toString();
    }
    
    /**
     * Sets a callback function for search actions.
     * This will be called whenever the user performs a search.
     * 
     * @param callback Consumer function that takes the search query
     */
    public void setSearchCallback(Consumer<String> callback) {
        this.searchCallback = callback;
    }
    
    /**
     * Gets the sign out button.
     * 
     * @return The sign out button
     */
    public JButton getSignOutButton() {
        return signOutButton;
    }
    
    /**
     * Initializes the search results panel to be shown when search is performed.
     */
    private void initializeSearchResultsPanel() {
        searchResultPanel = new SearchResultsPanel();
        searchResultPanel.setVisible(false);
        searchResultPanel.setQuestionSelectedListener(question -> {
            // Handle question selection
            hideResults();
            if (searchCallback != null) {
                // Forward selected question to callback
                searchCallback.accept("SELECTED:" + question.getId());
            }
        });
        
        // Add global click listener to hide search results when clicked outside
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (searchResultPanel != null && searchResultPanel.isVisible() && event instanceof MouseEvent) {
                MouseEvent mouseEvent = (MouseEvent) event;
                if (mouseEvent.getID() == MouseEvent.MOUSE_CLICKED) {
                    // Check if click is outside search panel and search field
                    Point point = SwingUtilities.convertPoint(
                        mouseEvent.getComponent(), mouseEvent.getPoint(), this);
                    
                    if (!searchField.getBounds().contains(point) && 
                        !searchResultPanel.getBounds().contains(point)) {
                        hideResults();
                    }
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);
        
        // Add the search result panel to the parent container
        // This will be properly positioned when search results are shown
        Container parent = SwingUtilities.getWindowAncestor(this);
        if (parent == null) {
            // If not yet in a window, add a component listener to do this when added
            addAncestorListener(new javax.swing.event.AncestorListener() {
                @Override
                public void ancestorAdded(javax.swing.event.AncestorEvent event) {
                    Container window = SwingUtilities.getWindowAncestor(HeroBar.this);
                    if (window != null) {
                        JLayeredPane lp = JLayeredPane.getLayeredPaneAbove(HeroBar.this);
                        if (lp != null) {
                            lp.add(searchResultPanel, JLayeredPane.POPUP_LAYER);
                        }
                    }
                    removeAncestorListener(this);
                }
                
                @Override
                public void ancestorRemoved(javax.swing.event.AncestorEvent event) {}
                
                @Override
                public void ancestorMoved(javax.swing.event.AncestorEvent event) {}
            });
        } else if (parent instanceof JFrame frame) {
            frame.getLayeredPane().add(searchResultPanel, JLayeredPane.POPUP_LAYER);
        } else if (parent instanceof JDialog dialog) {
            dialog.getLayeredPane().add(searchResultPanel, JLayeredPane.POPUP_LAYER);
        }
    }
    
    /**
     * Shows search results in the dropdown panel.
     * 
     * @param results The list of questions to display
     */
    private void showResults(List<Question> results) {
        if (searchResultPanel == null || results == null) {
            return;
        }
        
        searchResultPanel.setQuestions(results);
        
        // Position the results panel below the search field
        Point p = searchField.getLocationOnScreen();
        SwingUtilities.convertPointFromScreen(p, SwingUtilities.getWindowAncestor(this));
        
        int width = searchField.getWidth();
        searchResultPanel.setBounds(
            p.x, 
            p.y + searchField.getHeight(), 
            width,
            searchResultPanel.getPreferredSize().height
        );
        
        searchResultPanel.setVisible(true);
        searchResultPanel.repaint();
    }
    
    /**
     * Hides the search results panel.
     */
    private void hideResults() {
        if (searchResultPanel != null) {
            searchResultPanel.setVisible(false);
        }
    }
    
    /**
     * Performs a search using the SearchService.
     * 
     * @param query The query text
     */
    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            hideResults();
            return;
        }
        
        try {
            List<Question> results = searchService.search(query, 10, 0);
            SwingUtilities.invokeLater(() -> {
                if (searchField.getText().trim().equals(query)) {
                    if (results.isEmpty()) {
                        // Show a "no results" message
                        showResults(Collections.emptyList());
                    } else {
                        showResults(results);
                    }
                }
            });
        } catch (Exception e) {
            Logger.getInstance().logException("Error performing search", e);
            hideResults();
        }
    }

    /**
     * Cleans up resources used by this component.
     * Should be called when the component is no longer needed.
     */
    public void cleanup() {
        searchExecutor.shutdown();
    }
}