package com.mycompany.javasystem;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ResidentPaymentsController {

    @FXML private VBox paymentsTableBody;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatus;
    @FXML private Button logoutButton;
    @FXML private Label residentNameLabel;
    @FXML private Label alertBadge;
    @FXML private Button alertsButton;
    @FXML private Label totalPaidLabel;
    @FXML private Label pendingCountLabel;
    @FXML private Label paidCountLabel;
    @FXML private javafx.scene.shape.Circle topBarProfileCircle;
    @FXML private Label topBarProfileInitials;

    @FXML
    public void initialize() {
        loadUserProfile();
        filterStatus.getItems().addAll("All", "Pending", "Paid");
        filterStatus.setValue("All");
        loadPayments("", "All");
        loadSummary();
        ResidentNotifications.syncNotifications(UserSession.getCurrentUserEmail());
        refreshAlertBadge();
        loadTopBarProfilePicture();
    }

    private void loadUserProfile() {
        try {
            String currentUserEmail = UserSession.getCurrentUserEmail();
            if (currentUserEmail == null || currentUserEmail.trim().isEmpty()) {
                residentNameLabel.setText("Resident Name");
                return;
            }
            
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                residentNameLabel.setText("Resident Name");
                return;
            }
            
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT full_name FROM users WHERE email = ?");
            stmt.setString(1, currentUserEmail);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String fullName = rs.getString("full_name");
                residentNameLabel.setText(fullName != null && !fullName.trim().isEmpty() ? fullName : "Resident Name");
            } else {
                residentNameLabel.setText("Resident Name");
            }
            
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("Error loading user profile: " + e.getMessage());
            residentNameLabel.setText("Resident Name");
        }
    }

    private void loadSummary() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                totalPaidLabel.setText("₱0.00");
                pendingCountLabel.setText("0");
                paidCountLabel.setText("0");
                return;
            }

            // For demo purposes, using sample data
            totalPaidLabel.setText("₱450.00");
            pendingCountLabel.setText("1");
            paidCountLabel.setText("3");

            conn.close();
        } catch (Exception e) {
            totalPaidLabel.setText("₱0.00");
            pendingCountLabel.setText("0");
            paidCountLabel.setText("0");
        }
    }

    private void loadPayments(String search, String status) {
        paymentsTableBody.getChildren().clear();

        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                // Demo data for resident payments
                addSamplePayments();
                return;
            }
            
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM payments WHERE resident_name = 'Current Resident' AND archived = False ORDER BY ID DESC");
            ResultSet rs = stmt.executeQuery();

            boolean hasData = false;
            while (rs.next()) {
                String paymentId = rs.getString("payment_id");
                String refNumber = rs.getString("ref_number");
                String paymentType = rs.getString("payment_type");
                double amount = rs.getDouble("amount");
                String dateCreated = rs.getString("date_created");
                String payStatus = rs.getString("status");

                if (!search.isEmpty() && !refNumber.toLowerCase().contains(search.toLowerCase())) {
                    continue;
                }
                if (!status.equals("All") && !payStatus.equals(status)) continue;

                hasData = true;
                paymentsTableBody.getChildren().add(createPaymentRow(refNumber, paymentType, amount, dateCreated, payStatus, paymentId));
            }

            if (!hasData) {
                addSamplePayments();
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            addSamplePayments();
        }
    }

    private void addSamplePayments() {
        paymentsTableBody.getChildren().addAll(
            createPaymentRow("REQ-2024-001", "Barangay Clearance", 150.00, "2024-06-13", "Paid", "demo1"),
            createPaymentRow("REQ-2024-002", "Certificate of Residency", 100.00, "2024-06-12", "Paid", "demo2"),
            createPaymentRow("REQ-2024-003", "Indigency Certificate", 200.00, "2024-06-10", "Paid", "demo3"),
            createPaymentRow("REQ-2024-004", "Business Permit", 500.00, "2024-06-08", "Pending", "demo4")
        );
    }

    private HBox createPaymentRow(String refNumber, String paymentType, double amount, String dateCreated, String payStatus, String paymentId) {
        HBox row = new HBox();
        row.setStyle("-fx-padding: 14 0; -fx-border-color: #f8f8f8; -fx-border-width: 0 0 1 0;");

        Label refLabel = new Label(refNumber);
        refLabel.setPrefWidth(150);
        refLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2196f3; -fx-font-weight: bold;");

        Label typeLabel = new Label(paymentType);
        typeLabel.setPrefWidth(200);
        typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333333; -fx-font-weight: bold;");

        Label amountLabel = new Label(String.format("₱%.2f", amount));
        amountLabel.setPrefWidth(120);
        amountLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");

        Label dateLabel = new Label(dateCreated != null ? dateCreated : "N/A");
        dateLabel.setPrefWidth(140);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

        String statusBg = payStatus.equals("Paid") ? "#e8f5e9" : "#fff8e1";
        String statusFg = payStatus.equals("Paid") ? "#4caf50" : "#f59e0b";
        Label statusLabel = new Label(payStatus);
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

        if (payStatus.equals("Pending")) {
            Button payBtn = new Button("Pay Now");
            payBtn.setStyle("-fx-background-color: #2d2d2d;" +
                    "-fx-text-fill: #ffffff;" +
                    "-fx-font-size: 11px;" +
                    "-fx-background-radius: 6;" +
                    "-fx-padding: 5 10;" +
                    "-fx-cursor: hand;");
            payBtn.setOnAction(e -> handlePayment(refNumber));
            actionBox.getChildren().add(payBtn);
        } else {
            Label paidLabel = new Label("✓ Completed");
            paidLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #4caf50;");
            actionBox.getChildren().add(paidLabel);
        }

        row.getChildren().addAll(refLabel, typeLabel, amountLabel, dateLabel, statusBox, actionBox);
        return row;
    }

    private void handlePayment(String refNumber) {
        try {
            // Show payment options dialog
            Stage paymentDialog = new Stage();
            paymentDialog.initModality(Modality.APPLICATION_MODAL);
            paymentDialog.initOwner(logoutButton.getScene().getWindow());
            paymentDialog.setTitle("Payment Options - " + refNumber);
            
            VBox root = new VBox(0);
            root.setStyle("-fx-background-color: #f8f9fa;");
            
            // Header
            VBox header = new VBox(8);
            header.setStyle("-fx-background-color: #2d2d2d; -fx-padding: 20;");
            Label titleLabel = new Label("Choose Payment Method");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
            Label subtitleLabel = new Label("Select how you want to pay for " + refNumber);
            subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");
            header.getChildren().addAll(titleLabel, subtitleLabel);
            
            // Payment options
            VBox content = new VBox(16);
            content.setStyle("-fx-padding: 24; -fx-background-color: #ffffff;");
            
            // PayMongo option
            HBox paymongoOption = new HBox(12);
            paymongoOption.setStyle("-fx-padding: 16; -fx-background-color: #f8f9ff; -fx-background-radius: 8; -fx-border-color: #e3f2fd; -fx-border-width: 1; -fx-border-radius: 8; -fx-cursor: hand;");
            Label paymongoIcon = new Label("💳");
            paymongoIcon.setStyle("-fx-font-size: 24px;");
            VBox paymongoInfo = new VBox(4);
            Label paymongoTitle = new Label("Online Payment (PayMongo)");
            paymongoTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
            Label paymongoDesc = new Label("Pay securely with credit/debit card or GCash");
            paymongoDesc.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
            paymongoInfo.getChildren().addAll(paymongoTitle, paymongoDesc);
            HBox.setHgrow(paymongoInfo, Priority.ALWAYS);
            paymongoOption.getChildren().addAll(paymongoIcon, paymongoInfo);
            
            // Cash option
            HBox cashOption = new HBox(12);
            cashOption.setStyle("-fx-padding: 16; -fx-background-color: #fff8e1; -fx-background-radius: 8; -fx-border-color: #ffecb3; -fx-border-width: 1; -fx-border-radius: 8; -fx-cursor: hand;");
            Label cashIcon = new Label("💵");
            cashIcon.setStyle("-fx-font-size: 24px;");
            VBox cashInfo = new VBox(4);
            Label cashTitle = new Label("Pay at Barangay Office");
            cashTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
            Label cashDesc = new Label("Visit our office during business hours (8AM-5PM)");
            cashDesc.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
            cashInfo.getChildren().addAll(cashTitle, cashDesc);
            HBox.setHgrow(cashInfo, Priority.ALWAYS);
            cashOption.getChildren().addAll(cashIcon, cashInfo);
            
            content.getChildren().addAll(paymongoOption, cashOption);
            
            // Footer
            HBox footer = new HBox(8);
            footer.setAlignment(Pos.CENTER_RIGHT);
            footer.setStyle("-fx-padding: 16 24; -fx-background-color: #ffffff; -fx-border-color: #e8e8e8; -fx-border-width: 1 0 0 0;");
            Button cancelBtn = new Button("Cancel");
            cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555; -fx-font-size: 12px; -fx-background-radius: 6; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 8 16; -fx-cursor: hand;");
            cancelBtn.setOnAction(e -> paymentDialog.close());
            footer.getChildren().add(cancelBtn);
            
            // Payment option handlers
            paymongoOption.setOnMouseClicked(e -> {
                paymentDialog.close();
                processOnlinePayment(refNumber);
            });
            
            cashOption.setOnMouseClicked(e -> {
                paymentDialog.close();
                showCashPaymentInfo(refNumber);
            });
            
            root.getChildren().addAll(header, content, footer);
            
            Scene scene = new Scene(root, 450, 320);
            paymentDialog.setScene(scene);
            paymentDialog.setResizable(false);
            paymentDialog.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Payment Error");
            error.setHeaderText("Unable to process payment");
            error.setContentText("An error occurred while setting up payment options. Please try again.");
            error.showAndWait();
        }
    }
    
    private void processOnlinePayment(String refNumber) {
        try {
            // Get payment details
            double amount = getPaymentAmount(refNumber);
            String description = getPaymentDescription(refNumber);
            
            // Show processing dialog
            Alert processing = new Alert(Alert.AlertType.INFORMATION);
            processing.setTitle("Processing Payment");
            processing.setHeaderText("Setting up online payment...");
            processing.setContentText("Please wait while we create your payment link.");
            processing.show();
            
            // Create PayMongo payment link
            String paymentResult = PayMongoService.createPaymentLink(refNumber, description, (int)(amount * 100)); // Convert to centavos
            
            processing.close();
            
            if (paymentResult != null && !paymentResult.isEmpty()) {
                String[] parts = paymentResult.split("\\|");
                String paymentUrl = parts[0];
                String paymentId = parts.length > 1 ? parts[1] : "demo_payment_" + System.currentTimeMillis();
                
                // Show payment link dialog
                Stage paymentLinkDialog = new Stage();
                paymentLinkDialog.initModality(Modality.APPLICATION_MODAL);
                paymentLinkDialog.initOwner(logoutButton.getScene().getWindow());
                paymentLinkDialog.setTitle("PayMongo Payment");
                
                VBox root = new VBox(0);
                root.setStyle("-fx-background-color: #f8f9fa;");
                
                // Header
                VBox header = new VBox(8);
                header.setStyle("-fx-background-color: #2d2d2d; -fx-padding: 20;");
                Label titleLabel = new Label("Payment Link Ready");
                titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
                Label subtitleLabel = new Label("Your secure payment link has been generated");
                subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");
                header.getChildren().addAll(titleLabel, subtitleLabel);
                
                // Content
                VBox content = new VBox(16);
                content.setStyle("-fx-padding: 24; -fx-background-color: #ffffff;");
                
                // Payment details
                VBox paymentDetails = new VBox(8);
                paymentDetails.setStyle("-fx-padding: 16; -fx-background-color: #f8f9ff; -fx-background-radius: 8; -fx-border-color: #e3f2fd; -fx-border-width: 1; -fx-border-radius: 8;");
                
                Label refLabel = new Label("Reference: " + refNumber);
                refLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
                
                Label amountLabel = new Label("Amount: ₱" + String.format("%.2f", amount));
                amountLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2196f3;");
                
                Label descLabel = new Label("Description: " + description);
                descLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
                
                paymentDetails.getChildren().addAll(refLabel, amountLabel, descLabel);
                
                // Payment URL
                VBox urlSection = new VBox(8);
                Label urlTitle = new Label("Payment URL:");
                urlTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
                
                TextField urlField = new TextField(paymentUrl);
                urlField.setEditable(false);
                urlField.setStyle("-fx-font-size: 10px; -fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 4;");
                
                Button copyBtn = new Button("📋 Copy Link");
                copyBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 8 12; -fx-cursor: hand;");
                copyBtn.setOnAction(e -> {
                    javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                    javafx.scene.input.ClipboardContent clipContent = new javafx.scene.input.ClipboardContent();
                    clipContent.putString(paymentUrl);
                    clipboard.setContent(clipContent);
                    
                    copyBtn.setText("✓ Copied!");
                    copyBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 8 12; -fx-cursor: hand;");
                });
                
                urlSection.getChildren().addAll(urlTitle, urlField, copyBtn);
                
                // Instructions
                VBox instructions = new VBox(4);
                Label instrTitle = new Label("Payment Instructions:");
                instrTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
                
                Label instr1 = new Label("1. Copy the payment link above");
                instr1.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
                
                Label instr2 = new Label("2. Open it in your browser to complete payment");
                instr2.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
                
                Label instr3 = new Label("3. You can pay using credit/debit card or GCash");
                instr3.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
                
                Label instr4 = new Label("4. Payment confirmation will be sent to your email");
                instr4.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
                
                instructions.getChildren().addAll(instrTitle, instr1, instr2, instr3, instr4);
                
                content.getChildren().addAll(paymentDetails, urlSection, instructions);
                
                // Footer
                HBox footer = new HBox(8);
                footer.setAlignment(Pos.CENTER_RIGHT);
                footer.setStyle("-fx-padding: 16 24; -fx-background-color: #ffffff; -fx-border-color: #e8e8e8; -fx-border-width: 1 0 0 0;");
                
                Button simulateBtn = new Button("Simulate Payment Success");
                simulateBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 8 12; -fx-cursor: hand;");
                simulateBtn.setOnAction(e -> {
                    paymentLinkDialog.close();
                    simulatePaymentSuccess(refNumber, paymentId);
                });
                
                Button closeBtn = new Button("Close");
                closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555; -fx-font-size: 11px; -fx-background-radius: 6; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 8 12; -fx-cursor: hand;");
                closeBtn.setOnAction(e -> paymentLinkDialog.close());
                
                footer.getChildren().addAll(simulateBtn, closeBtn);
                
                root.getChildren().addAll(header, content, footer);
                
                Scene scene = new Scene(root, 500, 450);
                paymentLinkDialog.setScene(scene);
                paymentLinkDialog.setResizable(false);
                paymentLinkDialog.showAndWait();
                
            } else {
                throw new Exception("Failed to create payment link");
            }
            
        } catch (Exception e) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Payment Error");
            error.setHeaderText("Online payment failed");
            error.setContentText("Unable to process online payment: " + e.getMessage());
            error.showAndWait();
        }
    }
    
    private double getPaymentAmount(String refNumber) {
        // Get amount based on reference number
        if (refNumber.contains("001")) return 150.00; // Barangay Clearance
        if (refNumber.contains("002")) return 100.00; // Certificate of Residency
        if (refNumber.contains("003")) return 200.00; // Indigency Certificate
        if (refNumber.contains("004")) return 500.00; // Business Permit
        return 150.00; // Default
    }
    
    private String getPaymentDescription(String refNumber) {
        // Get description based on reference number
        if (refNumber.contains("001")) return "Barangay Clearance Fee";
        if (refNumber.contains("002")) return "Certificate of Residency Fee";
        if (refNumber.contains("003")) return "Indigency Certificate Fee";
        if (refNumber.contains("004")) return "Business Permit Fee";
        return "Document Processing Fee";
    }
    
    private void simulatePaymentSuccess(String refNumber, String paymentId) {
        try {
            // Simulate payment processing delay
            Alert processing = new Alert(Alert.AlertType.INFORMATION);
            processing.setTitle("Processing Payment");
            processing.setHeaderText("Confirming payment...");
            processing.setContentText("Please wait while we verify your payment.");
            processing.show();
            
            // Simulate delay
            Thread.sleep(2000);
            
            processing.close();
            
            // Update payment status
            updatePaymentStatus(refNumber, "Paid");
            
            // Create notification
            ResidentNotifications.addNotification(
                UserSession.getCurrentUserEmail(),
                "document",
                "Payment successful for " + refNumber + ". Your document is now being processed.",
                refNumber
            );
            
            // Show success dialog
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Payment Successful");
            success.setHeaderText("Payment Completed!");
            success.setContentText("Your payment for " + refNumber + " has been processed successfully.\n\n" +
                                 "Payment ID: " + paymentId + "\n" +
                                 "Amount: ₱" + String.format("%.2f", getPaymentAmount(refNumber)) + "\n" +
                                 "Status: Paid\n\n" +
                                 "A confirmation email will be sent to your registered email address.");
            success.showAndWait();
            
        } catch (Exception e) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Payment Error");
            error.setHeaderText("Payment verification failed");
            error.setContentText("Unable to verify payment: " + e.getMessage());
            error.showAndWait();
        }
    }
    
    private void showCashPaymentInfo(String refNumber) {
        Alert cashInfo = new Alert(Alert.AlertType.INFORMATION);
        cashInfo.setTitle("Cash Payment Instructions");
        cashInfo.setHeaderText("Pay at Barangay Office");
        cashInfo.setContentText("Please visit the Barangay San Isidro office with the following details:\n\n" +
                               "Reference Number: " + refNumber + "\n" +
                               "Office Hours: Monday-Friday, 8:00 AM - 5:00 PM\n" +
                               "Location: Barangay San Isidro Hall\n\n" +
                               "Bring a valid ID and mention your reference number to the staff.");
        cashInfo.showAndWait();
    }
    
    private void updatePaymentStatus(String refNumber, String status) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE payments SET status = ?, date_created = ? WHERE ref_number = ?");
                stmt.setString(1, status);
                stmt.setString(2, java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                stmt.setString(3, refNumber);
                stmt.executeUpdate();
                stmt.close();
                conn.close();
            }
            
            // Refresh the payments list
            loadPayments(searchField.getText().trim(), filterStatus.getValue());
            loadSummary();
            
            // Show success message
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Payment Successful");
            success.setHeaderText("Payment completed!");
            success.setContentText("Your payment for " + refNumber + " has been processed successfully.");
            success.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        loadPayments(searchField.getText().trim(), filterStatus.getValue());
    }

    @FXML
    private void handleFilter() {
        loadPayments(searchField.getText().trim(), filterStatus.getValue());
    }

    @FXML
    private void handleMouseEntered(javafx.scene.input.MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #f4f4f4; -fx-text-fill: #1a1a1a; -fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 11 16; -fx-alignment: CENTER_LEFT; -fx-cursor: hand;");
    }

    @FXML
    private void handleMouseExited(javafx.scene.input.MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555; -fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 11 16; -fx-alignment: CENTER_LEFT; -fx-cursor: hand;");
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
    private void goToPayments() {
        // Already on this page
    }

    @FXML
    private void goToMyProfile() {
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
    private void goToProfile() {
        goToMyProfile();
    }

    @FXML
    private void goToDashboard() {
        goBackToDashboard();
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
    private void goToRequestDocument() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("RequestDocument.fxml"));
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
    private void goToSettings() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("ResidentSettings.fxml"));
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

    private void refreshAlertBadge() {
        int count = ResidentNotifications.getUnreadCount(UserSession.getCurrentUserEmail());
        if (count > 0) {
            alertBadge.setText(count > 99 ? "99+" : String.valueOf(count));
            alertBadge.setVisible(true);
        } else {
            alertBadge.setVisible(false);
        }
    }

    @FXML
    private void handleAlertsClick() {
        try {
            javafx.stage.Stage popup = new javafx.stage.Stage();
            popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            popup.initOwner(logoutButton.getScene().getWindow());
            popup.setTitle("Notifications");
            
            VBox root = new VBox(0);
            root.setStyle("-fx-background-color: #f8f9fa;");
            
            VBox header = new VBox(4);
            header.setStyle("-fx-background-color: #2d2d2d; -fx-padding: 20;");
            Label titleLabel = new Label("Notifications");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
            Label subtitleLabel = new Label("Stay updated with your requests and announcements");
            subtitleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaaaaa;");
            header.getChildren().addAll(titleLabel, subtitleLabel);
            
            HBox filterBox = new HBox(8);
            filterBox.setStyle("-fx-padding: 12 20; -fx-background-color: #ffffff; -fx-border-color: #e8e8e8; -fx-border-width: 0 0 1 0;");
            Button unreadBtn = new Button("Unread");
            unreadBtn.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;");
            Button allBtn = new Button("Past Notifications");
            allBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;");
            filterBox.getChildren().addAll(unreadBtn, allBtn);
            
            VBox notifList = new VBox(0);
            ScrollPane scrollPane = new ScrollPane(notifList);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: #f8f9fa; -fx-background-color: #f8f9fa;");
            scrollPane.setPrefHeight(400);
            
            loadNotifications(notifList, false);
            
            unreadBtn.setOnAction(e -> {
                unreadBtn.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;");
                allBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;");
                loadNotifications(notifList, false);
            });
            
            allBtn.setOnAction(e -> {
                allBtn.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;");
                unreadBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;");
                loadNotifications(notifList, true);
            });
            
            HBox footer = new HBox();
            footer.setAlignment(Pos.CENTER_RIGHT);
            footer.setStyle("-fx-padding: 12 20; -fx-background-color: #ffffff; -fx-border-color: #e8e8e8; -fx-border-width: 1 0 0 0;");
            Button closeBtn = new Button("Close");
            closeBtn.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff; -fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand;");
            closeBtn.setOnAction(e -> {
                popup.close();
                refreshAlertBadge();
            });
            footer.getChildren().add(closeBtn);
            
            root.getChildren().addAll(header, filterBox, scrollPane, footer);
            
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 480, 550);
            popup.setScene(scene);
            popup.setResizable(false);
            popup.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadNotifications(VBox container, boolean showAll) {
        container.getChildren().clear();
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) return;
            
            String sql = "SELECT notif_id, type, message, reference_id, is_read, created_at FROM notifications WHERE user_email = ?";
            if (!showAll) sql += " AND is_read = 'false'";
            sql += " ORDER BY notif_id DESC";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, UserSession.getCurrentUserEmail());
            ResultSet rs = stmt.executeQuery();
            
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                int notifId = rs.getInt("notif_id");
                String type = rs.getString("type");
                String message = rs.getString("message");
                String refId = rs.getString("reference_id");
                boolean isRead = rs.getString("is_read").equals("true");
                String createdAt = rs.getString("created_at");
                
                HBox item = buildNotifItem(notifId, type, message, refId, isRead, createdAt);
                container.getChildren().add(item);
            }
            
            if (!hasData) {
                Label emptyLabel = new Label(showAll ? "No notifications yet" : "No unread notifications");
                emptyLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #888888; -fx-padding: 40;");
                VBox emptyBox = new VBox(emptyLabel);
                emptyBox.setAlignment(Pos.CENTER);
                container.getChildren().add(emptyBox);
            }
            
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HBox buildNotifItem(int notifId, String type, String message, String refId, boolean isRead, String createdAt) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-padding: 16 20; -fx-background-color: " + (isRead ? "#ffffff" : "#fafbff") + "; -fx-border-color: #e8e8e8; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
        
        String icon = type.equals("document") ? "📄" : type.equals("complaint") ? "📢" : "📣";
        String iconBg = type.equals("document") ? "#e3f2fd" : type.equals("complaint") ? "#ffebee" : "#fff8e1";
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 20px; -fx-background-color: " + iconBg + "; -fx-background-radius: 8; -fx-padding: 8; -fx-min-width: 40; -fx-min-height: 40; -fx-alignment: center;");
        
        VBox content = new VBox(4);
        content.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(content, Priority.ALWAYS);
        
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #1a1a1a;" + (isRead ? "" : " -fx-font-weight: bold;"));
        
        Label timeLabel = new Label(createdAt);
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888888;");
        
        content.getChildren().addAll(messageLabel, timeLabel);
        
        VBox indicator = new VBox();
        indicator.setAlignment(Pos.CENTER);
        if (!isRead) {
            Label dot = new Label("•");
            dot.setStyle("-fx-font-size: 20px; -fx-text-fill: #2196f3;");
            indicator.getChildren().add(dot);
        }
        
        item.getChildren().addAll(iconLabel, content, indicator);
        item.setOnMouseClicked(e -> {
            ResidentNotifications.markAsRead(notifId);
            refreshAlertBadge();
        });
        
        return item;
    }
    
    private void loadTopBarProfilePicture() {
        if (topBarProfileCircle != null && topBarProfileInitials != null) {
            ProfilePictureLoader.loadProfilePicture(topBarProfileCircle, topBarProfileInitials, UserSession.getCurrentUserEmail());
        }
    }
}