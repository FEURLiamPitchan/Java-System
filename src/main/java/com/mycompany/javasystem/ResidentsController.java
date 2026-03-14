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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ResidentsController {

    @FXML private VBox residentsTableBody;
    @FXML private TextField searchField;
    @FXML private Button logoutButton;

    @FXML
    public void initialize() {
        loadResidents("");
    }

    private void loadResidents(String search) {
        residentsTableBody.getChildren().clear();

        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql;
            PreparedStatement stmt;

            if (search.isEmpty()) {
                sql = "SELECT * FROM residents ORDER BY id";
                stmt = conn.prepareStatement(sql);
            } else {
                sql = "SELECT * FROM residents WHERE full_name LIKE ? OR address LIKE ? OR resident_id LIKE ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, "%" + search + "%");
                stmt.setString(2, "%" + search + "%");
                stmt.setString(3, "%" + search + "%");
            }

            ResultSet rs = stmt.executeQuery();
            boolean hasData = false;
            int rowNumber = 1;

            while (rs.next()) {
                hasData = true;
                String residentId = rs.getString("resident_id");
                String fullName = rs.getString("full_name");
                int age = rs.getInt("age");
                String address = rs.getString("address");
                String status = rs.getString("status");
                String dateAdded = rs.getString("date_added") != null
                        ? rs.getString("date_added") : "N/A";

                HBox row = new HBox();
                row.setStyle("-fx-padding: 12 0; -fx-border-color: #f8f8f8; -fx-border-width: 0 0 1 0;");

                Label idLabel = new Label(String.valueOf(rowNumber));
                idLabel.setPrefWidth(100);
                idLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

                Label nameLabel = new Label(fullName);
                nameLabel.setPrefWidth(200);
                nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333333;");

                Label ageLabel = new Label(String.valueOf(age));
                ageLabel.setPrefWidth(80);
                ageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

                Label addressLabel = new Label(address);
                addressLabel.setPrefWidth(250);
                addressLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

                String statusColor = status.equals("Active") ? "#e8f5e9" : "#fff8e1";
                String statusTextColor = status.equals("Active") ? "#4caf50" : "#f59e0b";
                Label statusLabel = new Label(status);
                statusLabel.setPrefWidth(100);
                statusLabel.setStyle("-fx-background-color: " + statusColor + ";" +
                        "-fx-text-fill: " + statusTextColor + ";" +
                        "-fx-font-size: 11px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 4; -fx-padding: 3 8;");

                Button viewBtn = new Button("View");
                viewBtn.setStyle("-fx-background-color: #f4f4f4;" +
                        "-fx-text-fill: #333333;" +
                        "-fx-font-size: 11px;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 5 12;" +
                        "-fx-cursor: hand;");

                final String fResidentId = residentId;
                final String fFullName = fullName;
                final int fAge = age;
                final String fAddress = address;
                final String fStatus = status;
                final String fDateAdded = dateAdded;

                viewBtn.setOnAction(e -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("ViewResidentModal.fxml"));
                        Parent modalRoot = loader.load();

                        ViewResidentController viewController = loader.getController();
                        viewController.setResident(fResidentId, fFullName, fAge, fAddress, fStatus, fDateAdded);
                        viewController.setOnDelete(() -> loadResidents(""));

                        Stage modalStage = new Stage();
                        modalStage.initModality(Modality.APPLICATION_MODAL);
                        modalStage.initOwner(logoutButton.getScene().getWindow());
                        modalStage.setTitle("View Resident");
                        modalStage.setScene(new Scene(modalRoot));
                        modalStage.setResizable(false);
                        modalStage.showAndWait();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                row.getChildren().addAll(idLabel, nameLabel, ageLabel, addressLabel, statusLabel, viewBtn);
                residentsTableBody.getChildren().add(row);
                rowNumber++;
            }

            if (!hasData) {
                Label empty = new Label("No residents found.");
                empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #aaaaaa; -fx-padding: 20 0;");
                VBox.setMargin(empty, new Insets(20, 0, 20, 0));
                residentsTableBody.getChildren().add(empty);
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            Label error = new Label("Error loading residents: " + e.getMessage());
            error.setStyle("-fx-font-size: 12px; -fx-text-fill: #e53935;");
            residentsTableBody.getChildren().add(error);
        }
    }

    @FXML
    private void handleSearch() {
        loadResidents(searchField.getText().trim());
    }

    @FXML
    private void handleAddResident() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddResidentModal.fxml"));
            Parent modalRoot = loader.load();

            AddResidentController modalController = loader.getController();
            modalController.setOnSuccess(() -> loadResidents(""));

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(logoutButton.getScene().getWindow());
            modalStage.setTitle("Add Resident");
            modalStage.setScene(new Scene(modalRoot));
            modalStage.setResizable(false);
            modalStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToDashboard() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "AdminDashboard.fxml", true, getClass());
    }

    @FXML
    private void handleLogout() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "login.fxml", false, getClass());
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
    private void goToComplaints() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Complaints.fxml", true, getClass());
    }
}