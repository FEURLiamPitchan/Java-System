package com.mycompany.javasystem;

import javafx.fxml.FXML;
<<<<<<< Updated upstream
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
=======
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
>>>>>>> Stashed changes

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        if (!email.contains("@")) {
            showError("Please enter a valid email address.");
            return;
        }
<<<<<<< Updated upstream

        // TODO: Replace with actual database authentication
        if (email.equals("admin@barangay.com") && password.equals("admin123")) {
            System.out.println("Login successful!");
            // TODO: Load dashboard here
        } else {
            showError("Invalid email or password.");
=======
        
        // Try database authentication first
        String role = authenticateUser(email, password);
        
        // Fallback to hardcoded credentials if database is not available
        if (role == null) {
            if (email.equals("admin@barangay.com") && password.equals("admin123")) {
                role = "admin";
                UserSession.setCurrentUser(email, role);
            } else if (email.equals("resident@email.com") && password.equals("resident123")) {
                role = "resident";
                UserSession.setCurrentUser(email, role);
            }
        }
        
        if (role != null) {
            loadDashboard(role);
        } else {
            showError("Invalid email or password.");
        }
    }
    
    private String authenticateUser(String email, String password) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                System.out.println("Database not available, using fallback authentication");
                return null;
            }
            
            System.out.println("Attempting to authenticate user: " + email);
            
            String sql = "SELECT role FROM users WHERE email = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, password);
            
            ResultSet rs = stmt.executeQuery();
            String role = null;
            if (rs.next()) {
                role = rs.getString("role");
                UserSession.setCurrentUser(email, role);
                System.out.println("User found with role: " + role);
            } else {
                System.out.println("No user found with email: " + email);
                
                // Debug: Check what users exist in the database
                PreparedStatement debugStmt = conn.prepareStatement("SELECT email, role FROM users");
                ResultSet debugRs = debugStmt.executeQuery();
                System.out.println("Users in database:");
                while (debugRs.next()) {
                    System.out.println("  - " + debugRs.getString("email") + " (" + debugRs.getString("role") + ")");
                }
                debugRs.close();
                debugStmt.close();
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
            return role;
        } catch (SQLException e) {
            System.out.println("Database authentication failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void loadDashboard(String role) {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            String dashboard = role.equalsIgnoreCase("admin") ? "AdminDashboard.fxml" : "ResidentDashboard.fxml";
            SceneTransition.slideTo(stage, dashboard, true, getClass());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading dashboard.");
>>>>>>> Stashed changes
        }
    }

    @FXML
    private void goToRegister() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("register.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #e53935; -fx-font-size: 12px;");
    }
}