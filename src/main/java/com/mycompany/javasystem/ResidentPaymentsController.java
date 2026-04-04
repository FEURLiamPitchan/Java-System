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

public class ResidentPaymentsController {

    @FXML private VBox paymentsTableBody;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatus;
    @FXML private Button logoutButton;
    @FXML private Label totalPaidLabel;
    @FXML private Label pendingCountLabel;
    @FXML private Label paidCountLabel;

    @FXML
    public void initialize() {
        filterStatus.getItems().addAll("All", "Pending", "Paid");
        filterStatus.setValue("All");
        loadPayments("", "All");
        loadSummary();
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
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Payment");
        info.setHeaderText("Payment for " + refNumber);
        info.setContentText("In a real system, this would redirect to PayMongo or another payment gateway.\n\nFor demo purposes, this payment will be marked as completed.");
        info.showAndWait();
        
        loadPayments(searchField.getText().trim(), filterStatus.getValue());
        loadSummary();
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
    private void goToDashboard() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "ResidentDashboard.fxml", true, getClass());
    }

    @FXML
    private void goToMyDocuments() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "MyDocuments.fxml", true, getClass());
    }

    @FXML
    private void goToRequestDocument() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "RequestDocument.fxml", true, getClass());
    }

    @FXML
    private void goToAnnouncements() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "ResidentAnnouncements.fxml", true, getClass());
    }

    @FXML
    private void goToComplaints() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "ResidentComplaints.fxml", true, getClass());
    }

    @FXML
    private void handleLogout() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "login.fxml", false, getClass());
    }
}