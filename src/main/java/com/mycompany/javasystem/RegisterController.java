package com.mycompany.javasystem;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
<<<<<<< Updated upstream
=======
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
>>>>>>> Stashed changes

public class RegisterController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;
    @FXML private ToggleButton residentToggle;
    @FXML private ToggleButton adminToggle;

<<<<<<< Updated upstream
=======
    private boolean passwordShown = false;
    private boolean confirmPasswordShown = false;

    @FXML
    public void initialize() {
        passwordVisible.textProperty().bindBidirectional(passwordField.textProperty());
        passwordVisible.setVisible(false);
        passwordVisible.setManaged(false);

        confirmPasswordVisible.textProperty().bindBidirectional(confirmPasswordField.textProperty());
        confirmPasswordVisible.setVisible(false);
        confirmPasswordVisible.setManaged(false);

        ToggleGroup roleGroup = new ToggleGroup();
        residentToggle.setToggleGroup(roleGroup);
        adminToggle.setToggleGroup(roleGroup);
        residentToggle.setSelected(true);

        residentToggle.selectedProperty().addListener((obs, old, selected) -> {
            if (selected) {
                residentToggle.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #111111; -fx-font-weight: bold; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #ffffff; -fx-border-width: 1; -fx-padding: 12 16; -fx-font-size: 12px; -fx-cursor: hand;");
                adminToggle.setStyle("-fx-background-color: rgba(30,30,30,0.9); -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #333333; -fx-border-width: 1; -fx-padding: 12 16; -fx-font-size: 12px; -fx-text-fill: #888888; -fx-cursor: hand;");
            }
        });

        adminToggle.selectedProperty().addListener((obs, old, selected) -> {
            if (selected) {
                adminToggle.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #111111; -fx-font-weight: bold; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #ffffff; -fx-border-width: 1; -fx-padding: 12 16; -fx-font-size: 12px; -fx-cursor: hand;");
                residentToggle.setStyle("-fx-background-color: rgba(30,30,30,0.9); -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #333333; -fx-border-width: 1; -fx-padding: 12 16; -fx-font-size: 12px; -fx-text-fill: #888888; -fx-cursor: hand;");
            }
        });
    }

    @FXML
    private void togglePassword() {
        passwordShown = !passwordShown;
        passwordField.setVisible(!passwordShown);
        passwordField.setManaged(!passwordShown);
        passwordVisible.setVisible(passwordShown);
        passwordVisible.setManaged(passwordShown);
        togglePasswordBtn.setText(passwordShown ? "🙈" : "👁");
    }

    @FXML
    private void toggleConfirmPassword() {
        confirmPasswordShown = !confirmPasswordShown;
        confirmPasswordField.setVisible(!confirmPasswordShown);
        confirmPasswordField.setManaged(!confirmPasswordShown);
        confirmPasswordVisible.setVisible(confirmPasswordShown);
        confirmPasswordVisible.setManaged(confirmPasswordShown);
        toggleConfirmPasswordBtn.setText(confirmPasswordShown ? "🙈" : "👁");
    }

>>>>>>> Stashed changes
    @FXML
    private void handleRegister() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String role = residentToggle.isSelected() ? "resident" : "admin";

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showMessage("Please fill in all fields.", false);
            return;
        }

        if (!email.contains("@")) {
            showMessage("Please enter a valid email address.", false);
            return;
        }

        if (password.length() < 6) {
            showMessage("Password must be at least 6 characters.", false);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showMessage("Passwords do not match.", false);
            return;
        }
<<<<<<< Updated upstream

        // TODO: Save to MS Access database here
        showMessage("Account created successfully!", true);
=======
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                showMessage("Database not available - running in demo mode. Registration disabled.", false);
                return;
            }
            
            System.out.println("Registering user: " + email + " with role: " + role);
            
            String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String sql = "INSERT INTO users (email, password, role, created_at) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, password);
            stmt.setString(3, role);
            stmt.setString(4, currentDate);
            stmt.executeUpdate();
            stmt.close();
            conn.close();
            
            System.out.println("User registered successfully: " + email);
            showMessage("Account created successfully! You can now login.", true);
            
            // Clear form after successful registration
            emailField.clear();
            passwordField.clear();
            confirmPasswordField.clear();
            residentToggle.setSelected(true);
            
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE") || e.getMessage().contains("duplicate")) {
                showMessage("Email already exists. Please use a different email.", false);
            } else {
                showMessage("Registration failed: " + e.getMessage(), false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Registration failed: " + e.getMessage(), false);
        }
>>>>>>> Stashed changes
    }

    @FXML
    private void goToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showMessage(String message, boolean isSuccess) {
        messageLabel.setText(message);
        if (isSuccess) {
            messageLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-size: 12px;");
        } else {
            messageLabel.setStyle("-fx-text-fill: #e53935; -fx-font-size: 12px;");
        }
    }
}