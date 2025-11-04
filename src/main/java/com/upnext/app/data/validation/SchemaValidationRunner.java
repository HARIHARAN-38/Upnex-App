package com.upnext.app.data.validation;

import com.upnext.app.core.Logger;

/**
 * Command-line utility to validate database schema.
 * Run this to check if the database meets all requirements for the Question Answering system.
 */
public class SchemaValidationRunner {
    private static final Logger LOGGER = Logger.getInstance();
    
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("UpNext Database Schema Validation");
        System.out.println("=".repeat(60));
        
        try {
            LOGGER.info("Starting database schema validation");
            
            DatabaseSchemaValidator.ValidationResult result = DatabaseSchemaValidator.validateSchema();
            
            // Print connection information
            System.out.println("\n📊 CONNECTION INFORMATION:");
            System.out.println("-".repeat(40));
            result.getConnectionInfo().forEach((key, value) -> 
                System.out.println("  " + key + ": " + value));
            
            // Print validation summary
            System.out.println("\n🔍 VALIDATION SUMMARY:");
            System.out.println("-".repeat(40));
            System.out.println("  Status: " + (result.isValid() ? "✅ VALID" : "❌ INVALID"));
            System.out.println("  Errors: " + result.getErrors().size());
            System.out.println("  Warnings: " + result.getWarnings().size());
            
            // Print errors if any
            if (!result.getErrors().isEmpty()) {
                System.out.println("\n🚨 ERRORS FOUND:");
                System.out.println("-".repeat(40));
                for (int i = 0; i < result.getErrors().size(); i++) {
                    System.out.println("  " + (i + 1) + ". " + result.getErrors().get(i));
                }
            }
            
            // Print warnings if any
            if (!result.getWarnings().isEmpty()) {
                System.out.println("\n⚠️  WARNINGS:");
                System.out.println("-".repeat(40));
                for (int i = 0; i < result.getWarnings().size(); i++) {
                    System.out.println("  " + (i + 1) + ". " + result.getWarnings().get(i));
                }
            }
            
            // Print recommendations
            System.out.println("\n💡 RECOMMENDATIONS:");
            System.out.println("-".repeat(40));
            
            if (result.isValid()) {
                System.out.println("  ✅ Database schema is ready for the Question Answering system!");
                System.out.println("  ✅ All required tables, columns, and relationships are present.");
                
                if (!result.getWarnings().isEmpty()) {
                    System.out.println("  ⚠️  Consider addressing warnings for optimal performance.");
                }
            } else {
                System.out.println("  ❌ Database schema needs attention before proceeding.");
                System.out.println("  📋 Please address the errors listed above.");
                
                if (result.getErrors().stream().anyMatch(e -> e.contains("does not exist"))) {
                    System.out.println("  💡 Run database migrations to create missing tables.");
                }
                
                if (result.getErrors().stream().anyMatch(e -> e.contains("connect"))) {
                    System.out.println("  💡 Check database connection settings and ensure MySQL is running.");
                }
            }
            
            System.out.println("\n" + "=".repeat(60));
            System.out.println("Validation completed. Check the output above for details.");
            System.out.println("=".repeat(60));
            
            // Exit with appropriate code
            System.exit(result.isValid() ? 0 : 1);
            
        } catch (Exception e) {
            LOGGER.logException("Schema validation failed", e);
            System.err.println("\n💥 VALIDATION FAILED:");
            System.err.println("   " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }
}