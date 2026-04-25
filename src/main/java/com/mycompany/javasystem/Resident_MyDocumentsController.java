package com.mycompany.javasystem;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Resident_MyDocumentsController {
    @FXML
    private Button logoutButton;
    @FXML
    private VBox documentsContainer;
    @FXML
    private Label loadingLabel;
    @FXML
    private Button alertsButton;
    @FXML
    private Label alertBadge;
    @FXML
    private Label residentNameLabel;
    @FXML
    private javafx.scene.shape.Circle topBarProfileCircle;
    @FXML
    private Label topBarProfileInitials;

    @FXML
    public void initialize() {
        loadUserProfile();
        loadDocuments();
        ResidentNotifications.syncNotifications(UserSession.getCurrentUserEmail());
        refreshAlertBadge();
        loadTopBarProfilePicture();
    }

    private void loadUserProfile() {
        try {
            String currentUserEmail = UserSession.getCurrentUserEmail();
            if (currentUserEmail == null || currentUserEmail.trim().isEmpty()) {
                residentNameLabel.setText("Resident Name");
                return;
            }
            
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                residentNameLabel.setText("Resident Name");
                return;
            }
            
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT full_name FROM users WHERE email = ?");
            stmt.setString(1, currentUserEmail);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String fullName = rs.getString("full_name");
                residentNameLabel.setText(fullName != null && !fullName.trim().isEmpty() ? fullName : "Resident Name");
            } else {
                residentNameLabel.setText("Resident Name");
            }
            
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("Error loading user profile: " + e.getMessage());
            residentNameLabel.setText("Resident Name");
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
        row.setStyle("-fx-padding: 16 8; -fx-background-color: #ffffff; -fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
        
        Label idLabel = new Label(requestId);
        idLabel.setPrefWidth(120);
        idLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2196f3; -fx-font-weight: bold;");
        
        Label typeLabel = new Label(docType);
        typeLabel.setPrefWidth(180);
        typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #1a1a1a; -fx-font-weight: bold;");
        
        Label purposeLabel = new Label(purpose != null ? purpose : "N/A");
        purposeLabel.setPrefWidth(200);
        purposeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");
        
        Label dateLabel = new Label(date != null ? date : "N/A");
        dateLabel.setPrefWidth(120);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");
        
        Label statusLabel = new Label(status != null ? status : "Unknown");
        statusLabel.setPrefWidth(120);
        
        // Set status styling
        switch (status != null ? status.toLowerCase() : "unknown") {
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
            case "completed":
                statusLabel.setStyle("-fx-background-color: #f3e5f5; -fx-text-fill: #9c27b0; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 10;");
                break;
            default:
                statusLabel.setStyle("-fx-background-color: #f4f4f4; -fx-text-fill: #555555; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 10;");
        }
        
        // Add action buttons
        HBox actionBox = new HBox(6);
        actionBox.setPrefWidth(150);
        actionBox.setAlignment(Pos.CENTER_LEFT);
        
        Button viewBtn = new Button("👁 View");
        viewBtn.setStyle("-fx-background-color: #f4f4f4; -fx-text-fill: #555555; -fx-font-size: 10px; -fx-background-radius: 4; -fx-padding: 4 8; -fx-cursor: hand;");
        viewBtn.setOnAction(e -> showDocumentDetails(requestId, docType, purpose, date, status));
        
        actionBox.getChildren().add(viewBtn);
        
        // Add download button for completed documents
        if (status != null && (status.equalsIgnoreCase("ready") || status.equalsIgnoreCase("completed") || status.equalsIgnoreCase("released"))) {
            Button downloadBtn = new Button("💾 Download");
            downloadBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: #ffffff; -fx-font-size: 10px; -fx-background-radius: 4; -fx-padding: 4 8; -fx-cursor: hand;");
            downloadBtn.setOnAction(e -> downloadDocument(requestId, docType));
            actionBox.getChildren().add(downloadBtn);
        }
        
        row.getChildren().addAll(idLabel, typeLabel, purposeLabel, dateLabel, statusLabel, actionBox);
        
        // Add hover effect
        row.setOnMouseEntered(e -> row.setStyle("-fx-padding: 16 8; -fx-background-color: #f8f9fa; -fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-padding: 16 8; -fx-background-color: #ffffff; -fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0; -fx-cursor: hand;"));
        
        return row;
    }
    
    private void showDocumentDetails(String requestId, String docType, String purpose, String date, String status) {
        try {
            Stage detailDialog = new Stage();
            detailDialog.initModality(Modality.APPLICATION_MODAL);
            detailDialog.initOwner(logoutButton.getScene().getWindow());
            detailDialog.setTitle("Document Details - " + requestId);
            
            VBox root = new VBox(0);
            root.setStyle("-fx-background-color: #f8f9fa;");
            
            // Header
            VBox header = new VBox(8);
            header.setStyle("-fx-background-color: #2d2d2d; -fx-padding: 20;");
            Label titleLabel = new Label(docType);
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
            Label subtitleLabel = new Label("Request ID: " + requestId);
            subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");
            header.getChildren().addAll(titleLabel, subtitleLabel);
            
            // Content
            VBox content = new VBox(16);
            content.setStyle("-fx-padding: 24; -fx-background-color: #ffffff;");
            
            // Status indicator
            HBox statusBox = new HBox(8);
            statusBox.setAlignment(Pos.CENTER_LEFT);
            Label statusIcon = new Label(getStatusIcon(status));
            statusIcon.setStyle("-fx-font-size: 20px;");
            Label statusText = new Label("Status: " + (status != null ? status : "Unknown"));
            statusText.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
            statusBox.getChildren().addAll(statusIcon, statusText);
            
            // Details grid
            GridPane detailsGrid = new GridPane();
            detailsGrid.setHgap(16);
            detailsGrid.setVgap(12);
            
            addDetailRow(detailsGrid, 0, "Document Type:", docType);
            addDetailRow(detailsGrid, 1, "Purpose:", purpose != null ? purpose : "N/A");
            addDetailRow(detailsGrid, 2, "Request Date:", date != null ? date : "N/A");
            addDetailRow(detailsGrid, 3, "Current Status:", status != null ? status : "Unknown");
            
            // Progress indicator
            VBox progressBox = new VBox(8);
            Label progressLabel = new Label("Processing Progress:");
            progressLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #555555;");
            
            HBox progressSteps = new HBox(4);
            progressSteps.setAlignment(Pos.CENTER_LEFT);
            
            String[] steps = {"Submitted", "In Review", "Processing", "Ready", "Released"};
            int currentStep = getCurrentStep(status);
            
            for (int i = 0; i < steps.length; i++) {
                Label step = new Label(steps[i]);
                if (i <= currentStep) {
                    step.setStyle("-fx-background-color: #4caf50; -fx-text-fill: #ffffff; -fx-font-size: 9px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 3 6;");
                } else {
                    step.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #888888; -fx-font-size: 9px; -fx-background-radius: 4; -fx-padding: 3 6;");
                }
                progressSteps.getChildren().add(step);
                
                if (i < steps.length - 1) {
                    Label arrow = new Label("→");
                    arrow.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 10px;");
                    progressSteps.getChildren().add(arrow);
                }
            }
            
            progressBox.getChildren().addAll(progressLabel, progressSteps);
            
            content.getChildren().addAll(statusBox, detailsGrid, progressBox);
            
            // Footer
            HBox footer = new HBox(8);
            footer.setAlignment(Pos.CENTER_RIGHT);
            footer.setStyle("-fx-padding: 16 24; -fx-background-color: #ffffff; -fx-border-color: #e8e8e8; -fx-border-width: 1 0 0 0;");
            
            Button closeBtn = new Button("Close");
            closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555; -fx-font-size: 12px; -fx-background-radius: 6; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 8 16; -fx-cursor: hand;");
            closeBtn.setOnAction(e -> detailDialog.close());
            
            footer.getChildren().add(closeBtn);
            
            // Add download button if document is ready
            if (status != null && (status.equalsIgnoreCase("ready") || status.equalsIgnoreCase("completed") || status.equalsIgnoreCase("released"))) {
                Button downloadBtn = new Button("💾 Download Document");
                downloadBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: #ffffff; -fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand;");
                downloadBtn.setOnAction(e -> {
                    detailDialog.close();
                    downloadDocument(requestId, docType);
                });
                footer.getChildren().add(downloadBtn);
            }
            
            root.getChildren().addAll(header, content, footer);
            
            Scene scene = new Scene(root, 500, 400);
            detailDialog.setScene(scene);
            detailDialog.setResizable(false);
            detailDialog.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #555555;");
        
        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-font-size: 12px; -fx-text-fill: #1a1a1a;");
        
        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }
    
    private String getStatusIcon(String status) {
        if (status == null) return "❓";
        switch (status.toLowerCase()) {
            case "pending": return "⏳";
            case "in progress": return "🔄";
            case "ready": return "✅";
            case "released":
            case "completed": return "🎉";
            default: return "❓";
        }
    }
    
    private int getCurrentStep(String status) {
        if (status == null) return 0;
        switch (status.toLowerCase()) {
            case "pending": return 0;
            case "in progress": return 2;
            case "ready": return 3;
            case "released":
            case "completed": return 4;
            default: return 0;
        }
    }
    
    private void downloadDocument(String requestId, String docType) {
        Alert downloadInfo = new Alert(Alert.AlertType.INFORMATION);
        downloadInfo.setTitle("Download Document");
        downloadInfo.setHeaderText("Document Ready for Download");
        downloadInfo.setContentText("Your " + docType + " (" + requestId + ") is ready!\n\n" +
                                   "In a real system, this would:\n" +
                                   "• Generate a PDF document\n" +
                                   "• Download it to your computer\n" +
                                   "• Include official signatures and seals\n\n" +
                                   "For demo purposes, we're showing this message.");
        downloadInfo.showAndWait();
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
    private void goToMyDocuments() {
        // Already on this page
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
    private void handleAlertsClick() {
        try {
            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
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
            footer.setAlignment(Pos.CENTER_RIGHT);
            footer.setStyle("-fx-padding: 12 20; -fx-background-color: #ffffff; -fx-border-color: #e8e8e8; -fx-border-width: 1 0 0 0;");
            Button closeBtn = new Button("Close");
            closeBtn.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff; -fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand;");
            closeBtn.setOnAction(e -> {
                popup.close();
                refreshAlertBadge();
            });
            footer.getChildren().add(closeBtn);
            
            root.getChildren().addAll(header, filterBox, scrollPane, footer);
            
            Scene scene = new Scene(root, 480, 550);
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
            if (!showAll) {
                sql += " AND is_read = 'false'";
            }
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
                emptyBox.setAlignment(Pos.CENTER);
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
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-padding: 16 20; -fx-background-color: " + (isRead ? "#ffffff" : "#fafbff") + "; -fx-border-color: #e8e8e8; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
        
        String icon = "";
        String iconBg = "";
        if (type.equals("document")) {
            icon = "📄";
            iconBg = "#e3f2fd";
        } else if (type.equals("complaint")) {
            icon = "📢";
            iconBg = "#ffebee";
        } else if (type.equals("announcement")) {
            icon = "📣";
            iconBg = "#fff8e1";
        }
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 20px; -fx-background-color: " + iconBg + "; -fx-background-radius: 8; -fx-padding: 8; -fx-min-width: 40; -fx-min-height: 40; -fx-alignment: center;");
        
        VBox content = new VBox(4);
        content.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(content, Priority.ALWAYS);
        
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #1a1a1a;" + (isRead ? "" : " -fx-font-weight: bold;"));
        
        Label timeLabel = new Label(createdAt);
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888888;");
        
        content.getChildren().addAll(messageLabel, timeLabel);
        
        VBox indicator = new VBox();
        indicator.setAlignment(Pos.CENTER);
        if (!isRead) {
            Label dot = new Label("•");
            dot.setStyle("-fx-font-size: 20px; -fx-text-fill: #2196f3;");
            indicator.getChildren().add(dot);
        } else {
            Label readBadge = new Label("Read");
            readBadge.setStyle("-fx-font-size: 9px; -fx-text-fill: #888888; -fx-background-color: #f0f0f0; -fx-background-radius: 4; -fx-padding: 2 6;");
            indicator.getChildren().add(readBadge);
        }
        
        item.getChildren().addAll(iconLabel, content, indicator);
        
        item.setOnMouseClicked(e -> showNotifDetail(notifId, type, message, refId, isRead));
        
        return item;
    }
    
    private void showNotifDetail(int notifId, String type, String message, String refId, boolean isRead) {
        try {
            Stage detailPopup = new Stage();
            detailPopup.initModality(Modality.APPLICATION_MODAL);
            detailPopup.initOwner(logoutButton.getScene().getWindow());
            detailPopup.setTitle("Notification Detail");
            
            VBox root = new VBox(0);
            root.setStyle("-fx-background-color: #f8f9fa;");
            
            VBox header = new VBox(4);
            header.setStyle("-fx-background-color: #2d2d2d; -fx-padding: 20;");
            String headerTitle = type.equals("document") ? "Document Update" : type.equals("complaint") ? "Complaint Update" : "Announcement";
            Label titleLabel = new Label(headerTitle);
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
            header.getChildren().add(titleLabel);
            
            VBox content = new VBox(16);
            content.setStyle("-fx-padding: 24; -fx-background-color: #ffffff;");
            
            String icon = type.equals("document") ? "📄" : type.equals("complaint") ? "📢" : "📣";
            Label iconLabel = new Label(icon);
            iconLabel.setStyle("-fx-font-size: 32px;");
            
            Text messageText = new Text(message);
            messageText.setWrappingWidth(360);
            messageText.setStyle("-fx-font-size: 13px; -fx-fill: #333333;");
            
            Button goToBtn = new Button("→ Go to " + (type.equals("document") ? "My Documents" : type.equals("complaint") ? "My Complaints" : "Announcements"));
            goToBtn.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff; -fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 10 16; -fx-cursor: hand;");
            goToBtn.setOnAction(e -> {
                ResidentNotifications.markAsRead(notifId);
                detailPopup.close();
                if (type.equals("document")) {
                    goBackToDashboard();
                } else if (type.equals("complaint")) {
                    goToComplaints();
                } else if (type.equals("announcement")) {
                    goToAnnouncements();
                }
            });
            
            content.getChildren().addAll(iconLabel, messageText, goToBtn);
            
            HBox footer = new HBox(8);
            footer.setAlignment(Pos.CENTER_RIGHT);
            footer.setStyle("-fx-padding: 12 20; -fx-background-color: #ffffff; -fx-border-color: #e8e8e8; -fx-border-width: 1 0 0 0;");
            
            Button closeBtn = new Button("Close");
            closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555; -fx-font-size: 12px; -fx-background-radius: 6; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 8 16; -fx-cursor: hand;");
            closeBtn.setOnAction(e -> detailPopup.close());
            
            footer.getChildren().add(closeBtn);
            
            if (!isRead) {
                Button markReadBtn = new Button("Mark as Read");
                markReadBtn.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff; -fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand;");
                markReadBtn.setOnAction(e -> {
                    ResidentNotifications.markAsRead(notifId);
                    detailPopup.close();
                    refreshAlertBadge();
                });
                footer.getChildren().add(markReadBtn);
            }
            
            root.getChildren().addAll(header, content, footer);
            
            Scene scene = new Scene(root, 420, 320);
            detailPopup.setScene(scene);
            detailPopup.setResizable(false);
            detailPopup.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadTopBarProfilePicture() {
        if (topBarProfileCircle != null && topBarProfileInitials != null) {
            ProfilePictureLoader.loadProfilePicture(topBarProfileCircle, topBarProfileInitials, UserSession.getCurrentUserEmail());
        }
    }
}