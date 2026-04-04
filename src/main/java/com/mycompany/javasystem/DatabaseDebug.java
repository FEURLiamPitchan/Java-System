package com.mycompany.javasystem;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.io.File;

public class DatabaseDebug {
    public static void main(String[] args) {
        System.out.println("=== DATABASE DEBUG UTILITY ===");
        
        // Check the exact path
        String dbPath = System.getProperty("user.home") + "\\Documents\\barangay.accdb";
        System.out.println("Expected database path: " + dbPath);
        
        File dbFile = new File(dbPath);
        System.out.println("Database file exists: " + dbFile.exists());
        
        if (dbFile.exists()) {
            System.out.println("File size: " + dbFile.length() + " bytes");
            System.out.println("Last modified: " + new java.util.Date(dbFile.lastModified()));
        }
        
        // Check if there are multiple database files
        File documentsDir = new File(System.getProperty("user.home") + "\\Documents");
        System.out.println("\nLooking for .accdb files in Documents folder:");
        if (documentsDir.exists()) {
            File[] files = documentsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".accdb"));
            if (files != null) {
                for (File file : files) {
                    System.out.println("Found: " + file.getName() + " (" + file.length() + " bytes)");
                }
            }
        }
        
        // Try to connect and check contents
        try {
            System.out.println("\n=== TESTING DATABASE CONNECTION ===");
            Connection conn = DatabaseConnection.getConnection();
            System.out.println("✓ Connection successful");
            
            // List all tables
            System.out.println("\n=== CHECKING TABLES ===");
            Statement stmt = conn.createStatement();
            
            // Check users table
            try {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM users");
                if (rs.next()) {
                    System.out.println("Users table: " + rs.getInt("count") + " records");
                }
                rs.close();
                
                // Show all users
                rs = stmt.executeQuery("SELECT email, role FROM users");
                System.out.println("Users in database:");
                while (rs.next()) {
                    System.out.println("  - " + rs.getString("email") + " (" + rs.getString("role") + ")");
                }
                rs.close();
            } catch (Exception e) {
                System.out.println("Users table error: " + e.getMessage());
            }
            
            // Check document_requests table
            try {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM document_requests");
                if (rs.next()) {
                    System.out.println("Document requests table: " + rs.getInt("count") + " records");
                }
                rs.close();
            } catch (Exception e) {
                System.out.println("Document requests table error: " + e.getMessage());
            }
            
            // Test insert
            System.out.println("\n=== TESTING INSERT ===");
            try {
                String testEmail = "test_" + System.currentTimeMillis() + "@test.com";
                int result = stmt.executeUpdate(
                    "INSERT INTO users (email, password, role, date_created) VALUES ('" + 
                    testEmail + "', 'test123', 'resident', '2024-06-15')");
                System.out.println("✓ Insert successful: " + result + " row affected");
                
                // Verify insert
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM users WHERE email = '" + testEmail + "'");
                if (rs.next()) {
                    System.out.println("✓ Verification: " + rs.getInt("count") + " record found");
                }
                rs.close();
            } catch (Exception e) {
                System.out.println("✗ Insert failed: " + e.getMessage());
            }
            
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            System.out.println("✗ Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== DEBUG COMPLETE ===");
    }
}