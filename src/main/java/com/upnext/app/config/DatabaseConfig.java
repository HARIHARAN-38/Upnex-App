package com.upnext.app.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Configuration class for database connection parameters.
 * Loads and provides access to database connection properties.
 */
public final class DatabaseConfig {
    // Default database connection properties
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DATABASE = "upnex";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "hari";
    
    // Configuration properties
    private static final Properties properties = new Properties();
    private static boolean initialized = false;
    
    /**
     * Private constructor to prevent instantiation.
     */
    private DatabaseConfig() {
    }
    
    /**
     * Initializes the database configuration by loading properties.
     * If the properties file exists, it will be loaded.
     * Otherwise, default properties will be used.
     */
    public static void initialize() {
        if (initialized) {
            return;
        }
        
        // Try to load from properties file if it exists
        try {
            loadFromFile();
        } catch (IOException e) {
            // File not found or cannot be read, use defaults
            setDefaults();
        }
        
        initialized = true;
    }
    
    /**
     * Loads database properties from a properties file.
     *
     * @throws IOException If the properties file cannot be read
     */
    private static void loadFromFile() throws IOException {
        String configPath = "config/database.properties";
        try (InputStream input = Files.newInputStream(Paths.get(configPath))) {
            properties.load(input);
            
            // Validate required properties
            String[] requiredProps = {"db.host", "db.port", "db.name", "db.user", "db.password"};
            for (String prop : requiredProps) {
                if (!properties.containsKey(prop)) {
                    throw new IOException("Missing required property: " + prop);
                }
            }
        }
    }
    
    /**
     * Sets default database connection properties.
     */
    private static void setDefaults() {
        properties.setProperty("db.host", DEFAULT_HOST);
        properties.setProperty("db.port", DEFAULT_PORT);
        properties.setProperty("db.name", DEFAULT_DATABASE);
        properties.setProperty("db.user", DEFAULT_USER);
        properties.setProperty("db.password", DEFAULT_PASSWORD);
    }
    
    /**
     * Gets the database host.
     *
     * @return The database host
     */
    public static String getHost() {
        ensureInitialized();
        return properties.getProperty("db.host");
    }
    
    /**
     * Gets the database port.
     *
     * @return The database port
     */
    public static String getPort() {
        ensureInitialized();
        return properties.getProperty("db.port");
    }
    
    /**
     * Gets the database name.
     *
     * @return The database name
     */
    public static String getDatabase() {
        ensureInitialized();
        return properties.getProperty("db.name");
    }
    
    /**
     * Gets the database username.
     *
     * @return The database username
     */
    public static String getUser() {
        ensureInitialized();
        return properties.getProperty("db.user");
    }
    
    /**
     * Gets the database password.
     *
     * @return The database password
     */
    public static String getPassword() {
        ensureInitialized();
        return properties.getProperty("db.password");
    }
    
    /**
     * Gets the JDBC URL for connecting to the database.
     *
     * @return The JDBC URL
     */
    public static String getJdbcUrl() {
        ensureInitialized();
        return String.format("jdbc:mysql://%s:%s/%s",
                getHost(), getPort(), getDatabase());
    }
    
    /**
     * Ensures that the configuration is initialized.
     *
     * @throws IllegalStateException If the configuration is not initialized
     */
    private static void ensureInitialized() {
        if (!initialized) {
            initialize();
        }
    }
}