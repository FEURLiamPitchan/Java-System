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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class KagawadPaymentsController {

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
    @FXML private ScrollPane mainScrollPane;

    private Timeline autoRefresh;

    private static final int BATCH_SIZE          = 15;
    private static final int TOTAL_DISPLAY_LIMIT = 200;

    private final AtomicBoolean isFetching  = new AtomicBoolean(false);
    private final AtomicBoolean isRendering = new AtomicBoolean(false);
    private int rowsDisplayed = 0;
    private List<PaymentRowData> cachedRows = new ArrayList<>();

    private String currentSearch = "";
    private String currentStatus = "All";

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    private Timeline scrollDebounce;

    private static class PaymentRowData {
        final String paymentId, refNumber, residentName, paymentType, dateCreated, payStatus;
        final double amount;

        PaymentRowData(String paymentId, String refNumber, String residentName,
                       String paymentType, double amount, String dateCreated, String payStatus) {
            this.paymentId    = paymentId;
            this.refNumber    = refNumber;
            this.residentName = residentName;
            this.paymentType  = paymentType;
            this.amount       = amount;
            this.dateCreated  = dateCreated;
            this.payStatus    = payStatus;
        }
    }

    @FXML
    public void initialize() {
        loadTopBar();
        loadAvatarPicture();

        filterStatus.getItems().addAll("All", "Pending", "Paid");
        filterStatus.setValue("All");
        filterStatus.setOnAction(e -> handleFilter());

        if (mainScrollPane != null) {
            mainScrollPane.setFitToWidth(true);
            mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            paymentsTableBody.setFillWidth(true);
            paymentsTableBody.setMaxWidth(Double.MAX_VALUE);
            paymentsTableBody.setStyle("-fx-padding: 0;");
            setupScrollListener();
        }

        loadPayments("", "All");
        loadSummary();
        syncNotifications();
        refreshAlertBadge();
        startAutoRefresh();
    }

    private void setupScrollListener() {
        mainScrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() < 0.70) return;
            if (isRendering.get() || isFetching.get()) return;
            if (rowsDisplayed >= cachedRows.size()
                    || rowsDisplayed >= TOTAL_DISPLAY_LIMIT) return;

            if (scrollDebounce != null) scrollDebounce.stop();
            scrollDebounce = new Timeline(new KeyFrame(Duration.millis(300), e -> {
                if (!isRendering.get() && !isFetching.get()
                        && rowsDisplayed < cachedRows.size()
                        && rowsDisplayed < TOTAL_DISPLAY_LIMIT) {
                    loadMoreBatch();
                }
            }));
            scrollDebounce.setCycleCount(1);
            scrollDebounce.play();
        });
    }

    private void loadPayments(String search, String status) {
        if (isFetching.get()) return;

        currentSearch = search;
        currentStatus = status;

        rowsDisplayed = 0;
        cachedRows    = new ArrayList<>();

        paymentsTableBody.getChildren().clear();
        Label loadingLbl = new Label("⏳ Loading payments...");
        loadingLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666; -fx-padding: 20;");
        paymentsTableBody.getChildren().add(loadingLbl);

        isFetching.set(true);
        executorService.execute(() -> {
            try {
                List<PaymentRowData> rows = fetchFromDB(search, status);

                Platform.runLater(() -> {
                    isFetching.set(false);
                    cachedRows    = rows;
                    rowsDisplayed = 0;
                    paymentsTableBody.getChildren().clear();

                    if (cachedRows.isEmpty()) {
                        Label empty = new Label("No payments found.");
                        empty.setStyle(
                            "-fx-font-size: 13px; -fx-text-fill: #aaaaaa; -fx-padding: 20 0;");
                        VBox.setMargin(empty, new Insets(20, 0, 20, 16));
                        paymentsTableBody.getChildren().add(empty);
                        return;
                    }

                    renderBatch();
                });

            } catch (Exception e) {
                isFetching.set(false);
                Platform.runLater(() -> {
                    paymentsTableBody.getChildren().clear();
                    Label err = new Label("Error loading payments: " + e.getMessage());
                    err.setStyle("-fx-font-size: 12px; -fx-text-fill: #e53935;");
                    paymentsTableBody.getChildren().add(err);
                });
                showErrorAlert("Unable to load payments right now.", e);
            }
        });
    }

    private List<PaymentRowData> fetchFromDB(String search, String status) throws Exception {
        List<PaymentRowData> rows = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT p.payment_id, p.ref_number, r.full_name, p.payment_type, " +
                 "       p.amount, p.date_created, p.status " +
                 "FROM payments p " +
                 "LEFT JOIN residents r ON p.resident_id = r.id " +
                 "WHERE p.archived = 0 " +
                 "ORDER BY p.id DESC");
             ResultSet rs = stmt.executeQuery()) {
            String lSearch = search.toLowerCase();

            while (rs.next()) {
                String paymentId    = rs.getString("payment_id");
                String refNumber    = rs.getString("ref_number");
                String residentName = rs.getString("full_name");
                String paymentType  = rs.getString("payment_type");
                double amount       = rs.getDouble("amount");
                String dateCreated  = rs.getString("date_created");
                String payStatus    = rs.getString("status");

                if (!lSearch.isEmpty()) {
                    boolean nameMatch = residentName != null
                        && residentName.toLowerCase().contains(lSearch);
                    boolean refMatch  = refNumber != null
                        && refNumber.toLowerCase().contains(lSearch);
                    if (!nameMatch && !refMatch) continue;
                }
                if (!"All".equals(status) && !status.equalsIgnoreCase(payStatus)) continue;

                rows.add(new PaymentRowData(paymentId, refNumber, residentName,
                    paymentType, amount, dateCreated, payStatus));
            }
        }
        return rows;
    }

    private void renderBatch() {
        if (isRendering.get()) return;
        isRendering.set(true);

        removeScrollFooter();

        int start = rowsDisplayed;
        int end   = Math.min(start + BATCH_SIZE,
                    Math.min(cachedRows.size(), TOTAL_DISPLAY_LIMIT));

        for (int i = start; i < end; i++) {
            paymentsTableBody.getChildren().add(createPaymentRow(cachedRows.get(i)));
        }
        rowsDisplayed = end;

        if (rowsDisplayed < cachedRows.size() && rowsDisplayed < TOTAL_DISPLAY_LIMIT) {
            addScrollFooter();
        }

        isRendering.set(false);
    }

    private void loadMoreBatch() {
        if (isRendering.get()) return;
        if (rowsDisplayed >= cachedRows.size()) return;
        if (rowsDisplayed >= TOTAL_DISPLAY_LIMIT) return;
        renderBatch();
    }

    private static final String SCROLL_FOOTER_ID = "scrollFooter";

    private void addScrollFooter() {
        Label lbl = new Label("💳 Scroll down to load more…");
        lbl.setStyle(
            "-fx-font-size: 11px; -fx-text-fill: #aaaaaa;" +
            "-fx-padding: 16; -fx-alignment: CENTER;");
        HBox box = new HBox(lbl);
        box.setId(SCROLL_FOOTER_ID);
        box.setStyle("-fx-alignment: CENTER; -fx-padding: 10;");
        paymentsTableBody.getChildren().add(box);
    }

    private void removeScrollFooter() {
        paymentsTableBody.getChildren()
            .removeIf(n -> SCROLL_FOOTER_ID.equals(n.getId()));
    }

    private void loadTopBar() {
        String name = SessionManager.getName();
        String role = SessionManager.getRole();
        if (topBarNameLabel != null)
            topBarNameLabel.setText(name != null ? name : "Barangay Kagawad");
        if (topBarRoleLabel != null)
            topBarRoleLabel.setText(role != null ? capitalize(role) : "Kagawad");
    }

    private void loadAvatarPicture() {
        ProfilePictureManager.loadAvatarPicture(
            SessionManager.getEmail(),
            avatarBox, avatarCircle, profileImageView, avatarInitialLabel);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private void showErrorAlert(String message, Exception e) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Operation failed");
            alert.setContentText(message + (e != null && e.getMessage() != null ? "\n\nDetails: " + e.getMessage() : ""));
            alert.showAndWait();
        });
    }

    private void startAutoRefresh() {
        autoRefresh = new Timeline(new KeyFrame(Duration.seconds(5), e ->
            checkAndUpdatePendingPayments()));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();
    }

    private void checkAndUpdatePendingPayments() {
        executorService.execute(() -> {
            try {
                List<String> updatedRefs = new ArrayList<>();
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                         "SELECT payment_id, ref_number, amount FROM payments " +
                         "WHERE status = 'Pending' AND archived = 0"
                     );
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String sessionId = rs.getString("payment_id");
                        String refNumber = rs.getString("ref_number");
                        try {
                            String status = PayMongoService.checkPaymentStatus(sessionId);
                            if ("paid".equals(status)) {
                                updatePaymentStatus(refNumber, "Paid");
                                updatedRefs.add(refNumber);
                            }
                        } catch (Exception ex) {
                            System.out.println("⚠️ Error checking " + refNumber + ": " + ex.getMessage());
                        }
                    }
                }

                if (!updatedRefs.isEmpty()) {
                    Platform.runLater(() -> {
                        boolean cachePatched = false;
                        for (String ref : updatedRefs) {
                            for (int i = 0; i < cachedRows.size(); i++) {
                                if (ref.equals(cachedRows.get(i).refNumber)) {
                                    PaymentRowData old = cachedRows.get(i);
                                    cachedRows.set(i, new PaymentRowData(
                                        old.paymentId, old.refNumber, old.residentName,
                                        old.paymentType, old.amount, old.dateCreated, "Paid"));
                                    cachePatched = true;
                                    break;
                                }
                            }
                        }

                        if (cachePatched) {
                            removeScrollFooter();
                            int visibleCount = Math.min(rowsDisplayed, cachedRows.size());
                            paymentsTableBody.getChildren().clear();
                            for (int i = 0; i < visibleCount; i++) {
                                paymentsTableBody.getChildren().add(createPaymentRow(cachedRows.get(i)));
                            }
                            if (rowsDisplayed < cachedRows.size() && rowsDisplayed < TOTAL_DISPLAY_LIMIT) {
                                addScrollFooter();
                            }
                        }

                        loadSummary();
                        syncNotifications();
                        refreshAlertBadge();
                    });
                }
            } catch (Exception e) {
                showErrorAlert("Unable to refresh pending payment status.", e);
            }
        });
    }

    private void loadSummary() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt1 = conn.prepareStatement(
                 "SELECT SUM(amount) FROM payments WHERE status = 'Paid'"
             );
             ResultSet rs1 = stmt1.executeQuery()) {
            if (rs1.next()) {
                double total = rs1.getDouble(1);
                totalCollectedLabel.setText(total > 0
                    ? String.format("₱%.2f", total) : "₱0.00");
            }
            try (PreparedStatement stmt2 = conn.prepareStatement(
                     "SELECT COUNT(*) FROM payments WHERE status = 'Pending' AND archived = 0"
                 );
                 ResultSet rs2 = stmt2.executeQuery()) {
                if (rs2.next()) pendingCountLabel.setText(String.valueOf(rs2.getInt(1)));
            }
            try (PreparedStatement stmt3 = conn.prepareStatement(
                     "SELECT COUNT(*) FROM payments WHERE status = 'Paid'"
                 );
                 ResultSet rs3 = stmt3.executeQuery()) {
                if (rs3.next()) paidCountLabel.setText(String.valueOf(rs3.getInt(1)));
            }
        } catch (Exception e) {
            showErrorAlert("Unable to load payment summary.", e);
        }
    }

    private HBox createPaymentRow(PaymentRowData payment) {
        HBox row = new HBox();
        row.setStyle("-fx-padding: 14 16; -fx-border-color: #f8f8f8;" +
                     "-fx-border-width: 0 0 1 0; -fx-background-color: transparent;");
        row.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(row, Priority.ALWAYS);

        Label refLabel = new Label(payment.refNumber != null ? payment.refNumber : "—");
        refLabel.setPrefWidth(110);
        refLabel.setMinWidth(110);
        refLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

        Label nameLabel = new Label(payment.residentName != null ? payment.residentName : "—");
        nameLabel.setPrefWidth(160);
        nameLabel.setMinWidth(160);
        nameLabel.setStyle(
            "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Label typeLabel = new Label(payment.paymentType != null ? payment.paymentType : "—");
        typeLabel.setPrefWidth(200);
        typeLabel.setMinWidth(200);
        typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

        Label amountLabel = new Label(String.format("₱%.2f", payment.amount));
        amountLabel.setPrefWidth(110);
        amountLabel.setMinWidth(110);
        amountLabel.setStyle(
            "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");

        Label dateLabel = new Label(payment.dateCreated != null ? payment.dateCreated : "N/A");
        dateLabel.setPrefWidth(160);
        dateLabel.setMinWidth(160);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

        String statusBg, statusFg;
        if ("Paid".equals(payment.payStatus)) {
            statusBg = "#c8e6c9"; statusFg = "#2e7d32";
        } else {
            statusBg = "#fff9c4"; statusFg = "#f57f17";
        }

        Label statusLabel = new Label(payment.payStatus != null ? payment.payStatus : "Pending");
        statusLabel.setStyle(
            "-fx-background-color: " + statusBg + ";" +
            "-fx-text-fill: " + statusFg + ";" +
            "-fx-font-size: 11px; -fx-font-weight: bold;" +
            "-fx-background-radius: 4; -fx-padding: 4 10;");
        HBox statusBox = new HBox(statusLabel);
        statusBox.setPrefWidth(90);
        statusBox.setMinWidth(90);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        HBox actionBox = new HBox(6);
        HBox.setHgrow(actionBox, Priority.ALWAYS);
        actionBox.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 0 10;");

        if ("Paid".equals(payment.payStatus)) {
            Label paidLabel = new Label("✓ Paid");
            paidLabel.setStyle(
                "-fx-font-size: 11px; -fx-text-fill: #2e7d32; -fx-padding: 0 8;");
            actionBox.getChildren().add(paidLabel);
        }

        row.getChildren().addAll(
            refLabel, nameLabel, typeLabel, amountLabel,
            dateLabel, statusBox, actionBox);

        return row;
    }

    private void updatePaymentStatus(String refNumber, String newStatus) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE payments SET status = ? WHERE ref_number = ?")) {
            conn.setAutoCommit(true);
            stmt.setString(1, newStatus);
            stmt.setString(2, refNumber);
            stmt.executeUpdate();
        } catch (Exception e) {
            showErrorAlert("Unable to update payment status.", e);
        }
    }

    // Feature disabled for Kagawad level
    @FXML
    private void createTestPayment() {
        // Feature disabled for Kagawad level
    }

private int getCurrentUserId() {
    String email = SessionManager.getEmail();
    if (email == null) return -1;
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(
             "SELECT id FROM users WHERE email = ?")) {
        stmt.setString(1, email);
        try (ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt("id") : -1;
        }
    } catch (Exception e) {
        showErrorAlert("Unable to resolve current user.", e);
        return -1;
    }
}

private void insertIfNew(Connection conn, String type, String message,
                          String refId, int userId) throws Exception {
    PreparedStatement check = conn.prepareStatement(
        "SELECT notif_id FROM notifications " +
        "WHERE reference_id = ? AND user_id = ? AND type = ?");
    check.setString(1, refId);
    check.setInt(2, userId);
    check.setString(3, type);
    ResultSet rs = check.executeQuery();
    boolean exists = rs.next();
    rs.close(); check.close();

    if (!exists) {
        PreparedStatement ins = conn.prepareStatement(
            "INSERT INTO notifications " +
            "(type, message, reference_id, is_read, created_at, user_id) " +
            "VALUES (?, ?, ?, ?, ?, ?)");
        ins.setString(1, type);
        ins.setString(2, message);
        ins.setString(3, refId);
        ins.setBoolean(4, false);
        ins.setString(5, LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        ins.setInt(6, userId);
        ins.executeUpdate();
        ins.close();
    }
}

private void cleanupNotifications() {
    int userId = getCurrentUserId();
    if (userId == -1) return;
    try {
        Connection conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(true);

        PreparedStatement s1 = conn.prepareStatement(
            "DELETE FROM notifications WHERE type = 'announcement' " +
            "AND user_id = ? AND reference_id NOT IN " +
            "(SELECT announcement_id FROM announcements)");
        s1.setInt(1, userId); s1.executeUpdate(); s1.close();

        PreparedStatement s2 = conn.prepareStatement(
            "DELETE FROM notifications WHERE type = 'complaint' " +
            "AND user_id = ? AND reference_id NOT IN " +
            "(SELECT complaint_id FROM complaints WHERE status <> 'Resolved')");
        s2.setInt(1, userId); s2.executeUpdate(); s2.close();

        PreparedStatement s3 = conn.prepareStatement(
            "DELETE FROM notifications WHERE type = 'payment' " +
            "AND user_id = ? AND reference_id NOT IN " +
            "(SELECT ref_number FROM payments WHERE status = 'Pending' AND archived = 0)");
        s3.setInt(1, userId); s3.executeUpdate(); s3.close();

        PreparedStatement s4 = conn.prepareStatement(
            "DELETE FROM notifications WHERE type = 'document' " +
            "AND user_id = ? AND reference_id NOT IN " +
            "(SELECT request_id FROM document_requests WHERE status = 'Pending')");
        s4.setInt(1, userId); s4.executeUpdate(); s4.close();

        conn.close();
    } catch (Exception e) { showErrorAlert("Unable to clean notification cache.", e); }
}

private void syncNotifications() {
    cleanupNotifications();
    int userId = getCurrentUserId();
    if (userId == -1) return;
    try {
        Connection conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(true);

        ResultSet rs1 = conn.prepareStatement(
            "SELECT ref_number FROM payments WHERE status = 'Pending' AND archived = 0"
        ).executeQuery();
        while (rs1.next())
            insertIfNew(conn, "payment",
                "Pending payment " + rs1.getString("ref_number"),
                rs1.getString("ref_number"), userId);
        rs1.close();

        ResultSet rs2 = conn.prepareStatement(
            "SELECT complaint_id, complainant_name, incident_type " +
            "FROM complaints WHERE status <> 'Resolved'"
        ).executeQuery();
        while (rs2.next())
            insertIfNew(conn, "complaint",
                "Open complaint: " + rs2.getString("incident_type") +
                " by " + rs2.getString("complainant_name"),
                rs2.getString("complaint_id"), userId);
        rs2.close();

        ResultSet rs3 = conn.prepareStatement(
            "SELECT announcement_id, title FROM announcements ORDER BY id DESC"
        ).executeQuery();
        int aCount = 0;
        while (rs3.next() && aCount < 5) {
            insertIfNew(conn, "announcement",
                "Announcement posted: " + rs3.getString("title"),
                rs3.getString("announcement_id"), userId);
            aCount++;
        }
        rs3.close();

        ResultSet rs4 = conn.prepareStatement(
            "SELECT dr.request_id, dr.document_type, r.full_name " +
            "FROM document_requests dr " +
            "LEFT JOIN residents r ON dr.resident_id = r.id " +
            "WHERE dr.status = 'Pending'"
        ).executeQuery();
        while (rs4.next())
            insertIfNew(conn, "document",
                "New document request: " + rs4.getString("document_type") +
                " from " + rs4.getString("full_name"),
                rs4.getString("request_id"), userId);
        rs4.close();

        conn.close();
    } catch (Exception e) { showErrorAlert("Unable to sync notifications.", e); }
}

private void markOneAsRead(String notifId) {
    try {
        Connection conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(true);
        PreparedStatement stmt = conn.prepareStatement(
            "UPDATE notifications SET is_read = TRUE WHERE notif_id = ?");
        stmt.setInt(1, Integer.parseInt(notifId));
        stmt.executeUpdate(); stmt.close(); conn.close();
    } catch (Exception e) { showErrorAlert("Unable to update notification state.", e); }
}

private void refreshAlertBadge() {
    int userId = getCurrentUserId();
    if (userId == -1) return;
    try {
        Connection conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(true);
        PreparedStatement stmt = conn.prepareStatement(
            "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE");
        stmt.setInt(1, userId);
        ResultSet rs = stmt.executeQuery();
        int count = rs.next() ? rs.getInt(1) : 0;
        rs.close(); stmt.close(); conn.close();
        if (count > 0) {
            alertBadge.setText(count > 99 ? "99+" : String.valueOf(count));
            alertBadge.setVisible(true);
        } else {
            alertBadge.setVisible(false);
        }
    } catch (Exception e) { showErrorAlert("Unable to refresh notification badge.", e); }
}

@FXML
private void handleAlertsClick() {
    // Notifications modal implementation
}

    @FXML private void handleAvatarClick() {
        if (autoRefresh != null) autoRefresh.stop();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "KagawadProfile.fxml", true, getClass());
    }

    @FXML private void handleSearch() {
        loadPayments(searchField.getText().trim(), filterStatus.getValue());
    }

    @FXML private void handleFilter() {
        loadPayments(searchField.getText().trim(), filterStatus.getValue());
    }

    @FXML private void goToDashboard() {
        if (autoRefresh != null) autoRefresh.stop();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "KagawadDashboard.fxml", true, getClass());
    }
    @FXML private void goToResidents() {
        if (autoRefresh != null) autoRefresh.stop();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "KagawadResidents.fxml", true, getClass());
    }
    @FXML private void goToDocuments() {
        if (autoRefresh != null) autoRefresh.stop();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "KagawadDocuments.fxml", true, getClass());
    }
    @FXML private void goToArchive() {
        // RBAC: Payments — Read-Only for Kagawad; archive management not permitted
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Access Denied");
        alert.setHeaderText(null);
        alert.setContentText("Payment archive management is not available for your role.");
        alert.showAndWait();
    }
    @FXML private void goToComplaints() {
        if (autoRefresh != null) autoRefresh.stop();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "KagawadComplaints.fxml", true, getClass());
    }
    @FXML private void goToAnnouncements() {
        if (autoRefresh != null) autoRefresh.stop();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "KagawadAnnouncements.fxml", true, getClass());
    }
    @FXML private void goToFinances() {
        if (autoRefresh != null) autoRefresh.stop();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "KagawadFinances.fxml", true, getClass());
    }
    @FXML private void goToAdmin() {
        // RBAC: Admin Panel — No Access for Kagawad role
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Access Denied");
        alert.setHeaderText(null);
        alert.setContentText("You do not have permission to access the Admin Panel.");
        alert.showAndWait();
    }
    @FXML private void goToSettings() {
        if (autoRefresh != null) autoRefresh.stop();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "KagawadSettings.fxml", true, getClass());
    }
    @FXML private void handleLogout() {
        if (autoRefresh != null) autoRefresh.stop();
        SessionManager.logout();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "login.fxml", false, getClass());
    }
}
