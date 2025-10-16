package com.upnext.app.domain;

/**
 * Represents a user in the UpNext application.
 * Contains user identity and authentication information.
 */
public class User {
    private Long id;
    private String name;
    private String email;
    private String passwordHash;
    private String salt;
    private boolean active;
    private String createdAt;
    
    /**
     * Creates a new user with default values.
     */
    public User() {
        this.active = true;
    }
    
    /**
     * Creates a new user with the specified details.
     * 
     * @param name The user's full name
     * @param email The user's email address
     */
    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.active = true;
    }
    
    /**
     * Creates a new user with the specified details.
     * 
     * @param id The user's unique ID
     * @param name The user's full name
     * @param email The user's email address
     * @param passwordHash The user's hashed password
     * @param salt The salt used for password hashing
     * @param active Whether the user is active
     * @param createdAt The timestamp when the user was created
     */
    public User(Long id, String name, String email, String passwordHash, String salt, boolean active, String createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.active = active;
        this.createdAt = createdAt;
    }

    /**
     * Gets the user's unique ID.
     * 
     * @return The user ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the user's unique ID.
     * 
     * @param id The user ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the user's full name.
     * 
     * @return The user's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user's full name.
     * 
     * @param name The user's name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the user's email address.
     * 
     * @return The user's email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     * 
     * @param email The user's email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the user's hashed password.
     * 
     * @return The password hash
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Sets the user's hashed password.
     * 
     * @param passwordHash The password hash
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    /**
     * Gets the salt used for password hashing.
     * 
     * @return The salt
     */
    public String getSalt() {
        return salt;
    }
    
    /**
     * Sets the salt used for password hashing.
     * 
     * @param salt The salt
     */
    public void setSalt(String salt) {
        this.salt = salt;
    }

    /**
     * Checks if the user account is active.
     * 
     * @return true if the user is active, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets whether the user account is active.
     * 
     * @param active true to activate the user, false to deactivate
     */
    public void setActive(boolean active) {
        this.active = active;
    }
    
    /**
     * Gets the timestamp when the user was created.
     * 
     * @return The creation timestamp
     */
    public String getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Sets the timestamp when the user was created.
     * 
     * @param createdAt The creation timestamp
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", active=" + active +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}