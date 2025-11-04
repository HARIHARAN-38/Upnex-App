package com.upnext.app.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import com.upnext.app.core.Logger;
import com.upnext.app.data.UserRepository;
import com.upnext.app.domain.Skill;
import com.upnext.app.domain.User;

/**
 * Service for user authentication operations.
 * Handles sign-up, sign-in, and password management.
 */
public class AuthService {
    // Singleton instance
    private static AuthService instance;
    
    // Dependencies
    private final UserRepository userRepository;
    private final SkillService skillService;
    private final Logger logger = Logger.getInstance();
    
    // Current user session
    private User currentUser;
    
    // Security configuration
    private static final int SALT_LENGTH = 16;
    private static final String HASH_ALGORITHM = "SHA-256";
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private AuthService() {
        this.userRepository = UserRepository.getInstance();
        this.skillService = SkillService.getInstance();
    }
    
    /**
     * Gets the singleton instance of the service.
     * 
     * @return The service instance
     */
    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }
    
    /**
     * Signs up a new user with the provided credentials.
     * 
     * @param name The user's full name
     * @param email The user's email address
     * @param password The user's password
     * @return The newly created user
     * @throws AuthException If there's an error during sign-up
     */
    public User signUp(String name, String email, String password) throws AuthException {
        return signUp(name, email, password, null);
    }
    
    /**
     * Signs up a new user with the provided credentials and initial skills.
     * 
     * @param name The user's full name
     * @param email The user's email address
     * @param password The user's password
     * @param initialSkills List of initial skills to add for the user
     * @return The newly created user
     * @throws AuthException If there's an error during sign-up
     */
    public User signUp(String name, String email, String password, List<Skill> initialSkills) throws AuthException {
        try {
            logger.info("Sign-up attempt for email: " + email);
            
            // Check if user already exists
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent()) {
                logger.warning("Sign-up failed: email already exists: " + email);
                throw new AuthException("A user with this email already exists");
            }
            
            // Generate salt and hash password
            String salt = generateSalt();
            String passwordHash = hashPassword(password, salt);
            
            // Create and save new user
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPasswordHash(passwordHash);
            user.setSalt(salt);
            user.setActive(true);
            
            User savedUser = userRepository.save(user);
            logger.info("User created successfully: " + email);
            
            // Add initial skills if provided
            if (initialSkills != null && !initialSkills.isEmpty()) {
                try {
                    skillService.addSkills(savedUser.getId(), initialSkills);
                    // Update the user object with the added skills
                    loadUserSkills(savedUser);
                } catch (SkillService.SkillException e) {
                    logger.logException("Error adding initial skills during sign-up", e);
                    // Continue with sign-up even if adding skills fails
                }
            }
            
            return savedUser;
        } catch (SQLException e) {
            logger.logException("Database error during sign-up", e);
            throw new AuthException("Database error during sign-up: " + e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            logger.logException("Error hashing password during sign-up", e);
            throw new AuthException("Error hashing password: " + e.getMessage(), e);
        }
    }
    
    /**
     * Signs in a user with the provided credentials.
     * 
     * @param email The user's email address
     * @param password The user's password
     * @return The authenticated user
     * @throws AuthException If the credentials are invalid or there's an error during sign-in
     */
    public User signIn(String email, String password) throws AuthException {
        try {
            logger.info("Sign-in attempt for email: " + email);
            
            // Find user by email
            Optional<User> optionalUser = userRepository.findByEmail(email);
            if (!optionalUser.isPresent()) {
                logger.warning("Sign-in failed: user not found: " + email);
                throw new AuthException("Invalid email or password");
            }
            
            User user = optionalUser.get();
            
            // Check if user is active
            if (!user.isActive()) {
                logger.warning("Sign-in failed: account deactivated: " + email);
                throw new AuthException("This account has been deactivated");
            }
            
            // Verify password
            String passwordHash = hashPassword(password, user.getSalt());
            if (!passwordHash.equals(user.getPasswordHash())) {
                logger.warning("Sign-in failed: invalid password for: " + email);
                throw new AuthException("Invalid email or password");
            }
            
            // Set current user and load skills
            currentUser = user;
            loadUserSkills(currentUser);
            logger.info("User signed in successfully: " + email);
            return user;
        } catch (SQLException e) {
            logger.logException("Database error during sign-in", e);
            throw new AuthException("Database error during sign-in: " + e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            logger.logException("Error verifying password during sign-in", e);
            throw new AuthException("Error verifying password: " + e.getMessage(), e);
        }
    }
    
    /**
     * Signs out the current user.
     */
    public void signOut() {
        if (currentUser != null) {
            logger.info("User signed out: " + currentUser.getEmail());
            currentUser = null;
        } else {
            logger.warning("Sign out attempted with no active user session");
        }
    }
    
    /**
     * Gets the currently signed-in user with their skills loaded.
     * 
     * @return The current user with skills loaded, or null if no user is signed in
     */
    public User getCurrentUser() {
        if (currentUser != null && currentUser.getSkills() == null) {
            loadUserSkills(currentUser);
        }
        return currentUser;
    }
    
    /**
     * Loads skills for a user.
     * 
     * @param user The user to load skills for
     */
    private void loadUserSkills(User user) {
        try {
            List<Skill> skills = skillService.getUserSkills(user.getId());
            user.setSkills(skills);
        } catch (SkillService.SkillException e) {
            logger.logException("Error loading user skills", e);
            // Don't throw the exception, just log it
        }
    }
    
    /**
     * Adds a skill for the currently signed-in user.
     * 
     * @param skillName The name of the skill
     * @param description The description of the skill
     * @param proficiencyLevel The proficiency level (1-10)
     * @return The added skill
     * @throws AuthException If no user is signed in or there's an error adding the skill
     */
    public Skill addSkillForCurrentUser(String skillName, String description, int proficiencyLevel) throws AuthException {
        if (currentUser == null) {
            throw new AuthException("No user is currently signed in");
        }
        
        try {
            Skill skill = skillService.addSkill(currentUser.getId(), skillName, description, proficiencyLevel);
            currentUser.addSkill(skill);
            return skill;
        } catch (SkillService.SkillException e) {
            logger.logException("Error adding skill for current user", e);
            throw new AuthException("Error adding skill: " + e.getMessage(), e);
        }
    }
    
    /**
     * Deletes a skill for the currently signed-in user.
     * 
     * @param skillId The ID of the skill to delete
     * @throws AuthException If no user is signed in or there's an error deleting the skill
     */
    public void deleteSkillForCurrentUser(Long skillId) throws AuthException {
        if (currentUser == null) {
            throw new AuthException("No user is currently signed in");
        }
        
        try {
            skillService.deleteSkill(skillId);
            currentUser.removeSkill(skillId);
        } catch (SkillService.SkillException e) {
            logger.logException("Error deleting skill for current user", e);
            throw new AuthException("Error deleting skill: " + e.getMessage(), e);
        }
    }
    
    /**
     * Updates a skill for the currently signed-in user.
     * 
     * @param skillId The ID of the skill to update
     * @param skillName The updated name
     * @param description The updated description
     * @param proficiencyLevel The updated proficiency level
     * @return The updated skill
     * @throws AuthException If no user is signed in or there's an error updating the skill
     */
    public Skill updateSkillForCurrentUser(Long skillId, String skillName, String description, int proficiencyLevel) throws AuthException {
        if (currentUser == null) {
            throw new AuthException("No user is currently signed in");
        }
        
        try {
            Skill skill = skillService.updateSkill(skillId, skillName, description, proficiencyLevel);
            // Refresh user skills after update
            loadUserSkills(currentUser);
            return skill;
        } catch (SkillService.SkillException e) {
            logger.logException("Error updating skill for current user", e);
            throw new AuthException("Error updating skill: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets all skills for the currently signed-in user.
     * 
     * @return The list of skills for the current user
     * @throws AuthException If no user is signed in
     */
    public List<Skill> getCurrentUserSkills() throws AuthException {
        if (currentUser == null) {
            throw new AuthException("No user is currently signed in");
        }
        
        // Make sure the skills are loaded
        loadUserSkills(currentUser);
        return currentUser.getSkills();
    }
    
    /**
     * Checks if a user is currently signed in.
     * 
     * @return true if a user is signed in, false otherwise
     */
    public boolean isSignedIn() {
        return currentUser != null;
    }
    
    /**
     * Generates a random salt for password hashing.
     * 
     * @return A base64-encoded salt string
     */
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * Hashes a password with the provided salt.
     * 
     * @param password The password to hash
     * @param salt The salt to use
     * @return The hashed password
     * @throws NoSuchAlgorithmException If the hashing algorithm is not available
     */
    private String hashPassword(String password, String salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
        
        // Combine password and salt
        String passwordWithSalt = password + salt;
        
        // Hash the combination
        byte[] hashedBytes = md.digest(passwordWithSalt.getBytes(StandardCharsets.UTF_8));
        
        // Convert to base64 string
        return Base64.getEncoder().encodeToString(hashedBytes);
    }
    
    /**
     * Exception for authentication errors.
     */
    public static class AuthException extends Exception {
        public AuthException(String message) {
            super(message);
        }
        
        public AuthException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}