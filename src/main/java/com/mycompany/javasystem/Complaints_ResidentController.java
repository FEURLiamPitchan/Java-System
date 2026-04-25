package com.mycompany.javasystem;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Complaints_ResidentController {

    @FXML private VBox complaintsTableBody;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatus;
    @FXML private ComboBox<String> filterType;
    @FXML private Button logoutButton;
    @FXML private Label residentNameLabel;
    @FXML private Label totalLabel;
    @FXML private Label pendingLabel;
    @FXML private Label underReviewLabel;
    @FXML private Label resolvedLabel;
    @FXML private Button alertsButton;
    @FXML private Label alertBadge;
    @FXML private javafx.scene.shape.Circle topBarProfileCircle;
    @FXML private Label topBarProfileInitials;

    @FXML
    public void initialize() {
        loadUserProfile();
        filterStatus.getItems().addAll("All", "Pending", "Under Review", "Resolved");
        filterStatus.setValue("All");
        filterType.getItems().addAll("All", "Noise Complaint", "Property Dispute",
                "Public Disturbance", "Infrastructure Issue", "Other");
        filterType.setValue("All");
        loadComplaints("", "All", "All");
        loadSummary();
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

    private void loadSummary() {
        String userEmail = UserSession.getCurrentUserEmail();
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                totalLabel.setText("0");
                pendingLabel.setText("0");
                underReviewLabel.setText("0");
                resolvedLabel.setText("0");
                return;
            }

            PreparedStatement ps1 = conn.prepareStatement(
                "SELECT COUNT(*) FROM complaints WHERE user_email = ?");
            ps1.setString(1, userEmail);
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) totalLabel.setText(String.valueOf(rs1.getInt(1)));

            PreparedStatement ps2 = conn.prepareStatement(
                "SELECT COUNT(*) FROM complaints WHERE user_email = ? AND status = 'Pending'");
            ps2.setString(1, userEmail);
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) pendingLabel.setText(String.valueOf(rs2.getInt(1)));

            PreparedStatement ps3 = conn.prepareStatement(
                "SELECT COUNT(*) FROM complaints WHERE user_email = ? AND status = 'Under Review'");
            ps3.setString(1, userEmail);
            ResultSet rs3 = ps3.executeQuery();
            if (rs3.next()) underReviewLabel.setText(String.valueOf(rs3.getInt(1)));

            PreparedStatement ps4 = conn.prepareStatement(
                "SELECT COUNT(*) FROM complaints WHERE user_email = ? AND status = 'Resolved'");
            ps4.setString(1, userEmail);
            ResultSet rs4 = ps4.executeQuery();
            if (rs4.next()) resolvedLabel.setText(String.valueOf(rs4.getInt(1)));

            conn.close();
        } catch (Exception e) {
            totalLabel.setText("0");
            pendingLabel.setText("0");
            underReviewLabel.setText("0");
            resolvedLabel.setText("0");
        }
    }

    private void loadComplaints(String search, String status, String type) {
        complaintsTableBody.getChildren().clear();
        String userEmail = UserSession.getCurrentUserEmail();

        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                showNoComplaints();
                return;
            }

            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM complaints WHERE user_email = ? ORDER BY id DESC");
            stmt.setString(1, userEmail);
            ResultSet rs = stmt.executeQuery();

            boolean hasData = false;

            while (rs.next()) {
                String complaintId = rs.getString("complaint_id");
                String incidentType = rs.getString("incident_type");
                String location = rs.getString("location");
                String dateFiled = rs.getString("date_filed");
                String complaintStatus = rs.getString("status");
                String details = rs.getString("incident_details");
                String photoPath = rs.getString("evidence_photos");
                String adminResponse = rs.getString("admin_response");

                if (!search.isEmpty()) {
                    if (!complaintId.toLowerCase().contains(search.toLowerCase()) &&
                        !incidentType.toLowerCase().contains(search.toLowerCase())) {
                        continue;
                    }
                }
                if (!status.equals("All") && !complaintStatus.equals(status)) continue;
                if (!type.equals("All") && !incidentType.equals(type)) continue;

                hasData = true;
                complaintsTableBody.getChildren().add(createComplaintRow(
                    complaintId, incidentType, location, dateFiled, 
                    complaintStatus, details, photoPath, adminResponse));
            }

            if (!hasData) {
                showNoComplaints();
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            showNoComplaints();
        }
    }

    private void showNoComplaints() {
        Label emptyLabel = new Label("No complaints found.");
        emptyLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #aaaaaa; -fx-padding: 40 0;");
        complaintsTableBody.getChildren().add(emptyLabel);
    }

    private HBox createComplaintRow(String complaintId, String incidentType,
            String location, String dateFiled, String complaintStatus, String details,
            String photoPath, String adminResponse) {

        HBox row = new HBox();
        row.setStyle("-fx-padding: 14 0; -fx-border-color: #f8f8f8; -fx-border-width: 0 0 1 0;");

        Label idLabel = new Label(complaintId);
        idLabel.setPrefWidth(140);
        idLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2196f3; -fx-font-weight: bold;");

        Label typeLabel = new Label(incidentType);
        typeLabel.setPrefWidth(200);
        typeLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Label locationLabel = new Label(location);
        locationLabel.setPrefWidth(180);
        locationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

        Label dateLabel = new Label(dateFiled != null ? dateFiled : "N/A");
        dateLabel.setPrefWidth(140);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

        String statusBg, statusFg;
        switch (complaintStatus) {
            case "Resolved":
                statusBg = "#e8f5e9"; statusFg = "#4caf50"; break;
            case "Under Review":
                statusBg = "#e3f2fd"; statusFg = "#1e88e5"; break;
            default:
                statusBg = "#fff8e1"; statusFg = "#f59e0b"; break;
        }
        Label statusLabel = new Label(complaintStatus);
        statusLabel.setStyle("-fx-background-color: " + statusBg + ";" +
                "-fx-text-fill: " + statusFg + ";" +
                "-fx-font-size: 11px; -fx-font-weight: bold;" +
                "-fx-background-radius: 4; -fx-padding: 3 8;");
        HBox statusBox = new HBox(statusLabel);
        statusBox.setPrefWidth(140);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        HBox actionBox = new HBox(6);
        actionBox.setPrefWidth(120);
        actionBox.setAlignment(Pos.CENTER_LEFT);

        Button viewBtn = new Button("View Details");
        viewBtn.setStyle("-fx-background-color: #2d2d2d;" +
                "-fx-text-fill: #ffffff;" +
                "-fx-font-size: 11px;" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 5 12;" +
                "-fx-cursor: hand;");
        viewBtn.setOnAction(e -> openComplaintModal(complaintId, incidentType,
                location, dateFiled, complaintStatus, details, photoPath, adminResponse));

        actionBox.getChildren().add(viewBtn);
        row.getChildren().addAll(idLabel, typeLabel, locationLabel,
                dateLabel, statusBox, actionBox);
        return row;
    }

    private void openComplaintModal(String complaintId, String type,
            String location, String date, String status, String details,
            String photoPath, String adminResponse) {
        try {
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(logoutButton.getScene().getWindow());
            modalStage.setTitle("Complaint Details");

            VBox root = new VBox(0);
            root.setStyle("-fx-background-color: #f8f9fa;");

            VBox header = new VBox(4);
            header.setStyle("-fx-background-color: #2d2d2d; -fx-padding: 20;");
            Label titleLabel = new Label("Complaint Details");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
            Label idLabel = new Label(complaintId);
            idLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");
            header.getChildren().addAll(titleLabel, idLabel);

            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: #f8f9fa; -fx-background-color: #f8f9fa;");

            VBox content = new VBox(16);
            content.setStyle("-fx-padding: 24;");

            VBox typeBox = createDetailBox("Incident Type", type);
            VBox locationBox = createDetailBox("Location", location);
            VBox dateBox = createDetailBox("Date Filed", date);
            VBox statusBox = createDetailBox("Status", status);
            VBox detailsBox = createDetailBox("Details", details);

            content.getChildren().addAll(typeBox, locationBox, dateBox, statusBox, detailsBox);

            if (adminResponse != null && !adminResponse.trim().isEmpty()) {
                VBox responseBox = new VBox(8);
                responseBox.setStyle("-fx-background-color: #e3f2fd; -fx-background-radius: 8; -fx-padding: 16;");
                Label responseTitle = new Label("Admin Response");
                responseTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1e88e5;");
                Text responseText = new Text(adminResponse);
                responseText.setWrappingWidth(460);
                responseText.setStyle("-fx-font-size: 12px; -fx-fill: #333333;");
                responseBox.getChildren().addAll(responseTitle, responseText);
                content.getChildren().add(responseBox);
            }

            scrollPane.setContent(content);

            HBox footer = new HBox();
            footer.setAlignment(Pos.CENTER_RIGHT);
            footer.setStyle("-fx-padding: 12 20; -fx-background-color: #ffffff; -fx-border-color: #e8e8e8; -fx-border-width: 1 0 0 0;");
            Button closeBtn = new Button("Close");
            closeBtn.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff; -fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand;");
            closeBtn.setOnAction(e -> modalStage.close());
            footer.getChildren().add(closeBtn);

            root.getChildren().addAll(header, scrollPane, footer);

            Scene scene = new Scene(root, 520, 600);
            modalStage.setScene(scene);
            modalStage.setResizable(false);
            modalStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox createDetailBox(String label, String value) {
        VBox box = new VBox(6);
        box.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8; -fx-padding: 16; -fx-border-color: #e8e8e8; -fx-border-width: 1; -fx-border-radius: 8;");
        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #888888;");
        Text valueText = new Text(value != null ? value : "N/A");
        valueText.setWrappingWidth(460);
        valueText.setStyle("-fx-font-size: 13px; -fx-fill: #333333;");
        box.getChildren().addAll(labelText, valueText);
        return box;
    }

    @FXML
    private void handleSearch() {
        loadComplaints(searchField.getText().trim(), filterStatus.getValue(), filterType.getValue());
    }

    @FXML
    private void handleFilter() {
        loadComplaints(searchField.getText().trim(), filterStatus.getValue(), filterType.getValue());
    }

    @FXML
    private void openSubmitComplaintModal() {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Submit New Complaint");
        
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: white;");
        
        // Light header with icon and close button
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10 16; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        header.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label("📝");
        icon.setStyle("-fx-font-size: 14px;");
        Label title = new Label("  Submit New Complaint");
        title.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #333;");
        HBox.setHgrow(title, Priority.ALWAYS);
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #666; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 0;");
        closeBtn.setOnAction(e -> modal.close());
        header.getChildren().addAll(icon, title, closeBtn);
        
        // Form content
        VBox form = new VBox(16);
        form.setStyle("-fx-padding: 20; -fx-background-color: white;");
        
        // Incident Type
        VBox typeBox = new VBox(6);
        Label typeLabel = new Label("INCIDENT TYPE");
        typeLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #999;");
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Noise Complaint", "Property Dispute", "Public Disturbance", "Infrastructure Issue", "Other");
        typeCombo.setValue("Other");
        typeCombo.setMaxWidth(Double.MAX_VALUE);
        typeCombo.setStyle("-fx-font-size: 12px; -fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 8;");
        typeBox.getChildren().addAll(typeLabel, typeCombo);
        
        // Date Filed
        VBox dateBox = new VBox(6);
        Label dateLabel = new Label("DATE FILED");
        dateLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #999;");
        DatePicker datePicker = new DatePicker(java.time.LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);
        datePicker.setStyle("-fx-font-size: 12px; -fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 4; -fx-background-radius: 4;");
        dateBox.getChildren().addAll(dateLabel, datePicker);
        
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 11px;");
        errorLabel.setWrapText(true);
        
        form.getChildren().addAll(typeBox, dateBox, errorLabel);
        
        // Footer with buttons
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle("-fx-padding: 12 20; -fx-background-color: #fafafa; -fx-border-color: #e0e0e0; -fx-border-width: 1 0 0 0;");
        
        Button cancelBtn = new Button("Close");
        cancelBtn.setStyle("-fx-background-color: white; -fx-text-fill: #555; -fx-font-size: 12px; -fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 8 20; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> modal.close());
        
        Button submitBtn = new Button("Submit Complaint");
        submitBtn.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 8 20; -fx-cursor: hand;");
        submitBtn.setOnAction(e -> {
            if (typeCombo.getValue() == null || datePicker.getValue() == null) {
                errorLabel.setText("⚠ Please fill in all required fields");
                return;
            }
            try {
                Connection conn = DatabaseConnection.getConnection();
                String id = "CMP-" + System.currentTimeMillis();
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO complaints (complaint_id, user_email, complainant_name, incident_type, location, incident_details, date_filed, status) VALUES (?,?,?,?,?,?,?,'Pending')");
                ps.setString(1, id);
                ps.setString(2, UserSession.getCurrentUserEmail());
                ps.setString(3, UserSession.getCurrentUserName());
                ps.setString(4, typeCombo.getValue());
                ps.setString(5, "");
                ps.setString(6, "");
                ps.setString(7, datePicker.getValue().toString());
                ps.executeUpdate();
                ps.close();
                conn.close();
                modal.close();
                loadComplaints("", "All", "All");
                loadSummary();
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Success");
                a.setHeaderText("Complaint Submitted");
                a.setContentText("Your complaint has been submitted successfully.\nComplaint ID: " + id);
                a.showAndWait();
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("⚠ Error: " + ex.getMessage());
            }
        });
        
        footer.getChildren().addAll(cancelBtn, submitBtn);
        root.getChildren().addAll(header, form, footer);
        
        Scene s = new Scene(root, 500, 480);
        modal.setScene(s);
        modal.setResizable(false);
        modal.showAndWait();
    }

    @FXML
    private void handleMouseEntered(javafx.scene.input.MouseEvent event) {
        javafx.scene.control.Button btn = (javafx.scene.control.Button) event.getSource();
        btn.setStyle("-fx-background-color: #f4f4f4; -fx-text-fill: #1a1a1a; -fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 11 16; -fx-alignment: CENTER_LEFT; -fx-cursor: hand;");
    }

    @FXML
    private void handleMouseExited(javafx.scene.input.MouseEvent event) {
        javafx.scene.control.Button btn = (javafx.scene.control.Button) event.getSource();
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555; -fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 11 16; -fx-alignment: CENTER_LEFT; -fx-cursor: hand;");
    }

    @FXML
    private void goToDashboard() {
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
    private void goToSettings() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("ResidentSettings.fxml"));
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
    private void goToMyProfile() {
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
                    goToMyDocuments();
                } else if (type.equals("complaint")) {
                    goToDashboard();
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