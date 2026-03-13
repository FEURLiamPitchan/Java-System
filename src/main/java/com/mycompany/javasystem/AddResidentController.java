package com.mycompany.javasystem;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class AddResidentController {

    @FXML private TextField residentIdField;
    @FXML private TextField fullNameField;
    @FXML private TextField ageField;
    @FXML private TextField addressField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label messageLabel;

    private Runnable onSuccess;

    @FXML
    public void initialize() {
        statusCombo.getItems().addAll("Active", "Inactive");
        statusCombo.setValue("Active");
    }

    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    @FXML
    private void handleSubmit() {
        String residentId = residentIdField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String ageText = ageField.getText().trim();
        String address = addressField.getText().trim();
        String status = statusCombo.getValue();

        if (residentId.isEmpty() || fullName.isEmpty() || ageText.isEmpty() || address.isEmpty() || status == null) {
            showMessage("Please fill in all fields.", false);
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageText);
            if (age <= 0 || age > 120) {
                showMessage("Please enter a valid age.", false);
                return;
            }
        } catch (NumberFormatException e) {
            showMessage("Age must be a number.", false);
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "INSERT INTO residents (resident_id, full_name, age, address, status) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, residentId);
            stmt.setString(2, fullName);
            stmt.setInt(3, age);
            stmt.setString(4, address);
            stmt.setString(5, status);
            stmt.executeUpdate();
            stmt.close();
            conn.close();

            showMessage("Resident added successfully!", true);

            if (onSuccess != null) {
                onSuccess.run();
            }

            // Close modal after short delay
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(800));
            pause.setOnFinished(e -> handleClose());
            pause.play();

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Error: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) residentIdField.getScene().getWindow();
        stage.close();
    }

    private void showMessage(String message, boolean isSuccess) {
        messageLabel.setText(message);
        messageLabel.setStyle(isSuccess
            ? "-fx-text-fill: #4caf50; -fx-font-size: 11px;"
            : "-fx-text-fill: #e53935; -fx-font-size: 11px;");
    }
}