package com.mycompany.javasystem;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ViewResidentController {

    @FXML private Label residentIdLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label ageLabel;
    @FXML private Label statusLabel;
    @FXML private Label addressLabel;
    @FXML private Label dateAddedLabel;

    private String residentId;
    private Runnable onDelete;

    public void setResident(String residentId, String fullName, int age,
                            String address, String status, String dateAdded) {
        this.residentId = residentId;
        residentIdLabel.setText(residentId);
        fullNameLabel.setText(fullName);
        ageLabel.setText(String.valueOf(age));
        addressLabel.setText(address);
        dateAddedLabel.setText(dateAdded != null ? dateAdded : "N/A");

        if (status.equals("Active")) {
            statusLabel.setText("Active");
            statusLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #4caf50;");
        } else {
            statusLabel.setText("Inactive");
            statusLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #f59e0b;");
        }
    }

    public void setOnDelete(Runnable callback) {
        this.onDelete = callback;
    }

    @FXML
    private void handleDelete() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Resident");
        confirm.setHeaderText("Are you sure you want to delete this resident?");
        confirm.setContentText("This action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    Connection conn = DatabaseConnection.getConnection();
                    String sql = "DELETE FROM residents WHERE resident_id = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, residentId);
                    stmt.executeUpdate();
                    stmt.close();
                    conn.close();

                    if (onDelete != null) {
                        onDelete.run();
                    }
                    handleClose();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) residentIdLabel.getScene().getWindow();
        stage.close();
    }
}