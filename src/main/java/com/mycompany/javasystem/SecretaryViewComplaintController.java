package com.mycompany.javasystem;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class SecretaryViewComplaintController {

    @FXML private Label complaintIdLabel;
    @FXML private Label nameLabel;
    @FXML private Label typeLabel;
    @FXML private Label locationLabel;
    @FXML private Label dateLabel;
    @FXML private TextArea detailsArea;
    @FXML private Label photoLabel;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextArea responseArea;
    @FXML private Button saveBtn;
    @FXML private Button closeBtn;
    @FXML private VBox rootContainer;
    @FXML private Label statusBadge;

    private String complaintId;
    private Runnable onUpdate;
    private boolean readOnlyMode;

    @FXML
    public void initialize() {
        // Add all status options with proper capitalization
        statusCombo.getItems().addAll("Pending", "In Progress", "Resolved", "Closed");
        statusCombo.setStyle(
            "-fx-font-size: 12px; -fx-padding: 8 12; -fx-background-color: #f4f4f4;" +
            "-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-background-radius: 6;");
        
        // Style buttons
        if (saveBtn != null) {
            saveBtn.setStyle(
                "-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff;" +
                "-fx-font-size: 12px; -fx-font-weight: bold;" +
                "-fx-background-radius: 6; -fx-padding: 10 24; -fx-cursor: hand;");
        }
        
        if (closeBtn != null) {
            closeBtn.setStyle(
                "-fx-background-color: #f4f4f4; -fx-text-fill: #555555;" +
                "-fx-font-size: 12px; -fx-background-radius: 6;" +
                "-fx-border-color: #e0e0e0; -fx-border-width: 1;" +
                "-fx-padding: 10 24; -fx-cursor: hand;");
        }
        
        // Style text areas
        if (detailsArea != null) {
            detailsArea.setStyle(
                "-fx-font-size: 12px; -fx-padding: 10; -fx-border-color: #e0e0e0;" +
                "-fx-border-width: 1; -fx-background-radius: 4; -fx-control-inner-background: #fafafa;");
            detailsArea.setWrapText(true);
            detailsArea.setEditable(false);
        }
        
        if (responseArea != null) {
            responseArea.setStyle(
                "-fx-font-size: 12px; -fx-padding: 10; -fx-border-color: #e0e0e0;" +
                "-fx-border-width: 1; -fx-background-radius: 4; -fx-control-inner-background: #ffffff;");
            responseArea.setWrapText(true);
        }
        
        // Listen for status changes to update badge color
        statusCombo.setOnAction(e -> updateStatusBadgeColor(statusCombo.getValue()));
    }

    public void setComplaint(String complaintId, String name, String type,
            String location, String date, String status, String details,
            String photoPath, String adminResponse) {
        this.complaintId = complaintId;
        
        // Set complaint information
        complaintIdLabel.setText("Complaint ID: " + complaintId);
        complaintIdLabel.setStyle(
            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
        
        nameLabel.setText(name);
        nameLabel.setStyle(
            "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        
        typeLabel.setText(type);
        typeLabel.setStyle(
            "-fx-font-size: 12px; -fx-text-fill: #555555;");
        
        locationLabel.setText(location);
        locationLabel.setStyle(
            "-fx-font-size: 12px; -fx-text-fill: #555555;");
        
        dateLabel.setText(date != null ? date : "N/A");
        dateLabel.setStyle(
            "-fx-font-size: 12px; -fx-text-fill: #888888;");
        
        // Set details in text area
        if (detailsArea != null) {
            detailsArea.setText(details != null && !details.isEmpty() ? details : "No details provided.");
        } else {
            // Fallback for label if FXML uses label instead of textarea
            Label detailsLabel = (Label) complaintIdLabel.getParent().lookup("#detailsLabel");
            if (detailsLabel != null) {
                detailsLabel.setText(details != null && !details.isEmpty() ? details : "No details provided.");
                detailsLabel.setWrapText(true);
            }
        }
        
        // Set photo path
        photoLabel.setText(photoPath != null && !photoPath.isEmpty()
                ? photoPath : "No photo attached.");
        photoLabel.setStyle(
            "-fx-font-size: 11px; -fx-text-fill: #999999; -fx-font-style: italic;");
        
        // Set status and update color
        statusCombo.setValue(status);
        updateStatusBadgeColor(status);
        
        // Set admin response
        responseArea.setText(adminResponse != null ? adminResponse : "");
    }

    private void updateStatusBadgeColor(String status) {
        if (statusBadge == null) return;
        
        String statusBg, statusFg;
        switch (status) {
            case "Pending":       statusBg = "#fff3cd"; statusFg = "#856404"; break;  // Amber
            case "In Progress":   statusBg = "#ffe0b2"; statusFg = "#e65100"; break;  // Orange
            case "Resolved":      statusBg = "#c8e6c9"; statusFg = "#2e7d32"; break;  // Green
            case "Closed":        statusBg = "#f3e5f5"; statusFg = "#6a1b9a"; break;  // Purple
            default:              statusBg = "#e0e0e0"; statusFg = "#424242"; break;  // Gray
        }
        
        statusBadge.setStyle(
            "-fx-background-color: " + statusBg + ";" +
            "-fx-text-fill: " + statusFg + ";" +
            "-fx-font-size: 12px; -fx-font-weight: bold;" +
            "-fx-background-radius: 4; -fx-padding: 5 10;");
        statusBadge.setText(status);
    }

    public void setOnUpdate(Runnable callback) {
        this.onUpdate = callback;
    }

    public void setReadOnlyMode(boolean readOnlyMode) {
        this.readOnlyMode = readOnlyMode;
        if (!readOnlyMode) return;
        if (statusCombo != null) statusCombo.setDisable(true);
        if (responseArea != null) responseArea.setEditable(false);
        if (saveBtn != null) {
            saveBtn.setManaged(false);
            saveBtn.setVisible(false);
        }
    }

    @FXML
    private void handleSave() {
        if (readOnlyMode) {
            handleClose();
            return;
        }
        String newStatus = statusCombo.getValue();
        String newResponse = responseArea.getText().trim();
        
        if (newStatus == null || newStatus.isEmpty()) {
            showAlert("Error", "Please select a status", Alert.AlertType.WARNING);
            return;
        }
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "UPDATE complaints SET status = ?, admin_response = ?, is_read = 1, status_changed_at = NOW() WHERE complaint_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, newStatus);
            stmt.setString(2, newResponse);
            stmt.setString(3, complaintId);
            int result = stmt.executeUpdate();
            stmt.close();
            conn.close();
            
            if (result > 0) {
                showAlert("Success", "Complaint updated successfully!", Alert.AlertType.INFORMATION);
                if (onUpdate != null) onUpdate.run();
                handleClose();
            } else {
                showAlert("Error", "Failed to update complaint", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Database error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) complaintIdLabel.getScene().getWindow();
        stage.close();
    }
}