package com.mycompany.javasystem;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.print.PrinterJob;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RequestDocumentController {
    @FXML
    private Button logoutButton;
    @FXML
    private ComboBox<String> documentTypeCombo;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField addressField;
    @FXML
    private DatePicker birthDatePicker;
    @FXML
    private ComboBox<String> civilStatusCombo;
    @FXML
    private TextField purposeField;
    @FXML
    private TextField yearsOfResidencyField;
    @FXML
    private Label errorLabel;

    @FXML
    public void initialize() {
        // Populate document types
        documentTypeCombo.setItems(FXCollections.observableArrayList(
            "Barangay Clearance",
            "Certificate of Residency",
            "Certificate of Indigency"
        ));
        
        // Populate civil status options
        civilStatusCombo.setItems(FXCollections.observableArrayList(
            "Single",
            "Married",
            "Widowed",
            "Separated",
            "Divorced"
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
    private void handleGenerateDocument() {
        errorLabel.setText("");

        // Validate fields
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
        if (birthDatePicker.getValue() == null) {
            showError("Please select your birthdate");
            return;
        }
        if (civilStatusCombo.getValue() == null || civilStatusCombo.getValue().isEmpty()) {
            showError("Please select your civil status");
            return;
        }
        if (purposeField.getText().trim().isEmpty()) {
            showError("Please enter the purpose of your request");
            return;
        }
        if (yearsOfResidencyField.getText().trim().isEmpty()) {
            showError("Please enter your years of residency");
            return;
        }

        // Validate years of residency is a number
        try {
            int years = Integer.parseInt(yearsOfResidencyField.getText().trim());
            if (years < 0) {
                showError("Years of residency must be a positive number");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Years of residency must be a valid number");
            return;
        }

        // Generate request ID
        String requestId = generateRequestId();
        
        // Format birthdate to ISO format (yyyy-MM-dd) for database compatibility
        String birthdate = birthDatePicker.getValue().toString(); // This gives yyyy-MM-dd format
        
        // Save to database
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                System.out.println("Saving document request to database...");
                System.out.println("Request ID: " + requestId);
                System.out.println("User Email: " + UserSession.getCurrentUserEmail());
                
                // Insert using column order matching Access table: request_id, birthdate, document_type, full_name, address, civil_status, purpose, years_of_residency, status, date_requested
                PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO document_requests (request_id, birthdate, document_type, full_name, address, civil_status, purpose, years_of_residency, status, date_requested) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                stmt.setString(1, requestId);
                stmt.setString(2, birthdate);
                stmt.setString(3, documentTypeCombo.getValue());
                stmt.setString(4, fullNameField.getText().trim());
                stmt.setString(5, addressField.getText().trim());
                stmt.setString(6, civilStatusCombo.getValue());
                stmt.setString(7, purposeField.getText().trim());
                stmt.setString(8, yearsOfResidencyField.getText().trim());
                stmt.setString(9, "Ready for Printing");
                stmt.setString(10, LocalDate.now().toString());
                
                int rowsAffected = stmt.executeUpdate();
                System.out.println("Document saved successfully. Rows affected: " + rowsAffected);
                
                stmt.close();
                conn.close();
            } else {
                System.out.println("Database connection is null, document not saved");
            }
        } catch (Exception e) {
            System.out.println("Error saving document request: " + e.getMessage());
            e.printStackTrace();
        }

        // Show success message with print option
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Document Generated");
        alert.setHeaderText("Document Ready for Printing!");
        alert.setContentText("Your " + documentTypeCombo.getValue() + 
                           " has been generated successfully.\n\n" +
                           "Request ID: " + requestId + "\n" +
                           "Status: Ready for Printing\n\n" +
                           "Would you like to print this document now?");
        
        ButtonType printButton = new ButtonType("Print Now");
        ButtonType laterButton = new ButtonType("Print Later", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(printButton, laterButton);
        
        alert.showAndWait().ifPresent(response -> {
            if (response == printButton) {
                printDocument(requestId, documentTypeCombo.getValue(), fullNameField.getText().trim(), 
                            addressField.getText().trim(), birthdate, civilStatusCombo.getValue(), 
                            purposeField.getText().trim(), yearsOfResidencyField.getText().trim());
            }
        });

        // Clear form
        clearForm();
    }
    
    private String generateRequestId() {
        String prefix = "";
        String docType = documentTypeCombo.getValue();
        
        if (docType.equals("Barangay Clearance")) {
            prefix = "BC";
        } else if (docType.equals("Certificate of Residency")) {
            prefix = "CR";
        } else if (docType.equals("Certificate of Indigency")) {
            prefix = "CI";
        }
        
        // Get current year
        int year = LocalDate.now().getYear();
        
        // Generate random number
        int random = (int)(Math.random() * 9000) + 1000;
        
        return prefix + "-" + year + "-" + random;
    }
    
    private void printDocument(String requestId, String docType, String fullName, String address, 
                              String birthdate, String civilStatus, String purpose, String yearsOfResidency) {
        try {
            // Format birthdate for display (convert yyyy-MM-dd to readable format)
            String displayBirthdate = birthdate;
            try {
                LocalDate date = LocalDate.parse(birthdate);
                displayBirthdate = date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
            } catch (Exception e) {
                // If parsing fails, use original format
            }
            
            // Create print content
            VBox printContent = new VBox(15);
            printContent.setPadding(new Insets(40));
            printContent.setAlignment(Pos.TOP_LEFT);
            printContent.setStyle("-fx-background-color: white;");
            
            // Header
            Text header1 = new Text("Republic of the Philippines");
            header1.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            Text header2 = new Text("Province of [Province Name]");
            header2.setFont(Font.font("Arial", 12));
            Text header3 = new Text("Municipality of [Municipality]");
            header3.setFont(Font.font("Arial", 12));
            Text header4 = new Text("BARANGAY SAN ISIDRO");
            header4.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            
            VBox headerBox = new VBox(5, header1, header2, header3, header4);
            headerBox.setAlignment(Pos.CENTER);
            
            // Office info
            Text officeInfo = new Text("Office of the Barangay Captain");
            officeInfo.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            VBox officeBox = new VBox(officeInfo);
            officeBox.setAlignment(Pos.CENTER);
            officeBox.setPadding(new Insets(20, 0, 20, 0));
            
            // Document title
            Text title = new Text(docType.toUpperCase());
            title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            VBox titleBox = new VBox(title);
            titleBox.setAlignment(Pos.CENTER);
            titleBox.setPadding(new Insets(10, 0, 20, 0));
            
            // Document body
            Text toWhom = new Text("TO WHOM IT MAY CONCERN:");
            toWhom.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            
            String bodyText = "\n\tThis is to certify that " + fullName.toUpperCase() + ", " + 
                            civilStatus + ", born on " + displayBirthdate + ", is a bonafide resident of " + 
                            address + ", Barangay San Isidro, and has been residing in this barangay for " + 
                            yearsOfResidency + " year(s).\n\n" +
                            "\tThis certification is issued upon the request of the above-named person for " + 
                            purpose + ".\n\n" +
                            "\tIssued this " + LocalDate.now().format(DateTimeFormatter.ofPattern("d 'day of' MMMM yyyy")) + 
                            " at Barangay San Isidro.";
            
            Text body = new Text(bodyText);
            body.setFont(Font.font("Arial", 12));
            body.setWrappingWidth(500);
            
            // Signature section
            VBox signatureBox = new VBox(10);
            signatureBox.setPadding(new Insets(40, 0, 0, 0));
            signatureBox.setAlignment(Pos.CENTER_RIGHT);
            
            Text signatureLine = new Text("_________________________________");
            signatureLine.setFont(Font.font("Arial", 12));
            Text captainName = new Text("BARANGAY CAPTAIN");
            captainName.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            Text captainTitle = new Text("Punong Barangay");
            captainTitle.setFont(Font.font("Arial", 10));
            
            signatureBox.getChildren().addAll(signatureLine, captainName, captainTitle);
            
            // Request ID footer
            Text requestIdText = new Text("\nDocument ID: " + requestId);
            requestIdText.setFont(Font.font("Arial", FontWeight.NORMAL, 9));
            
            // Add all to print content
            printContent.getChildren().addAll(headerBox, officeBox, titleBox, toWhom, body, signatureBox, requestIdText);
            
            // Create printer job
            PrinterJob printerJob = PrinterJob.createPrinterJob();
            
            if (printerJob != null) {
                boolean proceed = printerJob.showPrintDialog(logoutButton.getScene().getWindow());
                
                if (proceed) {
                    boolean printed = printerJob.printPage(printContent);
                    
                    if (printed) {
                        printerJob.endJob();
                        
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Print Successful");
                        successAlert.setHeaderText("Document Printed");
                        successAlert.setContentText("Your document has been sent to the printer successfully.");
                        successAlert.showAndWait();
                    } else {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Print Failed");
                        errorAlert.setHeaderText("Printing Error");
                        errorAlert.setContentText("Failed to print the document. Please try again.");
                        errorAlert.showAndWait();
                    }
                }
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Printer Not Available");
                errorAlert.setHeaderText("No Printer Found");
                errorAlert.setContentText("No printer is available. Please check your printer connection.");
                errorAlert.showAndWait();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Print Error");
            errorAlert.setHeaderText("Printing Failed");
            errorAlert.setContentText("An error occurred while printing: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }

    @FXML
    private void goBackToDashboard() {
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
    private void goToProfile() {
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

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #e53935; -fx-font-size: 12px;");
    }

    private void clearForm() {
        documentTypeCombo.setValue(null);
        fullNameField.clear();
        addressField.clear();
        birthDatePicker.setValue(null);
        civilStatusCombo.setValue(null);
        purposeField.clear();
        yearsOfResidencyField.clear();
        errorLabel.setText("");
    }
}
