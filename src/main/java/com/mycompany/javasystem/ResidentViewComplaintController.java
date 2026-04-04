package com.mycompany.javasystem;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ResidentViewComplaintController {

    @FXML private Label complaintIdLabel;
    @FXML private Label nameLabel;
    @FXML private Label typeLabel;
    @FXML private Label locationLabel;
    @FXML private Label dateLabel;
    @FXML private Label statusLabel;
    @FXML private Label detailsLabel;
    @FXML private Label photoLabel;
    @FXML private Label responseLabel;

    public void setComplaint(String complaintId, String name, String type,
            String location, String date, String status, String details,
            String photoPath, String adminResponse) {
        
        complaintIdLabel.setText("Complaint ID: " + complaintId);
        nameLabel.setText(name);
        typeLabel.setText(type);
        locationLabel.setText(location);
        dateLabel.setText(date != null ? date : "N/A");
        
        // Set status with appropriate styling
        statusLabel.setText(status);
        String statusStyle;
        switch (status) {
            case "Resolved":
                statusStyle = "-fx-background-color: #e8f5e9; -fx-text-fill: #4caf50;";
                break;
            case "Under Review":
                statusStyle = "-fx-background-color: #e3f2fd; -fx-text-fill: #1e88e5;";
                break;
            default:
                statusStyle = "-fx-background-color: #fff8e1; -fx-text-fill: #f59e0b;";
                break;
        }
        statusLabel.setStyle(statusStyle + " -fx-font-size: 12px; -fx-font-weight: bold; " +
                "-fx-background-radius: 4; -fx-padding: 4 8;");
        
        detailsLabel.setText(details != null ? details : "No details provided.");
        photoLabel.setText(photoPath != null && !photoPath.isEmpty()
                ? "Photo attached: " + photoPath : "No photo attached.");
        
        if (adminResponse != null && !adminResponse.trim().isEmpty()) {
            responseLabel.setText(adminResponse);
            responseLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 13px;");
        } else {
            responseLabel.setText("No response yet.");
            responseLabel.setStyle("-fx-text-fill: #888888; -fx-font-style: italic; -fx-font-size: 13px;");
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) complaintIdLabel.getScene().getWindow();
        stage.close();
    }
}