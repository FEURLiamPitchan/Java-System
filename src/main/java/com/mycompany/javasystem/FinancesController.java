package com.mycompany.javasystem;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FinancesController {

    @FXML private TableView<String[]> transactionTable;
    @FXML private TableColumn<String[], String> colDate;
    @FXML private TableColumn<String[], String> colResident;
    @FXML private TableColumn<String[], String> colCategory;
    @FXML private TableColumn<String[], String> colType;
    @FXML private TableColumn<String[], String> colAmount;
    @FXML private TableColumn<String[], String> colPaymentMethod;
    @FXML private TableColumn<String[], String> colStatus;
    @FXML private TableColumn<String[], String> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterType;
    @FXML private ComboBox<String> filterStatus;
    @FXML private DatePicker filterDateFrom;
    @FXML private DatePicker filterDateTo;

    @FXML private Label totalCollectionsLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private Label netBalanceLabel;
    @FXML private Label pendingLabel;
    @FXML private Label recordCountLabel;
    @FXML private Button logoutButton;
    @FXML private Button alertsButton;
    @FXML private HBox avatarBox;
    @FXML private Circle avatarCircle;
    @FXML private ImageView profileImageView;
    @FXML private Label avatarInitialLabel;
    @FXML private Label topBarNameLabel;
    @FXML private Label topBarRoleLabel;
    @FXML private Label alertBadge;

    @FXML private BarChart<String, Number> barChart;
    @FXML private PieChart pieChart;
    @FXML private ScrollPane mainScrollPane;
    @FXML private BorderPane rootPane;

    // ✅ Store pie chart data globally for tooltip access
    private Map<String, double[]> pieChartDataMap = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        filterType.getItems().addAll("All", "Income", "Expense");
        filterType.setValue("All");
        filterStatus.getItems().addAll("All", "Paid", "Pending");
        filterStatus.setValue("All");

        loadTopBar();
        loadAvatarPicture();
        setupTableColumns();
        transactionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        loadSummary();
        loadTransactions();
        loadBarChart();
        loadPieChart();
        syncNotifications();
        refreshAlertBadge();

        barChart.setCache(true);
        barChart.setCacheHint(CacheHint.SPEED);
        pieChart.setCache(true);
        pieChart.setCacheHint(CacheHint.SPEED);

        // Apply theme with delay to allow nodes to render
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                if (stage != null && stage.getScene() != null) {
                    System.out.println("[FinancesController] Applying theme - isDarkMode: " + ThemeManager.isDarkMode);
                    ThemeManager.applyTheme(stage);

                    // Apply dark mode specific fixes after a small delay
                    if (ThemeManager.isDarkMode) {
                        Platform.runLater(() -> {
                            applyThemeToRoot();
                            applyDarkModeOverrides();
                        });
                    }
                }
            } catch (Exception e) {
                System.out.println("[FinancesController] Error applying theme: " + e.getMessage());
            }
        });

        Platform.runLater(() -> {
            if (mainScrollPane != null) {
                mainScrollPane.getContent().setOnScroll(event -> {
                    double deltaY = event.getDeltaY() * 8;
                    double contentHeight = mainScrollPane.getContent()
                        .getBoundsInLocal().getHeight();
                    double viewportHeight = mainScrollPane.getViewportBounds().getHeight();
                    double scrollableHeight = contentHeight - viewportHeight;
                    if (scrollableHeight > 0) {
                        double delta = deltaY / scrollableHeight;
                        mainScrollPane.setVvalue(mainScrollPane.getVvalue() - delta);
                    }
                });
            }
        });
    }

    // ✅ Apply theme colors to entire page - DARK MODE (SIMPLIFIED)
    private void applyThemeToRoot() {
        if (!ThemeManager.isDarkMode) return;
        
        Platform.runLater(() -> {
            try {
                // Just set the background colors - NO node manipulation
                if (rootPane != null) {
                    rootPane.setStyle("-fx-background-color: #0d0d0d;");
                }
                
                if (mainScrollPane != null) {
                    mainScrollPane.setStyle(
                        "-fx-background-color: #0d0d0d;" +
                        "-fx-background: #0d0d0d;" +
                        "-fx-border-color: transparent;");
                }
                
            } catch (Exception e) {
                System.out.println("[FinancesController] Error applying theme to root: " + e.getMessage());
            }
        });
    }

    // ✅ Apply dark mode specific overrides - CHARTS & TABLES ONLY
    private void applyDarkModeOverrides() {
        try {
            // Fix stat boxes with emoji colors
            fixStatBox(totalCollectionsLabel);
            fixStatBox(totalExpensesLabel);
            fixStatBox(netBalanceLabel);
            fixStatBox(pendingLabel);

            // ✅ Fix Bar Chart with aggressive styling
            if (barChart != null) {
                barChart.setStyle(
                    "-fx-background-color: #1a1a1a;" +
                    "-fx-text-fill: #e8e8e8;");

                // Style chart plot background
                for (Node node : barChart.lookupAll(".chart-plot-background")) {
                    node.setStyle(
                        "-fx-background-color: #1a1a1a !important;" +
                        "-fx-background-radius: 8;");
                }

                // ✅ Style chart legend
                for (Node node : barChart.lookupAll(".chart-legend")) {
                    node.setStyle(
                        "-fx-background-color: #1a1a1a !important;" +
                        "-fx-text-fill: #e8e8e8 !important;");
                }

                // ✅ Style legend items
                for (Node node : barChart.lookupAll(".chart-legend-item")) {
                    node.setStyle(
                        "-fx-text-fill: #e8e8e8 !important;" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-color: transparent !important;");
                }

                // ✅ Fix legend item symbols
                Platform.runLater(() -> {
                    int index = 0;
                    for (Node node : barChart.lookupAll(".chart-legend-item-symbol")) {
                        if (index == 0) {
                            node.setStyle("-fx-background-color: #43a047 !important;");
                        } else {
                            node.setStyle("-fx-background-color: #e53935 !important;");
                        }
                        index++;
                    }
                });

                // Style axis labels
                for (Node node : barChart.lookupAll(".axis-label")) {
                    node.setStyle("-fx-text-fill: #b0b0b0 !important;");
                }

                // Style tick labels
                for (Node node : barChart.lookupAll(".axis")) {
                    node.setStyle("-fx-tick-label-fill: #b0b0b0 !important;");
                }

                // ✅ FORCE bar colors
                Platform.runLater(() -> {
                    for (XYChart.Series<String, Number> series : barChart.getData()) {
                        for (XYChart.Data<String, Number> data : series.getData()) {
                            Node node = data.getNode();
                            if (node != null) {
                                if (series.getName().equals("Income")) {
                                    node.setStyle("-fx-bar-fill: #43a047 !important;");
                                } else if (series.getName().equals("Expenses")) {
                                    node.setStyle("-fx-bar-fill: #e53935 !important;");
                                }
                            }
                        }
                    }
                });
            }

            // ✅ Fix Pie Chart
            if (pieChart != null) {
                pieChart.setStyle(
                    "-fx-background-color: #1a1a1a;" +
                    "-fx-text-fill: #e8e8e8;");

                for (Node node : pieChart.lookupAll(".chart-plot-background")) {
                    node.setStyle(
                        "-fx-background-color: #1a1a1a !important;" +
                        "-fx-background-radius: 8;");
                }

                for (Node node : pieChart.lookupAll(".chart-legend")) {
                    node.setStyle(
                        "-fx-background-color: #1a1a1a !important;" +
                        "-fx-text-fill: #e8e8e8 !important;");
                }

                for (Node node : pieChart.lookupAll(".chart-legend-item")) {
                    node.setStyle(
                        "-fx-text-fill: #e8e8e8 !important;" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-color: transparent !important;");
                }

                for (Node node : pieChart.lookupAll(".chart-pie-label")) {
                    node.setStyle("-fx-fill: #b0b0b0 !important;");
                }
            }

            // ✅ Fix Table
            if (transactionTable != null) {
                transactionTable.setStyle(
                    "-fx-background-color: #1e1e1e;" +
                    "-fx-control-inner-background: #1e1e1e;" +
                    "-fx-text-fill: #e8e8e8;");

                for (Node node : transactionTable.lookupAll(".column-header-background")) {
                    node.setStyle(
                        "-fx-background-color: #2a2a2a !important;" +
                        "-fx-border-color: #404040;");
                }

                for (Node node : transactionTable.lookupAll(".column-header")) {
                    node.setStyle(
                        "-fx-background-color: #2a2a2a !important;" +
                        "-fx-border-color: #404040 !important;" +
                        "-fx-text-fill: #e8e8e8;");
                }

                for (Node node : transactionTable.lookupAll(".column-header .label")) {
                    node.setStyle(
                        "-fx-text-fill: #e8e8e8 !important;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;");
                }

                for (Node node : transactionTable.lookupAll(".table-cell")) {
                    node.setStyle(
                        "-fx-background-color: #1e1e1e !important;" +
                        "-fx-text-fill: #e8e8e8 !important;" +
                        "-fx-border-color: #404040;");
                }

                for (Node node : transactionTable.lookupAll(".table-row-cell")) {
                    node.setStyle(
                        "-fx-background-color: #1e1e1e !important;" +
                        "-fx-text-fill: #e8e8e8 !important;" +
                        "-fx-border-color: transparent transparent #404040 transparent;" +
                        "-fx-border-width: 0 0 1 0;");
                }

                for (Node node : transactionTable.lookupAll(".filler")) {
                    node.setStyle("-fx-background-color: #2a2a2a !important;");
                }
            }

        } catch (Exception e) {
            System.out.println("[FinancesController] Error applying dark mode overrides: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ✅ Helper method to fix stat box backgrounds
    private void fixStatBox(Label label) {
        if (label != null && label.getParent() != null) {
            Node parent = label.getParent();
            while (parent != null && !(parent instanceof Region)) {
                parent = parent.getParent();
            }
            if (parent instanceof Region) {
                if (ThemeManager.isDarkMode) {
                    ((Region) parent).setStyle(
                        "-fx-background-color: #1a1a1a;" +
                        "-fx-text-fill: #e8e8e8;" +
                        "-fx-padding: 20;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #2a2a2a;" +
                        "-fx-border-width: 1;");
                    
                    label.setStyle(
                        "-fx-text-fill: #ffffff !important;" +
                        "-fx-font-size: 30px; -fx-font-weight: bold;");
                    
                    for (Node node : parent.lookupAll(".label")) {
                        if (node instanceof Label && node != label) {
                            Label lbl = (Label) node;
                            String text = lbl.getText();
                            // Force emojis to BLACK
                            if (text != null && text.matches(".*[💰📉⚖️⏳].*")) {
                                lbl.setStyle("-fx-text-fill: #000000 !important; -fx-font-size: 16px;");
                            } else {
                                String currentStyle = lbl.getStyle();
                                if (currentStyle == null || currentStyle.isEmpty()) {
                                    lbl.setStyle("-fx-text-fill: #e8e8e8 !important;");
                                } else {
                                    lbl.setStyle(currentStyle + "; -fx-text-fill: #e8e8e8 !important;");
                                }
                            }
                        }
                    }
                } else {
                    ((Region) parent).setStyle(
                        "-fx-background-color: #ffffff;" +
                        "-fx-text-fill: #333333;" +
                        "-fx-padding: 20;" +
                        "-fx-background-radius: 12;");
                }
            }
        }
    }
    // ── TOP BAR ───────────────────────────────────────────────────────────────────
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

    // ── AVATAR CLICK ───────────────────────────────────────────────────────────────
    @FXML
    private void handleAvatarClick() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Profile.fxml", true, getClass());
    }

    // ── NOTIFICATIONS ─────────────────────────────────────────────────────────────
    private void syncNotifications() {
        String email = SessionManager.getEmail();
        if (email == null) {
            System.out.println("[Sync] No email in session");
            return;
        }
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(true);

            // ✅ Get user ID from email
            PreparedStatement getUserStmt = conn.prepareStatement("SELECT id FROM users WHERE email = ?");
            getUserStmt.setString(1, email);
            ResultSet userRs = getUserStmt.executeQuery();
            int userId = -1;
            if (userRs.next()) userId = userRs.getInt("id");
            userRs.close();
            getUserStmt.close();

            System.out.println("[Sync] User ID for " + email + ": " + userId);

            if (userId == -1) {
                conn.close();
                System.out.println("[Sync] User not found!");
                return;
            }

            // ✅ Delete old notifications (cleanup)
            PreparedStatement deleteOld = conn.prepareStatement(
                "DELETE FROM notifications WHERE user_id = ? AND created_at < DATE_SUB(NOW(), INTERVAL 7 DAY)");
            deleteOld.setInt(1, userId);
            deleteOld.executeUpdate();
            deleteOld.close();

            // ✅ Sync pending DOCUMENT REQUESTS
            System.out.println("[Sync] Syncing pending document requests...");
            ResultSet rs0 = conn.prepareStatement(
                "SELECT id, document_type FROM document_requests WHERE status = 'Pending'"
            ).executeQuery();
            int docCount = 0;
            while (rs0.next()) {
                String docId = String.valueOf(rs0.getInt("id"));
                String docType = rs0.getString("document_type");
                String msg = "📋 Pending document request: " + docType;
                insertIfNew(conn, "document", msg, docId, userId);
                docCount++;
            }
            rs0.close();
            System.out.println("[Sync] Found " + docCount + " pending document requests");

            // ✅ Sync pending payments
            System.out.println("[Sync] Syncing pending payments...");
            ResultSet rs1 = conn.prepareStatement(
                "SELECT id, ref_number FROM payments WHERE status = 'Pending' AND archived = 0"
            ).executeQuery();
            int paymentCount = 0;
            while (rs1.next()) {
                String refNo = rs1.getString("ref_number");
                String paymentId = String.valueOf(rs1.getInt("id"));
                String msg = "💳 Pending payment: " + refNo;
                insertIfNew(conn, "payment", msg, paymentId, userId);
                paymentCount++;
            }
            rs1.close();
            System.out.println("[Sync] Found " + paymentCount + " pending payments");

            // ✅ Sync open complaints
            System.out.println("[Sync] Syncing open complaints...");
            ResultSet rs2 = conn.prepareStatement(
                "SELECT id, incident_type FROM complaints WHERE status <> 'Resolved'"
            ).executeQuery();
            int complaintCount = 0;
            while (rs2.next()) {
                String cid = String.valueOf(rs2.getInt("id"));
                String incidentType = rs2.getString("incident_type");
                String msg = "📢 Open complaint: " + incidentType;
                insertIfNew(conn, "complaint", msg, cid, userId);
                complaintCount++;
            }
            rs2.close();
            System.out.println("[Sync] Found " + complaintCount + " open complaints");

            // ✅ Sync recent announcements
            System.out.println("[Sync] Syncing announcements...");
            ResultSet rs3 = conn.prepareStatement(
                "SELECT id, title FROM announcements ORDER BY id DESC LIMIT 5"
            ).executeQuery();
            int announcementCount = 0;
            while (rs3.next()) {
                String aid = String.valueOf(rs3.getInt("id"));
                String title = rs3.getString("title");
                String msg = "📣 Announcement: " + title;
                insertIfNew(conn, "announcement", msg, aid, userId);
                announcementCount++;
            }
            rs3.close();
            System.out.println("[Sync] Found " + announcementCount + " announcements");

            conn.close();
            System.out.println("[Sync] Notification sync complete!");
        } catch (Exception e) {
            System.out.println("[Sync] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void insertIfNew(Connection conn, String type,
                              String message, String refId, int userId) throws Exception {
        PreparedStatement check = conn.prepareStatement(
            "SELECT id FROM notifications WHERE reference_id = ? AND user_id = ? AND type = ?");
        check.setString(1, refId);
        check.setInt(2, userId);
        check.setString(3, type);
        ResultSet rs = check.executeQuery();
        boolean exists = rs.next();
        rs.close(); 
        check.close();

        if (!exists) {
            PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO notifications (type, message, reference_id, is_read, created_at, user_id) " +
                "VALUES (?, ?, ?, 0, ?, ?)");
            ins.setString(1, type);
            ins.setString(2, message);
            ins.setString(3, refId);
            ins.setString(4, LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ins.setInt(5, userId);
            ins.executeUpdate();
            ins.close();
            System.out.println("[Notif] New: " + type + " - " + refId + " - " + message);
        } else {
            System.out.println("[Notif] Already exists: " + type + " - " + refId);
        }
    }

    private void markOneAsRead(String notifId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(true);
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE notifications SET is_read = 1 WHERE id = ?");
            stmt.setInt(1, Integer.parseInt(notifId));
            int updated = stmt.executeUpdate();
            System.out.println("[Read] notif_id=" + notifId + " updated=" + updated);
            stmt.close();
            conn.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void refreshAlertBadge() {
        String email = SessionManager.getEmail();
        if (email == null) return;
        try {
            Connection conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(true);
            
            // ✅ Get user ID first
            PreparedStatement userStmt = conn.prepareStatement("SELECT id FROM users WHERE email = ?");
            userStmt.setString(1, email);
            ResultSet userRs = userStmt.executeQuery();
            int userId = -1;
            if (userRs.next()) userId = userRs.getInt("id");
            userRs.close();
            userStmt.close();
            
            if (userId == -1) {
                conn.close();
                return;
            }
            
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0");
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            int count = rs.next() ? rs.getInt(1) : 0;
            rs.close(); stmt.close(); conn.close();
            System.out.println("[Badge] Unread count for user " + userId + " = " + count);
            if (count > 0) {
                alertBadge.setText(count > 99 ? "99+" : String.valueOf(count));
                alertBadge.setVisible(true);
            } else {
                alertBadge.setVisible(false);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── ALERTS POPUP ───────────────────────────────────────────────────────────────
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
        Button pastBtn = new Button("All Notifications");
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
            if (email == null) {
                System.out.println("[Notif Load] No email");
                return;
            }
            try {
                Connection conn = DatabaseConnection.getConnection();
                conn.setAutoCommit(true);
                
                // ✅ Get user ID
                PreparedStatement userStmt = conn.prepareStatement("SELECT id FROM users WHERE email = ?");
                userStmt.setString(1, email);
                ResultSet userRs = userStmt.executeQuery();
                int userId = -1;
                if (userRs.next()) userId = userRs.getInt("id");
                userRs.close();
                userStmt.close();

                System.out.println("[Notif Load] User ID: " + userId);

                if (userId == -1) {
                    conn.close();
                    VBox empty = new VBox(8);
                    empty.setStyle("-fx-alignment: CENTER; -fx-padding: 40;");
                    Label emptyLbl = new Label("User not found");
                    emptyLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #aaaaaa;");
                    empty.getChildren().add(emptyLbl);
                    notifBody.getChildren().add(empty);
                    return;
                }

                String sql = showingPast[0]
                    ? "SELECT id, type, message, is_read, created_at FROM notifications WHERE user_id = ? ORDER BY id DESC"
                    : "SELECT id, type, message, is_read, created_at FROM notifications WHERE user_id = ? AND is_read = 0 ORDER BY id DESC";
                
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                List<String[]> items = new ArrayList<>();
                while (rs.next()) {
                    items.add(new String[]{
                        rs.getString("id"),
                        rs.getString("type"),
                        rs.getString("message"),
                        rs.getString("is_read"),
                        rs.getString("created_at")
                    });
                }
                rs.close(); stmt.close(); conn.close();

                System.out.println("[Notif Load] Found " + items.size() + " notifications");

                if (items.isEmpty()) {
                    VBox empty = new VBox(8);
                    empty.setStyle("-fx-alignment: CENTER; -fx-padding: 40;");
                    Label emptyLbl = new Label(
                        showingPast[0]
                            ? "No notifications yet"
                            : "You're all caught up! 🎉");
                    emptyLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #aaaaaa;");
                    empty.getChildren().add(emptyLbl);
                    notifBody.getChildren().add(empty);
                } else {
                    for (String[] item : items)
                        notifBody.getChildren().add(
                            buildNotifItem(item, loadNotifsRef,
                                showingPast, alertStage));
                }
            } catch (Exception e) {
                System.out.println("[Notif Load] Error: " + e.getMessage());
                e.printStackTrace();
            }
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
        closeBtn.setOnAction(e -> {
            refreshAlertBadge();
            alertStage.close();
        });
        footer.getChildren().add(closeBtn);

        root.getChildren().addAll(header, filterRow, scrollPane, footer);
        alertStage.setScene(new Scene(root));
        Platform.runLater(() -> root.requestFocus());
        alertStage.showAndWait();
        refreshAlertBadge();
    }

    private VBox buildNotifItem(String[] item,
                                 Runnable[] loadNotifsRef,
                                 boolean[] showingPast,
                                 Stage alertStage) {
        String notifId = item[0];
        String type    = item[1];
        String message = item[2];
        String isRead  = item[3];
        String dateStr = item[4];
        if (dateStr != null && dateStr.length() > 16)
            dateStr = dateStr.substring(0, 16);

        String icon, bg;
        if ("complaint".equals(type))    { icon = "📢"; bg = "#ffebee"; }
        else if ("payment".equals(type)) { icon = "💳"; bg = "#fff8e1"; }
        else if ("document".equals(type)) { icon = "📋"; bg = "#f3e5f5"; }
        else                             { icon = "📣"; bg = "#e3f2fd"; }

        HBox row = new HBox(14);
        row.setStyle(
            "-fx-padding: 16 24;" +
            "-fx-border-color: #f4f4f4; -fx-border-width: 0 0 1 0;" +
            ("0".equals(isRead)
                ? "-fx-background-color: #fafbff; -fx-cursor: hand;"
                : "-fx-background-color: #ffffff; -fx-cursor: hand;"));
        row.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBox = new StackPane();
        iconBox.setStyle(
            "-fx-background-color: " + bg + "; -fx-background-radius: 10;" +
            "-fx-min-width: 40; -fx-min-height: 40;" +
            "-fx-max-width: 40; -fx-max-height: 40;");
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 16px;");
        iconBox.getChildren().add(iconLbl);

        VBox textBox = new VBox(4);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        Label msgLbl = new Label(message);
        msgLbl.setStyle(
            "-fx-font-size: 12px; -fx-text-fill: #1a1a1a;" +
            ("0".equals(isRead) ? " -fx-font-weight: bold;" : ""));
        msgLbl.setWrapText(true);
        Label dateLbl = new Label(dateStr != null ? dateStr : "");
        dateLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #aaaaaa;");
        textBox.getChildren().addAll(msgLbl, dateLbl);

        if ("0".equals(isRead)) {
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

        // Both unread and past are clickable — open detail modal
        final String finalDateStr = dateStr;
        row.setOnMouseClicked(e ->
            showNotifDetail(notifId, type, message, finalDateStr,
                icon, bg, isRead, loadNotifsRef, alertStage));

        return new VBox(row);
    }

    private void showNotifDetail(String notifId, String type, String message,
                                  String dateStr, String icon, String bg,
                                  String isRead,
                                  Runnable[] loadNotifsRef, Stage alertStage) {
        Stage detail = new Stage();
        detail.initModality(Modality.APPLICATION_MODAL);
        detail.initOwner(alertStage);
        detail.setTitle("Notification");
        detail.setResizable(false);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #ffffff; -fx-min-width: 440;");

        // Header
        VBox header = new VBox(6);
        header.setFocusTraversable(true);
        header.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 22 28;");
        Label titleLbl = new Label(
            "complaint".equals(type) ? "Complaint Alert" :
            "payment".equals(type)   ? "Payment Alert" :
            "document".equals(type)  ? "Document Request Alert" : "Announcement");
        titleLbl.setStyle(
            "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        Label dateLbl = new Label(dateStr != null ? dateStr : "");
        dateLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaaaaa;");
        header.getChildren().addAll(titleLbl, dateLbl);

        // Body
        VBox body = new VBox(20);
        body.setStyle("-fx-padding: 28;");

        // Icon + message
        HBox iconRow = new HBox(16);
        iconRow.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBox = new StackPane();
        iconBox.setStyle(
            "-fx-background-color: " + bg + "; -fx-background-radius: 12;" +
            "-fx-min-width: 52; -fx-min-height: 52;" +
            "-fx-max-width: 52; -fx-max-height: 52;");
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

        // Go to page button
        String goToLabel =
            "complaint".equals(type)   ? "→  Go to Complaints" :
            "payment".equals(type)     ? "→  Go to Payments" :
            "document".equals(type)    ? "→  Go to Documents"   :
                                         "→  Go to Announcements";
        String goToFxml =
            "complaint".equals(type)   ? "Complaints.fxml" :
            "payment".equals(type)     ? "Payments.fxml" :
            "document".equals(type)    ? "Documents.fxml"   :
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
            if ("0".equals(isRead)) markOneAsRead(notifId);
            detail.close();
            alertStage.close();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            SceneTransition.slideTo(stage, goToFxml, true, getClass());
        });
        body.getChildren().add(goToBtn);

        // Footer
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

        Button markBtn = new Button("Mark as Read");
        markBtn.setStyle(
            "-fx-background-color: #1a1a1a; -fx-text-fill: #ffffff;" +
            "-fx-font-size: 12px; -fx-font-weight: bold;" +
            "-fx-background-radius: 8; -fx-padding: 10 24; -fx-cursor: hand;");

        if ("1".equals(isRead)) {
            // Already read — only show Close
            footer.getChildren().add(cancelBtn);
        } else {
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

    // ── Table Columns ─────────────────────────────────────────────────────────────
    private void setupTableColumns() {
        colDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[0]));
        colResident.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[1]));
        colCategory.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[2]));
        colAmount.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[4]));
        colPaymentMethod.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[5]));

        colType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[3]));
        colType.setCellFactory(col -> new TableCell<String[], String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setGraphic(null); setStyle(""); return;
                }
                Label badge = new Label(item);
                badge.setStyle(item.equals("Income")
                    ? "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32;" +
                      "-fx-font-size: 11px; -fx-font-weight: bold;" +
                      "-fx-background-radius: 20; -fx-padding: 4 14;"
                    : "-fx-background-color: #ffebee; -fx-text-fill: #c62828;" +
                      "-fx-font-size: 11px; -fx-font-weight: bold;" +
                      "-fx-background-radius: 20; -fx-padding: 4 14;");
                setGraphic(badge);
                setText(null);
            }
        });

        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[6]));
        colStatus.setCellFactory(col -> new TableCell<String[], String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setGraphic(null); setStyle(""); return;
                }
                Label badge = new Label(item);
                badge.setStyle(item.equals("Paid")
                    ? "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32;" +
                      "-fx-font-size: 11px; -fx-font-weight: bold;" +
                      "-fx-background-radius: 20; -fx-padding: 4 14;"
                    : "-fx-background-color: #fff8e1; -fx-text-fill: #f57f17;" +
                      "-fx-font-size: 11px; -fx-font-weight: bold;" +
                      "-fx-background-radius: 20; -fx-padding: 4 14;");
                setGraphic(badge);
                setText(null);
            }
        });

        transactionTable.setRowFactory(tv -> new TableRow<String[]>() {
            @Override
            protected void updateItem(String[] item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setStyle(getIndex() % 2 == 0
                        ? "-fx-background-color: #ffffff; -fx-pref-height: 52px;"
                        : "-fx-background-color: #fafbfc; -fx-pref-height: 52px;");
                }
            }
        });

        colActions.setCellFactory(col -> new TableCell<String[], String>() {
            final Button viewBtn   = new Button("View");
            final Button editBtn   = new Button("Edit");
            final Button deleteBtn = new Button("Delete");
            final HBox   box       = new HBox(6, viewBtn, editBtn, deleteBtn);
            {
                box.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 0 0 0 4;");

                viewBtn.setStyle(
                    "-fx-background-color: #e3f2fd; -fx-text-fill: #1565c0;" +
                    "-fx-font-size: 11px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 6; -fx-border-color: #bbdefb;" +
                    "-fx-border-width: 1; -fx-padding: 5 10; -fx-cursor: hand;");
                editBtn.setStyle(
                    "-fx-background-color: #f4f4f4; -fx-text-fill: #444444;" +
                    "-fx-font-size: 11px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 6; -fx-border-color: #e0e0e0;" +
                    "-fx-border-width: 1; -fx-padding: 5 10; -fx-cursor: hand;");
                deleteBtn.setStyle(
                    "-fx-background-color: #ffebee; -fx-text-fill: #c62828;" +
                    "-fx-font-size: 11px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 6; -fx-border-color: #ffcdd2;" +
                    "-fx-border-width: 1; -fx-padding: 5 10; -fx-cursor: hand;");

                viewBtn.setOnAction(e -> {
                    String[] row = getTableView().getItems().get(getIndex());
                    openViewModal(row);
                });

                editBtn.setOnAction(e -> {
                    String[] row = getTableView().getItems().get(getIndex());
                    if (row[7].equals("finances")) {
                        openExpenseModal(row[8], row[1], row[2], row[4], row[6]);
                    } else {
                        openIncomeEditModal(row[8], row[1], row[2], row[4], row[5], row[6], row[0]);
                    }
                });

                deleteBtn.setOnAction(e -> {
                    String[] row = getTableView().getItems().get(getIndex());
                    if (row[7].equals("finances")) {
                        deleteExpense(row[8]);
                    } else {
                        showInfo("Income records can only be deleted from the Payments page.");
                    }
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    // ── Summary ──────────────────────────────────────────────────────────────────
    private void loadSummary() {
        double totalIncome = 0, totalExpenses = 0;
        int pending = 0;
        try {
            Connection conn = DatabaseConnection.getConnection();
            
            ResultSet rs1 = conn.prepareStatement(
                "SELECT COALESCE(SUM(amount), 0) AS total FROM payments WHERE status = 'Paid'"
            ).executeQuery();
            if (rs1.next()) totalIncome = rs1.getDouble("total");
            rs1.close();

            ResultSet rs2 = conn.prepareStatement(
                "SELECT COUNT(*) AS cnt FROM payments WHERE status = 'Pending' AND archived = 0"
            ).executeQuery();
            if (rs2.next()) pending = rs2.getInt("cnt");
            rs2.close();

            ResultSet rs3 = conn.prepareStatement(
                "SELECT COALESCE(SUM(amount), 0) AS total FROM finances WHERE type = 'Expense'"
            ).executeQuery();
            if (rs3.next()) totalExpenses = rs3.getDouble("total");
            rs3.close();
            conn.close();
        } catch (Exception e) { e.printStackTrace(); }

        double net = totalIncome - totalExpenses;
        totalCollectionsLabel.setText(String.format("₱%,.2f", totalIncome));
        totalExpensesLabel.setText(String.format("₱%,.2f", totalExpenses));
        netBalanceLabel.setText(String.format("₱%,.2f", net));
        netBalanceLabel.setStyle(
            "-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: " +
            (net >= 0 ? "#2e7d32" : "#c62828") + ";");
        pendingLabel.setText(String.valueOf(pending));
    }

    // ── Bar Chart ────────────────────────────────────���────────────────────────────
    private void loadBarChart() {
        barChart.getData().clear();
        barChart.setLegendVisible(true);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yy");

        Map<String, double[]> incomeMap  = new LinkedHashMap<>();
        Map<String, double[]> expenseMap = new LinkedHashMap<>();

        for (int i = 5; i >= 0; i--) {
            String label = LocalDate.now().minusMonths(i).format(fmt);
            incomeMap.put(label,  new double[]{0, 0});
            expenseMap.put(label, new double[]{0, 0});
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs1 = conn.prepareStatement(
                "SELECT date_created, amount FROM payments WHERE status = 'Paid'"
            ).executeQuery();
            while (rs1.next()) {
                try {
                    String raw = rs1.getString("date_created");
                    if (raw == null) continue;
                    String label = LocalDate.parse(raw.substring(0, 10)).format(fmt);
                    if (incomeMap.containsKey(label)) {
                        incomeMap.get(label)[0] += rs1.getDouble("amount");
                        incomeMap.get(label)[1]++;
                    }
                } catch (Exception ignored) {}
            }
            rs1.close();

            ResultSet rs2 = conn.prepareStatement(
                "SELECT date_recorded, amount FROM finances WHERE type = 'Expense'"
            ).executeQuery();
            while (rs2.next()) {
                try {
                    String raw = rs2.getString("date_recorded");
                    if (raw == null) continue;
                    String label = LocalDate.parse(raw.substring(0, 10)).format(fmt);
                    if (expenseMap.containsKey(label)) {
                        expenseMap.get(label)[0] += rs2.getDouble("amount");
                        expenseMap.get(label)[1]++;
                    }
                } catch (Exception ignored) {}
            }
            rs2.close();
            conn.close();
        } catch (Exception e) { e.printStackTrace(); }

        XYChart.Series<String, Number> incomeSeries  = new XYChart.Series<>();
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        expenseSeries.setName("Expenses");

        for (String month : incomeMap.keySet()) {
            double[] inc = incomeMap.get(month);
            double[] exp = expenseMap.get(month);

            XYChart.Data<String, Number> incData = new XYChart.Data<>(month, inc[0]);
            XYChart.Data<String, Number> expData = new XYChart.Data<>(month, exp[0]);

            final double incAmt = inc[0]; final int incCnt = (int) inc[1];
            final double expAmt = exp[0]; final int expCnt = (int) exp[1];

            incData.nodeProperty().addListener((obs, old, node) -> {
                if (node != null) {
                    node.setStyle("-fx-bar-fill: #43a047;");
                    Tooltip tip = new Tooltip(
                        "📈 Income: ₱" + String.format("%,.2f", incAmt) +
                        "\n👥 Payments made: " + incCnt);
                    tip.setStyle("-fx-font-size: 12px; -fx-background-color: #1a1a1a;" +
                        "-fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 12;");
                    tip.setShowDelay(Duration.millis(80));
                    tip.setHideDelay(Duration.millis(200));
                    Tooltip.install(node, tip);
                    node.setOnMouseEntered(e -> node.setStyle("-fx-bar-fill: #2e7d32;"));
                    node.setOnMouseExited(e  -> node.setStyle("-fx-bar-fill: #43a047;"));
                }
            });

            expData.nodeProperty().addListener((obs, old, node) -> {
                if (node != null) {
                    node.setStyle("-fx-bar-fill: #e53935;");
                    Tooltip tip = new Tooltip(
                        "📉 Expenses: ₱" + String.format("%,.2f", expAmt) +
                        "\n📋 Records: " + expCnt);
                    tip.setStyle("-fx-font-size: 12px; -fx-background-color: #1a1a1a;" +
                        "-fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 12;");
                    tip.setShowDelay(Duration.millis(80));
                    tip.setHideDelay(Duration.millis(200));
                    Tooltip.install(node, tip);
                    node.setOnMouseEntered(e -> node.setStyle("-fx-bar-fill: #b71c1c;"));
                    node.setOnMouseExited(e  -> node.setStyle("-fx-bar-fill: #e53935;"));
                }
            });

            incomeSeries.getData().add(incData);
            expenseSeries.getData().add(expData);
        }
        barChart.getData().addAll(incomeSeries, expenseSeries);
    }
    
    // ── Pie Chart ─────────────────────────────────────────────────────────────────
    private void loadPieChart() {
        pieChart.getData().clear();
        pieChart.setLegendVisible(true);
        pieChartDataMap.clear();
        
        // ✅ DYNAMIC color palette
        String[] colors = {"#43a047", "#1e88e5", "#fdd835", "#e53935", "#9c27b0", "#ff7043", "#00bcd4", "#ff9800"};
        Map<String, String> colorMap = new LinkedHashMap<>();
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // ✅ Query ALL payment types with their total amounts
            ResultSet rs = conn.prepareStatement(
                "SELECT IFNULL(payment_type, 'Unknown') AS payment_type, " +
                "COALESCE(SUM(amount), 0) AS total, " +
                "COUNT(*) AS cnt " +
                "FROM payments " +
                "WHERE archived = 0 " +
                "GROUP BY IFNULL(payment_type, 'Unknown') " +
                "ORDER BY total DESC"
            ).executeQuery();
            
            System.out.println("[PieChart] Loading all payment types...");
            List<String[]> allPaymentData = new ArrayList<>();
            
            while (rs.next()) {
                String type = rs.getString("payment_type");
                double total = rs.getDouble("total");
                int cnt = rs.getInt("cnt");
                
                System.out.println("[PieChart] Found: " + type + " | Amount: ₱" + total + " | Count: " + cnt);
                
                // Store for later use
                allPaymentData.add(new String[]{type, String.valueOf(total), String.valueOf(cnt)});
            }
            rs.close();
            
            // ✅ Assign colors to each type
            int colorIndex = 0;
            for (String[] data : allPaymentData) {
                String type = data[0];
                colorMap.put(type, colors[colorIndex % colors.length]);
                System.out.println("[PieChart] Color for " + type + ": " + colors[colorIndex % colors.length]);
                colorIndex++;
            }
            
            // ✅ Add all types to pie chart, even if zero
            System.out.println("[PieChart] Adding " + allPaymentData.size() + " types to pie chart");
            for (String[] data : allPaymentData) {
                String type = data[0];
                double total = Double.parseDouble(data[1]);
                int cnt = Integer.parseInt(data[2]);
                
                // ✅ Store in global map for tooltip access
                pieChartDataMap.put(type, new double[]{total, cnt});
                
                // Use 1 as minimum value for visibility if amount is 0
                double displayValue = (total > 0) ? total : 0.1;
                
                PieChart.Data slice = new PieChart.Data(type, displayValue);
                pieChart.getData().add(slice);
                
                final String color = colorMap.get(type);
                final String typeName = type;
                final double amount = total;
                final int count = cnt;
                
                slice.nodeProperty().addListener((obs, old, node) -> {
                    if (node != null) {
                        System.out.println("[PieChart] Styling node for " + typeName + " with color " + color);
                        node.setStyle("-fx-pie-color: " + color + " !important;");
                        
                        // ✅ Create tooltip with stored data
                        String tipText = "📄 " + typeName +
                            "\n₱" + String.format("%,.2f", amount) +
                            "\n" + count + " payment" + (count != 1 ? "s" : "");
                        
                        Tooltip tip = new Tooltip(tipText);
                        tip.setStyle("-fx-font-size: 12px; -fx-background-color: #1a1a1a;" +
                            "-fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10 12;");
                        tip.setShowDelay(Duration.millis(200));
                        tip.setHideDelay(Duration.millis(1000));
                        Tooltip.install(node, tip);
                        
                        node.setOnMouseEntered(e -> {
                            node.setStyle("-fx-pie-color: " + color + "; -fx-opacity: 0.7;");
                        });
                        node.setOnMouseExited(e -> {
                            node.setStyle("-fx-pie-color: " + color + "; -fx-opacity: 1.0;");
                        });
                    }
                });
            }
            
            System.out.println("[PieChart] Pie chart loaded with " + allPaymentData.size() + " types");
            
            conn.close();
        } catch (Exception e) {
            System.out.println("[PieChart] Database error: " + e.getMessage());
            e.printStackTrace();
        }

        // ✅ If NO data found at all, show empty state
        if (pieChart.getData().isEmpty()) {
            System.out.println("[PieChart] ⚠️ NO PAYMENT DATA FOUND");
            PieChart.Data emptySlice = new PieChart.Data("No Data Available", 1);
            pieChart.getData().add(emptySlice);
            
            emptySlice.nodeProperty().addListener((obs, old, node) -> {
                if (node != null) {
                    node.setStyle("-fx-pie-color: #e0e0e0 !important;");
                    
                    Tooltip emptyTip = new Tooltip("No payments recorded yet.");
                    emptyTip.setStyle("-fx-font-size: 11px; -fx-background-color: #666666;" +
                        "-fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 10;");
                    Tooltip.install(node, emptyTip);
                }
            });
        }
    }

    // ── Load Transactions ─────────────────────────────────────────────────────────
    private void loadTransactions() {
        String    search = searchField.getText().trim().toLowerCase();
        String    type   = filterType.getValue();
        String    status = filterStatus.getValue();
        LocalDate from   = filterDateFrom.getValue();
        LocalDate to     = filterDateTo.getValue();

        List<String[]> rows = new ArrayList<>();
        DateTimeFormatter displayFmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");

        try {
            Connection conn = DatabaseConnection.getConnection();

            if (type.equals("All") || type.equals("Income")) {
                StringBuilder sql = new StringBuilder(
                    "SELECT p.id, p.ref_number, r.full_name, p.payment_type, " +
                    "p.amount, p.status, p.date_created FROM payments p " +
                    "LEFT JOIN residents r ON p.resident_id = r.id WHERE 1=1");
                if (!status.equals("All"))
                    sql.append(" AND p.status = '").append(status).append("'");
                if (from != null)
                    sql.append(" AND DATE(p.date_created) >= '").append(from).append("'");
                if (to != null)
                    sql.append(" AND DATE(p.date_created) <= '").append(to).append("'");
                sql.append(" ORDER BY p.date_created DESC");

                ResultSet rs = conn.prepareStatement(sql.toString()).executeQuery();
                while (rs.next()) {
                    String resident = rs.getString("full_name");
                    String ref      = rs.getString("ref_number");
                    String cat      = rs.getString("payment_type");
                    String amt      = String.format("₱%,.2f", rs.getDouble("amount"));
                    String stat     = rs.getString("status");
                    String rawDate  = rs.getString("date_created");
                    String id       = rs.getString("id");
                    String method   = (ref != null && ref.toUpperCase().startsWith("CASH"))
                                        ? "Cash" : "GCash";
                    String date = rawDate;
                    try {
                        date = LocalDate.parse(rawDate.substring(0, 10)).format(displayFmt);
                    } catch (Exception ignored) {}

                    if (!search.isEmpty()
                        && !resident.toLowerCase().contains(search)
                        && !ref.toLowerCase().contains(search)) continue;

                    rows.add(new String[]{
                        date, resident, cat, "Income", amt, method, stat, "payments", id
                    });
                }
                rs.close();
            }

            if (type.equals("All") || type.equals("Expense")) {
                StringBuilder sql = new StringBuilder(
                    "SELECT id, description, category, amount, " +
                    "status, date_recorded FROM finances WHERE type = 'Expense'");
                if (!status.equals("All"))
                    sql.append(" AND status = '").append(status).append("'");
                if (from != null)
                    sql.append(" AND DATE(date_recorded) >= '").append(from).append("'");
                if (to != null)
                    sql.append(" AND DATE(date_recorded) <= '").append(to).append("'");
                sql.append(" ORDER BY date_recorded DESC");

                ResultSet rs = conn.prepareStatement(sql.toString()).executeQuery();
                while (rs.next()) {
                    String desc    = rs.getString("description");
                    String cat     = rs.getString("category");
                    String amt     = String.format("₱%,.2f", rs.getDouble("amount"));
                    String stat    = rs.getString("status");
                    String rawDate = rs.getString("date_recorded");
                    String id      = String.valueOf(rs.getInt("id"));
                    String date = rawDate;
                    try {
                        date = LocalDate.parse(rawDate.substring(0, 10)).format(displayFmt);
                    } catch (Exception ignored) {}

                    if (!search.isEmpty()
                        && !desc.toLowerCase().contains(search)
                        && !cat.toLowerCase().contains(search)) continue;

                    rows.add(new String[]{
                        date, desc, cat, "Expense", amt, "N/A", stat, "finances", id
                    });
                }
                rs.close();
            }
            conn.close();
        } catch (Exception e) { e.printStackTrace(); }

        transactionTable.setItems(FXCollections.observableArrayList(rows));
        if (recordCountLabel != null)
            recordCountLabel.setText(rows.size() + " record" + (rows.size() != 1 ? "s" : ""));
    }

    // ── MODAL HELPERS ─────────────────────────────────────────────────────────────

    private VBox buildModalRoot() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #ffffff; -fx-min-width: 460; -fx-max-width: 460;");
        return root;
    }

    private VBox buildModalHeader(String title, String subtitle) {
        VBox header = new VBox(6);
        header.setStyle(
            "-fx-background-color: #1a1a1a; -fx-padding: 24 28 22 28;");
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
            "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        Label subLabel = new Label(subtitle);
        subLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");
        header.getChildren().addAll(titleLabel, subLabel);
        return header;
    }

    private VBox buildModalBody() {
        VBox body = new VBox(14);
        body.setStyle("-fx-padding: 24 28 10 28; -fx-background-color: #ffffff;");
        return body;
    }

    private VBox buildFieldGroup(String labelText, javafx.scene.Node field) {
        VBox group = new VBox(6);
        Label lbl = new Label(labelText);
        lbl.setStyle(
            "-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #999999;");
        group.getChildren().addAll(lbl, field);
        return group;
    }

    private TextField buildReadOnlyField(String value) {
        TextField field = new TextField(value != null ? value : "");
        field.setEditable(false);
        field.setStyle(
            "-fx-font-size: 13px; -fx-padding: 10 14; -fx-background-radius: 8;" +
            "-fx-border-color: #eeeeee; -fx-border-width: 1; -fx-border-radius: 8;" +
            "-fx-background-color: #f8f8f8; -fx-text-fill: #555555;");
        return field;
    }

    private TextField buildEditableField(String value) {
        TextField field = new TextField(value != null ? value : "");
        field.setStyle(
            "-fx-font-size: 13px; -fx-padding: 10 14; -fx-background-radius: 8;" +
            "-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8;" +
            "-fx-background-color: #fafafa; -fx-text-fill: #1a1a1a;");
        return field;
    }

    private HBox buildModalFooter(Button... buttons) {
        HBox footer = new HBox(10);
        footer.setStyle(
            "-fx-padding: 16 28 24 28; -fx-alignment: CENTER_RIGHT;" +
            "-fx-border-color: #f0f0f0; -fx-border-width: 1 0 0 0;");
        footer.getChildren().addAll(buttons);
        return footer;
    }

    private Button buildCancelButton(Stage stage) {
        Button btn = new Button("Cancel");
        btn.setStyle(
            "-fx-background-color: #f4f4f4; -fx-text-fill: #555555;" +
            "-fx-font-size: 12px; -fx-font-weight: bold;" +
            "-fx-background-radius: 8; -fx-border-color: #e0e0e0;" +
            "-fx-border-width: 1; -fx-padding: 10 20; -fx-cursor: hand;");
        btn.setOnAction(e -> stage.close());
        return btn;
    }

    private Button buildPrimaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: #1a1a1a; -fx-text-fill: #ffffff;" +
            "-fx-font-size: 12px; -fx-font-weight: bold;" +
            "-fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;");
        return btn;
    }

    // ── VIEW MODAL ────────────────────────────────────────────────────────────────
    private void openViewModal(String[] row) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.initOwner(logoutButton.getScene().getWindow());
        modal.setTitle("Transaction Details");
        modal.setResizable(false);

        boolean isIncome = row[7].equals("payments");

        VBox root   = buildModalRoot();
        VBox header = buildModalHeader(
            isIncome ? "Income Details" : "Expense Details",
            isIncome ? "Payment record from the Payments page"
                     : "Expense record from the Finance ledger");
        VBox body   = buildModalBody();

        HBox row1 = new HBox(16);
        VBox dateGroup = buildFieldGroup("DATE", buildReadOnlyField(row[0]));
        HBox.setHgrow(dateGroup, Priority.ALWAYS);
        VBox typeGroup = buildFieldGroup("TYPE", buildReadOnlyField(row[3]));
        HBox.setHgrow(typeGroup, Priority.ALWAYS);
        row1.getChildren().addAll(dateGroup, typeGroup);

        VBox residentGroup = buildFieldGroup(
            isIncome ? "RESIDENT NAME" : "DESCRIPTION",
            buildReadOnlyField(row[1]));

        VBox categoryGroup = buildFieldGroup("CATEGORY", buildReadOnlyField(row[2]));

        HBox row2 = new HBox(16);
        VBox amountGroup = buildFieldGroup("AMOUNT", buildReadOnlyField(row[4]));
        HBox.setHgrow(amountGroup, Priority.ALWAYS);
        VBox methodGroup = buildFieldGroup("PAYMENT METHOD", buildReadOnlyField(row[5]));
        HBox.setHgrow(methodGroup, Priority.ALWAYS);
        row2.getChildren().addAll(amountGroup, methodGroup);

        // Status badge
        Label statusBadge = new Label(row[6]);
        statusBadge.setStyle(row[6].equals("Paid")
            ? "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32;" +
              "-fx-font-size: 12px; -fx-font-weight: bold;" +
              "-fx-background-radius: 20; -fx-padding: 6 16;"
            : "-fx-background-color: #fff8e1; -fx-text-fill: #f57f17;" +
              "-fx-font-size: 12px; -fx-font-weight: bold;" +
              "-fx-background-radius: 20; -fx-padding: 6 16;");
        VBox statusGroup = buildFieldGroup("STATUS", statusBadge);

        body.getChildren().addAll(row1, residentGroup, categoryGroup, row2, statusGroup);

        Button closeBtn = buildPrimaryButton("Close");
        closeBtn.setOnAction(e -> modal.close());
        HBox footer = buildModalFooter(closeBtn);

        root.getChildren().addAll(header, body, footer);
        modal.setScene(new Scene(root));
        Platform.runLater(() -> root.requestFocus());
        modal.showAndWait();
    }

    // ── ADD / EDIT EXPENSE MODAL ──────────────────────────────────────────────────
    @FXML
    private void handleAddExpense() {
        openExpenseModal(null, null, null, null, null);
    }

    private void openExpenseModal(String financeId, String description,
                                   String category, String amount, String status) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.initOwner(logoutButton.getScene().getWindow());
        modal.setTitle(financeId == null ? "Add Expense" : "Edit Expense");
        modal.setResizable(false);

        boolean isEdit = financeId != null;

        VBox root   = buildModalRoot();
        VBox header = buildModalHeader(
            isEdit ? "Edit Expense" : "Add Expense",
            isEdit ? "Update this expense record"
                   : "Record a new barangay expense");
        VBox body   = buildModalBody();

        Label descLbl = new Label("DESCRIPTION");
        descLbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #999999;");
        TextField descField = buildEditableField(description);
        descField.setPromptText("e.g. Electricity bill, Office supplies");
        VBox descGroup = new VBox(6, descLbl, descField);

        Label catLbl = new Label("CATEGORY");
        catLbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #999999;");
        ComboBox<String> catBox = new ComboBox<>();
        catBox.getItems().addAll("Utilities", "Events", "Supplies", "Salaries", "Maintenance", "Other");
        catBox.setValue(category != null ? category : "Utilities");
        catBox.setMaxWidth(Double.MAX_VALUE);
        catBox.setStyle(
            "-fx-font-size: 13px; -fx-background-radius: 8;" +
            "-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8;");
        VBox catGroup = new VBox(6, catLbl, catBox);

        HBox row1 = new HBox(16);
        Label amtLbl = new Label("AMOUNT (₱)");
        amtLbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #999999;");
        TextField amountField = buildEditableField(
            amount != null ? amount.replace("₱", "").replace(",", "") : "");
        amountField.setPromptText("e.g. 500.00");
        VBox amtGroup = new VBox(6, amtLbl, amountField);
        HBox.setHgrow(amtGroup, Priority.ALWAYS);

        Label statLbl = new Label("STATUS");
        statLbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #999999;");
        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Paid", "Pending");
        statusBox.setValue(status != null ? status : "Paid");
        statusBox.setMaxWidth(Double.MAX_VALUE);
        statusBox.setStyle(
            "-fx-font-size: 13px; -fx-background-radius: 8;" +
            "-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8;");
        VBox statGroup = new VBox(6, statLbl, statusBox);
        HBox.setHgrow(statGroup, Priority.ALWAYS);
        row1.getChildren().addAll(amtGroup, statGroup);

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #c62828; -fx-font-size: 11px;");

        body.getChildren().addAll(descGroup, catGroup, row1, errorLabel);

        Button cancelBtn = buildCancelButton(modal);
        Button saveBtn   = buildPrimaryButton(isEdit ? "Save Changes" : "Add Expense");

        String finalId = financeId;
        saveBtn.setOnAction(e -> {
            String desc = descField.getText().trim();
            String cat  = catBox.getValue();
            String amt  = amountField.getText().trim();
            String stat = statusBox.getValue();

            if (desc.isEmpty() || amt.isEmpty()) {
                errorLabel.setText("⚠  Please fill in all fields.");
                return;
            }
            double parsedAmt;
            try {
                parsedAmt = Double.parseDouble(amt);
                if (parsedAmt <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                errorLabel.setText("⚠  Enter a valid amount greater than 0.");
                return;
            }
            try {
                Connection conn = DatabaseConnection.getConnection();
                if (finalId == null) {
                    PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO finances (description, category, type, amount, status, date_recorded, recorded_by) " +
                        "VALUES (?, ?, 'Expense', ?, ?, NOW(), (SELECT id FROM users WHERE email = ?))");
                    stmt.setString(1, desc);
                    stmt.setString(2, cat);
                    stmt.setDouble(3, parsedAmt);
                    stmt.setString(4, stat);
                    stmt.setString(5, SessionManager.getEmail());
                    stmt.executeUpdate();
                    stmt.close();
                } else {
                    PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE finances SET description=?, category=?, amount=?, status=? WHERE id=?");
                    stmt.setString(1, desc);
                    stmt.setString(2, cat);
                    stmt.setDouble(3, parsedAmt);
                    stmt.setString(4, stat);
                    stmt.setInt(5, Integer.parseInt(finalId));
                    stmt.executeUpdate();
                    stmt.close();
                }
                conn.close();
                modal.close();
                loadSummary();
                loadTransactions();
                loadBarChart();
                loadPieChart();
                syncNotifications();
                refreshAlertBadge();
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("⚠  Database error. Please try again.");
            }
        });

        HBox footer = buildModalFooter(cancelBtn, saveBtn);
        root.getChildren().addAll(header, body, footer);
        modal.setScene(new Scene(root));
        Platform.runLater(() -> root.requestFocus());
        modal.showAndWait();
    }

    // ── EDIT INCOME MODAL (status only) ──────────────────────────────────────────
    private void openIncomeEditModal(String paymentId, String resident, String category,
                                      String amount, String method, String status, String date) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.initOwner(logoutButton.getScene().getWindow());
        modal.setTitle("Edit Payment Status");
        modal.setResizable(false);

        VBox root   = buildModalRoot();
        VBox header = buildModalHeader(
            "Edit Payment",
            "Only the payment status can be changed here");
        VBox body   = buildModalBody();

        HBox row1 = new HBox(16);
        VBox dateGroup = buildFieldGroup("DATE", buildReadOnlyField(date));
        HBox.setHgrow(dateGroup, Priority.ALWAYS);
        VBox catGroup  = buildFieldGroup("CATEGORY", buildReadOnlyField(category));
        HBox.setHgrow(catGroup, Priority.ALWAYS);
        row1.getChildren().addAll(dateGroup, catGroup);

        VBox residentGroup = buildFieldGroup("RESIDENT NAME", buildReadOnlyField(resident));

        HBox row2 = new HBox(16);
        VBox amountGroup = buildFieldGroup("AMOUNT", buildReadOnlyField(amount));
        HBox.setHgrow(amountGroup, Priority.ALWAYS);
        VBox methodGroup = buildFieldGroup("PAYMENT METHOD", buildReadOnlyField(method));
        HBox.setHgrow(methodGroup, Priority.ALWAYS);
        row2.getChildren().addAll(amountGroup, methodGroup);

        // Only editable field
        Label statLbl = new Label("STATUS");
        statLbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #999999;");
        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Paid", "Pending");
        statusBox.setValue(status);
        statusBox.setMaxWidth(Double.MAX_VALUE);
        statusBox.setStyle(
            "-fx-font-size: 13px; -fx-background-radius: 8;" +
            "-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8;");
        VBox statGroup = new VBox(6, statLbl, statusBox);

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #c62828; -fx-font-size: 11px;");

        body.getChildren().addAll(row1, residentGroup, row2, statGroup, errorLabel);

        Button cancelBtn = buildCancelButton(modal);
        Button saveBtn   = buildPrimaryButton("Save Changes");

        saveBtn.setOnAction(e -> {
            String newStatus = statusBox.getValue();
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE payments SET status = ? WHERE id = ?");
                stmt.setString(1, newStatus);
                stmt.setString(2, paymentId);
                stmt.executeUpdate();
                stmt.close();
                conn.close();
                modal.close();
                loadSummary();
                loadTransactions();
                loadBarChart();
                loadPieChart();
                syncNotifications();
                refreshAlertBadge();
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("⚠  Database error. Please try again.");
            }
        });

        HBox footer = buildModalFooter(cancelBtn, saveBtn);
        root.getChildren().addAll(header, body, footer);
        modal.setScene(new Scene(root));
        Platform.runLater(() -> root.requestFocus());
        modal.showAndWait();
    }

    // ── Delete Expense ────────────────────────────────────────────────────────────
    private void deleteExpense(String financeId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Expense");
        confirm.setHeaderText("Delete this expense record?");
        confirm.setContentText("This action cannot be undone.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM finances WHERE id = ?");
                    stmt.setInt(1, Integer.parseInt(financeId));
                    stmt.executeUpdate();
                    stmt.close();
                    conn.close();
                    loadSummary();
                    loadTransactions();
                    loadBarChart();
                    loadPieChart();
                    syncNotifications();
                    refreshAlertBadge();
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    // ── Export CSV ────────────────────────────────────────────────────────────────
    @FXML
    private void handleExportCSV() {
        try {
            String fileName = "finances_export_" + LocalDate.now() + ".csv";
            FileWriter writer = new FileWriter(fileName);
            writer.write("Date,Resident/Description,Category,Type,Amount,Method,Status\n");
            for (String[] row : transactionTable.getItems()) {
                writer.write(String.join(",",
                    row[0], row[1], row[2], row[3], row[4], row[5], row[6]) + "\n");
            }
            writer.close();
            showInfo("✅  Exported successfully!\nFile saved as: " + fileName);
        } catch (Exception e) {
            e.printStackTrace();
            showInfo("Export failed. Please try again.");
        }
    }

    // ── Filters ───────────────────────────────────────────────────────────────────
    @FXML private void handleSearch() { loadTransactions(); }
    @FXML private void handleFilter() { loadTransactions(); }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        filterType.setValue("All");
        filterStatus.setValue("All");
        filterDateFrom.setValue(null);
        filterDateTo.setValue(null);
        loadTransactions();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────────
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ── Navigation ────────────────────────────────────────────────────────────────
    @FXML private void goToDashboard() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "AdminDashboard.fxml", true, getClass());
    }
    @FXML private void goToResidents() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Residents.fxml", true, getClass());
    }
    @FXML private void goToDocuments() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Documents.fxml", true, getClass());
    }
    @FXML private void goToPayments() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Payments.fxml", true, getClass());
    }
    @FXML private void goToArchive() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "PaymentArchive.fxml", true, getClass());
    }
    @FXML private void goToComplaints() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Complaints.fxml", true, getClass());
    }
    @FXML private void goToAnnouncements() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Announcements.fxml", true, getClass());
    }
    @FXML private void goToAdmin() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Admin.fxml", true, getClass());
    }
    @FXML private void handleLogout() {
        SessionManager.logout();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "login.fxml", false, getClass());
    }
    @FXML private void goToSettings() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Settings.fxml", true, getClass());
    }
}