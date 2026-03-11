package com.mycompany.javasystem;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    @FXML
    private void handleRegister() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

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

        // TODO: Save to MS Access database here
        showMessage("Account created successfully!", true);
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