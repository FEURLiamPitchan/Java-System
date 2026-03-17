package com.mycompany.javasystem;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PaymentArchiveController {

    @FXML private VBox archiveTableBody;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterType;
    @FXML private Button logoutButton;

    @FXML
    public void initialize() {
        filterType.getItems().addAll("All", "Clearance", "Residency", "Indigency");
        filterType.setValue("All");
        loadArchive("", "All");
    }

    private void loadArchive(String search, String type) {
        archiveTableBody.getChildren().clear();

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM payments WHERE archived = True ORDER BY ID DESC");
            ResultSet rs = stmt.executeQuery();

            boolean hasData = false;

            while (rs.next()) {
                String refNumber = rs.getString("ref_number");
                String residentName = rs.getString("resident_name");
                String paymentType = rs.getString("payment_type");
                double amount = rs.getDouble("amount");
                String dateCreated = rs.getString("date_created");
                String payStatus = rs.getString("status");

                if (!search.isEmpty()) {
                    if (!residentName.toLowerCase().contains(search.toLowerCase()) &&
                        !refNumber.toLowerCase().contains(search.toLowerCase())) {
                        continue;
                    }
                }
                if (!type.equals("All") && !paymentType.equals(type)) continue;

                hasData = true;

                HBox row = new HBox();
                row.setStyle("-fx-padding: 14 0; -fx-border-color: #f8f8f8; -fx-border-width: 0 0 1 0;");

                Label refLabel = new Label(refNumber);
                refLabel.setPrefWidth(120);
                refLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

                Label nameLabel = new Label(residentName);
                nameLabel.setPrefWidth(200);
                nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333333;");

                Label typeLabel = new Label(paymentType);
                typeLabel.setPrefWidth(180);
                typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

                Label amountLabel = new Label(String.format("₱%.2f", amount));
                amountLabel.setPrefWidth(120);
                amountLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");

                Label dateLabel = new Label(dateCreated != null ? dateCreated : "N/A");
                dateLabel.setPrefWidth(140);
                dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

                Label statusLabel = new Label(payStatus);
                statusLabel.setStyle("-fx-background-color: #e8f5e9;" +
                        "-fx-text-fill: #4caf50;" +
                        "-fx-font-size: 11px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 4; -fx-padding: 3 8;");
                HBox statusBox = new HBox(statusLabel);
                statusBox.setPrefWidth(120);
                statusBox.setAlignment(Pos.CENTER_LEFT);

                row.getChildren().addAll(refLabel, nameLabel, typeLabel, amountLabel, dateLabel, statusBox);
                archiveTableBody.getChildren().add(row);
            }

            if (!hasData) {
                Label empty = new Label("No archived payments found.");
                empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #aaaaaa; -fx-padding: 20 0;");
                VBox.setMargin(empty, new Insets(20, 0, 20, 0));
                archiveTableBody.getChildren().add(empty);
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            Label error = new Label("Error loading archive: " + e.getMessage());
            error.setStyle("-fx-font-size: 12px; -fx-text-fill: #e53935;");
            archiveTableBody.getChildren().add(error);
        }
    }

    @FXML
    private void handleSearch() {
        loadArchive(searchField.getText().trim(), filterType.getValue());
    }

    @FXML
    private void handleFilter() {
        loadArchive(searchField.getText().trim(), filterType.getValue());
    }

    @FXML
    private void goToPayments() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Payments.fxml", true, getClass());
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
    private void goToArchive() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "PaymentArchive.fxml", true, getClass());
    }
    @FXML
    private void goToComplaints() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Complaints.fxml", true, getClass());
    }
    @FXML 
    private void goToAnnouncements() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Announcements.fxml", true, getClass());
    }
    @FXML private void goToFinances() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Finances.fxml", true, getClass());
    }
    @FXML
    private void handleLogout() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "login.fxml", false, getClass());
    }
}