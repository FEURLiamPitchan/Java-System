 package com.mycompany.javasystem;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisible;
    @FXML private Button togglePasswordBtn;
    @FXML private Label errorLabel;

    private boolean passwordShown = false;

    @FXML
    public void initialize() {
        passwordVisible.textProperty().bindBidirectional(passwordField.textProperty());
        passwordVisible.setVisible(false);
        passwordVisible.setManaged(false);
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
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT role, full_name FROM users WHERE email = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String role = rs.getString("role");
                String fullName = rs.getString("full_name") != null ? rs.getString("full_name") : "Admin";
                SessionManager.login(email, role, fullName);
                rs.close();
                stmt.close();
                conn.close();
                loadDashboard(role);
            } else {
                rs.close();
                stmt.close();
                conn.close();
                showError("Invalid email or password.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Database connection error.");
        }
    }

    private void loadDashboard(String role) {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            String fxml;

            switch (role.toLowerCase()) {
                case "admin":
                    fxml = "AdminDashboard.fxml";
                    break;
                case "barangay_captain":
                    fxml = "BarangayCaptainDashboard.fxml";
                    break;
                case "secretary":
                    fxml = "SecretaryDashboard.fxml";
                    break;
                case "treasurer":
                    fxml = "TreasurerDashboard.fxml";
                    break;
                default:
                    fxml = "ResidentDashboard.fxml";
                    break;
            }

            SceneTransition.slideTo(stage, fxml, true, getClass());

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading dashboard.");
        }
    }
    @FXML
    private void goToRegister() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        SceneTransition.slideTo(stage, "register.fxml", false, getClass());
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #ef5350; -fx-font-size: 11px;");
    }
}