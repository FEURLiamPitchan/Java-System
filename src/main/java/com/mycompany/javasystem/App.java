package com.mycompany.javasystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import java.sql.Connection;
import java.io.File;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
<<<<<<< Updated upstream
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        Scene scene = new Scene(root);
        stage.setTitle("Barangay System");
=======
        System.setProperty("prism.text", "t2k");
        
        // Initialize database connection on startup
        initializeDatabase();
        
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        Scene scene = new Scene(root, 1366, 768);
        stage.setTitle("Barangay San Isidro - Resident Portal");
>>>>>>> Stashed changes
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
    
    private void initializeDatabase() {
        try {
            System.out.println("=== Barangay System Database Initialization ===");
            
            // Check for barangay.accdb in Documents folder
            String dbPath = System.getProperty("user.home") + "\\Documents\\barangay.accdb";
            File dbFile = new File(dbPath);
            
            if (dbFile.exists()) {
                System.out.println("✓ Found barangay.accdb at: " + dbPath);
                
                // Test connection to the actual database
                try {
                    Connection conn = DatabaseConnection.getConnection();
                    if (conn != null) {
                        System.out.println("✓ Successfully connected to barangay.accdb");
                        System.out.println("✓ Using Microsoft Access database");
                        conn.close();
                    }
                } catch (Exception e) {
                    System.out.println("⚠ Error connecting to barangay.accdb: " + e.getMessage());
                    System.out.println("⚠ Falling back to in-memory database");
                    testInMemoryDatabase();
                }
            } else {
                System.out.println("⚠ barangay.accdb not found at: " + dbPath);
                System.out.println("ℹ Creating in-memory database with sample data...");
                testInMemoryDatabase();
            }
            
        } catch (Exception e) {
            System.err.println("✗ Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void testInMemoryDatabase() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                System.out.println("✓ In-memory database created successfully");
                System.out.println("✓ Sample data loaded for resident functionality");
                System.out.println("ℹ Database includes: payments, complaints, announcements, document_requests");
                conn.close();
            } else {
                System.out.println("⚠ Running in demo mode without database");
            }
        } catch (Exception e) {
            System.err.println("✗ In-memory database test failed: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
<<<<<<< Updated upstream
=======
        System.setProperty("prism.text", "t2k");
        System.out.println("Starting Barangay San Isidro Resident Portal...");
>>>>>>> Stashed changes
        launch(args);
    }
}