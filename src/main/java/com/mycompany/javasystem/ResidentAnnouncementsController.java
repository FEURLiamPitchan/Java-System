package com.mycompany.javasystem;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.sql.Connection;
import java.sql.ResultSet;

public class ResidentAnnouncementsController {

    @FXML private FlowPane announcementsPane;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterPriority;
    @FXML private ComboBox<String> filterCategory;
    @FXML private Label totalLabel;
    @FXML private Label emergencyLabel;
    @FXML private Label urgentLabel;
    @FXML private Label normalLabel;
    @FXML private Button logoutButton;
    @FXML private Label residentNameLabel;
    @FXML private Button alertsButton;
    @FXML private Label alertBadge;
    @FXML private javafx.scene.shape.Circle topBarProfileCircle;
    @FXML private Label topBarProfileInitials;

    @FXML
    public void initialize() {
        loadUserProfile();
        filterPriority.getItems().addAll("All", "Emergency", "Urgent", "Normal", "Low");
        filterPriority.setValue("All");
        filterCategory.getItems().addAll("All", "Health", "Safety & Security",
                "Environment", "Events", "Government Services", "Other");
        filterCategory.setValue("All");
        loadAnnouncements();
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
            
            java.sql.PreparedStatement stmt = conn.prepareStatement(
                "SELECT full_name FROM users WHERE email = ?");
            stmt.setString(1, currentUserEmail);
            java.sql.ResultSet rs = stmt.executeQuery();
            
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
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                // Demo data when no database available
                totalLabel.setText("6");
                emergencyLabel.setText("1");
                urgentLabel.setText("2");
                normalLabel.setText("3");
                return;
            }
            
            ResultSet rs = conn.prepareStatement(
                "SELECT priority, COUNT(*) as cnt FROM announcements GROUP BY priority"
            ).executeQuery();
            int total = 0, emergency = 0, urgent = 0, normal = 0;
            while (rs.next()) {
                int cnt = rs.getInt("cnt");
                total += cnt;
                switch (rs.getString("priority")) {
                    case "Emergency": emergency = cnt; break;
                    case "Urgent": urgent = cnt; break;
                    case "Normal": case "Low": normal += cnt; break;
                }
            }
            totalLabel.setText(String.valueOf(total));
            emergencyLabel.setText(String.valueOf(emergency));
            urgentLabel.setText(String.valueOf(urgent));
            normalLabel.setText(String.valueOf(normal));
            rs.close();
            conn.close();
        } catch (Exception e) {
            // Fallback to demo data
            totalLabel.setText("6");
            emergencyLabel.setText("1");
            urgentLabel.setText("2");
            normalLabel.setText("3");
        }
    }

    private void loadAnnouncements() {
        announcementsPane.getChildren().clear();
        String search = searchField.getText() != null ? searchField.getText().trim() : "";
        String priority = filterPriority.getValue() != null ? filterPriority.getValue() : "All";
        String category = filterCategory.getValue() != null ? filterCategory.getValue() : "All";

        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                addSampleAnnouncements();
                return;
            }
            
            ResultSet rs = conn.prepareStatement(
                "SELECT * FROM announcements ORDER BY id DESC").executeQuery();

            boolean hasData = false;

            while (rs.next()) {
                String title = rs.getString("title");
                String content = rs.getString("content");
                String prio = rs.getString("priority");
                String cat = rs.getString("category");
                String postedBy = rs.getString("posted_by");
                String datePosted = rs.getString("date_posted");

                if (!search.isEmpty() &&
                    !title.toLowerCase().contains(search.toLowerCase()) &&
                    !content.toLowerCase().contains(search.toLowerCase())) continue;
                if (!priority.equals("All") && !prio.equals(priority)) continue;
                if (!category.equals("All") && !cat.equals(category)) continue;

                hasData = true;
                announcementsPane.getChildren().add(
                    buildCard(title, content, prio, cat, postedBy, datePosted));
            }

            if (!hasData) {
                Label empty = new Label("No announcements found.");
                empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #aaaaaa; -fx-padding: 20;");
                announcementsPane.getChildren().add(empty);
            }

            rs.close();
            conn.close();
        } catch (Exception e) {
            addSampleAnnouncements();
        }
    }
    
    private void addSampleAnnouncements() {
        announcementsPane.getChildren().addAll(
            buildCard("Community Clean-Up Drive", 
                "Join us this Saturday, June 22nd, for our monthly community clean-up drive. Meeting point at the barangay hall at 6:00 AM. Bring your own gloves and cleaning materials.", 
                "Normal", "Events", "Barangay Captain", "2024-06-15"),
            buildCard("Water Service Interruption", 
                "Water service will be temporarily interrupted on June 20th from 8:00 AM to 5:00 PM due to pipeline maintenance. Please store water in advance.", 
                "Urgent", "Government Services", "Public Works Office", "2024-06-14"),
            buildCard("Health Center Vaccination Schedule", 
                "Free vaccination for children (0-5 years old) will be available at the barangay health center every Tuesday and Thursday from 8:00 AM to 12:00 PM.", 
                "Normal", "Health", "Health Officer", "2024-06-13"),
            buildCard("Emergency: Flash Flood Warning", 
                "URGENT: Heavy rains expected tonight. Residents in low-lying areas are advised to evacuate to the evacuation center at the elementary school.", 
                "Emergency", "Safety & Security", "Disaster Risk Office", "2024-06-12"),
            buildCard("Barangay Assembly Meeting", 
                "Monthly barangay assembly meeting on June 25th at 7:00 PM at the community center. All residents are encouraged to attend.", 
                "Normal", "Government Services", "Barangay Secretary", "2024-06-11"),
            buildCard("Garbage Collection Schedule Change", 
                "Starting June 18th, garbage collection will be moved from Tuesday to Wednesday due to the new truck schedule.", 
                "Urgent", "Environment", "Sanitation Office", "2024-06-10")
        );
    }

    private VBox buildCard(String title, String content, String priority,
            String category, String postedBy, String datePosted) {

        String prioBg, prioFg, borderColor;
        switch (priority) {
            case "Emergency":
                prioBg = "#fdecea"; prioFg = "#e53935"; borderColor = "#ffcdd2"; break;
            case "Urgent":
                prioBg = "#fff8e1"; prioFg = "#f59e0b"; borderColor = "#ffe082"; break;
            case "Normal":
                prioBg = "#e3f2fd"; prioFg = "#1e88e5"; borderColor = "#bbdefb"; break;
            default:
                prioBg = "#e8f5e9"; prioFg = "#4caf50"; borderColor = "#c8e6c9"; break;
        }

        VBox card = new VBox(10);
        card.setPrefWidth(320);
        card.setStyle("-fx-background-color: #ffffff;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: " + borderColor + ";" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 10;" +
                "-fx-padding: 18;");

        HBox topRow = new HBox(8);
        topRow.setStyle("-fx-alignment: CENTER_LEFT;");

        Label prioBadge = new Label(priority);
        prioBadge.setStyle("-fx-background-color: " + prioBg + ";" +
                "-fx-text-fill: " + prioFg + ";" +
                "-fx-font-size: 10px; -fx-font-weight: bold;" +
                "-fx-background-radius: 4; -fx-padding: 3 8;");

        Label catBadge = new Label(category);
        catBadge.setStyle("-fx-background-color: #f4f4f4;" +
                "-fx-text-fill: #777777;" +
                "-fx-font-size: 10px;" +
                "-fx-background-radius: 4; -fx-padding: 3 8;");

        topRow.getChildren().addAll(prioBadge, catBadge);

        Label titleLabel = new Label(title);
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");

        String preview = content != null && content.length() > 150
                ? content.substring(0, 150) + "..." : content;
        Label contentLabel = new Label(preview);
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

        VBox footer = new VBox(2);
        footer.setStyle("-fx-alignment: CENTER_LEFT;");

        Label postedByLabel = new Label("Posted by: " + (postedBy != null ? postedBy : "N/A"));
        postedByLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaaaaa;");

        Label dateLabel = new Label(datePosted != null ? datePosted : "N/A");
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaaaaa;");

        footer.getChildren().addAll(postedByLabel, dateLabel);
        card.getChildren().addAll(topRow, titleLabel, contentLabel, footer);

        return card;
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

    @FXML private void handleSearch() { loadAnnouncements(); }
    @FXML private void handleFilter() { loadAnnouncements(); }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        filterPriority.setValue("All");
        filterCategory.setValue("All");
        loadAnnouncements();
    }

    private void switchScene(String fxml, boolean maximize) {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Parent currentRoot = stage.getScene().getRoot();

            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentRoot);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                try {
                    Parent newRoot = FXMLLoader.load(getClass().getResource(fxml));
                    newRoot.setOpacity(0.0);
                    stage.setMaximized(maximize);
                    stage.getScene().setRoot(newRoot);

                    FadeTransition fadeIn = new FadeTransition(Duration.millis(200), newRoot);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            fadeOut.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        switchScene("login.fxml", false);
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
    private void goToProfile() {
        goToMyProfile();
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
            java.sql.Connection conn = DatabaseConnection.getConnection();
            if (conn == null) return;
            
            String sql = "SELECT notif_id, type, message, reference_id, is_read, created_at FROM notifications WHERE user_email = ?";
            if (!showAll) sql += " AND is_read = 'false'";
            sql += " ORDER BY notif_id DESC";
            
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, UserSession.getCurrentUserEmail());
            java.sql.ResultSet rs = stmt.executeQuery();
            
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
}
