package com.mycompany.javasystem;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.util.Duration;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PaymentsController {

    @FXML private VBox paymentsTableBody;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatus;
    @FXML private Button logoutButton;
    @FXML private Label totalCollectedLabel;
    @FXML private Label pendingCountLabel;
    @FXML private Label paidCountLabel;

    private Timeline autoRefresh;

    @FXML
    public void initialize() {
        filterStatus.getItems().addAll("All", "Pending", "Paid");
        filterStatus.setValue("All");
        loadPayments("", "All");
        loadSummary();
        startAutoRefresh();
    }

    private void startAutoRefresh() {
        autoRefresh = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
            checkAndUpdatePendingPayments();
        }));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();
    }

    private void checkAndUpdatePendingPayments() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.prepareStatement(
                "SELECT payment_id, ref_number FROM payments WHERE status = 'Pending' AND archived = False").executeQuery();

            boolean anyUpdated = false;
            while (rs.next()) {
                String paymentId = rs.getString("payment_id");
                String refNumber = rs.getString("ref_number");
                try {
                    String status = PayMongoService.checkPaymentStatus(paymentId);
                    if (status.equals("paid")) {
                        updatePaymentStatus(refNumber, "Paid");
                        anyUpdated = true;
                    }
                } catch (Exception ignored) {}
            }

            rs.close();
            conn.close();

            if (anyUpdated) {
                Platform.runLater(() -> {
                    loadPayments(searchField.getText().trim(), filterStatus.getValue());
                    loadSummary();
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSummary() {
        try {
            Connection conn = DatabaseConnection.getConnection();

            // Total collected — all paid including archived
            ResultSet rs1 = conn.prepareStatement(
                "SELECT SUM(amount) FROM payments WHERE status = 'Paid'").executeQuery();
            if (rs1.next()) {
                double total = rs1.getDouble(1);
                totalCollectedLabel.setText(String.format("₱%.2f", total));
            }

            // Pending count — only non-archived
            ResultSet rs2 = conn.prepareStatement(
                "SELECT COUNT(*) FROM payments WHERE status = 'Pending' AND archived = False").executeQuery();
            if (rs2.next()) pendingCountLabel.setText(String.valueOf(rs2.getInt(1)));

            // Paid count — ALL paid including archived
            ResultSet rs3 = conn.prepareStatement(
                "SELECT COUNT(*) FROM payments WHERE status = 'Paid'").executeQuery();
            if (rs3.next()) paidCountLabel.setText(String.valueOf(rs3.getInt(1)));

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPayments(String search, String status) {
        paymentsTableBody.getChildren().clear();

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM payments WHERE archived = False ORDER BY ID DESC");
            ResultSet rs = stmt.executeQuery();

            boolean hasData = false;

            while (rs.next()) {
                String paymentId = rs.getString("payment_id");
                String refNumber = rs.getString("ref_number");
                String residentName = rs.getString("resident_name");
                String paymentType = rs.getString("payment_type");
                double amount = rs.getDouble("amount");
                String dateCreated = rs.getString("date_created");
                String payStatus = rs.getString("status");

                if (!search.isEmpty()) {
                    if (!residentName.toLowerCase().contains(search.toLowerCase()) &&
                        !refNumber.toLowerCase().contains(search.toLowerCase())) {
                        continue;
                    }
                }
                if (!status.equals("All") && !payStatus.equals(status)) continue;

                hasData = true;

                HBox row = new HBox();
                row.setStyle("-fx-padding: 14 0; -fx-border-color: #f8f8f8; -fx-border-width: 0 0 1 0;");

                Label refLabel = new Label(refNumber);
                refLabel.setPrefWidth(120);
                refLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

                Label nameLabel = new Label(residentName);
                nameLabel.setPrefWidth(200);
                nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333333;");

                Label typeLabel = new Label(paymentType);
                typeLabel.setPrefWidth(180);
                typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

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
                actionBox.setPrefWidth(180);
                actionBox.setAlignment(Pos.CENTER_LEFT);

                final String fPaymentId = paymentId;
                final String fRefNumber = refNumber;

                if (payStatus.equals("Pending")) {
                    Button viewBtn = new Button("View Payment");
                    viewBtn.setStyle("-fx-background-color: #2d2d2d;" +
                            "-fx-text-fill: #ffffff;" +
                            "-fx-font-size: 11px;" +
                            "-fx-background-radius: 6;" +
                            "-fx-padding: 5 10;" +
                            "-fx-cursor: hand;");
                    viewBtn.setOnAction(e -> openPaymentLink(fPaymentId, fRefNumber));
                    actionBox.getChildren().add(viewBtn);
                } else {
                    Label paidLabel = new Label("✓ Paid");
                    paidLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #4caf50; -fx-padding: 0 8 0 0;");

                    Button archiveBtn = new Button("Archive");
                    archiveBtn.setStyle("-fx-background-color: #f4f4f4;" +
                            "-fx-text-fill: #555555;" +
                            "-fx-font-size: 11px;" +
                            "-fx-background-radius: 6;" +
                            "-fx-padding: 5 10;" +
                            "-fx-cursor: hand;");
                    archiveBtn.setOnAction(e -> archivePayment(fRefNumber));
                    actionBox.getChildren().addAll(paidLabel, archiveBtn);
                }

                row.getChildren().addAll(refLabel, nameLabel, typeLabel, amountLabel, dateLabel, statusBox, actionBox);
                paymentsTableBody.getChildren().add(row);
            }

            if (!hasData) {
                Label empty = new Label("No payments found.");
                empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #aaaaaa; -fx-padding: 20 0;");
                VBox.setMargin(empty, new Insets(20, 0, 20, 0));
                paymentsTableBody.getChildren().add(empty);
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            Label error = new Label("Error loading payments: " + e.getMessage());
            error.setStyle("-fx-font-size: 12px; -fx-text-fill: #e53935;");
            paymentsTableBody.getChildren().add(error);
        }
    }

    private void openPaymentLink(String paymentId, String refNumber) {
        try {
            String status = PayMongoService.checkPaymentStatus(paymentId);
            if (status.equals("paid")) {
                updatePaymentStatus(refNumber, "Paid");
            } else {
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Payment Status");
                info.setHeaderText("Payment for " + refNumber);
                info.setContentText("Status: " + status + "\nThe resident has not completed payment yet.");
                info.showAndWait();
            }
            loadPayments(searchField.getText().trim(), filterStatus.getValue());
            loadSummary();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updatePaymentStatus(String refNumber, String newStatus) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "UPDATE payments SET status = ? WHERE ref_number = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, newStatus);
            stmt.setString(2, refNumber);
            stmt.executeUpdate();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void archivePayment(String refNumber) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "UPDATE payments SET archived = True WHERE ref_number = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, refNumber);
            stmt.executeUpdate();
            stmt.close();
            conn.close();
            loadPayments(searchField.getText().trim(), filterStatus.getValue());
            loadSummary();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
   private void createTestPayment() {
       try {
           String result = PayMongoService.createPaymentLink("REQ-001", "Clearance", 10000);
           String[] parts = result.split("\\|");
           String checkoutUrl = parts[0];
           String linkId = parts[1];

           Connection conn = DatabaseConnection.getConnection();
           String sql = "INSERT INTO payments (payment_id, ref_number, resident_name, payment_type, amount, status, date_created, archived) VALUES (?, ?, ?, ?, ?, ?, ?, False)";
           PreparedStatement stmt = conn.prepareStatement(sql);
           stmt.setString(1, linkId);
           stmt.setString(2, "REQ-001");
           stmt.setString(3, "Maria Santos");
           stmt.setString(4, "Clearance");
           stmt.setDouble(5, 100.00);
           stmt.setString(6, "Pending");
           stmt.setString(7, java.time.LocalDateTime.now()
                   .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
           stmt.executeUpdate();
           stmt.close();
           conn.close();

           loadPayments("", "All");
           loadSummary();

           // Show modal FIRST — browser only opens after user clicks OK
           Alert info = new Alert(Alert.AlertType.INFORMATION);
           info.setTitle("Test Payment Created");
           info.setHeaderText("PayMongo checkout will open after you click OK");
           info.setContentText(
               "Use test card:\n" +
               "Card: 4343 4343 4343 4343\n" +
               "Expiry: Any future date\n" +
               "CVV: Any 3 digits\n\n" +
               "The status will auto-update once payment is completed.\n\n" +
               "Click OK to open the payment page in your browser.");
           info.showAndWait();

           // Browser opens AFTER modal is dismissed
           java.awt.Desktop.getDesktop().browse(new java.net.URI(checkoutUrl));

       } catch (Exception e) {
           e.printStackTrace();
           Alert error = new Alert(Alert.AlertType.ERROR);
           error.setTitle("Error");
           error.setHeaderText("Failed to create test payment");
           error.setContentText(e.getMessage());
           error.showAndWait();
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
    private void goToDashboard() {
        if (autoRefresh != null) autoRefresh.stop();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "AdminDashboard.fxml", true, getClass());
    }

    @FXML
    private void goToResidents() {
        if (autoRefresh != null) autoRefresh.stop();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Residents.fxml", true, getClass());
    }

    @FXML
    private void goToDocuments() {
        if (autoRefresh != null) autoRefresh.stop();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Documents.fxml", true, getClass());
    }

    @FXML
    private void goToArchive() {
        if (autoRefresh != null) autoRefresh.stop();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "PaymentArchive.fxml", true, getClass());
    }
    @FXML
    private void goToComplaints() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Complaints.fxml", true, getClass());
    }
    @FXML 
    private void goToAnnouncements() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Announcements.fxml", true, getClass());
    }
    @FXML private void goToFinances() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Finances.fxml", true, getClass());
    }
    @FXML
    private void handleLogout() {
        if (autoRefresh != null) autoRefresh.stop();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "login.fxml", false, getClass());
    }
}