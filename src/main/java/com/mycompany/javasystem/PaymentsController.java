package com.mycompany.javasystem;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PaymentsController {

    @FXML private VBox paymentsTableBody;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatus;
    @FXML private Button logoutButton;
    @FXML private Button alertsButton;
    @FXML private Label alertBadge;
    @FXML private HBox avatarBox;
    @FXML private Circle avatarCircle;
    @FXML private ImageView profileImageView;
    @FXML private Label avatarInitialLabel;
    @FXML private Label topBarNameLabel;
    @FXML private Label topBarRoleLabel;
    @FXML private Label totalCollectedLabel;
    @FXML private Label pendingCountLabel;
    @FXML private Label paidCountLabel;

    private Timeline autoRefresh;

    @FXML
    public void initialize() {
        loadTopBar();
        loadAvatarPicture();
        filterStatus.getItems().addAll("All", "Pending", "Paid");
        filterStatus.setValue("All");
        loadPayments("", "All");
        loadSummary();
        syncNotifications();
        refreshAlertBadge();
        startAutoRefresh();
    }

    // ── Top Bar ───────────────────────────────────────────────────────────────────
    private void loadTopBar() {
        String name = SessionManager.getName();
        String role = SessionManager.getRole();
        if (topBarNameLabel != null)
            topBarNameLabel.setText(name != null ? name : "Administrator");
        if (topBarRoleLabel != null)
            topBarRoleLabel.setText(role != null ? capitalize(role) : "Admin");
    }

    private void loadAvatarPicture() {
        ProfilePictureManager.loadAvatarPicture(
            SessionManager.getEmail(),
            avatarBox,
            avatarCircle,
            profileImageView,
            avatarInitialLabel
        );
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    // ── Auto Refresh ──────────────────────────────────────────────────────────────
    private void startAutoRefresh() {
        autoRefresh = new Timeline(new KeyFrame(Duration.seconds(5), e ->
            checkAndUpdatePendingPayments()));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();
    }

    private void checkAndUpdatePendingPayments() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.prepareStatement(
                "SELECT payment_id, ref_number FROM payments " +
                "WHERE status = 'Pending' AND archived = False"
            ).executeQuery();
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
            rs.close(); conn.close();
            if (anyUpdated) {
                Platform.runLater(() -> {
                    loadPayments(searchField.getText().trim(), filterStatus.getValue());
                    loadSummary();
                    syncNotifications();
                    refreshAlertBadge();
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Summary ───────────────────────────────────────────────────────────────────
    private void loadSummary() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs1 = conn.prepareStatement(
                "SELECT SUM(amount) FROM payments WHERE status = 'Paid'"
            ).executeQuery();
            if (rs1.next())
                totalCollectedLabel.setText(String.format("₱%.2f", rs1.getDouble(1)));
            rs1.close();
            ResultSet rs2 = conn.prepareStatement(
                "SELECT COUNT(*) FROM payments WHERE status = 'Pending' AND archived = False"
            ).executeQuery();
            if (rs2.next()) pendingCountLabel.setText(String.valueOf(rs2.getInt(1)));
            rs2.close();
            ResultSet rs3 = conn.prepareStatement(
                "SELECT COUNT(*) FROM payments WHERE status = 'Paid'"
            ).executeQuery();
            if (rs3.next()) paidCountLabel.setText(String.valueOf(rs3.getInt(1)));
            rs3.close();
            conn.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Load Payments ─────────────────────────────────────────────────────────────
    private void loadPayments(String search, String status) {
        paymentsTableBody.getChildren().clear();
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.prepareStatement(
                "SELECT * FROM payments WHERE archived = False ORDER BY ID DESC"
            ).executeQuery();
            boolean hasData = false;

            while (rs.next()) {
                String paymentId    = rs.getString("payment_id");
                String refNumber    = rs.getString("ref_number");
                String residentName = rs.getString("resident_name");
                String paymentType  = rs.getString("payment_type");
                double amount       = rs.getDouble("amount");
                String dateCreated  = rs.getString("date_created");
                String payStatus    = rs.getString("status");

                if (!search.isEmpty()) {
                    if (!residentName.toLowerCase().contains(search.toLowerCase()) &&
                        !refNumber.toLowerCase().contains(search.toLowerCase())) continue;
                }
                if (!status.equals("All") && !payStatus.equals(status)) continue;

                hasData = true;
                HBox row = new HBox();
                row.setStyle("-fx-padding: 14 0; -fx-border-color: #f8f8f8;" +
                    "-fx-border-width: 0 0 1 0;");

                Label refLabel = new Label(refNumber);
                refLabel.setPrefWidth(120);
                refLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

                Label nameLabel = new Label(residentName);
                nameLabel.setPrefWidth(200);
                nameLabel.setStyle(
                    "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333333;");

                Label typeLabel = new Label(paymentType);
                typeLabel.setPrefWidth(180);
                typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

                Label amountLabel = new Label(String.format("₱%.2f", amount));
                amountLabel.setPrefWidth(120);
                amountLabel.setStyle(
                    "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");

                Label dateLabel = new Label(dateCreated != null ? dateCreated : "N/A");
                dateLabel.setPrefWidth(140);
                dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

                String statusBg = "Paid".equals(payStatus) ? "#e8f5e9" : "#fff8e1";
                String statusFg = "Paid".equals(payStatus) ? "#4caf50" : "#f59e0b";
                Label statusLabel = new Label(payStatus);
                statusLabel.setStyle(
                    "-fx-background-color: " + statusBg + ";" +
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

                if ("Pending".equals(payStatus)) {
                    Button viewBtn = new Button("View Payment");
                    viewBtn.setStyle(
                        "-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff;" +
                        "-fx-font-size: 11px; -fx-background-radius: 6;" +
                        "-fx-padding: 5 10; -fx-cursor: hand;");
                    viewBtn.setOnAction(e -> openPaymentLink(fPaymentId, fRefNumber));
                    actionBox.getChildren().add(viewBtn);
                } else {
                    Label paidLabel = new Label("✓ Paid");
                    paidLabel.setStyle(
                        "-fx-font-size: 11px; -fx-text-fill: #4caf50; -fx-padding: 0 8 0 0;");
                    Button archiveBtn = new Button("Archive");
                    archiveBtn.setStyle(
                        "-fx-background-color: #f4f4f4; -fx-text-fill: #555555;" +
                        "-fx-font-size: 11px; -fx-background-radius: 6;" +
                        "-fx-padding: 5 10; -fx-cursor: hand;");
                    archiveBtn.setOnAction(e -> archivePayment(fRefNumber));
                    actionBox.getChildren().addAll(paidLabel, archiveBtn);
                }

                row.getChildren().addAll(
                    refLabel, nameLabel, typeLabel, amountLabel,
                    dateLabel, statusBox, actionBox);
                paymentsTableBody.getChildren().add(row);
            }

            if (!hasData) {
                Label empty = new Label("No payments found.");
                empty.setStyle(
                    "-fx-font-size: 13px; -fx-text-fill: #aaaaaa; -fx-padding: 20 0;");
                VBox.setMargin(empty, new Insets(20, 0, 20, 0));
                paymentsTableBody.getChildren().add(empty);
            }
            rs.close(); conn.close();
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
                info.setContentText(
                    "Status: " + status + "\nThe resident has not completed payment yet.");
                info.showAndWait();
            }
            loadPayments(searchField.getText().trim(), filterStatus.getValue());
            loadSummary();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updatePaymentStatus(String refNumber, String newStatus) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE payments SET status = ? WHERE ref_number = ?");
            stmt.setString(1, newStatus);
            stmt.setString(2, refNumber);
            stmt.executeUpdate();
            stmt.close(); conn.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void archivePayment(String refNumber) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE payments SET archived = True WHERE ref_number = ?");
            stmt.setString(1, refNumber);
            stmt.executeUpdate();
            stmt.close(); conn.close();
            loadPayments(searchField.getText().trim(), filterStatus.getValue());
            loadSummary();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void createTestPayment() {
        try {
            String result = PayMongoService.createPaymentLink("REQ-001", "Clearance", 10000);
            String[] parts = result.split("\\|");
            String checkoutUrl = parts[0];
            String linkId = parts[1];

            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO payments (payment_id, ref_number, resident_name, " +
                "payment_type, amount, status, date_created, archived) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, False)");
            stmt.setString(1, linkId);
            stmt.setString(2, "REQ-001");
            stmt.setString(3, "Maria Santos");
            stmt.setString(4, "Clearance");
            stmt.setDouble(5, 100.00);
            stmt.setString(6, "Pending");
            stmt.setString(7, LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            stmt.executeUpdate();
            stmt.close(); conn.close();

            loadPayments("", "All");
            loadSummary();

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

    // ── Notifications ─────────────────────────────────────────────────────────────
    private void cleanupNotifications() {
        String email = SessionManager.getEmail();
        if (email == null) return;
        try {
            Connection conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(true);
            PreparedStatement s1 = conn.prepareStatement(
                "DELETE FROM notifications WHERE type = 'announcement' " +
                "AND user_email = ? AND reference_id NOT IN " +
                "(SELECT announcement_id FROM announcements)");
            s1.setString(1, email); s1.executeUpdate(); s1.close();
            PreparedStatement s2 = conn.prepareStatement(
                "DELETE FROM notifications WHERE type = 'complaint' " +
                "AND user_email = ? AND reference_id NOT IN " +
                "(SELECT complaint_id FROM complaints WHERE status <> 'Resolved')");
            s2.setString(1, email); s2.executeUpdate(); s2.close();
            PreparedStatement s3 = conn.prepareStatement(
                "DELETE FROM notifications WHERE type = 'payment' " +
                "AND user_email = ? AND reference_id NOT IN " +
                "(SELECT ref_number FROM payments " +
                "WHERE status = 'Pending' AND archived = False)");
            s3.setString(1, email); s3.executeUpdate(); s3.close();
            conn.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void syncNotifications() {
        cleanupNotifications();
        String email = SessionManager.getEmail();
        if (email == null) return;
        try {
            Connection conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(true);
            ResultSet rs1 = conn.prepareStatement(
                "SELECT ref_number, resident_name FROM payments " +
                "WHERE status = 'Pending' AND archived = False"
            ).executeQuery();
            while (rs1.next()) {
                String refNo = rs1.getString("ref_number");
                insertIfNew(conn, "payment",
                    "Pending payment from " + rs1.getString("resident_name") +
                    " (" + refNo + ")", refNo, email);
            }
            rs1.close();
            ResultSet rs2 = conn.prepareStatement(
                "SELECT complaint_id, complainant_name, incident_type " +
                "FROM complaints WHERE status <> 'Resolved'"
            ).executeQuery();
            while (rs2.next()) {
                String cid = rs2.getString("complaint_id");
                insertIfNew(conn, "complaint",
                    "Open complaint: " + rs2.getString("incident_type") +
                    " by " + rs2.getString("complainant_name"), cid, email);
            }
            rs2.close();
            ResultSet rs3 = conn.prepareStatement(
                "SELECT announcement_id, title FROM announcements ORDER BY id DESC"
            ).executeQuery();
            int aCount = 0;
            while (rs3.next() && aCount < 5) {
                String aid = rs3.getString("announcement_id");
                insertIfNew(conn, "announcement",
                    "Announcement posted: " + rs3.getString("title"), aid, email);
                aCount++;
            }
            rs3.close();
            conn.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void insertIfNew(Connection conn, String type,
                              String message, String refId,
                              String email) throws Exception {
        PreparedStatement check = conn.prepareStatement(
            "SELECT notif_id FROM notifications " +
            "WHERE reference_id = ? AND user_email = ? AND type = ?");
        check.setString(1, refId); check.setString(2, email); check.setString(3, type);
        ResultSet rs = check.executeQuery();
        boolean exists = rs.next();
        rs.close(); check.close();
        if (!exists) {
            PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO notifications " +
                "(type, message, reference_id, is_read, created_at, user_email) " +
                "VALUES (?, ?, ?, 'false', ?, ?)");
            ins.setString(1, type); ins.setString(2, message);
            ins.setString(3, refId);
            ins.setString(4, LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ins.setString(5, email);
            ins.executeUpdate(); ins.close();
        }
    }

    private void markOneAsRead(String notifId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(true);
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE notifications SET is_read = 'true' WHERE notif_id = " + notifId);
            stmt.executeUpdate(); stmt.close(); conn.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void refreshAlertBadge() {
        String email = SessionManager.getEmail();
        if (email == null) return;
        try {
            Connection conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(true);
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM notifications " +
                "WHERE user_email = ? AND is_read = 'false'");
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            int count = rs.next() ? rs.getInt(1) : 0;
            rs.close(); stmt.close(); conn.close();
            if (count > 0) {
                alertBadge.setText(count > 99 ? "99+" : String.valueOf(count));
                alertBadge.setVisible(true);
            } else {
                alertBadge.setVisible(false);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Alerts Popup ──────────────────────────────────────────────────────────────
    @FXML
    private void handleAlertsClick() {
        Stage alertStage = new Stage();
        alertStage.initModality(Modality.APPLICATION_MODAL);
        alertStage.initOwner(logoutButton.getScene().getWindow());
        alertStage.setTitle("Notifications");
        alertStage.setResizable(false);

        VBox root = new VBox(0);
        root.setStyle(
            "-fx-background-color: #ffffff; -fx-min-width: 480; -fx-max-width: 480;");

        VBox header = new VBox(4);
        header.setFocusTraversable(true);
        header.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 20 24;");
        Label titleLbl = new Label("Notifications");
        titleLbl.setStyle(
            "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        Label subLbl = new Label("Click a notification to view and take action");
        subLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaaaaa;");
        header.getChildren().addAll(titleLbl, subLbl);

        HBox filterRow = new HBox(8);
        filterRow.setStyle(
            "-fx-padding: 12 24; -fx-background-color: #f8f9fa;" +
            "-fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0;" +
            "-fx-alignment: CENTER_LEFT;");

        final boolean[] showingPast = {false};

        Button unreadBtn = new Button("Unread");
        unreadBtn.setStyle(
            "-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff;" +
            "-fx-font-size: 11px; -fx-font-weight: bold;" +
            "-fx-background-radius: 20; -fx-padding: 5 14; -fx-cursor: hand;");
        Button pastBtn = new Button("Past Notifications");
        pastBtn.setStyle(
            "-fx-background-color: #f4f4f4; -fx-text-fill: #555555;" +
            "-fx-font-size: 11px; -fx-background-radius: 20;" +
            "-fx-border-color: #e0e0e0; -fx-border-width: 1;" +
            "-fx-padding: 5 14; -fx-cursor: hand;");
        Region filterSpacer = new Region();
        HBox.setHgrow(filterSpacer, Priority.ALWAYS);
        filterRow.getChildren().addAll(unreadBtn, pastBtn, filterSpacer);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(380);
        scrollPane.setStyle(
            "-fx-background-color: transparent; -fx-background: transparent;" +
            "-fx-border-color: transparent;");

        VBox notifBody = new VBox(0);
        notifBody.setStyle("-fx-background-color: #ffffff;");

        Runnable[] loadNotifsRef = {null};

        Runnable loadNotifs = () -> {
            notifBody.getChildren().clear();
            String email = SessionManager.getEmail();
            if (email == null) return;
            try {
                Connection conn = DatabaseConnection.getConnection();
                conn.setAutoCommit(true);
                String sql = showingPast[0]
                    ? "SELECT * FROM notifications WHERE user_email = '" + email +
                      "' ORDER BY notif_id DESC"
                    : "SELECT * FROM notifications WHERE user_email = '" + email +
                      "' AND is_read = 'false' ORDER BY notif_id DESC";
                ResultSet rs = conn.prepareStatement(sql).executeQuery();
                List<String[]> items = new ArrayList<>();
                while (rs.next()) {
                    items.add(new String[]{
                        rs.getString("notif_id"), rs.getString("type"),
                        rs.getString("message"), rs.getString("is_read"),
                        rs.getString("created_at")
                    });
                }
                rs.close(); conn.close();
                if (items.isEmpty()) {
                    VBox empty = new VBox(8);
                    empty.setStyle("-fx-alignment: CENTER; -fx-padding: 40;");
                    Label emptyLbl = new Label(showingPast[0]
                        ? "No past notifications." : "You're all caught up! 🎉");
                    emptyLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #aaaaaa;");
                    empty.getChildren().add(emptyLbl);
                    notifBody.getChildren().add(empty);
                } else {
                    for (String[] item : items)
                        notifBody.getChildren().add(
                            buildNotifItem(item, loadNotifsRef, showingPast, alertStage));
                }
            } catch (Exception e) { e.printStackTrace(); }
        };

        loadNotifsRef[0] = loadNotifs;
        loadNotifs.run();
        scrollPane.setContent(notifBody);

        unreadBtn.setOnAction(e -> {
            showingPast[0] = false;
            unreadBtn.setStyle(
                "-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff;" +
                "-fx-font-size: 11px; -fx-font-weight: bold;" +
                "-fx-background-radius: 20; -fx-padding: 5 14; -fx-cursor: hand;");
            pastBtn.setStyle(
                "-fx-background-color: #f4f4f4; -fx-text-fill: #555555;" +
                "-fx-font-size: 11px; -fx-background-radius: 20;" +
                "-fx-border-color: #e0e0e0; -fx-border-width: 1;" +
                "-fx-padding: 5 14; -fx-cursor: hand;");
            loadNotifs.run();
        });
        pastBtn.setOnAction(e -> {
            showingPast[0] = true;
            pastBtn.setStyle(
                "-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff;" +
                "-fx-font-size: 11px; -fx-font-weight: bold;" +
                "-fx-background-radius: 20; -fx-padding: 5 14; -fx-cursor: hand;");
            unreadBtn.setStyle(
                "-fx-background-color: #f4f4f4; -fx-text-fill: #555555;" +
                "-fx-font-size: 11px; -fx-background-radius: 20;" +
                "-fx-border-color: #e0e0e0; -fx-border-width: 1;" +
                "-fx-padding: 5 14; -fx-cursor: hand;");
            loadNotifs.run();
        });

        HBox footer = new HBox();
        footer.setStyle(
            "-fx-padding: 14 24; -fx-alignment: CENTER_RIGHT;" +
            "-fx-border-color: #f0f0f0; -fx-border-width: 1 0 0 0;");
        Button closeBtn = new Button("Close");
        closeBtn.setStyle(
            "-fx-background-color: #1a1a1a; -fx-text-fill: #ffffff;" +
            "-fx-font-size: 12px; -fx-font-weight: bold;" +
            "-fx-background-radius: 8; -fx-padding: 10 24; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> { refreshAlertBadge(); alertStage.close(); });
        footer.getChildren().add(closeBtn);

        root.getChildren().addAll(header, filterRow, scrollPane, footer);
        alertStage.setScene(new Scene(root));
        Platform.runLater(() -> root.requestFocus());
        alertStage.showAndWait();
        refreshAlertBadge();
    }

    private VBox buildNotifItem(String[] item, Runnable[] loadNotifsRef,
                                 boolean[] showingPast, Stage alertStage) {
        String notifId = item[0]; String type   = item[1];
        String message = item[2]; String isRead  = item[3];
        String dateStr = item[4];
        if (dateStr != null && dateStr.length() > 16) dateStr = dateStr.substring(0, 16);

        String icon, bg;
        if ("complaint".equals(type))    { icon = "📢"; bg = "#ffebee"; }
        else if ("payment".equals(type)) { icon = "💳"; bg = "#fff8e1"; }
        else                             { icon = "📣"; bg = "#e3f2fd"; }

        HBox row = new HBox(14);
        row.setStyle(
            "-fx-padding: 16 24; -fx-border-color: #f4f4f4; -fx-border-width: 0 0 1 0;" +
            ("false".equals(isRead)
                ? "-fx-background-color: #fafbff; -fx-cursor: hand;"
                : "-fx-background-color: #ffffff; -fx-cursor: hand;"));
        row.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBox = new StackPane();
        iconBox.setStyle(
            "-fx-background-color: " + bg + "; -fx-background-radius: 10;" +
            "-fx-min-width: 40; -fx-min-height: 40; -fx-max-width: 40; -fx-max-height: 40;");
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 16px;");
        iconBox.getChildren().add(iconLbl);

        VBox textBox = new VBox(4);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        Label msgLbl = new Label(message);
        msgLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #1a1a1a;" +
            ("false".equals(isRead) ? " -fx-font-weight: bold;" : ""));
        msgLbl.setWrapText(true);
        Label dateLbl = new Label(dateStr != null ? dateStr : "");
        dateLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #aaaaaa;");
        textBox.getChildren().addAll(msgLbl, dateLbl);

        if ("false".equals(isRead)) {
            Circle dot = new Circle(4);
            dot.setStyle("-fx-fill: #1565c0;");
            row.getChildren().addAll(iconBox, textBox, dot);
        } else {
            Label readBadge = new Label("Read");
            readBadge.setStyle(
                "-fx-background-color: #f4f4f4; -fx-text-fill: #aaaaaa;" +
                "-fx-font-size: 9px; -fx-background-radius: 20; -fx-padding: 2 8;");
            row.getChildren().addAll(iconBox, textBox, readBadge);
        }

        final String finalDateStr = dateStr;
        row.setOnMouseClicked(e ->
            showNotifDetail(notifId, type, message, finalDateStr,
                icon, bg, isRead, loadNotifsRef, alertStage));
        return new VBox(row);
    }

    private void showNotifDetail(String notifId, String type, String message,
                                  String dateStr, String icon, String bg,
                                  String isRead, Runnable[] loadNotifsRef,
                                  Stage alertStage) {
        Stage detail = new Stage();
        detail.initModality(Modality.APPLICATION_MODAL);
        detail.initOwner(alertStage);
        detail.setTitle("Notification");
        detail.setResizable(false);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #ffffff; -fx-min-width: 440;");

        VBox header = new VBox(6);
        header.setFocusTraversable(true);
        header.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 22 28;");
        Label titleLbl = new Label(
            "complaint".equals(type) ? "Complaint Alert" :
            "payment".equals(type)   ? "Payment Alert"   : "Announcement");
        titleLbl.setStyle(
            "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        Label dateLbl = new Label(dateStr != null ? dateStr : "");
        dateLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaaaaa;");
        header.getChildren().addAll(titleLbl, dateLbl);

        VBox body = new VBox(20);
        body.setStyle("-fx-padding: 28;");
        HBox iconRow = new HBox(16);
        iconRow.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBox = new StackPane();
        iconBox.setStyle(
            "-fx-background-color: " + bg + "; -fx-background-radius: 12;" +
            "-fx-min-width: 52; -fx-min-height: 52; -fx-max-width: 52; -fx-max-height: 52;");
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 22px;");
        iconBox.getChildren().add(iconLbl);
        Label msgLbl = new Label(message);
        msgLbl.setStyle(
            "-fx-font-size: 13px; -fx-text-fill: #1a1a1a; -fx-font-weight: bold;");
        msgLbl.setWrapText(true);
        HBox.setHgrow(msgLbl, Priority.ALWAYS);
        iconRow.getChildren().addAll(iconBox, msgLbl);
        body.getChildren().add(iconRow);

        String goToLabel =
            "complaint".equals(type) ? "→  Go to Complaints" :
            "payment".equals(type)   ? "→  Go to Payments"   :
                                       "→  Go to Announcements";
        String goToFxml =
            "complaint".equals(type) ? "Complaints.fxml" :
            "payment".equals(type)   ? "Payments.fxml"   :
                                       "Announcements.fxml";

        Button goToBtn = new Button(goToLabel);
        goToBtn.setMaxWidth(Double.MAX_VALUE);
        goToBtn.setStyle(
            "-fx-background-color: #f4f4f4; -fx-text-fill: #1a1a1a;" +
            "-fx-font-size: 12px; -fx-font-weight: bold;" +
            "-fx-background-radius: 8; -fx-border-color: #e0e0e0;" +
            "-fx-border-width: 1; -fx-padding: 11 20; -fx-cursor: hand;" +
            "-fx-alignment: CENTER_LEFT;");
        goToBtn.setOnAction(e -> {
            if ("false".equals(isRead)) markOneAsRead(notifId);
            detail.close(); alertStage.close();
            if (autoRefresh != null) autoRefresh.stop();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            SceneTransition.slideTo(stage, goToFxml, true, getClass());
        });
        body.getChildren().add(goToBtn);

        HBox footer = new HBox(10);
        footer.setStyle(
            "-fx-padding: 16 28 24 28; -fx-alignment: CENTER_RIGHT;" +
            "-fx-border-color: #f0f0f0; -fx-border-width: 1 0 0 0;");
        Button cancelBtn = new Button("Close");
        cancelBtn.setStyle(
            "-fx-background-color: #f4f4f4; -fx-text-fill: #555555;" +
            "-fx-font-size: 12px; -fx-background-radius: 8;" +
            "-fx-border-color: #e0e0e0; -fx-border-width: 1;" +
            "-fx-padding: 10 20; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> detail.close());

        if ("true".equals(isRead)) {
            footer.getChildren().add(cancelBtn);
        } else {
            Button markBtn = new Button("Mark as Read");
            markBtn.setStyle(
                "-fx-background-color: #1a1a1a; -fx-text-fill: #ffffff;" +
                "-fx-font-size: 12px; -fx-font-weight: bold;" +
                "-fx-background-radius: 8; -fx-padding: 10 24; -fx-cursor: hand;");
            markBtn.setOnAction(e -> {
                markOneAsRead(notifId);
                detail.close();
                if (loadNotifsRef[0] != null) loadNotifsRef[0].run();
                refreshAlertBadge();
            });
            footer.getChildren().addAll(cancelBtn, markBtn);
        }

        root.getChildren().addAll(header, body, footer);
        detail.setScene(new Scene(root));
        Platform.runLater(() -> root.requestFocus());
        detail.showAndWait();
    }

    // ── Avatar Click ──────────────────────────────────────────────────────────────
    @FXML
    private void handleAvatarClick() {
        if (autoRefresh != null) autoRefresh.stop();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Profile.fxml", true, getClass());
    }

    // ── Search / Filter ───────────────────────────────────────────────────────────
    @FXML
    private void handleSearch() {
        loadPayments(searchField.getText().trim(), filterStatus.getValue());
    }

    @FXML
    private void handleFilter() {
        loadPayments(searchField.getText().trim(), filterStatus.getValue());
    }

    // ── Navigation ────────────────────────────────────────────────────────────────
    @FXML private void goToDashboard() {
        if (autoRefresh != null) autoRefresh.stop();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "AdminDashboard.fxml", true, getClass());
    }
    @FXML private void goToResidents() {
        if (autoRefresh != null) autoRefresh.stop();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Residents.fxml", true, getClass());
    }
    @FXML private void goToDocuments() {
        if (autoRefresh != null) autoRefresh.stop();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Documents.fxml", true, getClass());
    }
    @FXML private void goToArchive() {
        if (autoRefresh != null) autoRefresh.stop();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "PaymentArchive.fxml", true, getClass());
    }
    @FXML private void goToComplaints() {
        if (autoRefresh != null) autoRefresh.stop();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Complaints.fxml", true, getClass());
    }
    @FXML private void goToAnnouncements() {
        if (autoRefresh != null) autoRefresh.stop();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Announcements.fxml", true, getClass());
    }
    @FXML private void goToFinances() {
        if (autoRefresh != null) autoRefresh.stop();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Finances.fxml", true, getClass());
    }
    @FXML private void goToAdmin() {
        if (autoRefresh != null) autoRefresh.stop();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Admin.fxml", true, getClass());
    }
    @FXML private void goToSettings() {
        if (autoRefresh != null) autoRefresh.stop();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Settings.fxml", true, getClass());
    }
    @FXML private void handleLogout() {
        if (autoRefresh != null) autoRefresh.stop();
        SessionManager.logout();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "login.fxml", false, getClass());
    }
}