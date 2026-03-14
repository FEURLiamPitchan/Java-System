package com.mycompany.javasystem;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AddResidentController {

    @FXML private TextField residentIdField;
    @FXML private TextField fullNameField;
    @FXML private TextField ageField;
    @FXML private TextField addressField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label messageLabel;
    @FXML private Label suggestedIdLabel;

    private Runnable onSuccess;

    @FXML
    public void initialize() {
        statusCombo.getItems().addAll("Active", "Inactive");
        statusCombo.setValue("Active");

        // Load suggested ID FIRST before adding the listener
        loadNextSuggestedId();

        // Force RES- prefix and numbers only AFTER setting the field
        residentIdField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.startsWith("RES-")) {
                residentIdField.setText("RES-");
                return;
            }
            String afterPrefix = newVal.substring(4);
            if (!afterPrefix.matches("\\d*")) {
                residentIdField.setText(oldVal);
            }
        });

        // Prevent cursor from going before RES-
        residentIdField.caretPositionProperty().addListener((obs, oldPos, newPos) -> {
            if (newPos.intValue() < 4) {
                residentIdField.positionCaret(4);
            }
        });

        // Allow numbers only in age field, max 3 digits
        ageField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                ageField.setText(oldVal);
            } else if (newVal.length() > 3) {
                ageField.setText(oldVal);
            }
        });
    }

    private void loadNextSuggestedId() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT resident_id FROM residents ORDER BY id";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            // Collect all existing numbers
            java.util.Set<Integer> existingNumbers = new java.util.HashSet<>();
            while (rs.next()) {
                String id = rs.getString("resident_id");
                if (id != null && id.startsWith("RES-")) {
                    try {
                        int num = Integer.parseInt(id.substring(4));
                        existingNumbers.add(num);
                    } catch (NumberFormatException ignored) {}
                }
            }

            rs.close();
            stmt.close();
            conn.close();

            // Find next number that doesn't exist
            int nextNumber = 1;
            while (existingNumbers.contains(nextNumber)) {
                nextNumber++;
            }

            String suggested = String.format("RES-%03d", nextNumber);
            residentIdField.setText(suggested);
            suggestedIdLabel.setText("Available Resident ID: " + suggested);
            suggestedIdLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #4caf50;");

        } catch (Exception e) {
            e.printStackTrace();
            suggestedIdLabel.setText("Could not load suggested ID.");
            suggestedIdLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #aaaaaa;");
            residentIdField.setText("RES-");
        }
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

        if (residentId.isEmpty() || residentId.equals("RES-") || fullName.isEmpty()
                || ageText.isEmpty() || address.isEmpty() || status == null) {
            showMessage("Please fill in all fields.", false);
            return;
        }

        if (!residentId.matches("RES-\\d+")) {
            showMessage("Resident ID must follow the format RES-001.", false);
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageText);
            if (age <= 0 || age > 130) {
                showMessage("Please enter a valid age (1-130).", false);
                return;
            }
        } catch (NumberFormatException e) {
            showMessage("Age must be a number.", false);
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();

            // Check if resident_id already exists
            String checkSql = "SELECT COUNT(*) FROM residents WHERE resident_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, residentId);
            ResultSet checkRs = checkStmt.executeQuery();
            checkRs.next();
            int count = checkRs.getInt(1);
            checkRs.close();
            checkStmt.close();

            if (count > 0) {
                showMessage("Resident ID already exists. Please use a different ID.", false);
                conn.close();
                return;
            }

            String dateAdded = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            String sql = "INSERT INTO residents (resident_id, full_name, age, address, status, date_added) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, residentId);
            stmt.setString(2, fullName);
            stmt.setInt(3, age);
            stmt.setString(4, address);
            stmt.setString(5, status);
            stmt.setString(6, dateAdded);
            stmt.executeUpdate();
            stmt.close();
            conn.close();

            showMessage("Resident added successfully!", true);

            if (onSuccess != null) {
                onSuccess.run();
            }

            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                    javafx.util.Duration.millis(800));
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