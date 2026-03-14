package com.mycompany.javasystem;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ViewComplaintController {

    @FXML private Label complaintIdLabel;
    @FXML private Label nameLabel;
    @FXML private Label typeLabel;
    @FXML private Label locationLabel;
    @FXML private Label dateLabel;
    @FXML private Label detailsLabel;
    @FXML private Label photoLabel;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextArea responseArea;

    private String complaintId;
    private Runnable onUpdate;

    @FXML
    public void initialize() {
        statusCombo.getItems().addAll("Pending", "Under Review", "Resolved");
    }

    public void setComplaint(String complaintId, String name, String type,
            String location, String date, String status, String details,
            String photoPath, String adminResponse) {
        this.complaintId = complaintId;
        complaintIdLabel.setText("Complaint ID: " + complaintId);
        nameLabel.setText(name);
        typeLabel.setText(type);
        locationLabel.setText(location);
        dateLabel.setText(date != null ? date : "N/A");
        detailsLabel.setText(details != null ? details : "No details provided.");
        photoLabel.setText(photoPath != null && !photoPath.isEmpty()
                ? photoPath : "No photo attached.");
        statusCombo.setValue(status);
        responseArea.setText(adminResponse != null ? adminResponse : "");
    }

    public void setOnUpdate(Runnable callback) {
        this.onUpdate = callback;
    }

    @FXML
    private void handleSave() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "UPDATE complaints SET status = ?, admin_response = ?, is_read = True WHERE complaint_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, statusCombo.getValue());
            stmt.setString(2, responseArea.getText().trim());
            stmt.setString(3, complaintId);
            stmt.executeUpdate();
            stmt.close();
            conn.close();

            if (onUpdate != null) onUpdate.run();
            handleClose();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) complaintIdLabel.getScene().getWindow();
        stage.close();
    }
}