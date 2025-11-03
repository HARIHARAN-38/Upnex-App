package com.upnext.app.test;

import java.sql.Connection;
import java.sql.SQLException;

import com.upnext.app.data.JdbcConnectionProvider;

/**
 * Simple test to check database connectivity.
 */
public class DatabaseTest {
    public static void main(String[] args) {
        System.out.println("Testing database connection...");
        
        try {
            JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
            System.out.println("Connection provider initialized successfully");
            
            Connection connection = provider.getConnection();
            System.out.println("Database connection established successfully!");
            System.out.println("Connection URL: " + connection.getMetaData().getURL());
            System.out.println("Database Product: " + connection.getMetaData().getDatabaseProductName());
            System.out.println("Database Version: " + connection.getMetaData().getDatabaseProductVersion());
            
            provider.releaseConnection(connection);
            System.out.println("Connection released successfully");
            
        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}