package com.mycompany.javasystem;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MyDocumentsController {
    @FXML
    private Button logoutButton;
    @FXML
    private VBox documentsContainer;
    @FXML
    private Label loadingLabel;

    @FXML
    public void initialize() {
        loadDocuments();
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

    private void loadDocuments() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                loadingLabel.setText("Database not available. Showing sample data.");
                loadSampleData();
                return;
            }

            String userEmail = UserSession.getCurrentUserEmail();
            System.out.println("Loading documents for user: " + userEmail);
            
            // Query to get documents for current user OR documents with no user_email (legacy data)
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM document_requests WHERE user_email = ? OR user_email IS NULL ORDER BY date_requested DESC");
            stmt.setString(1, userEmail);
            ResultSet rs = stmt.executeQuery();

            // Clear loading label
            documentsContainer.getChildren().remove(loadingLabel);

            // Create table header
            HBox header = createTableHeader();
            documentsContainer.getChildren().add(header);

            boolean hasData = false;
            int count = 0;
            while (rs.next()) {
                hasData = true;
                count++;
                System.out.println("Found document: " + rs.getString("request_id") + " - " + rs.getString("document_type"));
                HBox row = createDocumentRow(
                    rs.getString("request_id"),
                    rs.getString("document_type"),
                    rs.getString("purpose"),
                    rs.getString("date_requested"),
                    rs.getString("status")
                );
                documentsContainer.getChildren().add(row);
            }
            
            System.out.println("Total documents loaded: " + count);

            if (!hasData) {
                System.out.println("No documents found for user: " + userEmail);
                Label noData = new Label("No document requests found. Click 'New Request' to submit your first request.");
                noData.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888; -fx-padding: 20;");
                documentsContainer.getChildren().add(noData);
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            System.out.println("Error loading documents: " + e.getMessage());
            e.printStackTrace();
            loadingLabel.setText("Error loading documents. Showing sample data.");
            loadSampleData();
        }
    }

    private void loadSampleData() {
        // Clear loading label
        documentsContainer.getChildren().remove(loadingLabel);

        // Create table header
        HBox header = createTableHeader();
        documentsContainer.getChildren().add(header);

        // Add sample rows
        documentsContainer.getChildren().add(createDocumentRow("#BR-2024-001", "Barangay Clearance", "Employment", "2024-06-13", "In Progress"));
        documentsContainer.getChildren().add(createDocumentRow("#BR-2024-002", "Certificate of Residency", "School Requirements", "2024-06-12", "Ready"));
        documentsContainer.getChildren().add(createDocumentRow("#BR-2024-003", "Certificate of Indigency", "Medical Assistance", "2024-06-10", "Released"));
    }

    private HBox createTableHeader() {
        HBox header = new HBox();
        header.setStyle("-fx-padding: 12 8; -fx-background-color: #f8f8f8; -fx-background-radius: 6; -fx-border-color: #e8e8e8; -fx-border-width: 1; -fx-border-radius: 6;");
        
        Label idLabel = new Label("REQUEST ID");
        idLabel.setPrefWidth(120);
        idLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #888888;");
        
        Label typeLabel = new Label("DOCUMENT TYPE");
        typeLabel.setPrefWidth(180);
        typeLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #888888;");
        
        Label purposeLabel = new Label("PURPOSE");
        purposeLabel.setPrefWidth(200);
        purposeLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #888888;");
        
        Label dateLabel = new Label("REQUEST DATE");
        dateLabel.setPrefWidth(120);
        dateLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #888888;");
        
        Label statusLabel = new Label("STATUS");
        statusLabel.setPrefWidth(120);
        statusLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #888888;");
        
        header.getChildren().addAll(idLabel, typeLabel, purposeLabel, dateLabel, statusLabel);
        
        return header;
    }

    private HBox createDocumentRow(String requestId, String docType, String purpose, String date, String status) {
        HBox row = new HBox();
        row.setStyle("-fx-padding: 16 8; -fx-background-color: #ffffff; -fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0;");
        
        Label idLabel = new Label(requestId);
        idLabel.setPrefWidth(120);
        idLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2196f3; -fx-font-weight: bold;");
        
        Label typeLabel = new Label(docType);
        typeLabel.setPrefWidth(180);
        typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #1a1a1a; -fx-font-weight: bold;");
        
        Label purposeLabel = new Label(purpose);
        purposeLabel.setPrefWidth(200);
        purposeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");
        
        Label dateLabel = new Label(date);
        dateLabel.setPrefWidth(120);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");
        
        Label statusLabel = new Label(status);
        statusLabel.setPrefWidth(120);
        
        // Set status styling
        switch (status.toLowerCase()) {
            case "pending":
                statusLabel.setStyle("-fx-background-color: #fff8e1; -fx-text-fill: #f59e0b; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 10;");
                break;
            case "in progress":
                statusLabel.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #2196f3; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 10;");
                break;
            case "ready":
                statusLabel.setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #4caf50; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 10;");
                break;
            case "released":
                statusLabel.setStyle("-fx-background-color: #f3e5f5; -fx-text-fill: #9c27b0; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 10;");
                break;
            default:
                statusLabel.setStyle("-fx-background-color: #f4f4f4; -fx-text-fill: #555555; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 10;");
        }
        
        row.getChildren().addAll(idLabel, typeLabel, purposeLabel, dateLabel, statusLabel);
        
        return row;
    }

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
    private void goToPayments() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("Payments.fxml"));
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
    private void goToProfile() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("MyProfile.fxml"));
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
}