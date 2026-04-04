package com.mycompany.javasystem;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.collections.FXCollections;

public class SettingsController {
    @FXML
    private Button logoutButton;
    @FXML
    private Label residentNameLabel;
    
    // Account Settings
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField addressField;
    
    // Security Settings
    @FXML
    private PasswordField currentPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    
    // Notification Settings
    @FXML
    private CheckBox emailNotificationsCheck;
    @FXML
    private CheckBox smsNotificationsCheck;
    @FXML
    private CheckBox pushNotificationsCheck;
    @FXML
    private CheckBox announcementNotificationsCheck;
    
    // Privacy Settings
    @FXML
    private CheckBox dataSharingCheck;
    @FXML
    private ComboBox<String> profileVisibilityCombo;

    @FXML
    public void initialize() {
        // Initialize profile visibility options
        profileVisibilityCombo.setItems(FXCollections.observableArrayList(
            "Private", "Barangay Officials Only", "Public"
        ));
        profileVisibilityCombo.setValue("Private");
        
        // Load user settings (in real app, load from database)
        loadUserSettings();
    }

    private void loadUserSettings() {
        // In a real application, load these from database
        residentNameLabel.setText("Juan Dela Cruz");
        fullNameField.setText("Juan Dela Cruz");
        emailField.setText("juan.delacruz@email.com");
        phoneField.setText("+63 912 345 6789");
        addressField.setText("123 Main Street, Barangay San Isidro");
    }

    @FXML
    private void updateAccountInfo() {
        // Validate fields
        if (fullNameField.getText().trim().isEmpty() || 
            emailField.getText().trim().isEmpty() ||
            phoneField.getText().trim().isEmpty() ||
            addressField.getText().trim().isEmpty()) {
            showAlert("Error", "Please fill in all required fields", Alert.AlertType.ERROR);
            return;
        }

        // Validate email format
        if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert("Error", "Please enter a valid email address", Alert.AlertType.ERROR);
            return;
        }

        // In real app, save to database
        showAlert("Success", "Account information updated successfully!", Alert.AlertType.INFORMATION);
        residentNameLabel.setText(fullNameField.getText());
    }

    @FXML
    private void changePassword() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate fields
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Error", "Please fill in all password fields", Alert.AlertType.ERROR);
            return;
        }

        // Check if new passwords match
        if (!newPassword.equals(confirmPassword)) {
            showAlert("Error", "New passwords do not match", Alert.AlertType.ERROR);
            return;
        }

        // Validate password strength
        if (newPassword.length() < 6) {
            showAlert("Error", "Password must be at least 6 characters long", Alert.AlertType.ERROR);
            return;
        }

        // In real app, verify current password and update in database
        showAlert("Success", "Password changed successfully!", Alert.AlertType.INFORMATION);
        
        // Clear password fields
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    @FXML
    private void saveNotificationSettings() {
        // In real app, save alert preferences to database
        StringBuilder settings = new StringBuilder("Alert settings saved:\n");
        settings.append("Email: ").append(emailNotificationsCheck.isSelected() ? "Enabled" : "Disabled").append("\n");
        settings.append("SMS: ").append(smsNotificationsCheck.isSelected() ? "Enabled" : "Disabled").append("\n");
        settings.append("Push: ").append(pushNotificationsCheck.isSelected() ? "Enabled" : "Disabled").append("\n");
        settings.append("Announcements: ").append(announcementNotificationsCheck.isSelected() ? "Enabled" : "Disabled");
        
        showAlert("Success", settings.toString(), Alert.AlertType.INFORMATION);
    }

    @FXML
    private void savePrivacySettings() {
        // In real app, save privacy settings to database
        String visibility = profileVisibilityCombo.getValue();
        boolean dataSharing = dataSharingCheck.isSelected();
        
        String message = "Privacy settings saved:\n" +
                        "Profile Visibility: " + visibility + "\n" +
                        "Data Sharing: " + (dataSharing ? "Enabled" : "Disabled");
        
        showAlert("Success", message, Alert.AlertType.INFORMATION);
    }

    @FXML
    private void downloadData() {
        // In real app, generate and download user data
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Download Data");
        alert.setHeaderText("Data Export Request");
        alert.setContentText("Your data export request has been submitted. " +
                            "You will receive an email with your data within 24 hours.");
        alert.showAndWait();
    }

    @FXML
    private void deleteAccount() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Account");
        confirmAlert.setHeaderText("Are you sure you want to delete your account?");
        confirmAlert.setContentText("This action cannot be undone. All your data will be permanently deleted.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Alert finalAlert = new Alert(Alert.AlertType.INFORMATION);
                finalAlert.setTitle("Account Deletion");
                finalAlert.setHeaderText("Account Deletion Request Submitted");
                finalAlert.setContentText("Your account deletion request has been submitted. " +
                                        "Please contact the barangay office to complete the process.");
                finalAlert.showAndWait();
            }
        });
    }

    @FXML
    private void handleMouseEntered(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #f4f4f4; -fx-text-fill: #1a1a1a; " +
                    "-fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 11 16; " +
                    "-fx-alignment: CENTER_LEFT; -fx-cursor: hand;");
    }

    @FXML
    private void handleMouseExited(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555; " +
                    "-fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 11 16; " +
                    "-fx-alignment: CENTER_LEFT; -fx-cursor: hand;");
    }

    @FXML
    private void goBackToDashboard() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "ResidentDashboard.fxml", true, getClass());
    }

    @FXML
    private void goToMyDocuments() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "MyDocuments.fxml", true, getClass());
    }

    @FXML
    private void goToRequestDocument() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "RequestDocument.fxml", true, getClass());
    }

    @FXML
    private void goToPayments() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Payments.fxml", true, getClass());
    }

    @FXML
    private void goToComplaints() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Complaints_Resident.fxml", true, getClass());
    }

    @FXML
    private void goToAnnouncements() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "ResidentAnnouncements.fxml", true, getClass());
    }

    @FXML
    private void handleLogout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Logout");
        confirmAlert.setHeaderText("Are you sure you want to logout?");
        confirmAlert.setContentText("You will need to login again to access your account.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                SceneTransition.slideTo(stage, "login.fxml", false, getClass());
            }
        });
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}