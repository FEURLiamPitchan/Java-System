package com.mycompany.javasystem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ResidentDashboardController {
    @FXML
    private Button logoutButton;
    @FXML
    private Label residentNameLabel;
    @FXML
    private Button alertsButton;
    @FXML
    private Label alertBadge;
    @FXML
    private javafx.scene.shape.Circle topBarProfileCircle;
    @FXML
    private Label topBarProfileInitials;

    @FXML
    public void initialize() {
        loadUserProfile();
        loadDashboardStats();
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

    private void loadDashboardStats() {
        // This method could load real statistics from database
        // For now, we'll keep the static data but this shows where dynamic data would go
        System.out.println("Dashboard stats loaded");
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

    @FXML
    private void showNotifications() {
        handleAlertsClick();
    }

    private void switchScene(String fxml, boolean maximize) {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Parent newRoot = FXMLLoader.load(getClass().getResource(fxml));
            stage.setMaximized(maximize);
            stage.getScene().setRoot(newRoot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        switchScene("login.fxml", false);
    }

    @FXML
    private void goToMyDocuments() {
        switchScene("MyDocuments.fxml", true);
    }

    @FXML
    private void goToRequestDocument() {
        switchScene("RequestDocument.fxml", true);
    }

    @FXML
    private void goToAnnouncements() {
        switchScene("ResidentAnnouncements.fxml", true);
    }

    @FXML
    private void goToComplaints() {
        switchScene("Complaints_Resident.fxml", true);
    }

    @FXML
    private void goToPayments() {
        switchScene("ResidentPayments.fxml", true);
    }

    
    private VBox createSummaryCard(String title, String value, String subtitle, String color) {
        VBox card = new VBox(4);
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; -fx-border-color: #eeeeee; -fx-border-width: 1; -fx-padding: 20;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaaaaa;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
        
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + color + ";");
        
        card.getChildren().addAll(titleLabel, valueLabel, subtitleLabel);
        return card;
    }
    
    private HBox createPaymentRow(String ref, String service, String amount, String status, boolean isPaid) {
        HBox row = new HBox();
        row.setStyle("-fx-padding: 14 0; -fx-border-color: #f8f8f8; -fx-border-width: 0 0 1 0;");
        
        Label refLabel = new Label(ref);
        refLabel.setPrefWidth(150);
        refLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2196f3; -fx-font-weight: bold;");
        
        Label serviceLabel = new Label(service);
        serviceLabel.setPrefWidth(200);
        serviceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333333; -fx-font-weight: bold;");
        
        Label amountLabel = new Label(amount);
        amountLabel.setPrefWidth(120);
        amountLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
        
        String statusBg = isPaid ? "#e8f5e9" : "#fff8e1";
        String statusFg = isPaid ? "#4caf50" : "#f59e0b";
        Label statusLabel = new Label(status);
        statusLabel.setStyle("-fx-background-color: " + statusBg + "; -fx-text-fill: " + statusFg + "; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 3 8;");
        
        HBox statusBox = new HBox(statusLabel);
        statusBox.setPrefWidth(120);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        
        HBox actionBox = new HBox(6);
        actionBox.setPrefWidth(150);
        actionBox.setAlignment(Pos.CENTER_LEFT);
        
        if (isPaid) {
            Label completedLabel = new Label("✓ Completed");
            completedLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #4caf50;");
            actionBox.getChildren().add(completedLabel);
        } else {
            Button payBtn = new Button("Pay Now");
            payBtn.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 5 10; -fx-cursor: hand;");
            payBtn.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Payment");
                alert.setHeaderText("Payment for " + ref);
                alert.setContentText("Payment functionality will be available soon!");
                alert.showAndWait();
            });
            actionBox.getChildren().add(payBtn);
        }
        
        row.getChildren().addAll(refLabel, serviceLabel, amountLabel, statusBox, actionBox);
        return row;
    }
    
    private HBox createTopBar(String pageName) {
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPrefHeight(65);
        topBar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e8e8e8; -fx-border-width: 0 0 1 0; -fx-padding: 0 24;");
        
        HBox leftSection = new HBox(12);
        leftSection.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(leftSection, Priority.ALWAYS);
        
        javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(18);
        circle.setFill(javafx.scene.paint.Color.web("#2d2d2d"));
        
        VBox titleBox = new VBox(2);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        Label mainTitle = new Label("Barangay San Isidro");
        mainTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
        Label subTitle = new Label("Resident Portal");
        subTitle.setStyle("-fx-font-size: 10px; -fx-text-fill: #aaaaaa;");
        titleBox.getChildren().addAll(mainTitle, subTitle);
        
        leftSection.getChildren().addAll(circle, titleBox);
        
        HBox rightSection = new HBox(12);
        rightSection.setAlignment(Pos.CENTER_RIGHT);
        
        Button backBtn = new Button("← Back to Dashboard");
        backBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #555555; -fx-font-size: 12px; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 8 16; -fx-cursor: hand;");
        backBtn.setOnAction(e -> {
            try {
                Parent dashRoot = FXMLLoader.load(getClass().getResource("ResidentDashboard.fxml"));
                Stage stage = (Stage) backBtn.getScene().getWindow();
                stage.getScene().setRoot(dashRoot);
                stage.setMaximized(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        rightSection.getChildren().add(backBtn);
        topBar.getChildren().addAll(leftSection, rightSection);
        
        return topBar;
    }
    
    private void createRequestDocumentPage(Stage stage) {
        try {
            VBox root = new VBox(0);
            root.setStyle("-fx-background-color: #f4f4f4;");
            
            // Top Bar
            HBox topBar = createTopBar("Request Document");
            
            // Main Content
            VBox content = new VBox(20);
            content.setStyle("-fx-padding: 40;");
            
            Label pageTitle = new Label("Request Document");
            pageTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
            
            Label pageSubtitle = new Label("Request official documents from the barangay");
            pageSubtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
            
            // Document Types Grid
            HBox docGrid = new HBox(16);
            
            VBox clearanceCard = createDocumentCard("Barangay Clearance", "₱150.00", "For employment, business, or legal purposes", "#4caf50");
            VBox residencyCard = createDocumentCard("Certificate of Residency", "₱100.00", "Proof of residence in the barangay", "#2196f3");
            VBox indigencyCard = createDocumentCard("Indigency Certificate", "₱200.00", "For financial assistance applications", "#ff9800");
            
            HBox.setHgrow(clearanceCard, Priority.ALWAYS);
            HBox.setHgrow(residencyCard, Priority.ALWAYS);
            HBox.setHgrow(indigencyCard, Priority.ALWAYS);
            
            docGrid.getChildren().addAll(clearanceCard, residencyCard, indigencyCard);
            
            content.getChildren().addAll(pageTitle, pageSubtitle, docGrid);
            root.getChildren().addAll(topBar, content);
            
            stage.getScene().setRoot(root);
            stage.setMaximized(true);
            System.out.println("Request Document page created successfully");
            
        } catch (Exception e) {
            System.err.println("Error creating Request Document page: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private VBox createDocumentCard(String title, String price, String description, String color) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; -fx-border-color: #eeeeee; -fx-border-width: 1; -fx-padding: 24;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
        
        Label priceLabel = new Label(price);
        priceLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666; -fx-wrap-text: true;");
        descLabel.setMaxWidth(200);
        
        Button requestBtn = new Button("Request Now");
        requestBtn.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff; -fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 10 16; -fx-cursor: hand;");
        requestBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Document Request");
            alert.setHeaderText("Request " + title);
            alert.setContentText("Document request functionality will be available soon!");
            alert.showAndWait();
        });
        
        card.getChildren().addAll(titleLabel, priceLabel, descLabel, requestBtn);
        return card;
    }
    
    private void createAnnouncementsPage(Stage stage) {
        try {
            VBox root = new VBox(0);
            root.setStyle("-fx-background-color: #f4f4f4;");
            
            // Top Bar
            HBox topBar = createTopBar("Announcements");
            
            // Main Content
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: #f4f4f4; -fx-background-color: #f4f4f4;");
            
            VBox content = new VBox(20);
            content.setStyle("-fx-padding: 40;");
            
            Label pageTitle = new Label("Announcements");
            pageTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
            
            Label pageSubtitle = new Label("Stay updated with barangay news and events");
            pageSubtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
            
            // Announcements List
            VBox announcementsList = new VBox(16);
            
            announcementsList.getChildren().addAll(
                createAnnouncementCard("Community Clean-up Drive", "Join us this Saturday for our monthly community clean-up. Bring your own cleaning materials.", "2 days ago", "#4caf50"),
                createAnnouncementCard("Barangay Assembly Meeting", "Monthly barangay assembly meeting scheduled for next Friday at 7:00 PM at the barangay hall.", "5 days ago", "#2196f3"),
                createAnnouncementCard("Health Center Schedule", "The barangay health center will be closed on Monday for maintenance. Emergency services available.", "1 week ago", "#ff9800")
            );
            
            content.getChildren().addAll(pageTitle, pageSubtitle, announcementsList);
            scrollPane.setContent(content);
            
            root.getChildren().addAll(topBar, scrollPane);
            stage.getScene().setRoot(root);
            stage.setMaximized(true);
            System.out.println("Announcements page created successfully");
            
        } catch (Exception e) {
            System.err.println("Error creating Announcements page: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private VBox createAnnouncementCard(String title, String content, String date, String color) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; -fx-border-color: #eeeeee; -fx-border-width: 1; -fx-padding: 20;");
        
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        
        Label dateLabel = new Label(date);
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888888;");
        
        header.getChildren().addAll(titleLabel, dateLabel);
        
        Label contentLabel = new Label(content);
        contentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-wrap-text: true;");
        contentLabel.setMaxWidth(800);
        
        Label categoryLabel = new Label("• Important");
        categoryLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + color + "; -fx-font-weight: bold;");
        
        card.getChildren().addAll(header, contentLabel, categoryLabel);
        return card;
    }
    
    private void createComplaintsPage(Stage stage) {
        try {
            VBox root = new VBox(0);
            root.setStyle("-fx-background-color: #f4f4f4;");
            
            // Top Bar
            HBox topBar = createTopBar("My Complaints");
            
            // Main Content
            VBox content = new VBox(20);
            content.setStyle("-fx-padding: 40;");
            
            Label pageTitle = new Label("My Complaints");
            pageTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
            
            Label pageSubtitle = new Label("Track your submitted complaints and their status");
            pageSubtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
            
            // New Complaint Button
            Button newComplaintBtn = new Button("+ Submit New Complaint");
            newComplaintBtn.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff; -fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 12 20; -fx-cursor: hand;");
            newComplaintBtn.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("New Complaint");
                alert.setHeaderText("Submit New Complaint");
                alert.setContentText("Complaint submission form will be available soon!");
                alert.showAndWait();
            });
            
            // Complaints List
            VBox complaintsList = new VBox(0);
            complaintsList.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; -fx-border-color: #eeeeee; -fx-border-width: 1;");
            
            // Header
            HBox listHeader = new HBox();
            listHeader.setStyle("-fx-padding: 14 16; -fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0; -fx-background-color: #fafafa; -fx-background-radius: 10 10 0 0;");
            
            Label refHeader = new Label("COMPLAINT ID");
            refHeader.setPrefWidth(150);
            refHeader.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #aaaaaa;");
            
            Label subjectHeader = new Label("SUBJECT");
            subjectHeader.setPrefWidth(250);
            subjectHeader.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #aaaaaa;");
            
            Label statusHeader = new Label("STATUS");
            statusHeader.setPrefWidth(120);
            statusHeader.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #aaaaaa;");
            
            Label dateHeader = new Label("DATE SUBMITTED");
            dateHeader.setPrefWidth(150);
            dateHeader.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #aaaaaa;");
            
            listHeader.getChildren().addAll(refHeader, subjectHeader, statusHeader, dateHeader);
            
            // Complaint Rows
            VBox complaintRows = new VBox(0);
            complaintRows.setStyle("-fx-padding: 0 16;");
            
            complaintRows.getChildren().addAll(
                createComplaintRow("CMP-2024-001", "Noise complaint from neighbor", "Under Review", "Dec 15, 2024", "#f59e0b"),
                createComplaintRow("CMP-2024-002", "Street light not working", "Resolved", "Dec 10, 2024", "#4caf50"),
                createComplaintRow("CMP-2024-003", "Garbage collection issue", "In Progress", "Dec 8, 2024", "#2196f3")
            );
            
            complaintsList.getChildren().addAll(listHeader, complaintRows);
            
            content.getChildren().addAll(pageTitle, pageSubtitle, newComplaintBtn, complaintsList);
            root.getChildren().addAll(topBar, content);
            
            stage.getScene().setRoot(root);
            stage.setMaximized(true);
            System.out.println("Complaints page created successfully");
            
        } catch (Exception e) {
            System.err.println("Error creating Complaints page: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private HBox createComplaintRow(String id, String subject, String status, String date, String statusColor) {
        HBox row = new HBox();
        row.setStyle("-fx-padding: 14 0; -fx-border-color: #f8f8f8; -fx-border-width: 0 0 1 0;");
        
        Label idLabel = new Label(id);
        idLabel.setPrefWidth(150);
        idLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2196f3; -fx-font-weight: bold;");
        
        Label subjectLabel = new Label(subject);
        subjectLabel.setPrefWidth(250);
        subjectLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333333;");
        
        Label statusLabel = new Label(status);
        statusLabel.setStyle("-fx-background-color: " + statusColor + "20; -fx-text-fill: " + statusColor + "; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 3 8;");
        
        HBox statusBox = new HBox(statusLabel);
        statusBox.setPrefWidth(120);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        
        Label dateLabel = new Label(date);
        dateLabel.setPrefWidth(150);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");
        
        row.getChildren().addAll(idLabel, subjectLabel, statusBox, dateLabel);
        return row;
    }
    
    private void createMyDocumentsPage(Stage stage) {
        try {
            VBox root = new VBox(0);
            root.setStyle("-fx-background-color: #f4f4f4;");
            
            // Top Bar
            HBox topBar = createTopBar("My Documents");
            
            // Main Content
            VBox content = new VBox(20);
            content.setStyle("-fx-padding: 40;");
            
            Label pageTitle = new Label("My Documents");
            pageTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
            
            Label pageSubtitle = new Label("View and track your document requests");
            pageSubtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
            
            // Summary Cards
            HBox summaryCards = new HBox(16);
            
            VBox totalCard = createSummaryCard("Total Requests", "4", "All time", "#2196f3");
            VBox pendingCard = createSummaryCard("Pending", "1", "Awaiting approval", "#f59e0b");
            VBox completedCard = createSummaryCard("Completed", "3", "Ready for pickup", "#4caf50");
            
            HBox.setHgrow(totalCard, Priority.ALWAYS);
            HBox.setHgrow(pendingCard, Priority.ALWAYS);
            HBox.setHgrow(completedCard, Priority.ALWAYS);
            
            summaryCards.getChildren().addAll(totalCard, pendingCard, completedCard);
            
            // Documents List
            VBox documentsList = new VBox(0);
            documentsList.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; -fx-border-color: #eeeeee; -fx-border-width: 1;");
            
            // Header
            HBox listHeader = new HBox();
            listHeader.setStyle("-fx-padding: 14 16; -fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0; -fx-background-color: #fafafa; -fx-background-radius: 10 10 0 0;");
            
            Label refHeader = new Label("REF #");
            refHeader.setPrefWidth(150);
            refHeader.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #aaaaaa;");
            
            Label docHeader = new Label("DOCUMENT TYPE");
            docHeader.setPrefWidth(200);
            docHeader.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #aaaaaa;");
            
            Label statusHeader = new Label("STATUS");
            statusHeader.setPrefWidth(120);
            statusHeader.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #aaaaaa;");
            
            Label dateHeader = new Label("DATE REQUESTED");
            dateHeader.setPrefWidth(150);
            dateHeader.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #aaaaaa;");
            
            Label actionHeader = new Label("ACTION");
            actionHeader.setPrefWidth(120);
            actionHeader.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #aaaaaa;");
            
            listHeader.getChildren().addAll(refHeader, docHeader, statusHeader, dateHeader, actionHeader);
            
            // Document Rows
            VBox documentRows = new VBox(0);
            documentRows.setStyle("-fx-padding: 0 16;");
            
            documentRows.getChildren().addAll(
                createDocumentRow("REQ-2024-001", "Barangay Clearance", "Ready for Pickup", "Dec 10, 2024", "#4caf50", true),
                createDocumentRow("REQ-2024-002", "Certificate of Residency", "Ready for Pickup", "Dec 8, 2024", "#4caf50", true),
                createDocumentRow("REQ-2024-003", "Indigency Certificate", "Ready for Pickup", "Dec 5, 2024", "#4caf50", true),
                createDocumentRow("REQ-2024-004", "Business Permit", "Under Review", "Dec 15, 2024", "#f59e0b", false)
            );
            
            documentsList.getChildren().addAll(listHeader, documentRows);
            
            content.getChildren().addAll(pageTitle, pageSubtitle, summaryCards, documentsList);
            root.getChildren().addAll(topBar, content);
            
            stage.getScene().setRoot(root);
            stage.setMaximized(true);
            System.out.println("My Documents page created successfully");
            
        } catch (Exception e) {
            System.err.println("Error creating My Documents page: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private HBox createDocumentRow(String ref, String docType, String status, String date, String statusColor, boolean isReady) {
        HBox row = new HBox();
        row.setStyle("-fx-padding: 14 0; -fx-border-color: #f8f8f8; -fx-border-width: 0 0 1 0;");
        
        Label refLabel = new Label(ref);
        refLabel.setPrefWidth(150);
        refLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2196f3; -fx-font-weight: bold;");
        
        Label docLabel = new Label(docType);
        docLabel.setPrefWidth(200);
        docLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333333; -fx-font-weight: bold;");
        
        Label statusLabel = new Label(status);
        statusLabel.setStyle("-fx-background-color: " + statusColor + "20; -fx-text-fill: " + statusColor + "; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 3 8;");
        
        HBox statusBox = new HBox(statusLabel);
        statusBox.setPrefWidth(120);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        
        Label dateLabel = new Label(date);
        dateLabel.setPrefWidth(150);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");
        
        HBox actionBox = new HBox(6);
        actionBox.setPrefWidth(120);
        actionBox.setAlignment(Pos.CENTER_LEFT);
        
        if (isReady) {
            Button downloadBtn = new Button("Download");
            downloadBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 5 10; -fx-cursor: hand;");
            downloadBtn.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Download");
                alert.setHeaderText("Download " + ref);
                alert.setContentText("Document download will be available soon!");
                alert.showAndWait();
            });
            actionBox.getChildren().add(downloadBtn);
        } else {
            Label pendingLabel = new Label("⏳ Pending");
            pendingLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #f59e0b;");
            actionBox.getChildren().add(pendingLabel);
        }
        
        row.getChildren().addAll(refLabel, docLabel, statusBox, dateLabel, actionBox);
        return row;
    }

    @FXML
    private void goToMyProfile() {
        switchScene("MyProfile.fxml", true);
    }

    @FXML
    private void goToSettings() {
        switchScene("ResidentSettings.fxml", true);
    }
    
    private void loadTopBarProfilePicture() {
        if (topBarProfileCircle != null && topBarProfileInitials != null) {
            ProfilePictureLoader.loadProfilePicture(topBarProfileCircle, topBarProfileInitials, UserSession.getCurrentUserEmail());
        }
    }
    
    
    private void createErrorPage(String pageName, String errorMessage) {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            
            VBox root = new VBox(20);
            root.setStyle("-fx-background-color: #f4f4f4; -fx-padding: 40; -fx-alignment: center;");
            
            Label errorIcon = new Label("⚠️");
            errorIcon.setStyle("-fx-font-size: 48px;");
            
            Label title = new Label("Error Loading " + pageName);
            title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #d32f2f;");
            
            Label subtitle = new Label("Sorry, we couldn't load the " + pageName + " page.");
            subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
            
            Label errorDetails = new Label("Error: " + errorMessage);
            errorDetails.setStyle("-fx-font-size: 12px; -fx-text-fill: #999999; -fx-wrap-text: true;");
            errorDetails.setMaxWidth(600);
            
            Button backBtn = new Button("← Back to Dashboard");
            backBtn.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff; -fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 10 16; -fx-cursor: hand;");
            backBtn.setOnAction(e -> {
                try {
                    Parent dashRoot = FXMLLoader.load(getClass().getResource("ResidentDashboard.fxml"));
                    stage.getScene().setRoot(dashRoot);
                    stage.setMaximized(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            
            root.getChildren().addAll(errorIcon, title, subtitle, errorDetails, backBtn);
            stage.getScene().setRoot(root);
            stage.setMaximized(true);
            
        } catch (Exception e) {
            System.err.println("Error creating error page: " + e.getMessage());
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
            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.initOwner(logoutButton.getScene().getWindow());
            popup.setTitle("Notifications");
            
            VBox root = new VBox(0);
            root.setStyle("-fx-background-color: #f8f9fa;");
            
            // Header
            VBox header = new VBox(4);
            header.setStyle("-fx-background-color: #2d2d2d; -fx-padding: 20;");
            Label titleLabel = new Label("Notifications");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
            Label subtitleLabel = new Label("Stay updated with your requests and announcements");
            subtitleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaaaaa;");
            header.getChildren().addAll(titleLabel, subtitleLabel);
            
            // Filter buttons
            HBox filterBox = new HBox(8);
            filterBox.setStyle("-fx-padding: 12 20; -fx-background-color: #ffffff; -fx-border-color: #e8e8e8; -fx-border-width: 0 0 1 0;");
            Button unreadBtn = new Button("Unread");
            unreadBtn.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;");
            Button allBtn = new Button("Past Notifications");
            allBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;");
            filterBox.getChildren().addAll(unreadBtn, allBtn);
            
            // Notifications list
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
            
            // Footer
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
        
        // Icon
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
        
        // Content
        VBox content = new VBox(4);
        content.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(content, Priority.ALWAYS);
        
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #1a1a1a;" + (isRead ? "" : " -fx-font-weight: bold;"));
        
        Label timeLabel = new Label(createdAt);
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888888;");
        
        content.getChildren().addAll(messageLabel, timeLabel);
        
        // Indicator
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
            
            // Header
            VBox header = new VBox(4);
            header.setStyle("-fx-background-color: #2d2d2d; -fx-padding: 20;");
            String headerTitle = type.equals("document") ? "Document Update" : type.equals("complaint") ? "Complaint Update" : "Announcement";
            Label titleLabel = new Label(headerTitle);
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
            header.getChildren().add(titleLabel);
            
            // Content
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
                    goToMyDocuments();
                } else if (type.equals("complaint")) {
                    goToComplaints();
                } else if (type.equals("announcement")) {
                    goToAnnouncements();
                }
            });
            
            content.getChildren().addAll(iconLabel, messageText, goToBtn);
            
            // Footer
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
}
