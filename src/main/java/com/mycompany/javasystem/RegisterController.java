package com.mycompany.javasystem;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class RegisterController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisible;
    @FXML private Button togglePasswordBtn;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField confirmPasswordVisible;
    @FXML private Button toggleConfirmPasswordBtn;
    @FXML private Label messageLabel;

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
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "INSERT INTO users (email, password, role) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, password);
            stmt.setString(3, "resident");
            stmt.executeUpdate();
            stmt.close();
            conn.close();
            showMessage("Account created successfully!", true);
        } catch (Exception e) {
            e.printStackTrace();
            showMessage(e.getMessage(), false);
        }
    }

    @FXML
    private void goToLogin() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        SceneTransition.slideTo(stage, "login.fxml", false, getClass());
    }

    private void showMessage(String message, boolean isSuccess) {
        messageLabel.setText(message);
        messageLabel.setStyle(isSuccess
            ? "-fx-text-fill: #66bb6a; -fx-font-size: 11px;"
            : "-fx-text-fill: #ef5350; -fx-font-size: 11px;");
    }
}