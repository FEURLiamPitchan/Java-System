package com.mycompany.javasystem;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.FileChooser;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.scene.shape.Circle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class MyProfileController {
    @FXML private Button logoutButton;
    @FXML private Label residentNameLabel;
    @FXML private Label alertBadge;
    @FXML private Button alertsButton;
    @FXML private javafx.scene.shape.Circle topBarProfileCircle;
    @FXML private Label topBarProfileInitials;
    @FXML private Label profileNameLabel;
    @FXML private Label profileEmailLabel;
    @FXML private Label memberSinceLabel;
    @FXML private Circle profileAvatar;
    @FXML private Label profileInitialsLabel;
    @FXML private Label statusBadge;
    @FXML private Label roleBadge;
    @FXML private Label picErrorLabel;
    
    // Form fields
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private DatePicker birthDatePicker;
    @FXML private ComboBox<String> genderCombo;
    @FXML private TextArea addressArea;
    
    // Password fields
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    
    // UI elements
    @FXML private Button editButton;
    @FXML private HBox actionButtons;
    @FXML private Label errorLabel;
    @FXML private Label passwordErrorLabel;
    
    private boolean isEditMode = false;
    private String currentProfilePicturePath = null;
    private String originalEmail = null;
    
    @FXML
    public void initialize() {
        genderCombo.setItems(FXCollections.observableArrayList(
            "Male", "Female", "Other", "Prefer not to say"
        ));
        setupProfileAvatar();
        loadUserProfile();
        loadTopBarProfilePicture();
        ResidentNotifications.syncNotifications(UserSession.getCurrentUserEmail());
        refreshAlertBadge();
    }
    
    private void setupProfileAvatar() {
        if (profileAvatar != null) {
            profileAvatar.setStroke(Color.web("#e0e0e0"));
            profileAvatar.setStrokeWidth(2);
        }
    }
    
    @FXML
    private void handleMouseEntered(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #f4f4f4; -fx-text-fill: #1a1a1a; -fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 11 16; -fx-alignment: CENTER_LEFT; -fx-cursor: hand;");
    }

    @FXML
    private void handleMouseExited(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555; -fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 11 16; -fx-alignment: CENTER_LEFT; -fx-cursor: hand;");
    }
    
    private void loadUserProfile() {
        try {
            String currentUserEmail = UserSession.getCurrentUserEmail();
            
            // Validate user_email exists
            if (currentUserEmail == null || currentUserEmail.trim().isEmpty()) {
                System.out.println("No user email found in session");
                loadSampleProfile();
                return;
            }
            
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                loadSampleProfile();
                return;
            }
            
            // Query only users table (no JOIN since document_requests structure is different)
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM users WHERE email = ?");
            stmt.setString(1, currentUserEmail);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                // Load fields from users table
                String fullName = rs.getString("full_name");
                String email = rs.getString("email");
                String role = rs.getString("role");
                String status = rs.getString("status");
                String dateCreated = rs.getString("date_created");
                String profilePicture = rs.getString("profile_picture");
                
                // Split full_name into first and last name
                String firstName = "";
                String lastName = "";
                if (fullName != null && !fullName.isEmpty()) {
                    String[] nameParts = fullName.trim().split(" ", 2);
                    firstName = nameParts[0];
                    if (nameParts.length > 1) {
                        lastName = nameParts[1];
                    }
                }
                
                // Store original email for cascade updates
                originalEmail = email;
                currentProfilePicturePath = profilePicture;
                
                // Populate form fields
                firstNameField.setText(firstName);
                lastNameField.setText(lastName);
                emailField.setText(email != null ? email : "");
                phoneField.setText(""); // Not in database
                addressArea.setText(""); // Not in database
                genderCombo.setValue("Prefer not to say"); // Not in database
                birthDatePicker.setValue(null); // Not in database
                
                // Format member since date
                if (dateCreated != null && !dateCreated.isEmpty()) {
                    try {
                        LocalDate created = LocalDate.parse(dateCreated.split(" ")[0], DateTimeFormatter.ofPattern("M/d/yyyy"));
                        memberSinceLabel.setText(created.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
                    } catch (Exception e) {
                        memberSinceLabel.setText("January 2024");
                    }
                } else {
                    memberSinceLabel.setText("January 2024");
                }
                
                // Update status and role badges
                updateStatusBadge(status);
                updateRoleBadge(role);
                
                // Load profile picture or initials
                loadProfilePicture(profilePicture, firstName, lastName);
                
                System.out.println("Profile loaded successfully");
                updateDisplayLabels();
            } else {
                loadSampleProfile();
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            System.out.println("Error loading profile: " + e.getMessage());
            e.printStackTrace();
            loadSampleProfile();
        }
    }
    
    private void loadSampleProfile() {
        // Load sample data if database unavailable
        firstNameField.setText("Juan");
        lastNameField.setText("Dela Cruz");
        emailField.setText("resident@email.com");
        phoneField.setText("09123456789");
        addressArea.setText("123 Main Street, Barangay San Isidro, City");
        genderCombo.setValue("Male");
        birthDatePicker.setValue(LocalDate.of(1990, 1, 1));
        memberSinceLabel.setText("January 2024");
        
        updateDisplayLabels();
    }
    

    
    private void updateDisplayLabels() {
        String fullName = firstNameField.getText() + " " + lastNameField.getText();
        if (fullName.trim().isEmpty()) {
            fullName = "Resident Name";
        }
        
        profileNameLabel.setText(fullName);
        residentNameLabel.setText(fullName);
        profileEmailLabel.setText(emailField.getText());
    }
    
    private void updateStatusBadge(String status) {
        if (statusBadge != null) {
            boolean active = "Active".equalsIgnoreCase(status);
            statusBadge.setText(active ? "Active" : "Inactive");
            statusBadge.setStyle(
                "-fx-background-color: " + (active ? "#e8f5e9" : "#ffebee") + ";" +
                "-fx-text-fill: " + (active ? "#4caf50" : "#f44336") + ";" +
                "-fx-font-size: 11px; -fx-font-weight: bold;" +
                "-fx-background-radius: 4; -fx-padding: 4 10;"
            );
        }
    }
    
    private void updateRoleBadge(String role) {
        if (roleBadge != null) {
            String displayRole = role != null ? role.substring(0, 1).toUpperCase() + role.substring(1) : "Resident";
            roleBadge.setText(displayRole);
            roleBadge.setStyle(
                "-fx-background-color: #e3f2fd;" +
                "-fx-text-fill: #2196f3;" +
                "-fx-font-size: 11px; -fx-font-weight: bold;" +
                "-fx-background-radius: 4; -fx-padding: 4 10;"
            );
        }
    }
    
    private void loadProfilePicture(String picturePath, String firstName, String lastName) {
        // Use the utility class for consistent loading
        ProfilePictureLoader.loadProfilePicture(profileAvatar, profileInitialsLabel, UserSession.getCurrentUserEmail());
    }
    
    private void showInitials(String firstName, String lastName) {
        if (profileAvatar != null) {
            profileAvatar.setFill(Color.web("#2d2d2d"));
        }
        if (profileInitialsLabel != null) {
            String initials = "";
            if (firstName != null && !firstName.isEmpty()) {
                initials += firstName.charAt(0);
            }
            if (lastName != null && !lastName.isEmpty()) {
                initials += lastName.charAt(0);
            }
            if (initials.isEmpty()) {
                initials = "R";
            }
            profileInitialsLabel.setText(initials.toUpperCase());
            profileInitialsLabel.setVisible(true);
        }
    }
    
    @FXML
    private void handleChangePicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File selectedFile = fileChooser.showOpenDialog(logoutButton.getScene().getWindow());
        if (selectedFile != null) {
            // Check file size (max 2MB)
            if (selectedFile.length() > 2 * 1024 * 1024) {
                showPicError("Image must be less than 2MB");
                return;
            }
            
            try {
                // Read file and convert to Base64
                byte[] fileBytes = Files.readAllBytes(selectedFile.toPath());
                String base64Image = java.util.Base64.getEncoder().encodeToString(fileBytes);
                
                // Update database with Base64 string
                String currentUserEmail = UserSession.getCurrentUserEmail();
                if (currentUserEmail != null && !currentUserEmail.trim().isEmpty()) {
                    Connection conn = DatabaseConnection.getConnection();
                    if (conn != null) {
                        PreparedStatement stmt = conn.prepareStatement(
                            "UPDATE users SET profile_picture = ? WHERE email = ?");
                        stmt.setString(1, base64Image);
                        stmt.setString(2, currentUserEmail);
                        stmt.executeUpdate();
                        stmt.close();
                        conn.close();
                        
                        // Update UI using utility
                        currentProfilePicturePath = base64Image;
                        ProfilePictureLoader.loadProfilePicture(profileAvatar, profileInitialsLabel, currentUserEmail);
                        showPicSuccess("Picture updated!");
                    }
                }
            } catch (Exception e) {
                System.out.println("Error uploading profile picture: " + e.getMessage());
                e.printStackTrace();
                showPicError("Failed to upload picture");
            }
        }
    }
    
    @FXML
    private void handleRemovePicture() {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Remove Picture");
        confirmDialog.setHeaderText("Remove your profile picture?");
        confirmDialog.setContentText("Your initials will be displayed instead.");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    String currentUserEmail = UserSession.getCurrentUserEmail();
                    if (currentUserEmail != null && !currentUserEmail.trim().isEmpty()) {
                        Connection conn = DatabaseConnection.getConnection();
                        if (conn != null) {
                            PreparedStatement stmt = conn.prepareStatement(
                                "UPDATE users SET profile_picture = NULL WHERE email = ?");
                            stmt.setString(1, currentUserEmail);
                            stmt.executeUpdate();
                            stmt.close();
                            conn.close();
                            
                            // Update UI using utility
                            currentProfilePicturePath = null;
                            ProfilePictureLoader.loadProfilePicture(profileAvatar, profileInitialsLabel, currentUserEmail);
                            showPicSuccess("Picture removed!");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error removing profile picture: " + e.getMessage());
                    e.printStackTrace();
                    showPicError("Failed to remove picture");
                }
            }
        });
    }
    
    @FXML
    private void toggleEditMode() {
        isEditMode = !isEditMode;
        
        // Toggle field states
        firstNameField.setDisable(!isEditMode);
        lastNameField.setDisable(!isEditMode);
        emailField.setDisable(!isEditMode);
        phoneField.setDisable(!isEditMode);
        birthDatePicker.setDisable(!isEditMode);
        genderCombo.setDisable(!isEditMode);
        addressArea.setDisable(!isEditMode);
        
        // Update field styles
        String editableStyle = "-fx-font-size: 13px; -fx-background-color: #ffffff; -fx-border-color: #2196f3; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 10 12;";
        String disabledStyle = "-fx-font-size: 13px; -fx-background-color: #f8f8f8; -fx-border-color: #e0e0e0; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 10 12;";
        
        String style = isEditMode ? editableStyle : disabledStyle;
        firstNameField.setStyle(style);
        lastNameField.setStyle(style);
        emailField.setStyle(style);
        phoneField.setStyle(style);
        addressArea.setStyle(style);
        
        // Toggle buttons
        editButton.setText(isEditMode ? "Cancel" : "Edit");
        editButton.setStyle(isEditMode ? 
            "-fx-background-color: #f44336; -fx-text-fill: #ffffff; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand;" :
            "-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand;");
        
        actionButtons.setVisible(isEditMode);
        actionButtons.setManaged(isEditMode);
        
        errorLabel.setText("");
    }
    
    @FXML
    private void handleSave() {
        errorLabel.setText("");
        
        // Validate user_email exists
        String currentUserEmail = UserSession.getCurrentUserEmail();
        if (currentUserEmail == null || currentUserEmail.trim().isEmpty()) {
            showError("No user session found. Please login again.");
            return;
        }
        
        // Validate required fields
        if (firstNameField.getText().trim().isEmpty()) {
            showError("First name is required");
            return;
        }
        if (lastNameField.getText().trim().isEmpty()) {
            showError("Last name is required");
            return;
        }
        if (emailField.getText().trim().isEmpty() || !emailField.getText().contains("@")) {
            showError("Valid email address is required");
            return;
        }
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                // Combine first and last name into full_name
                String fullName = firstNameField.getText().trim() + " " + lastNameField.getText().trim();
                
                // Update profile fields in users table (matching actual database structure)
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE users SET full_name = ?, email = ? WHERE email = ?");
                
                stmt.setString(1, fullName);
                stmt.setString(2, emailField.getText().trim());
                stmt.setString(3, currentUserEmail);
                
                int result = stmt.executeUpdate();
                stmt.close();
                
                // Update UserSession with new name and email
                String newEmail = emailField.getText().trim();
                UserSession.setCurrentUser(
                    newEmail,
                    UserSession.getCurrentUserRole(),
                    fullName
                );
                originalEmail = newEmail;
                
                conn.close();
                
                if (result > 0) {
                    showSuccess("Profile updated successfully!");
                    
                    // Update display labels
                    updateDisplayLabels();
                    
                    // Refresh profile picture
                    ProfilePictureLoader.loadProfilePicture(profileAvatar, profileInitialsLabel, 
                        emailField.getText().trim());
                    
                    toggleEditMode();
                } else {
                    showError("Failed to update profile");
                }
            } else {
                showSuccess("Profile updated successfully! (Database not available)");
                updateDisplayLabels();
                toggleEditMode();
            }
        } catch (Exception e) {
            System.out.println("Error saving profile: " + e.getMessage());
            e.printStackTrace();
            showError("Error saving profile: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancel() {
        loadUserProfile(); // Reload original data
        toggleEditMode(); // Exit edit mode
    }
    
    @FXML
    private void handlePasswordChange() {
        passwordErrorLabel.setText("");
        
        // Validate user_email exists
        String currentUserEmail = UserSession.getCurrentUserEmail();
        if (currentUserEmail == null || currentUserEmail.trim().isEmpty()) {
            showPasswordError("No user session found. Please login again.");
            return;
        }
        
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Validate fields
        if (currentPassword.isEmpty()) {
            showPasswordError("Current password is required");
            return;
        }
        if (newPassword.isEmpty()) {
            showPasswordError("New password is required");
            return;
        }
        if (newPassword.length() < 6) {
            showPasswordError("New password must be at least 6 characters");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            showPasswordError("New passwords do not match");
            return;
        }
        
        // Verify current password and update
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                // Check current password
                PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT password FROM users WHERE email = ?");
                checkStmt.setString(1, currentUserEmail);
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next() && rs.getString("password").equals(currentPassword)) {
                    // Update password
                    PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE users SET password = ? WHERE email = ?");
                    updateStmt.setString(1, newPassword);
                    updateStmt.setString(2, currentUserEmail);
                    
                    int result = updateStmt.executeUpdate();
                    updateStmt.close();
                    
                    if (result > 0) {
                        showPasswordSuccess("Password updated successfully!");
                        clearPasswordFields();
                    } else {
                        showPasswordError("Failed to update password");
                    }
                } else {
                    showPasswordError("Current password is incorrect");
                }
                
                rs.close();
                checkStmt.close();
                conn.close();
            } else {
                showPasswordSuccess("Password updated successfully! (Database not available)");
                clearPasswordFields();
            }
        } catch (Exception e) {
            System.out.println("Error updating password: " + e.getMessage());
            e.printStackTrace();
            showPasswordError("Error updating password");
        }
    }
    
    private void clearPasswordFields() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }
    
    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setStyle("-fx-text-fill: #e53935; -fx-font-size: 12px;");
        }
    }
    
    private void showSuccess(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 12px;");
        }
    }
    
    private void showPicError(String message) {
        if (picErrorLabel != null) {
            picErrorLabel.setText(message);
            picErrorLabel.setStyle("-fx-text-fill: #c62828; -fx-font-size: 10px;");
        }
    }
    
    private void showPicSuccess(String message) {
        if (picErrorLabel != null) {
            picErrorLabel.setText(message);
            picErrorLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-size: 10px;");
        }
    }
    
    private void showPasswordError(String message) {
        passwordErrorLabel.setText(message);
        passwordErrorLabel.setStyle("-fx-text-fill: #e53935; -fx-font-size: 12px;");
    }
    
    private void showPasswordSuccess(String message) {
        passwordErrorLabel.setText(message);
        passwordErrorLabel.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 12px;");
    }
    
    // Navigation methods
    @FXML
    private void goBackToDashboard() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("ResidentDashboard.fxml"));
            stage.getScene().setRoot(root);
            stage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void goToMyDocuments() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("MyDocuments.fxml"));
            stage.getScene().setRoot(root);
            stage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void goToRequestDocument() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("RequestDocument.fxml"));
            stage.getScene().setRoot(root);
            stage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void goToPayments() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("ResidentPayments.fxml"));
            stage.getScene().setRoot(root);
            stage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void goToComplaints() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("Complaints_Resident.fxml"));
            stage.getScene().setRoot(root);
            stage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void goToAnnouncements() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("ResidentAnnouncements.fxml"));
            stage.getScene().setRoot(root);
            stage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToSettings() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("ResidentSettings.fxml"));
            stage.getScene().setRoot(root);
            stage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleLogout() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("login.fxml"));
            stage.getScene().setRoot(root);
            stage.setMaximized(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshAlertBadge() {
        int count = ResidentNotifications.getUnreadCount(UserSession.getCurrentUserEmail());
        if (count > 0) {
            alertBadge.setText(count > 99 ? "99+" : String.valueOf(count));
            alertBadge.setVisible(true);
        } else {
            alertBadge.setVisible(false);
        }
    }

    @FXML
    private void handleAlertsClick() {
        try {
            javafx.stage.Stage popup = new javafx.stage.Stage();
            popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            popup.initOwner(logoutButton.getScene().getWindow());
            popup.setTitle("Notifications");
            
            VBox root = new VBox(0);
            root.setStyle("-fx-background-color: #f8f9fa;");
            
            VBox header = new VBox(4);
            header.setStyle("-fx-background-color: #2d2d2d; -fx-padding: 20;");
            Label titleLabel = new Label("Notifications");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
            Label subtitleLabel = new Label("Stay updated with your requests and announcements");
            subtitleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaaaaa;");
            header.getChildren().addAll(titleLabel, subtitleLabel);
            
            HBox filterBox = new HBox(8);
            filterBox.setStyle("-fx-padding: 12 20; -fx-background-color: #ffffff; -fx-border-color: #e8e8e8; -fx-border-width: 0 0 1 0;");
            Button unreadBtn = new Button("Unread");
            unreadBtn.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;");
            Button allBtn = new Button("Past Notifications");
            allBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;");
            filterBox.getChildren().addAll(unreadBtn, allBtn);
            
            VBox notifList = new VBox(0);
            ScrollPane scrollPane = new ScrollPane(notifList);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: #f8f9fa; -fx-background-color: #f8f9fa;");
            scrollPane.setPrefHeight(400);
            
            loadNotifications(notifList, false);
            
            unreadBtn.setOnAction(e -> {
                unreadBtn.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;");
                allBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;");
                loadNotifications(notifList, false);
            });
            
            allBtn.setOnAction(e -> {
                allBtn.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;");
                unreadBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;");
                loadNotifications(notifList, true);
            });
            
            HBox footer = new HBox();
            footer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            footer.setStyle("-fx-padding: 12 20; -fx-background-color: #ffffff; -fx-border-color: #e8e8e8; -fx-border-width: 1 0 0 0;");
            Button closeBtn = new Button("Close");
            closeBtn.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff; -fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand;");
            closeBtn.setOnAction(e -> {
                popup.close();
                refreshAlertBadge();
            });
            footer.getChildren().add(closeBtn);
            
            root.getChildren().addAll(header, filterBox, scrollPane, footer);
            
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 480, 550);
            popup.setScene(scene);
            popup.setResizable(false);
            popup.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadNotifications(VBox container, boolean showAll) {
        container.getChildren().clear();
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) return;
            
            String sql = "SELECT notif_id, type, message, reference_id, is_read, created_at FROM notifications WHERE user_email = ?";
            if (!showAll) sql += " AND is_read = 'false'";
            sql += " ORDER BY notif_id DESC";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, UserSession.getCurrentUserEmail());
            ResultSet rs = stmt.executeQuery();
            
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                int notifId = rs.getInt("notif_id");
                String type = rs.getString("type");
                String message = rs.getString("message");
                String refId = rs.getString("reference_id");
                boolean isRead = rs.getString("is_read").equals("true");
                String createdAt = rs.getString("created_at");
                
                HBox item = buildNotifItem(notifId, type, message, refId, isRead, createdAt);
                container.getChildren().add(item);
            }
            
            if (!hasData) {
                Label emptyLabel = new Label(showAll ? "No notifications yet" : "No unread notifications");
                emptyLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #888888; -fx-padding: 40;");
                VBox emptyBox = new VBox(emptyLabel);
                emptyBox.setAlignment(javafx.geometry.Pos.CENTER);
                container.getChildren().add(emptyBox);
            }
            
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HBox buildNotifItem(int notifId, String type, String message, String refId, boolean isRead, String createdAt) {
        HBox item = new HBox(12);
        item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        item.setStyle("-fx-padding: 16 20; -fx-background-color: " + (isRead ? "#ffffff" : "#fafbff") + "; -fx-border-color: #e8e8e8; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
        
        String icon = type.equals("document") ? "📄" : type.equals("complaint") ? "📢" : "📣";
        String iconBg = type.equals("document") ? "#e3f2fd" : type.equals("complaint") ? "#ffebee" : "#fff8e1";
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 20px; -fx-background-color: " + iconBg + "; -fx-background-radius: 8; -fx-padding: 8; -fx-min-width: 40; -fx-min-height: 40; -fx-alignment: center;");
        
        VBox content = new VBox(4);
        content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(content, javafx.scene.layout.Priority.ALWAYS);
        
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #1a1a1a;" + (isRead ? "" : " -fx-font-weight: bold;"));
        
        Label timeLabel = new Label(createdAt);
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888888;");
        
        content.getChildren().addAll(messageLabel, timeLabel);
        
        VBox indicator = new VBox();
        indicator.setAlignment(javafx.geometry.Pos.CENTER);
        if (!isRead) {
            Label dot = new Label("•");
            dot.setStyle("-fx-font-size: 20px; -fx-text-fill: #2196f3;");
            indicator.getChildren().add(dot);
        }
        
        item.getChildren().addAll(iconLabel, content, indicator);
        item.setOnMouseClicked(e -> {
            ResidentNotifications.markAsRead(notifId);
            refreshAlertBadge();
        });
        
        return item;
    }
    
    private void loadTopBarProfilePicture() {
        if (topBarProfileCircle != null && topBarProfileInitials != null) {
            ProfilePictureLoader.loadProfilePicture(topBarProfileCircle, topBarProfileInitials, UserSession.getCurrentUserEmail());
        }
    }
    
    private void cascadeEmailUpdate(Connection conn, String oldEmail, String newEmail) {
        try {
            // Update document_requests
            PreparedStatement stmt1 = conn.prepareStatement(
                "UPDATE document_requests SET user_email = ? WHERE user_email = ?");
            stmt1.setString(1, newEmail);
            stmt1.setString(2, oldEmail);
            stmt1.executeUpdate();
            stmt1.close();
            
            // Update payments
            PreparedStatement stmt2 = conn.prepareStatement(
                "UPDATE payments SET user_email = ? WHERE user_email = ?");
            stmt2.setString(1, newEmail);
            stmt2.setString(2, oldEmail);
            stmt2.executeUpdate();
            stmt2.close();
            
            // Update complaints
            PreparedStatement stmt3 = conn.prepareStatement(
                "UPDATE complaints SET user_email = ? WHERE user_email = ?");
            stmt3.setString(1, newEmail);
            stmt3.setString(2, oldEmail);
            stmt3.executeUpdate();
            stmt3.close();
            
            // Update notifications
            PreparedStatement stmt4 = conn.prepareStatement(
                "UPDATE notifications SET user_email = ? WHERE user_email = ?");
            stmt4.setString(1, newEmail);
            stmt4.setString(2, oldEmail);
            stmt4.executeUpdate();
            stmt4.close();
            
            // Update settings
            PreparedStatement stmt5 = conn.prepareStatement(
                "UPDATE settings SET user_email = ? WHERE user_email = ?");
            stmt5.setString(1, newEmail);
            stmt5.setString(2, oldEmail);
            stmt5.executeUpdate();
            stmt5.close();
            
            System.out.println("Email cascaded successfully from " + oldEmail + " to " + newEmail);
        } catch (Exception e) {
            System.out.println("Error cascading email update: " + e.getMessage());
            e.printStackTrace();
        }
    }
}