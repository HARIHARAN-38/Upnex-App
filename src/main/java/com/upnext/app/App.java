package com.upnext.app;

import com.upnext.app.core.Logger;
import com.upnext.app.domain.User;
import com.upnext.app.service.AuthService;
import com.upnext.app.ui.components.FeedbackManager;
import com.upnext.app.ui.navigation.ViewNavigator;
import com.upnext.app.ui.screens.CreateAccountScreen;
import com.upnext.app.ui.screens.HomeScreen;
import com.upnext.app.ui.screens.SignInScreen;
import com.upnext.app.ui.theme.AppTheme;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * Entry point for the UpNext desktop application.
 */
public final class App {
    // Screen identifiers
    public static final String SIGN_IN_SCREEN = "signIn";
    public static final String CREATE_ACCOUNT_SCREEN = "createAccount";
    public static final String HOME_SCREEN = "home";
    
    private static final Logger logger = Logger.getInstance();
    
    private App() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::start);
    }

    private static void start() {
        try {
            logger.info("Starting UpNext application");
            
            // Initialize theme
            AppTheme.apply();
            
            // Initialize database schema
            try {
                logger.info("Checking database connection and schema on startup...");
                boolean schemaInitialized = com.upnext.app.data.SchemaInitializer.initialize();
                if (!schemaInitialized) {
                    FeedbackManager.showError(null, 
                        "Could not initialize database schema. Check log file for details.", 
                        "Database Error");
                }
            } catch (Exception e) {
                logger.logException("Failed to initialize schema", e);
                FeedbackManager.showException(null, 
                    "Database schema initialization failed", 
                    "Startup Error", e);
            }

            JFrame frame = new JFrame("UpNext");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setSize(new Dimension(1024, 640));
            frame.setLocationRelativeTo(null);

            // Container for all screens
            JPanel contentPanel = new JPanel();
            frame.add(contentPanel);
            
            // Initialize the ViewNavigator
            ViewNavigator navigator = ViewNavigator.initialize(contentPanel);
            
            // Create and register screens
            SignInScreen signInScreen = new SignInScreen();
            CreateAccountScreen createAccountScreen = new CreateAccountScreen();
            HomeScreen homeScreen = new HomeScreen();
            
            navigator.registerScreen(SIGN_IN_SCREEN, signInScreen);
            navigator.registerScreen(CREATE_ACCOUNT_SCREEN, createAccountScreen);
            navigator.registerScreen(HOME_SCREEN, homeScreen);
            
            // Setup navigation
            setupNavigation(signInScreen, createAccountScreen, homeScreen);
            
            // Start with sign-in screen
            navigator.navigateTo(SIGN_IN_SCREEN);
            logger.info("Application UI initialized, showing sign-in screen");

            frame.setVisible(true);
        } catch (Exception e) {
            logger.logException("Failed to start application", e);
            FeedbackManager.showException(null, "The application failed to start properly.", "Startup Error", e);
            System.exit(1);
        }
    }
    
    /**
     * Sets up the navigation between screens.
     * 
     * @param signInScreen The sign-in screen
     * @param createAccountScreen The create account screen
     * @param homeScreen The home screen
     */
    private static void setupNavigation(
            SignInScreen signInScreen,
            CreateAccountScreen createAccountScreen,
            HomeScreen homeScreen) {
        
        final ViewNavigator navigator = ViewNavigator.getInstance();
        final AuthService authService = AuthService.getInstance();
        
        // Sign In Screen -> Create Account Screen
        signInScreen.getCreateAccountLink().addActionListener(_ -> navigator.navigateTo(CREATE_ACCOUNT_SCREEN));
        
        // Sign In Screen -> Home Screen (real authentication)
        signInScreen.getSignInButton().addActionListener(_ -> {
            String email = signInScreen.getEmailField().getText();
            String password = new String(signInScreen.getPasswordField().getPassword());
            
            try {
                logger.info("Attempting sign in for: " + email);
                
                // Attempt to sign in
                User user = authService.signIn(email, password);
                
                // Clear sensitive fields
                signInScreen.getEmailField().setText("");
                signInScreen.getPasswordField().clear();
                
                // Navigate to home screen on success
                navigator.navigateTo(HOME_SCREEN);
                
                // Update home screen welcome message
                homeScreen.updateWelcomeMessage();
                
                // Log success and show welcome message
                logger.info("User signed in successfully: " + user.getEmail());
                FeedbackManager.showSuccess(
                    signInScreen,
                    "Welcome back, " + user.getName() + "!",
                    "Sign In Successful"
                );
                
            } catch (AuthService.AuthException ex) {
                // Log failure and show error message
                logger.warning("Sign in failed: " + ex.getMessage());
                FeedbackManager.showError(
                    signInScreen,
                    ex.getMessage(),
                    "Authentication Error"
                );
            }
        });
        
        // Create Account Screen -> Sign In Screen
        createAccountScreen.getSignInLink().addActionListener(_ -> navigator.navigateTo(SIGN_IN_SCREEN));
        
        // Create Account Screen -> Sign In Screen (after successful account creation)
        createAccountScreen.getCreateAccountButton().addActionListener(_ -> {
            String name = createAccountScreen.getNameField().getText();
            String email = createAccountScreen.getEmailField().getText();
            String password = new String(createAccountScreen.getPasswordField().getPassword());
            String confirmPassword = new String(createAccountScreen.getConfirmPasswordField().getPassword());
            
            // Validate passwords match
            if (!password.equals(confirmPassword)) {
                logger.warning("Account creation validation failed: passwords don't match");
                FeedbackManager.showWarning(
                    createAccountScreen,
                    "Passwords do not match.",
                    "Validation Error"
                );
                return;
            }
            
            try {
                logger.info("Attempting to create account for: " + email);
                
                // Attempt to create account
                authService.signUp(name, email, password);
                
                // Clear sensitive fields
                createAccountScreen.getNameField().setText("");
                createAccountScreen.getEmailField().setText("");
                createAccountScreen.getPasswordField().clear();
                createAccountScreen.getConfirmPasswordField().clear();
                
                // Log success and show success message
                logger.info("Account created successfully for: " + email);
                FeedbackManager.showSuccess(
                    createAccountScreen,
                    "Account created successfully! Please sign in.",
                    "Account Created"
                );
                
                // Navigate to sign in screen
                navigator.navigateTo(SIGN_IN_SCREEN);
                
            } catch (AuthService.AuthException ex) {
                // Log failure and show error message
                logger.warning("Account creation failed: " + ex.getMessage());
                FeedbackManager.showError(
                    createAccountScreen,
                    ex.getMessage(),
                    "Account Creation Error"
                );
            }
        });
        
        // Home Screen -> Sign In Screen (sign out)
        homeScreen.getSignOutButton().addActionListener(_ -> {
            // Sign out the current user
            User user = authService.getCurrentUser();
            String email = user != null ? user.getEmail() : "unknown";
            
            logger.info("User signing out: " + email);
            authService.signOut();
            
            // Navigate back to sign in screen
            navigator.navigateTo(SIGN_IN_SCREEN);
            
            // Show sign out message
            logger.info("User signed out successfully: " + email);
            FeedbackManager.showInfo(
                homeScreen,
                "You have been signed out successfully.",
                "Sign Out"
            );
        });
    }
}
