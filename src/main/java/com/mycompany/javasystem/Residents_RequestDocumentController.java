package com.mycompany.javasystem;

import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Residents_RequestDocumentController {
    @FXML private Button logoutButton;
    @FXML private ToggleButton clearanceBtn;
    @FXML private ToggleButton residencyBtn;
    @FXML private ToggleButton indigencyBtn;
    @FXML private VBox formContainer;
    @FXML private Label formTitle;
    @FXML private GridPane formGrid;
    @FXML private Button submitBtn;
    @FXML private Label statusLabel;
    @FXML private javafx.scene.shape.Circle topBarProfileCircle;
    @FXML private Label topBarProfileInitials;
    @FXML private Label residentNameLabel;

    private ToggleGroup documentTypeGroup;
    private Map<String, TextField> formFields;
    private Map<String, ComboBox<String>> formComboBoxes;
    private Map<String, String> uploadedFiles;
    private String selectedDocumentType = "";

    @FXML
    public void initialize() {
        documentTypeGroup = new ToggleGroup();
        clearanceBtn.setToggleGroup(documentTypeGroup);
        residencyBtn.setToggleGroup(documentTypeGroup);
        indigencyBtn.setToggleGroup(documentTypeGroup);

        formFields = new HashMap<>();
        formComboBoxes = new HashMap<>();
        uploadedFiles = new HashMap<>();
        loadUserProfile();
        loadTopBarProfilePicture();
    }

    @FXML
    private void handleDocumentTypeSelection() {
        ToggleButton selected = (ToggleButton) documentTypeGroup.getSelectedToggle();
        if (selected == null) {
            formContainer.setVisible(false);
            return;
        }

        // Update button styles
        clearanceBtn.setStyle(getButtonStyle(clearanceBtn == selected));
        residencyBtn.setStyle(getButtonStyle(residencyBtn == selected));
        indigencyBtn.setStyle(getButtonStyle(indigencyBtn == selected));

        // Determine document type
        if (selected == clearanceBtn) {
            selectedDocumentType = "Barangay Clearance";
            formTitle.setText("Barangay Clearance Request Form");
        } else if (selected == residencyBtn) {
            selectedDocumentType = "Certificate of Residency";
            formTitle.setText("Certificate of Residency Request Form");
        } else if (selected == indigencyBtn) {
            selectedDocumentType = "Certificate of Indigency";
            formTitle.setText("Certificate of Indigency Request Form");
        }

        generateForm();
        formContainer.setVisible(true);
    }

    private String getButtonStyle(boolean selected) {
        if (selected) {
            return "-fx-background-color: #2d2d2d;" +
                   "-fx-text-fill: #ffffff;" +
                   "-fx-font-size: 13px;" +
                   "-fx-font-weight: bold;" +
                   "-fx-background-radius: 8;" +
                   "-fx-border-radius: 8;" +
                   "-fx-border-color: #2d2d2d;" +
                   "-fx-border-width: 1;" +
                   "-fx-padding: 16;" +
                   "-fx-alignment: CENTER_LEFT;" +
                   "-fx-cursor: hand;";
        } else {
            return "-fx-background-color: #f8f9fa;" +
                   "-fx-text-fill: #495057;" +
                   "-fx-font-size: 13px;" +
                   "-fx-background-radius: 8;" +
                   "-fx-border-radius: 8;" +
                   "-fx-border-color: #dee2e6;" +
                   "-fx-border-width: 1;" +
                   "-fx-padding: 16;" +
                   "-fx-alignment: CENTER_LEFT;" +
                   "-fx-cursor: hand;";
        }
    }

    private void generateForm() {
        formGrid.getChildren().clear();
        formFields.clear();
        formComboBoxes.clear();

        int row = 0;

        if (selectedDocumentType.equals("Certificate of Residency")) {
            generateResidencyForm();
        } else if (selectedDocumentType.equals("Certificate of Indigency")) {
            generateIndigencyForm();
        } else {
            // Default form for other document types
            generateDefaultForm();
        }
    }

    private void generateResidencyForm() {
        int row = 0;

        // Full Name
        addTextField("Full Name", "full_name", "Enter your full name", row++);
        prefillUserData();

        // Age
        addTextField("Age", "age", "Enter your age", row++);

        // Gender
        addComboBox("Gender", "gender", new String[]{"Male", "Female", "Other"}, row++);

        // Birth Place
        addTextField("Birth Place", "birth_place", "Enter your birth place", row++);

        // Birth Date
        addTextField("Birth Date", "birth_date", "MM/DD/YYYY", row++);

        // Civil Status
        addComboBox("Civil Status", "civil_status", 
                   new String[]{"Single", "Married", "Divorced", "Widowed"}, row++);

        // Address
        addTextField("Address", "address", "Enter your complete address", row++);

        // Length of Residency
        addTextField("Length of Residency (Years)", "years_residency", "Enter years lived in barangay", row++);

        // Contact Number
        addTextField("Contact Number", "contact_number", "Enter your contact number", row++);

        // Email Address
        addTextField("Email Address", "email_address", "Enter your email address", row++);

        // Purpose
        addTextField("Purpose of Request", "purpose", "Enter purpose of this document", row++);

        // Proof of Residency Upload
        addFileUpload("Proof of Residency", "proof_of_residency", 
                     "Upload utility bill, barangay ID, or other proof", row++);
    }

    private void generateIndigencyForm() {
        int row = 0;

        // Full Name
        addTextField("Full Name", "full_name", "Enter your full name", row++);
        prefillUserData();

        // Age
        addTextField("Age", "age", "Enter your age", row++);

        // Gender
        addComboBox("Gender", "gender", new String[]{"Male", "Female", "Other"}, row++);

        // Birth Date
        addTextField("Birth Date", "birth_date", "MM/DD/YYYY", row++);

        // Civil Status
        addComboBox("Civil Status", "civil_status", 
                   new String[]{"Single", "Married", "Divorced", "Widowed"}, row++);

        // Occupation
        addTextField("Occupation", "occupation", "Enter your occupation or 'Unemployed'", row++);

        // Address
        addTextField("Address", "address", "Enter your complete address", row++);

        // Years of Residency
        addTextField("Years of Residency", "years_residency", "Enter years lived in barangay", row++);

        // Name of Head of Family
        addTextField("Name of Head of Family", "head_of_family", "Enter head of family name", row++);

        // Number of Family Members
        addTextField("Number of Family Members", "family_members", "Enter total family members", row++);

        // Family Monthly Income
        addTextField("Family Monthly Income (PHP)", "monthly_income", "Enter monthly income amount", row++);

        // Source of Income
        addTextField("Source of Income", "income_source", "Enter main source of income", row++);

        // Contact Number
        addTextField("Contact Number", "contact_number", "Enter your contact number", row++);

        // Email Address
        addTextField("Email Address", "email_address", "Enter your email address", row++);

        // Purpose
        addTextField("Purpose of Request", "purpose", "Enter purpose of this document", row++);

        // Proof of Income Upload
        addFileUpload("Proof of Income", "proof_of_income", 
                     "Upload payslip, certificate of employment, etc.", row++);

        // Proof of Residency Upload
        addFileUpload("Proof of Residency", "proof_of_residency", 
                     "Upload barangay ID, utility bill, etc.", row++);
    }



    private void generateDefaultForm() {
        int row = 0;

        // Full Name
        addTextField("Full Name", "full_name", "Enter your full name", row++);
        prefillUserData();

        // Age
        addTextField("Age", "age", "Enter your age", row++);

        // Gender
        addComboBox("Gender", "gender", new String[]{"Male", "Female", "Other"}, row++);

        // Birth Place
        addTextField("Birth Place", "birth_place", "Enter your birth place", row++);

        // Birth Date
        addTextField("Birth Date", "birth_date", "MM/DD/YYYY", row++);

        // Civil Status
        addComboBox("Civil Status", "civil_status", 
                   new String[]{"Single", "Married", "Divorced", "Widowed"}, row++);

        // Address
        addTextField("Address", "address", "Enter your complete address", row++);

        // Years of Residency
        addTextField("Years of Residency", "years_residency", "Enter years lived in barangay", row++);

        // Contact Number
        addTextField("Contact Number", "contact_number", "Enter your contact number", row++);

        // Email Address
        addTextField("Email Address", "email_address", "Enter your email address", row++);

        // Purpose
        addTextField("Purpose of Request", "purpose", "Enter purpose of this document", row++);

        // Valid ID Upload (for Barangay Clearance)
        addFileUpload("Valid ID", "valid_id_image", 
                     "Upload clear photo of your valid ID", row++);
    }

    private void addTextField(String labelText, String fieldName, String promptText, int row) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #555555;");
        
        TextField textField = new TextField();
        textField.setPromptText(promptText);
        textField.setStyle("-fx-background-color: #f9f9f9;" +
                          "-fx-border-color: #e0e0e0;" +
                          "-fx-border-radius: 8;" +
                          "-fx-background-radius: 8;" +
                          "-fx-padding: 12 16;" +
                          "-fx-font-size: 13px;");
        textField.setMaxWidth(Double.MAX_VALUE);

        formFields.put(fieldName, textField);

        // For Certificate of Indigency, use single column layout for better organization
        if (selectedDocumentType.equals("Certificate of Indigency")) {
            formGrid.add(label, 0, row * 2);
            formGrid.add(textField, 0, row * 2 + 1);
            // Add column span for full width
            GridPane.setColumnSpan(label, 2);
            GridPane.setColumnSpan(textField, 2);
        } else {
            // Use two-column layout for other forms
            int col = row % 2;
            int gridRow = row / 2;
            formGrid.add(label, col, gridRow * 2);
            formGrid.add(textField, col, gridRow * 2 + 1);
        }
    }

    private void addComboBox(String labelText, String fieldName, String[] options, int row) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #555555;");
        
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(options);
        comboBox.setStyle("-fx-background-color: #f9f9f9;" +
                         "-fx-border-color: #e0e0e0;" +
                         "-fx-border-radius: 8;" +
                         "-fx-background-radius: 8;" +
                         "-fx-padding: 8 12;" +
                         "-fx-font-size: 13px;");
        comboBox.setMaxWidth(Double.MAX_VALUE);

        formComboBoxes.put(fieldName, comboBox);

        // For Certificate of Indigency, use single column layout for better organization
        if (selectedDocumentType.equals("Certificate of Indigency")) {
            formGrid.add(label, 0, row * 2);
            formGrid.add(comboBox, 0, row * 2 + 1);
            // Add column span for full width
            GridPane.setColumnSpan(label, 2);
            GridPane.setColumnSpan(comboBox, 2);
        } else {
            // Use two-column layout for other forms
            int col = row % 2;
            int gridRow = row / 2;
            formGrid.add(label, col, gridRow * 2);
            formGrid.add(comboBox, col, gridRow * 2 + 1);
        }
    }

    private void addFileUpload(String labelText, String fieldName, String promptText, int row) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #555555;");
        
        HBox fileUploadBox = new HBox(8);
        fileUploadBox.setStyle("-fx-alignment: CENTER_LEFT;");
        
        TextField filePathField = new TextField();
        filePathField.setPromptText(promptText);
        filePathField.setEditable(false);
        filePathField.setStyle("-fx-background-color: #f9f9f9;" +
                              "-fx-border-color: #e0e0e0;" +
                              "-fx-border-radius: 8;" +
                              "-fx-background-radius: 8;" +
                              "-fx-padding: 12 16;" +
                              "-fx-font-size: 13px;");
        
        Button browseButton = new Button("Browse");
        browseButton.setStyle("-fx-background-color: #2d2d2d;" +
                             "-fx-text-fill: #ffffff;" +
                             "-fx-font-size: 12px;" +
                             "-fx-background-radius: 6;" +
                             "-fx-padding: 8 16;" +
                             "-fx-cursor: hand;");
        
        browseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            
            if (fieldName.equals("proof_of_income")) {
                fileChooser.setTitle("Select Proof of Income");
            } else if (fieldName.equals("proof_of_residency")) {
                fileChooser.setTitle("Select Proof of Residency");
            } else {
                fileChooser.setTitle("Select File");
            }
            
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
            );
            
            File selectedFile = fileChooser.showOpenDialog(browseButton.getScene().getWindow());
            if (selectedFile != null) {
                try {
                    // Create uploads directory based on file type
                    String subDir = fieldName.equals("proof_of_income") ? "proof_of_income" : 
                                   fieldName.equals("proof_of_residency") ? "proof_of_residency" : "valid_id";
                    Path uploadsDir = Paths.get("uploads", subDir);
                    Files.createDirectories(uploadsDir);
                    
                    // Generate unique filename
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                    String extension = getFileExtension(selectedFile.getName());
                    String newFileName = fieldName + "_" + timestamp + extension;
                    
                    // Copy file to uploads directory
                    Path targetPath = uploadsDir.resolve(newFileName);
                    Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                    
                    // Store the relative path
                    String relativePath = "uploads/" + subDir + "/" + newFileName;
                    uploadedFiles.put(fieldName, relativePath);
                    filePathField.setText(selectedFile.getName() + " (uploaded)");
                    
                } catch (IOException ex) {
                    showStatus("❌ Error uploading file: " + ex.getMessage(), false);
                }
            }
        });
        
        fileUploadBox.getChildren().addAll(filePathField, browseButton);
        HBox.setHgrow(filePathField, javafx.scene.layout.Priority.ALWAYS);
        
        // For Certificate of Indigency, use single column layout for better organization
        if (selectedDocumentType.equals("Certificate of Indigency")) {
            formGrid.add(label, 0, row * 2);
            formGrid.add(fileUploadBox, 0, row * 2 + 1);
            // Add column span for full width
            GridPane.setColumnSpan(label, 2);
            GridPane.setColumnSpan(fileUploadBox, 2);
        } else {
            // Use two-column layout for other forms
            int col = row % 2;
            int gridRow = row / 2;
            formGrid.add(label, col, gridRow * 2);
            formGrid.add(fileUploadBox, col, gridRow * 2 + 1);
        }
    }
    
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : fileName.substring(lastDotIndex);
    }

    @FXML
    private void handleSubmitRequest() {
        if (selectedDocumentType.isEmpty()) {
            showStatus("Please select a document type first.", false);
            return;
        }

        // Validate required fields
        if (!validateForm()) {
            return;
        }

        // Show loading state
        submitBtn.setDisable(true);
        submitBtn.setText("Submitting...");
        showStatus("⏳ Processing your request...", true);

        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                showStatus("❌ Database connection failed. Please try again later.", false);
                resetSubmitButton();
                return;
            }

            // Generate request ID
            String requestId = generateRequestId();
            String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String userEmail = UserSession.getCurrentUserEmail();

            String sql;
            PreparedStatement stmt;
            
            if (selectedDocumentType.equals("Certificate of Indigency")) {
                sql = "INSERT INTO document_requests (request_id, document_type, full_name, age, gender, " +
                      "birth_date, civil_status, occupation, address, years_residency, head_of_family, " +
                      "family_members, monthly_income, income_source, contact_number, email_address, " +
                      "purpose, proof_of_income_path, proof_of_residency_path, status, date_requested, user_email) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, requestId);
                stmt.setString(2, selectedDocumentType);
                stmt.setString(3, getFieldValue("full_name"));
                stmt.setInt(4, Integer.parseInt(getFieldValue("age")));
                stmt.setString(5, getComboValue("gender"));
                stmt.setString(6, getFieldValue("birth_date"));
                stmt.setString(7, getComboValue("civil_status"));
                stmt.setString(8, getFieldValue("occupation"));
                stmt.setString(9, getFieldValue("address"));
                stmt.setInt(10, Integer.parseInt(getFieldValue("years_residency")));
                stmt.setString(11, getFieldValue("head_of_family"));
                stmt.setInt(12, Integer.parseInt(getFieldValue("family_members")));
                stmt.setDouble(13, Double.parseDouble(getFieldValue("monthly_income")));
                stmt.setString(14, getFieldValue("income_source"));
                stmt.setString(15, getFieldValue("contact_number"));
                stmt.setString(16, getFieldValue("email_address"));
                stmt.setString(17, getFieldValue("purpose"));
                stmt.setString(18, uploadedFiles.getOrDefault("proof_of_income", ""));
                stmt.setString(19, uploadedFiles.getOrDefault("proof_of_residency", ""));
                stmt.setString(20, "Pending");
                stmt.setString(21, currentDate);
                stmt.setString(22, userEmail);
            } else if (selectedDocumentType.equals("Certificate of Residency")) {
                sql = "INSERT INTO document_requests (request_id, document_type, full_name, age, gender, " +
                      "birth_place, birth_date, civil_status, address, years_residency, contact_number, " +
                      "email_address, purpose, proof_of_residency_path, status, date_requested, user_email) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

                stmt = conn.prepareStatement(sql);
                stmt.setString(1, requestId);
                stmt.setString(2, selectedDocumentType);
                stmt.setString(3, getFieldValue("full_name"));
                stmt.setInt(4, Integer.parseInt(getFieldValue("age")));
                stmt.setString(5, getComboValue("gender"));
                stmt.setString(6, getFieldValue("birth_place"));
                stmt.setString(7, getFieldValue("birth_date"));
                stmt.setString(8, getComboValue("civil_status"));
                stmt.setString(9, getFieldValue("address"));
                stmt.setInt(10, Integer.parseInt(getFieldValue("years_residency")));
                stmt.setString(11, getFieldValue("contact_number"));
                stmt.setString(12, getFieldValue("email_address"));
                stmt.setString(13, getFieldValue("purpose"));
                stmt.setString(14, uploadedFiles.getOrDefault("proof_of_residency", ""));
                stmt.setString(15, "Pending");
                stmt.setString(16, currentDate);
                stmt.setString(17, userEmail);
            } else {
                // Barangay Clearance
                sql = "INSERT INTO document_requests (request_id, document_type, full_name, age, gender, " +
                      "birth_place, birth_date, civil_status, address, years_residency, contact_number, " +
                      "email_address, purpose, valid_id_path, status, date_requested, user_email) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

                stmt = conn.prepareStatement(sql);
                stmt.setString(1, requestId);
                stmt.setString(2, selectedDocumentType);
                stmt.setString(3, getFieldValue("full_name"));
                stmt.setInt(4, Integer.parseInt(getFieldValue("age")));
                stmt.setString(5, getComboValue("gender"));
                stmt.setString(6, getFieldValue("birth_place"));
                stmt.setString(7, getFieldValue("birth_date"));
                stmt.setString(8, getComboValue("civil_status"));
                stmt.setString(9, getFieldValue("address"));
                stmt.setInt(10, Integer.parseInt(getFieldValue("years_residency")));
                stmt.setString(11, getFieldValue("contact_number"));
                stmt.setString(12, getFieldValue("email_address"));
                stmt.setString(13, getFieldValue("purpose"));
                stmt.setString(14, uploadedFiles.getOrDefault("valid_id_image", ""));
                stmt.setString(15, "Pending");
                stmt.setString(16, currentDate);
                stmt.setString(17, userEmail);
            }

            stmt.executeUpdate();
            
            // Create notification for the user
            createNotification(conn, userEmail, "document", 
                "Your " + selectedDocumentType + " request (" + requestId + ") has been submitted and is being processed.", 
                requestId);
            
            stmt.close();
            conn.close();

            showStatus("✅ Request submitted successfully! Request ID: " + requestId + ". You will be notified of updates.", true);
            
            // Auto-redirect to My Documents after 3 seconds
            javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(3), e -> {
                    try {
                        switchScene("MyDocuments.fxml", true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                })
            );
            timeline.play();
            
            handleClearForm();

        } catch (Exception e) {
            e.printStackTrace();
            showStatus("❌ Error submitting request: " + e.getMessage(), false);
        } finally {
            resetSubmitButton();
        }
    }
    
    private void resetSubmitButton() {
        submitBtn.setDisable(false);
        submitBtn.setText("Submit Request");
    }
    
    private void createNotification(Connection conn, String userEmail, String type, String message, String refId) {
        try {
            PreparedStatement notifStmt = conn.prepareStatement(
                "INSERT INTO notifications (type, message, reference_id, is_read, created_at, user_email) VALUES (?,?,?,?,?,?)");
            notifStmt.setString(1, type);
            notifStmt.setString(2, message);
            notifStmt.setString(3, refId);
            notifStmt.setString(4, "false");
            notifStmt.setString(5, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            notifStmt.setString(6, userEmail);
            notifStmt.executeUpdate();
            notifStmt.close();
        } catch (Exception e) {
            System.err.println("Error creating notification: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        // Check required text fields
        for (Map.Entry<String, TextField> entry : formFields.entrySet()) {
            String fieldName = entry.getKey();
            TextField field = entry.getValue();
            String value = field.getText().trim();
            
            if (value.isEmpty()) {
                showStatus("⚠️ Please fill in all required fields. Missing: " + getFieldDisplayName(fieldName), false);
                field.requestFocus();
                return false;
            }
            
            // Specific field validations
            if (fieldName.equals("email_address") && !isValidEmail(value)) {
                showStatus("⚠️ Please enter a valid email address.", false);
                field.requestFocus();
                return false;
            }
            
            if (fieldName.equals("contact_number") && !isValidPhoneNumber(value)) {
                showStatus("⚠️ Please enter a valid contact number (11 digits).", false);
                field.requestFocus();
                return false;
            }
            
            if (fieldName.equals("birth_date") && !isValidDate(value)) {
                showStatus("⚠️ Please enter birth date in MM/DD/YYYY format.", false);
                field.requestFocus();
                return false;
            }
        }

        // Check required combo boxes
        for (Map.Entry<String, ComboBox<String>> entry : formComboBoxes.entrySet()) {
            if (entry.getValue().getValue() == null) {
                showStatus("⚠️ Please select all required options. Missing: " + getFieldDisplayName(entry.getKey()), false);
                entry.getValue().requestFocus();
                return false;
            }
        }

        // Validate numeric fields
        try {
            int age = Integer.parseInt(getFieldValue("age"));
            if (age < 1 || age > 120) {
                showStatus("⚠️ Please enter a valid age (1-120).", false);
                return false;
            }
            
            int yearsResidency = Integer.parseInt(getFieldValue("years_residency"));
            if (yearsResidency < 0 || yearsResidency > age) {
                showStatus("⚠️ Years of residency cannot be negative or greater than age.", false);
                return false;
            }
            
            // Additional validation for Certificate of Indigency
            if (selectedDocumentType.equals("Certificate of Indigency")) {
                int familyMembers = Integer.parseInt(getFieldValue("family_members"));
                if (familyMembers < 1) {
                    showStatus("⚠️ Number of family members must be at least 1.", false);
                    return false;
                }
                
                double monthlyIncome = Double.parseDouble(getFieldValue("monthly_income"));
                if (monthlyIncome < 0) {
                    showStatus("⚠️ Monthly income cannot be negative.", false);
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            if (selectedDocumentType.equals("Certificate of Indigency")) {
                showStatus("⚠️ Age, Years of Residency, Family Members, and Monthly Income must be valid numbers.", false);
            } else {
                showStatus("⚠️ Age and Years of Residency must be valid numbers.", false);
            }
            return false;
        }

        // File upload validations
        if (selectedDocumentType.equals("Barangay Clearance")) {
            if (!uploadedFiles.containsKey("valid_id_image") || uploadedFiles.get("valid_id_image").isEmpty()) {
                showStatus("⚠️ Please upload a clear photo of your valid ID.", false);
                return false;
            }
        }
        
        if (selectedDocumentType.equals("Certificate of Residency")) {
            if (!uploadedFiles.containsKey("proof_of_residency") || uploadedFiles.get("proof_of_residency").isEmpty()) {
                showStatus("⚠️ Please upload proof of residency (utility bill, barangay ID, etc.).", false);
                return false;
            }
        }
        
        if (selectedDocumentType.equals("Certificate of Indigency")) {
            if (!uploadedFiles.containsKey("proof_of_income") || uploadedFiles.get("proof_of_income").isEmpty()) {
                showStatus("⚠️ Please upload proof of income (payslip, certificate of employment, etc.).", false);
                return false;
            }
            if (!uploadedFiles.containsKey("proof_of_residency") || uploadedFiles.get("proof_of_residency").isEmpty()) {
                showStatus("⚠️ Please upload proof of residency (barangay ID, utility bill, etc.).", false);
                return false;
            }
        }


        return true;
    }
    
    private String getFieldDisplayName(String fieldName) {
        switch (fieldName) {
            case "full_name": return "Full Name";
            case "age": return "Age";
            case "gender": return "Gender";
            case "birth_place": return "Birth Place";
            case "birth_date": return "Birth Date";
            case "civil_status": return "Civil Status";
            case "occupation": return "Occupation";
            case "address": return "Address";
            case "years_residency": return "Years of Residency";
            case "head_of_family": return "Head of Family";
            case "family_members": return "Number of Family Members";
            case "monthly_income": return "Monthly Income";
            case "income_source": return "Source of Income";
            case "contact_number": return "Contact Number";
            case "email_address": return "Email Address";
            case "purpose": return "Purpose";
            case "business_name": return "Business Name";
            case "business_type": return "Business Type";
            default: return fieldName;
        }
    }
    
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    private boolean isValidPhoneNumber(String phone) {
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        return cleanPhone.length() == 11 && cleanPhone.startsWith("09");
    }
    
    private boolean isValidDate(String date) {
        try {
            String[] parts = date.split("/");
            if (parts.length != 3) return false;
            
            int month = Integer.parseInt(parts[0]);
            int day = Integer.parseInt(parts[1]);
            int year = Integer.parseInt(parts[2]);
            
            if (month < 1 || month > 12) return false;
            if (day < 1 || day > 31) return false;
            if (year < 1900 || year > 2024) return false;
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String getFieldValue(String fieldName) {
        TextField field = formFields.get(fieldName);
        return field != null ? field.getText().trim() : "";
    }

    private String getComboValue(String fieldName) {
        ComboBox<String> combo = formComboBoxes.get(fieldName);
        return combo != null ? combo.getValue() : "";
    }

    private String generateRequestId() {
        String prefix = "REQ";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return prefix + "-" + timestamp;
    }

    @FXML
    private void handleClearForm() {
        for (TextField field : formFields.values()) {
            field.clear();
        }
        for (ComboBox<String> combo : formComboBoxes.values()) {
            combo.setValue(null);
        }
        uploadedFiles.clear();
        statusLabel.setText("");
        
        // Clear file upload displays
        formGrid.getChildren().forEach(node -> {
            if (node instanceof HBox) {
                HBox hbox = (HBox) node;
                hbox.getChildren().forEach(child -> {
                    if (child instanceof TextField) {
                        TextField tf = (TextField) child;
                        if (!tf.isEditable()) {
                            tf.clear();
                        }
                    }
                });
            }
        });
    }

    private void showStatus(String message, boolean isSuccess) {
        statusLabel.setText(message);
        statusLabel.setStyle(isSuccess
            ? "-fx-text-fill: #4caf50; -fx-font-size: 12px;"
            : "-fx-text-fill: #e53935; -fx-font-size: 12px;");
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

    private void switchScene(String fxml, boolean maximize) {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Parent currentRoot = stage.getScene().getRoot();

            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentRoot);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                try {
                    Parent newRoot = FXMLLoader.load(getClass().getResource(fxml));
                    newRoot.setOpacity(0.0);
                    stage.setMaximized(maximize);
                    stage.getScene().setRoot(newRoot);

                    FadeTransition fadeIn = new FadeTransition(Duration.millis(200), newRoot);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            fadeOut.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        switchScene("login.fxml", false);
    }

    @FXML
    private void goToDashboard() {
        switchScene("ResidentDashboard.fxml", true);
    }

    @FXML
    private void goToMyDocuments() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Parent newRoot = FXMLLoader.load(getClass().getResource("MyDocuments.fxml"));
            stage.setMaximized(true);
            stage.getScene().setRoot(newRoot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToPayments() {
        try {
            switchScene("ResidentPayments.fxml", true);
        } catch (Exception e) {
            e.printStackTrace();
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Navigation Error");
            error.setHeaderText("Failed to load Payments page");
            error.setContentText("Error: " + e.getMessage());
            error.showAndWait();
        }
    }

    @FXML
    private void goToAnnouncements() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Parent newRoot = FXMLLoader.load(getClass().getResource("ResidentAnnouncements.fxml"));
            stage.setMaximized(true);
            stage.getScene().setRoot(newRoot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToMyProfile() {
        switchScene("MyProfile.fxml", true);
    }

    @FXML
    private void goToSettings() {
        switchScene("ResidentSettings.fxml", true);
    }

    private void prefillUserData() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                PreparedStatement stmt = conn.prepareStatement(
                    "SELECT full_name, email FROM users WHERE email = ?");
                stmt.setString(1, UserSession.getCurrentUserEmail());
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    String fullName = rs.getString("full_name");
                    String email = rs.getString("email");
                    
                    if (formFields.containsKey("full_name") && fullName != null) {
                        formFields.get("full_name").setText(fullName);
                    }
                    if (formFields.containsKey("email_address") && email != null) {
                        formFields.get("email_address").setText(email);
                    }
                }
                
                rs.close();
                stmt.close();
                conn.close();
            }
        } catch (Exception e) {
            System.err.println("Error prefilling user data: " + e.getMessage());
        }
    }
    
    private void loadTopBarProfilePicture() {
        if (topBarProfileCircle != null && topBarProfileInitials != null) {
            ProfilePictureLoader.loadProfilePicture(topBarProfileCircle, topBarProfileInitials, UserSession.getCurrentUserEmail());
        }
    }
    
    private void loadUserProfile() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                PreparedStatement stmt = conn.prepareStatement(
                    "SELECT full_name FROM users WHERE email = ?");
                stmt.setString(1, UserSession.getCurrentUserEmail());
                java.sql.ResultSet rs = stmt.executeQuery();
                
                if (rs.next() && residentNameLabel != null) {
                    String fullName = rs.getString("full_name");
                    residentNameLabel.setText(fullName != null && !fullName.trim().isEmpty() ? fullName : "Resident Name");
                }
                
                rs.close();
                stmt.close();
                conn.close();
            }
        } catch (Exception e) {
            System.err.println("Error loading user profile: " + e.getMessage());
        }
    }
}