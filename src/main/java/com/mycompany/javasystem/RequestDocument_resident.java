package com.mycompany.javasystem;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.collections.FXCollections;

public class RequestDocument_resident {
    @FXML
    private Button logoutButton;
    @FXML
    private ComboBox<String> documentTypeCombo;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField contactField;
    @FXML
    private TextField purposeField;
    @FXML
    private TextArea notesArea;
    @FXML
    private Label errorLabel;

    @FXML
    public void initialize() {
        documentTypeCombo.setItems(FXCollections.observableArrayList(
            "Barangay Clearance",
            "Certificate of Residency",
            "Certificate of Indigency",
            "Business Permit",
            "Barangay ID",
            "Community Tax Certificate",
            "Certificate of Good Moral"
        ));
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

    @FXML
    private void handleSubmit() {
        errorLabel.setText("");

        if (documentTypeCombo.getValue() == null || documentTypeCombo.getValue().isEmpty()) {
            showError("Please select a document type");
            return;
        }
        if (fullNameField.getText().trim().isEmpty()) {
            showError("Please enter your full name");
            return;
        }
        if (addressField.getText().trim().isEmpty()) {
            showError("Please enter your complete address");
            return;
        }
        if (contactField.getText().trim().isEmpty()) {
            showError("Please enter your contact number");
            return;
        }
        if (purposeField.getText().trim().isEmpty()) {
            showError("Please enter the purpose of your request");
            return;
        }

        String contact = contactField.getText().trim();
        if (!contact.matches("\\d{10,11}")) {
            showError("Please enter a valid contact number (10-11 digits)");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Request Submitted");
        alert.setHeaderText("Document Request Successful!");
        alert.setContentText("Your request for " + documentTypeCombo.getValue() + 
                           " has been submitted successfully.\n\n" +
                           "Request ID: #BR-2024-" + String.format("%03d", (int)(Math.random() * 1000)) + "\n" +
                           "Status: Pending\n\n" +
                           "You will be notified via SMS when your document is ready for pickup.");
        alert.showAndWait();

        clearForm();
    }

    @FXML
    private void goBackToDashboard() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "ResidentDashboard_resident.fxml", true, getClass());
    }

    @FXML
    private void goToMyDocuments() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "MyDocuments_resident.fxml", true, getClass());
    }

    @FXML
    private void handleLogout() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "login.fxml", false, getClass());
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    private void clearForm() {
        documentTypeCombo.setValue(null);
        fullNameField.clear();
        addressField.clear();
        contactField.clear();
        purposeField.clear();
        notesArea.clear();
        errorLabel.setText("");
    }
}
