package com.mycompany.javasystem;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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
    @FXML private Label totalLabel;
    @FXML private Label pendingLabel;
    @FXML private Label underReviewLabel;
    @FXML private Label resolvedLabel;

    @FXML
    public void initialize() {
        filterStatus.getItems().addAll("All", "Pending", "Under Review", "Resolved");
        filterStatus.setValue("All");
        filterType.getItems().addAll("All", "Noise Complaint", "Property Dispute",
                "Public Disturbance", "Infrastructure Issue", "Other");
        filterType.setValue("All");
        loadComplaints("", "All", "All");
        loadSummary();
    }

    private void loadSummary() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                // Demo data for residents
                totalLabel.setText("12");
                pendingLabel.setText("2");
                underReviewLabel.setText("3");
                resolvedLabel.setText("7");
                return;
            }

            ResultSet rs1 = conn.prepareStatement(
                "SELECT COUNT(*) FROM complaints").executeQuery();
            if (rs1.next()) totalLabel.setText(String.valueOf(rs1.getInt(1)));

            ResultSet rs2 = conn.prepareStatement(
                "SELECT COUNT(*) FROM complaints WHERE status = 'Pending'").executeQuery();
            if (rs2.next()) pendingLabel.setText(String.valueOf(rs2.getInt(1)));

            ResultSet rs3 = conn.prepareStatement(
                "SELECT COUNT(*) FROM complaints WHERE status = 'Under Review'").executeQuery();
            if (rs3.next()) underReviewLabel.setText(String.valueOf(rs3.getInt(1)));

            ResultSet rs4 = conn.prepareStatement(
                "SELECT COUNT(*) FROM complaints WHERE status = 'Resolved'").executeQuery();
            if (rs4.next()) resolvedLabel.setText(String.valueOf(rs4.getInt(1)));

            conn.close();
        } catch (Exception e) {
            // Demo data fallback
            totalLabel.setText("12");
            pendingLabel.setText("2");
            underReviewLabel.setText("3");
            resolvedLabel.setText("7");
        }
    }

    private void loadComplaints(String search, String status, String type) {
        complaintsTableBody.getChildren().clear();

        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                addSampleComplaints();
                return;
            }

            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM complaints ORDER BY id DESC");
            ResultSet rs = stmt.executeQuery();

            boolean hasData = false;

            while (rs.next()) {
                String complaintId = rs.getString("complaint_id");
                String name = rs.getString("complainant_name");
                String incidentType = rs.getString("incident_type");
                String location = rs.getString("location");
                String dateFiled = rs.getString("date_filed");
                String complaintStatus = rs.getString("status");
                String details = rs.getString("incident_details");
                String photoPath = rs.getString("photo_path");
                String adminResponse = rs.getString("admin_response");

                if (!search.isEmpty()) {
                    if (!name.toLowerCase().contains(search.toLowerCase()) &&
                        !complaintId.toLowerCase().contains(search.toLowerCase())) {
                        continue;
                    }
                }
                if (!status.equals("All") && !complaintStatus.equals(status)) continue;
                if (!type.equals("All") && !incidentType.equals(type)) continue;

                hasData = true;
                complaintsTableBody.getChildren().add(createComplaintRow(
                    complaintId, name, incidentType, location, dateFiled, 
                    complaintStatus, details, photoPath, adminResponse));
            }

            if (!hasData) {
                addSampleComplaints();
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            addSampleComplaints();
        }
    }

    private void addSampleComplaints() {
        complaintsTableBody.getChildren().addAll(
            createComplaintRow("CMP-2024-001", "Maria Santos", "Noise Complaint", 
                "Block 1, Lot 15", "2024-06-13", "Under Review", 
                "Loud music from neighbor until 2 AM", null, "We are investigating this matter."),
            createComplaintRow("CMP-2024-002", "Juan Dela Cruz", "Infrastructure Issue", 
                "Main Street", "2024-06-12", "Resolved", 
                "Broken streetlight causing safety concerns", null, "Streetlight has been repaired."),
            createComplaintRow("CMP-2024-003", "Ana Rodriguez", "Property Dispute", 
                "Block 2, Lot 8", "2024-06-10", "Pending", 
                "Boundary dispute with neighbor", null, null),
            createComplaintRow("CMP-2024-004", "Pedro Garcia", "Public Disturbance", 
                "Community Center", "2024-06-08", "Resolved", 
                "Unauthorized gathering causing disturbance", null, "Issue has been resolved with the parties involved.")
        );
    }

    private HBox createComplaintRow(String complaintId, String name, String incidentType,
            String location, String dateFiled, String complaintStatus, String details,
            String photoPath, String adminResponse) {

        HBox row = new HBox();
        row.setStyle("-fx-padding: 14 0; -fx-border-color: #f8f8f8; -fx-border-width: 0 0 1 0;");

        Label idLabel = new Label(complaintId);
        idLabel.setPrefWidth(120);
        idLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2196f3; -fx-font-weight: bold;");

        Label nameLabel = new Label(name);
        nameLabel.setPrefWidth(160);
        nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Label typeLabel = new Label(incidentType);
        typeLabel.setPrefWidth(160);
        typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

        Label locationLabel = new Label(location);
        locationLabel.setPrefWidth(140);
        locationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

        Label dateLabel = new Label(dateFiled != null ? dateFiled : "N/A");
        dateLabel.setPrefWidth(120);
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
        statusBox.setPrefWidth(120);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        HBox actionBox = new HBox(6);
        actionBox.setPrefWidth(120);
        actionBox.setAlignment(Pos.CENTER_LEFT);

        Button viewBtn = new Button("View");
        viewBtn.setStyle("-fx-background-color: #2d2d2d;" +
                "-fx-text-fill: #ffffff;" +
                "-fx-font-size: 11px;" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 5 10;" +
                "-fx-cursor: hand;");
        viewBtn.setOnAction(e -> openComplaintModal(complaintId, name, incidentType,
                location, dateFiled, complaintStatus, details, photoPath, adminResponse));

        actionBox.getChildren().add(viewBtn);
        row.getChildren().addAll(idLabel, nameLabel, typeLabel, locationLabel,
                dateLabel, statusBox, actionBox);
        return row;
    }

    private void openComplaintModal(String complaintId, String name, String type,
            String location, String date, String status, String details,
            String photoPath, String adminResponse) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ViewComplaint_ResidentModal.fxml"));
            Parent modalRoot = loader.load();

            ViewComplaint_ResidentController ctrl = loader.getController();
            ctrl.setComplaint(complaintId, name, type, location, date,
                    status, details, photoPath, adminResponse);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(logoutButton.getScene().getWindow());
            modalStage.setTitle("View Complaint Details");
            modalStage.setScene(new Scene(modalRoot));
            modalStage.setResizable(false);
            modalStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
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
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("Payments.fxml"));
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
}