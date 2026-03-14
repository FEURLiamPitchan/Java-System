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

public class ComplaintsController {

    @FXML private VBox complaintsTableBody;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatus;
    @FXML private ComboBox<String> filterType;
    @FXML private Button logoutButton;
    @FXML private Button alertsButton;
    @FXML private Label alertBadge;
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
        updateAlertBadge();
    }

    private void updateAlertBadge() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.prepareStatement(
                "SELECT COUNT(*) FROM complaints WHERE is_read = False").executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                if (count > 0) {
                    alertBadge.setText(String.valueOf(count));
                    alertBadge.setVisible(true);
                } else {
                    alertBadge.setVisible(false);
                }
            }
            rs.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAlerts() {
        // Mark all as read
        try {
            Connection conn = DatabaseConnection.getConnection();
            conn.prepareStatement("UPDATE complaints SET is_read = True").executeUpdate();
            conn.close();
            alertBadge.setVisible(false);
            loadComplaints(searchField.getText().trim(), filterStatus.getValue(), filterType.getValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSummary() {
        try {
            Connection conn = DatabaseConnection.getConnection();

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
            e.printStackTrace();
        }
    }

    private void loadComplaints(String search, String status, String type) {
        complaintsTableBody.getChildren().clear();

        try {
            Connection conn = DatabaseConnection.getConnection();
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
                boolean isRead = rs.getBoolean("is_read");

                if (!search.isEmpty()) {
                    if (!name.toLowerCase().contains(search.toLowerCase()) &&
                        !complaintId.toLowerCase().contains(search.toLowerCase())) {
                        continue;
                    }
                }
                if (!status.equals("All") && !complaintStatus.equals(status)) continue;
                if (!type.equals("All") && !incidentType.equals(type)) continue;

                hasData = true;

                HBox row = new HBox();
                String rowBg = !isRead ? "#fffde7" : "transparent";
                row.setStyle("-fx-padding: 14 0; -fx-border-color: #f8f8f8;" +
                        "-fx-border-width: 0 0 1 0; -fx-background-color: " + rowBg + ";");

                Label idLabel = new Label(complaintId);
                idLabel.setPrefWidth(100);
                idLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

                Label nameLabel = new Label(name);
                nameLabel.setPrefWidth(180);
                nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333333;");

                Label typeLabel = new Label(incidentType);
                typeLabel.setPrefWidth(160);
                typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

                Label locationLabel = new Label(location);
                locationLabel.setPrefWidth(160);
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
                actionBox.setPrefWidth(150);
                actionBox.setAlignment(Pos.CENTER_LEFT);

                final String fComplaintId = complaintId;
                final String fName = name;
                final String fType = incidentType;
                final String fLocation = location;
                final String fDate = dateFiled;
                final String fStatus = complaintStatus;
                final String fDetails = details;
                final String fPhoto = photoPath;
                final String fResponse = adminResponse;

                Button viewBtn = new Button("View");
                viewBtn.setStyle("-fx-background-color: #2d2d2d;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-font-size: 11px;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 5 10;" +
                        "-fx-cursor: hand;");
                viewBtn.setOnAction(e -> openComplaintModal(fComplaintId, fName, fType,
                        fLocation, fDate, fStatus, fDetails, fPhoto, fResponse));

                Button deleteBtn = new Button("Delete");
                deleteBtn.setStyle("-fx-background-color: #fff0f0;" +
                        "-fx-text-fill: #e53935;" +
                        "-fx-font-size: 11px;" +
                        "-fx-background-radius: 6;" +
                        "-fx-border-color: #ffcdd2;" +
                        "-fx-border-width: 1;" +
                        "-fx-padding: 5 10;" +
                        "-fx-cursor: hand;");
                deleteBtn.setOnAction(e -> deleteComplaint(fComplaintId));

                actionBox.getChildren().addAll(viewBtn, deleteBtn);
                row.getChildren().addAll(idLabel, nameLabel, typeLabel, locationLabel,
                        dateLabel, statusBox, actionBox);
                complaintsTableBody.getChildren().add(row);
            }

            if (!hasData) {
                Label empty = new Label("No complaints found.");
                empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #aaaaaa; -fx-padding: 20 0;");
                VBox.setMargin(empty, new Insets(20, 0, 20, 0));
                complaintsTableBody.getChildren().add(empty);
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            Label error = new Label("Error loading complaints: " + e.getMessage());
            error.setStyle("-fx-font-size: 12px; -fx-text-fill: #e53935;");
            complaintsTableBody.getChildren().add(error);
        }
    }

    private void openComplaintModal(String complaintId, String name, String type,
            String location, String date, String status, String details,
            String photoPath, String adminResponse) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ViewComplaintModal.fxml"));
            Parent modalRoot = loader.load();

            ViewComplaintController ctrl = loader.getController();
            ctrl.setComplaint(complaintId, name, type, location, date,
                    status, details, photoPath, adminResponse);
            ctrl.setOnUpdate(() -> {
                loadComplaints(searchField.getText().trim(),
                        filterStatus.getValue(), filterType.getValue());
                loadSummary();
                updateAlertBadge();
            });

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(logoutButton.getScene().getWindow());
            modalStage.setTitle("View Complaint");
            modalStage.setScene(new Scene(modalRoot));
            modalStage.setResizable(false);
            modalStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteComplaint(String complaintId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Complaint");
        confirm.setHeaderText("Delete complaint " + complaintId + "?");
        confirm.setContentText("This action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    Connection conn = DatabaseConnection.getConnection();
                    String sql = "DELETE FROM complaints WHERE complaint_id = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, complaintId);
                    stmt.executeUpdate();
                    stmt.close();
                    conn.close();
                    loadComplaints(searchField.getText().trim(),
                            filterStatus.getValue(), filterType.getValue());
                    loadSummary();
                    updateAlertBadge();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
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
    private void goToDashboard() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "AdminDashboard.fxml", true, getClass());
    }

    @FXML
    private void goToResidents() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Residents.fxml", true, getClass());
    }

    @FXML
    private void goToDocuments() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Documents.fxml", true, getClass());
    }

    @FXML
    private void goToPayments() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Payments.fxml", true, getClass());
    }

    @FXML
    private void goToArchive() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "PaymentArchive.fxml", true, getClass());
    }

    @FXML
    private void goToAnnouncements() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Announcements.fxml", true, getClass());
    }

    @FXML
    private void handleLogout() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "login.fxml", false, getClass());
    }
}