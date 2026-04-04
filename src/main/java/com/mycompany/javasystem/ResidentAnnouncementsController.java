package com.mycompany.javasystem;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

    @FXML
    public void initialize() {
        filterPriority.getItems().addAll("All", "Emergency", "Urgent", "Normal", "Low");
        filterPriority.setValue("All");
        filterCategory.getItems().addAll("All", "Health", "Safety & Security",
                "Environment", "Events", "Government Services", "Other");
        filterCategory.setValue("All");
        loadAnnouncements();
        loadSummary();
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
}
