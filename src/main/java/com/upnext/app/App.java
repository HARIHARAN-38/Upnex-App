package com.upnext.app;

import java.awt.Dimension;
 import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.upnext.app.core.Logger;
import com.upnext.app.domain.Skill;
import com.upnext.app.domain.User;
import com.upnext.app.service.AuthService;
import com.upnext.app.ui.components.FeedbackManager;
import com.upnext.app.ui.navigation.ViewNavigator;
import com.upnext.app.ui.screens.AddQuestionScreen;
import com.upnext.app.ui.screens.CreateAccountScreen;
import com.upnext.app.ui.screens.HomeScreen;
import com.upnext.app.ui.screens.QuestionDetailScreen;
import com.upnext.app.ui.screens.SignInScreen;
import com.upnext.app.ui.screens.SkillAddScreen;
import com.upnext.app.ui.screens.SkillsetScreen;
import com.upnext.app.ui.theme.AppTheme;

/**
 * Entry point for the UpNext desktop application.
 */
public final class App {
    // Screen identifiers
    public static final String SIGN_IN_SCREEN = "signIn";
    public static final String CREATE_ACCOUNT_SCREEN = "createAccount";
    public static final String HOME_SCREEN = "home";
    public static final String SKILLSET_SCREEN = "skillset";
    public static final String SKILL_ADD_SCREEN = "add-skill";
    public static final String QUESTION_DETAIL_SCREEN = "question-detail";
    public static final String ADD_QUESTION_SCREEN = "add-question";
    
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
            SkillsetScreen skillsetScreen = new SkillsetScreen();
            SkillAddScreen skillAddScreen = new SkillAddScreen();
            QuestionDetailScreen questionDetailScreen = new QuestionDetailScreen();
            AddQuestionScreen addQuestionScreen = new AddQuestionScreen();
            
            navigator.registerScreen(SIGN_IN_SCREEN, signInScreen);
            navigator.registerScreen(CREATE_ACCOUNT_SCREEN, createAccountScreen);
            navigator.registerScreen(HOME_SCREEN, homeScreen);
            navigator.registerScreen(SKILLSET_SCREEN, skillsetScreen);
            navigator.registerScreen(SKILL_ADD_SCREEN, skillAddScreen);
            navigator.registerScreen(QUESTION_DETAIL_SCREEN, questionDetailScreen);
            navigator.registerScreen(ADD_QUESTION_SCREEN, addQuestionScreen);
            
            // Setup navigation
            setupNavigation(signInScreen, createAccountScreen, homeScreen, skillsetScreen, skillAddScreen, questionDetailScreen, addQuestionScreen);
            
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
     * @param skillsetScreen The skillset screen
     * @param skillAddScreen The skill add screen
     * @param questionDetailScreen The question detail screen
     * @param addQuestionScreen The add question screen
     */
    private static void setupNavigation(
            SignInScreen signInScreen,
            CreateAccountScreen createAccountScreen,
            HomeScreen homeScreen,
            SkillsetScreen skillsetScreen,
            SkillAddScreen skillAddScreen,
            QuestionDetailScreen questionDetailScreen,
            AddQuestionScreen addQuestionScreen) {
        
        final ViewNavigator navigator = ViewNavigator.getInstance();
        final AuthService authService = AuthService.getInstance();
        
        // Sign In Screen -> Create Account Screen
    signInScreen.getCreateAccountLink().addActionListener(event -> navigator.navigateTo(CREATE_ACCOUNT_SCREEN));
        
        // Sign In Screen -> Home Screen (real authentication)
    signInScreen.getSignInButton().addActionListener(event -> {
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
    createAccountScreen.getSignInLink().addActionListener(event -> navigator.navigateTo(SIGN_IN_SCREEN));
        
        // Create Account Screen -> Skillset Screen (after validating initial data)
    createAccountScreen.getCreateAccountButton().addActionListener(event -> {
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

            // Basic validation for other fields
            if (name.trim().isEmpty()) {
                FeedbackManager.showWarning(
                    createAccountScreen,
                    "Please enter your name.",
                    "Validation Error"
                );
                return;
            }
            
            if (email.trim().isEmpty()) {
                FeedbackManager.showWarning(
                    createAccountScreen,
                    "Please enter your email address.",
                    "Validation Error"
                );
                return;
            }
            
            // Store user data in skillset screen for later use
            skillsetScreen.setUserData(name, email, password);
            
            // Navigate to skillset screen to collect skills
            logger.info("Proceeding to skills collection for: " + email);
            navigator.navigateTo(SKILLSET_SCREEN);
        });
        
        // Home Screen -> Sign In Screen (sign out)
    homeScreen.getSignOutButton().addActionListener(event -> {
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
        
        // SkillsetScreen -> CreateAccountScreen (back button)
    skillsetScreen.getBackButton().addActionListener(event -> {
            navigator.navigateTo(CREATE_ACCOUNT_SCREEN);
        });
        
        // SkillsetScreen -> SkillAddScreen (add new skill)
    skillsetScreen.getAddSkillButton().addActionListener(event -> {
            navigator.navigateTo(SKILL_ADD_SCREEN);
        });
        
        // SkillAddScreen navigation is handled within the SkillAddScreen class itself
        // We're keeping the skillAddScreen parameter for completeness and future extensions
        
        // SkillsetScreen -> HomeScreen (after final account creation)
    skillsetScreen.getCreateAccountButton().addActionListener(event -> {
            try {
                // Get user registration data stored in the skillset screen
                final String userName = skillsetScreen.getUserName();
                final String userEmail = skillsetScreen.getUserEmail(); 
                final String userPassword = skillsetScreen.getUserPassword();
                
                // Check if we have all required user data
                if (userName == null || userEmail == null || userPassword == null || 
                    userName.isEmpty() || userEmail.isEmpty() || userPassword.isEmpty()) {
                    throw new IllegalStateException("Missing user registration data");
                }
                
                // Ensure at least one skill was added
                if (skillsetScreen.getPendingSkills().isEmpty()) {
                    FeedbackManager.showWarning(
                        skillsetScreen,
                        "Please add at least one skill to continue.",
                        "Validation Error"
                    );
                    return;
                }
                
                logger.info("Attempting to create account for: " + userEmail);
                
                // Get the list of skills to be associated with the account
                List<Skill> userSkills = skillsetScreen.getPendingSkills();
                logger.info("Creating account with " + userSkills.size() + " skills for: " + userEmail);
                
                // Create account with skills
                authService.signUp(userName, userEmail, userPassword, userSkills);
                
                // Clear skill list after successful account creation
                skillsetScreen.clearSkills();
                
                // Navigate to home screen
                navigator.navigateTo(HOME_SCREEN);
                
                // Update home screen welcome message
                homeScreen.updateWelcomeMessage();
                
                // Log success and show welcome message
                logger.info("Account created successfully for: " + userEmail);
                FeedbackManager.showSuccess(
                    skillsetScreen,
                    "Welcome to UpNext, " + userName + "!",
                    "Account Created"
                );
                
            } catch (IllegalStateException ex) {
                // This should not happen in normal flow
                logger.error("Account creation failed: " + ex.getMessage());
                FeedbackManager.showError(
                    skillsetScreen,
                    "An error occurred during account creation. Please try again.",
                    "Account Creation Error"
                );
                
                // Return to create account screen
                navigator.navigateTo(CREATE_ACCOUNT_SCREEN);
                
            } catch (AuthService.AuthException ex) {
                // Handle authentication service errors
                logger.warning("Account creation failed: " + ex.getMessage());
                FeedbackManager.showError(
                    skillsetScreen,
                    ex.getMessage(),
                    "Account Creation Error"
                );
            }
        });
        
        // AddQuestionScreen -> Home Screen (navigation back and after successful question creation)
        addQuestionScreen.setOnNavigateBack(() -> {
            logger.info("Navigating back from Add Question screen to Home");
            navigator.navigateTo(HOME_SCREEN);
        });
        
        addQuestionScreen.setOnQuestionCreated(question -> {
            logger.info("Question created successfully, returning to Home and refreshing feed");
            // Refresh the home screen's question feed with the new question
            homeScreen.addNewQuestionToFeed(question);
            navigator.navigateTo(HOME_SCREEN);
        });
    }
}
