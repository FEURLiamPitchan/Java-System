package com.mycompany.javasystem;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsController {

    @FXML private Button logoutButton;
    @FXML private Button alertsButton;
    @FXML private Label alertBadge;
    @FXML private HBox avatarBox;
    @FXML private Circle avatarCircle;
    @FXML private ImageView profileImageView;
    @FXML private Label avatarInitialLabel;
    @FXML private Label topBarNameLabel;
    @FXML private Label topBarRoleLabel;
    @FXML private StackPane tabContent;
    @FXML private ScrollPane mainScrollPane;
    @FXML private Button tabGeneralBtn;
    @FXML private Button tabNotifBtn;
    @FXML private Button tabAppearanceBtn;
    @FXML private Button tabDataBtn;

    // Settings state
    private boolean notifComplaints    = true;
    private boolean notifPayments      = true;
    private boolean notifAnnouncements = true;
    private String  currentFontSize    = "Medium";
    private boolean isDarkModeState    = false;

    // Toggle switch nodes for notifications
    private StackPane complaintsToggle;
    private StackPane paymentsToggle;
    private StackPane announcementsToggle;
    private StackPane darkModeToggle;

    private Node generalTabNode;
    private Node notifTabNode;
    private Node appearanceTabNode;
    private Node dataTabNode;
    
    private int currentTabIndex = 0;
    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    private final String activeTabStyle =
        "-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff;" +
        "-fx-font-size: 12px; -fx-font-weight: bold;" +
        "-fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;";
    private final String inactiveTabStyle =
        "-fx-background-color: #ffffff; -fx-text-fill: #555555;" +
        "-fx-font-size: 12px; -fx-background-radius: 8;" +
        "-fx-border-color: #e0e0e0; -fx-border-width: 1;" +
        "-fx-padding: 10 20; -fx-cursor: hand;";

@FXML
public void initialize() {
    loadTopBar();
    loadAvatarPicture();
    loadSettingsFromDB();

    Platform.runLater(() -> {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            if (stage != null && stage.getScene() != null) {
                ThemeManager.setSettingsStage(stage);
                ThemeManager.applyTheme(stage);
            }
        } catch (Exception e) {
            System.out.println("[SettingsController] Theme error: " + e.getMessage());
        }
    });

    System.out.println("[SettingsController] Building all tabs at init time...");

    generalTabNode = buildGeneralTab();
    notifTabNode = buildNotifTab();
    appearanceTabNode = buildAppearanceTab();
    dataTabNode = buildDataTab();

    // ✅ IMPORTANT FIX: delay first tab load
    Platform.runLater(() -> {
        showGeneralTab();
        refreshAlertBadge();
    });

    Platform.runLater(() -> {
        if (mainScrollPane != null) {
            mainScrollPane.getContent().setOnScroll(event -> {
                double deltaY = event.getDeltaY() * 8;
                double contentHeight = mainScrollPane.getContent().getBoundsInLocal().getHeight();
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
// =========================================================================
//  NOTIFICATIONS
// =========================================================================
private int getCurrentUserId() {
    String email = SessionManager.getEmail();
    if (email == null) return -1;
    try {
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(
            "SELECT id FROM users WHERE email = ?");
        stmt.setString(1, email);
        ResultSet rs = stmt.executeQuery();
        int userId = rs.next() ? rs.getInt("id") : -1;
        rs.close(); stmt.close(); conn.close();
        return userId;
    } catch (Exception e) { e.printStackTrace(); return -1; }
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
        System.out.println("[Notif] ✅ Inserted: " + type + " - " + refId);
    } else {
        System.out.println("[Notif] ⏭ Already exists: " + type + " - " + refId);
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
    } catch (Exception e) { e.printStackTrace(); }
}

private void syncNotifications() {
    cleanupNotifications();
    int userId = getCurrentUserId();
    if (userId == -1) {
        System.out.println("[Sync] ❌ No user found, aborting");
        return;
    }
    System.out.println("[Sync] ✅ Running for userId=" + userId);
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
    } catch (Exception e) { e.printStackTrace(); }
}

private void markOneAsRead(String notifId) {
    try {
        Connection conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(true);
        PreparedStatement stmt = conn.prepareStatement(
            "UPDATE notifications SET is_read = TRUE WHERE notif_id = ?");
        stmt.setInt(1, Integer.parseInt(notifId));
        stmt.executeUpdate(); stmt.close(); conn.close();
    } catch (Exception e) { e.printStackTrace(); }
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
    } catch (Exception e) { e.printStackTrace(); }
}

@FXML
private void handleAlertsClick() {
    Stage alertStage = new Stage();
    alertStage.initModality(Modality.APPLICATION_MODAL);
    alertStage.initOwner(logoutButton.getScene().getWindow());
    alertStage.setTitle("Notifications");
    alertStage.setResizable(false);

    VBox root = new VBox(0);
    root.setStyle("-fx-background-color: #ffffff; -fx-min-width: 480; -fx-max-width: 480;");

    VBox header = new VBox(4);
    header.setFocusTraversable(true);
    header.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 20 24;");
    Label titleLbl = new Label("Notifications");
    titleLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
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
    Button markAllBtn = new Button("Mark All as Read");
    markAllBtn.setStyle(
        "-fx-background-color: #f4f4f4; -fx-text-fill: #555555;" +
        "-fx-font-size: 11px; -fx-background-radius: 20;" +
        "-fx-border-color: #e0e0e0; -fx-border-width: 1;" +
        "-fx-padding: 5 14; -fx-cursor: hand;");

    Region filterSpacer = new Region();
    HBox.setHgrow(filterSpacer, Priority.ALWAYS);

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
        int userId = getCurrentUserId();
        if (userId == -1) return;
        try {
            Connection conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(true);
            String sql = showingPast[0]
                ? "SELECT * FROM notifications WHERE user_id = ? ORDER BY notif_id DESC"
                : "SELECT * FROM notifications WHERE user_id = ? AND is_read = FALSE ORDER BY notif_id DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            List<String[]> items = new ArrayList<>();
            while (rs.next()) {
                items.add(new String[]{
                    rs.getString("notif_id"), rs.getString("type"),
                    rs.getString("message"), rs.getString("is_read"),
                    rs.getString("created_at")
                });
            }
            rs.close(); stmt.close(); conn.close();
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

    markAllBtn.setOnAction(e -> {
        int userId = getCurrentUserId();
        if (userId == -1) return;
        try {
            Connection conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(true);
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE");
            stmt.setInt(1, userId);
            int updated = stmt.executeUpdate();
            stmt.close(); conn.close();
            System.out.println("[Notif] ✅ Marked all as read: " + updated + " notifications");
            loadNotifsRef[0].run();
            refreshAlertBadge();
        } catch (Exception ex) { ex.printStackTrace(); }
    });

    filterRow.getChildren().addAll(unreadBtn, pastBtn, markAllBtn, filterSpacer);

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

    // ← document type added
    String icon, bg;
    if      ("complaint".equals(type)) { icon = "📢"; bg = "#ffebee"; }
    else if ("payment".equals(type))   { icon = "💳"; bg = "#fff8e1"; }
    else if ("document".equals(type))  { icon = "📄"; bg = "#e8f5e9"; }
    else                               { icon = "📣"; bg = "#e3f2fd"; }

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
    // ← document type added
    Label titleLbl = new Label(
        "complaint".equals(type) ? "Complaint Alert"  :
        "payment".equals(type)   ? "Payment Alert"    :
        "document".equals(type)  ? "Document Request" : "Announcement");
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

    // ← document type added
    String goToLabel =
        "complaint".equals(type) ? "→  Go to Complaints" :
        "payment".equals(type)   ? "→  Go to Payments"   :
        "document".equals(type)  ? "→  Go to Documents"  : "→  Go to Announcements";
    String goToFxml =
        "complaint".equals(type) ? "KagawadComplaints.fxml" :
        "payment".equals(type)   ? "KagawadPayments.fxml"   :
        "document".equals(type)  ? "KagawadDocuments.fxml"  : "KagawadAnnouncements.fxml";

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

    private void loadSettingsFromDB() {
        String email = SessionManager.getEmail();
        if (email == null) return;
        try {
            Connection conn = DatabaseConnection.getConnection();
            
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
            
            ResultSet rs = conn.prepareStatement(
                "SELECT * FROM settings WHERE user_id = " + userId
            ).executeQuery();
            if (rs.next()) {
                notifComplaints    = "true".equalsIgnoreCase(rs.getString("notif_complaints"));
                notifPayments      = "true".equalsIgnoreCase(rs.getString("notif_payments"));
                notifAnnouncements = "true".equalsIgnoreCase(rs.getString("notif_announcements"));
                String fs = rs.getString("font_size");
                if (fs != null) currentFontSize = fs;
                String dm = rs.getString("dark_mode");
                isDarkModeState    = "true".equalsIgnoreCase(dm);
                
                // ✅ SYNC WITH THEME MANAGER
                ThemeManager.isDarkMode = isDarkModeState;
                
                System.out.println("[SettingsController] Loaded dark_mode from DB: " + dm + " -> " + isDarkModeState);
                System.out.println("[SettingsController] ThemeManager.isDarkMode synced to: " + ThemeManager.isDarkMode);
            } else {
                insertDefaultSettings(email, userId);
            }
            rs.close();
            conn.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void insertDefaultSettings(String email, int userId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO settings (user_id, dark_mode, font_size, " +
                "notif_complaints, notif_payments, notif_announcements, base_population) " +
                "VALUES (?, 'false', 'Medium', 'true', 'true', 'true', 6474)");
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            stmt.close();
            conn.close();
            
            isDarkModeState = false;
            ThemeManager.isDarkMode = false;
            System.out.println("[SettingsController] Created default settings with dark_mode = false");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setActiveTab(Button active, int tabIndex) {
        for (Button btn : new Button[]{
            tabGeneralBtn, tabNotifBtn, tabAppearanceBtn, tabDataBtn}) {
            btn.setStyle(inactiveTabStyle);
        }
        active.setStyle(activeTabStyle);
        currentTabIndex = tabIndex;
    }

     @FXML
     private void showGeneralTab() {
         setActiveTab(tabGeneralBtn, 0);
         tabContent.getChildren().setAll(generalTabNode);
     }

     @FXML
     private void showNotifTab() {
         setActiveTab(tabNotifBtn, 1);
         tabContent.getChildren().setAll(notifTabNode);
     }

     @FXML
     private void showAppearanceTab() {
         setActiveTab(tabAppearanceBtn, 2);
         tabContent.getChildren().setAll(appearanceTabNode);
     }

     @FXML
     private void showDataTab() {
         setActiveTab(tabDataBtn, 3);
         tabContent.getChildren().setAll(dataTabNode);
     }


private Node buildGeneralTab() {

    boolean dark = ThemeManager.isDarkMode;

    VBox container = new VBox(0);
    container.setMaxWidth(Double.MAX_VALUE);

    String containerBg = dark ? "#1a1a1a" : "#ffffff";
    String containerBorder = dark ? "#333333" : "#e8e8e8";
    String headerBg = "#1a1a1a";

    container.setStyle(
        "-fx-background-color: " + containerBg + "; -fx-background-radius: 16;" +
        "-fx-border-color: " + containerBorder + "; -fx-border-width: 1;"
    );

    VBox header = new VBox(4);
    header.setStyle(
        "-fx-background-color: " + headerBg + "; -fx-background-radius: 16 16 0 0;" +
        "-fx-padding: 22 28;"
    );

    Label title = new Label("General Settings");
    title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

    Label sub = new Label("Configure system-wide preferences");
    sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");

    header.getChildren().addAll(title, sub);

    VBox body = new VBox(0);

    String bodyBg = dark ? "#1a1a1a" : "#ffffff";
    String fieldBg = dark ? "#242424" : "#f8f9fa";
    String fieldBorder = dark ? "#444444" : "#e8e8e8";
    String fieldText = dark ? "#ffffff" : "#000000";

    body.setStyle("-fx-padding: 0; -fx-background-color: " + bodyBg + ";");

    TextField popField = new TextField();
    popField.setStyle(
        "-fx-font-size: 13px; -fx-padding: 10 14; -fx-background-radius: 10;" +
        "-fx-border-color: " + fieldBorder + "; -fx-border-width: 1; -fx-border-radius: 10;" +
        "-fx-background-color: " + fieldBg + "; -fx-text-fill: " + fieldText + ";" +
        "-fx-max-width: 200;"
    );
    popField.setPromptText("e.g. 6474");

    Label popErrorLbl = new Label("");
    popErrorLbl.setStyle("-fx-font-size: 11px;");

    try {
        Connection conn = DatabaseConnection.getConnection();

        String email = SessionManager.getEmail();
        PreparedStatement userStmt = conn.prepareStatement("SELECT id FROM users WHERE email = ?");
        userStmt.setString(1, email);
        ResultSet userRs = userStmt.executeQuery();

        int userId = -1;
        if (userRs.next()) userId = userRs.getInt("id");

        userRs.close();
        userStmt.close();

        if (userId != -1) {
            ResultSet rs = conn.prepareStatement(
                "SELECT base_population FROM settings WHERE user_id = " + userId
            ).executeQuery();

            if (rs.next()) {
                popField.setText(String.valueOf(rs.getInt("base_population")));
            }

            rs.close();
        }

        conn.close();

    } catch (Exception e) {
        e.printStackTrace();
    }

    Button savePopBtn = new Button("Save");
    savePopBtn.setStyle(
        "-fx-background-color: #1a1a1a; -fx-text-fill: #ffffff;" +
        "-fx-font-size: 12px; -fx-font-weight: bold;" +
        "-fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;"
    );

    savePopBtn.setOnAction(e -> {
        try {
            int pop = Integer.parseInt(popField.getText().trim());

            Connection conn = DatabaseConnection.getConnection();

            String email = SessionManager.getEmail();
            PreparedStatement userStmt = conn.prepareStatement("SELECT id FROM users WHERE email = ?");
            userStmt.setString(1, email);
            ResultSet userRs = userStmt.executeQuery();

            int userId = -1;
            if (userRs.next()) userId = userRs.getInt("id");

            userRs.close();
            userStmt.close();

            if (userId != -1) {
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE settings SET base_population = ? WHERE user_id = ?"
                );
                stmt.setInt(1, pop);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
                stmt.close();
            }

            conn.close();

            setStatus(popErrorLbl, "✅ Saved!", true);

        } catch (Exception ex) {
            setStatus(popErrorLbl, "⚠ Enter a valid number.", false);
        }
    });

    HBox popRow = new HBox(12);
    popRow.setStyle("-fx-alignment: CENTER_LEFT;");
    popRow.getChildren().addAll(popField, savePopBtn, popErrorLbl);

    body.getChildren().addAll(
        buildSettingRow(
            "Population Base",
            "The base census count added to your DB resident count on the dashboard",
            popRow,
            false
        ),
        buildSettingRow(
            "System Version",
            "Current version of the Barangay Management System",
            buildReadOnlyValue("v1.0.0"),
            false
        ),
        buildSettingRow(
            "Database",
            "Connected to Microsoft Access database",
            buildReadOnlyValue("MS Access — Connected ✅"),
            true
        )
    );

    container.getChildren().addAll(header, body);
    return container;
}
    private Node buildNotifTab() {
        VBox container = new VBox(0);
        container.setMaxWidth(Double.MAX_VALUE);
        
        String containerBg = ThemeManager.isDarkMode ? "#1a1a1a" : "#ffffff";
        String containerBorder = ThemeManager.isDarkMode ? "#333333" : "#e8e8e8";
        String headerBg = "#1a1a1a";
        
        container.setStyle(
            "-fx-background-color: " + containerBg + "; -fx-background-radius: 16;" +
            "-fx-border-color: " + containerBorder + "; -fx-border-width: 1;");

        VBox header = new VBox(4);
        header.setStyle(
            "-fx-background-color: " + headerBg + "; -fx-background-radius: 16 16 0 0;" +
            "-fx-padding: 22 28;");
        Label title = new Label("Notification Settings");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        Label sub = new Label("Choose which alerts you want to receive");
        sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");
        header.getChildren().addAll(title, sub);

        complaintsToggle    = buildToggle(notifComplaints);
        paymentsToggle      = buildToggle(notifPayments);
        announcementsToggle = buildToggle(notifAnnouncements);

        Label errorLbl = new Label("");
        errorLbl.setStyle("-fx-font-size: 11px; -fx-padding: 0 28;");

        Button saveBtn = new Button("Save Notifications");
        saveBtn.setStyle(
            "-fx-background-color: #1a1a1a; -fx-text-fill: #ffffff;" +
            "-fx-font-size: 12px; -fx-font-weight: bold;" +
            "-fx-background-radius: 8; -fx-padding: 11 24; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> {
            try {
                Connection conn = DatabaseConnection.getConnection();
                
                String email = SessionManager.getEmail();
                PreparedStatement userStmt = conn.prepareStatement("SELECT id FROM users WHERE email = ?");
                userStmt.setString(1, email);
                ResultSet userRs = userStmt.executeQuery();
                int userId = -1;
                if (userRs.next()) userId = userRs.getInt("id");
                userRs.close();
                userStmt.close();
                
                if (userId != -1) {
                    PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE settings SET notif_complaints=?, notif_payments=?," +
                        " notif_announcements=? WHERE user_id=?");
                    stmt.setString(1, isToggleOn(complaintsToggle)    ? "true" : "false");
                    stmt.setString(2, isToggleOn(paymentsToggle)      ? "true" : "false");
                    stmt.setString(3, isToggleOn(announcementsToggle) ? "true" : "false");
                    stmt.setInt(4, userId);
                    stmt.executeUpdate();
                    stmt.close();
                }
                conn.close();
                setStatus(errorLbl, "✅  Notification settings saved!", true);
            } catch (Exception ex) {
                ex.printStackTrace();
                setStatus(errorLbl, "⚠  Database error.", false);
            }
        });

        HBox footer = new HBox(10);
        footer.setStyle(
            "-fx-padding: 20 28; -fx-alignment: CENTER_RIGHT;" +
            "-fx-border-color: #f4f4f4; -fx-border-width: 1 0 0 0;");
        footer.getChildren().addAll(errorLbl, saveBtn);

        VBox body = new VBox(0);
        String bodyBg = ThemeManager.isDarkMode ? "#1a1a1a" : "#ffffff";
        body.setStyle("-fx-background-color: " + bodyBg + ";");
        
        body.getChildren().addAll(
            buildToggleRow("Complaint Alerts",
                "Get notified when a new complaint is filed",
                complaintsToggle, false),
            buildToggleRow("Payment Alerts",
                "Get notified when a payment is made or goes pending",
                paymentsToggle, false),
            buildToggleRow("Announcement Alerts",
                "Get notified when a new announcement is posted",
                announcementsToggle, true)
        );

        container.getChildren().addAll(header, body, footer);
        return container;
    }

    private Node buildAppearanceTab() {
        VBox container = new VBox(0);
        container.setMaxWidth(Double.MAX_VALUE);
        
        String containerBg = ThemeManager.isDarkMode ? "#1a1a1a" : "#ffffff";
        String containerBorder = ThemeManager.isDarkMode ? "#333333" : "#e8e8e8";
        String headerBg = "#1a1a1a";
        
        container.setStyle(
            "-fx-background-color: " + containerBg + "; -fx-background-radius: 16;" +
            "-fx-border-color: " + containerBorder + "; -fx-border-width: 1;");

        VBox header = new VBox(4);
        header.setStyle(
            "-fx-background-color: " + headerBg + "; -fx-background-radius: 16 16 0 0;" +
            "-fx-padding: 22 28;");
        Label title = new Label("Appearance");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        Label sub = new Label("Customize how the system looks and feels");
        sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");
        header.getChildren().addAll(title, sub);

        Button fontSmallBtn  = new Button("Small");
        Button fontMediumBtn = new Button("Medium");
        Button fontLargeBtn  = new Button("Large");

        Label fontErrorLbl = new Label("");
        fontErrorLbl.setStyle("-fx-font-size: 11px;");

        Runnable updateFontBtns = () -> {
            String active =
                "-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff;" +
                "-fx-font-size: 12px; -fx-font-weight: bold;" +
                "-fx-background-radius: 8; -fx-padding: 9 20; -fx-cursor: hand;";
            String inactive =
                "-fx-background-color: #f4f4f4; -fx-text-fill: #555555;" +
                "-fx-font-size: 12px; -fx-background-radius: 8;" +
                "-fx-border-color: #e0e0e0; -fx-border-width: 1;" +
                "-fx-padding: 9 20; -fx-cursor: hand;";
            fontSmallBtn.setStyle(currentFontSize.equals("Small")   ? active : inactive);
            fontMediumBtn.setStyle(currentFontSize.equals("Medium") ? active : inactive);
            fontLargeBtn.setStyle(currentFontSize.equals("Large")   ? active : inactive);
        };
        updateFontBtns.run();

        fontSmallBtn.setOnAction(e  -> { currentFontSize = "Small";  updateFontBtns.run(); });
        fontMediumBtn.setOnAction(e -> { currentFontSize = "Medium"; updateFontBtns.run(); });
        fontLargeBtn.setOnAction(e  -> { currentFontSize = "Large";  updateFontBtns.run(); });

        Button saveFontBtn = new Button("Save");
        saveFontBtn.setStyle(
            "-fx-background-color: #1a1a1a; -fx-text-fill: #ffffff;" +
            "-fx-font-size: 12px; -fx-font-weight: bold;" +
            "-fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;");
        saveFontBtn.setOnAction(e -> {
            try {
                Connection conn = DatabaseConnection.getConnection();
                
                String email = SessionManager.getEmail();
                PreparedStatement userStmt = conn.prepareStatement("SELECT id FROM users WHERE email = ?");
                userStmt.setString(1, email);
                ResultSet userRs = userStmt.executeQuery();
                int userId = -1;
                if (userRs.next()) userId = userRs.getInt("id");
                userRs.close();
                userStmt.close();
                
                if (userId != -1) {
                    PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE settings SET font_size = ? WHERE user_id = ?");
                    stmt.setString(1, currentFontSize);
                    stmt.setInt(2, userId);
                    stmt.executeUpdate();
                    stmt.close();
                }
                conn.close();
                setStatus(fontErrorLbl, "✅  Saved!", true);
            } catch (Exception ex) {
                setStatus(fontErrorLbl, "⚠  Error saving.", false);
            }
        });

        HBox fontRow = new HBox(10);
        fontRow.setStyle("-fx-alignment: CENTER_LEFT;");
        fontRow.getChildren().addAll(
            fontSmallBtn, fontMediumBtn, fontLargeBtn, saveFontBtn, fontErrorLbl);

        System.out.println("[SettingsController] isDarkModeState = " + isDarkModeState + ", ThemeManager.isDarkMode = " + ThemeManager.isDarkMode);
        darkModeToggle = buildToggle(isDarkModeState);
        
        Label darkModeErrorLbl = new Label("");
        darkModeErrorLbl.setStyle("-fx-font-size: 11px;");

        HBox darkModeControlRow = new HBox(12);
        darkModeControlRow.setStyle("-fx-alignment: CENTER_LEFT;");
        darkModeControlRow.getChildren().addAll(darkModeToggle, darkModeErrorLbl);

        darkModeToggle.setOnMouseClicked(e -> {
            System.out.println("\n========== [SETTINGS] DARK MODE TOGGLE CLICKED ==========");
            boolean currentState = (boolean) darkModeToggle.getUserData();
            System.out.println("[SETTINGS] Current state: " + currentState);
            
            boolean newDarkMode = !currentState;
            System.out.println("[SETTINGS] New state: " + newDarkMode);
            
            Rectangle track = (Rectangle) darkModeToggle.getChildren().get(0);
            Circle thumb = (Circle) darkModeToggle.getChildren().get(1);
            
            TranslateTransition tt = new TranslateTransition(Duration.millis(200), thumb);
            if (newDarkMode) {
                track.setFill(Color.web("#2e7d32"));
                tt.setToX(12);
            } else {
                track.setFill(Color.web("#cccccc"));
                tt.setToX(-12);
            }
            tt.play();
            
            darkModeToggle.setUserData(newDarkMode);
            isDarkModeState = newDarkMode;
            
            // ✅ SAVE TO DATABASE IN BACKGROUND
            executorService.execute(() -> {
                try {
                    Connection conn = DatabaseConnection.getConnection();
                    
                    String email = SessionManager.getEmail();
                    PreparedStatement userStmt = conn.prepareStatement("SELECT id FROM users WHERE email = ?");
                    userStmt.setString(1, email);
                    ResultSet userRs = userStmt.executeQuery();
                    int userId = -1;
                    if (userRs.next()) userId = userRs.getInt("id");
                    userRs.close();
                    userStmt.close();
                    
                    if (userId != -1) {
                        PreparedStatement stmt = conn.prepareStatement(
                            "UPDATE settings SET dark_mode = ? WHERE user_id = ?");
                        stmt.setString(1, newDarkMode ? "true" : "false");
                        stmt.setInt(2, userId);
                        int updated = stmt.executeUpdate();
                        System.out.println("[SETTINGS] DB updated: " + updated + " rows");
                        stmt.close();
                    }
                    conn.close();
                    
                    // ✅ VERIFY IT WAS SAVED
                    conn = DatabaseConnection.getConnection();
                    userStmt = conn.prepareStatement("SELECT id FROM users WHERE email = ?");
                    userStmt.setString(1, email);
                    userRs = userStmt.executeQuery();
                    userId = -1;
                    if (userRs.next()) userId = userRs.getInt("id");
                    userRs.close();
                    userStmt.close();
                    
                    if (userId != -1) {
                        PreparedStatement verifyStmt = conn.prepareStatement(
                            "SELECT dark_mode FROM settings WHERE user_id = ?");
                        verifyStmt.setInt(1, userId);
                        ResultSet verifyRs = verifyStmt.executeQuery();
                        if (verifyRs.next()) {
                            String savedValue = verifyRs.getString("dark_mode");
                            System.out.println("[SETTINGS] Verified saved value: " + savedValue);
                        }
                        verifyRs.close();
                        verifyStmt.close();
                    }
                    conn.close();
                    
                    setStatus(darkModeErrorLbl, "✅  Theme saved!", true);
                } catch (Exception ex) {
                    System.out.println("[ERROR] " + ex.getMessage());
                    ex.printStackTrace();
                    setStatus(darkModeErrorLbl, "⚠  Error saving theme.", false);
                }
            });
            
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            ThemeManager.isDarkMode = newDarkMode;
            System.out.println("[SETTINGS] ThemeManager.isDarkMode set to: " + ThemeManager.isDarkMode);
            
            System.out.println("[SETTINGS] Rebuilding all tabs with new theme...");
            Platform.runLater(() -> {
                generalTabNode = buildGeneralTab();
                notifTabNode = buildNotifTab();
                dataTabNode = buildDataTab();
                appearanceTabNode = buildAppearanceTab();

                switch (currentTabIndex) {
                    case 0:
                        showGeneralTab();
                        break;
                    case 1:
                        showNotifTab();
                        break;
                    case 2:
                        showAppearanceTab();
                        break;
                    case 3:
                        showDataTab();
                        break;
                }
            });

            
            System.out.println("[SETTINGS] Applying theme to stage...");
            ThemeManager.applyTheme(stage);
            
            if (currentTabIndex == 0) showGeneralTab();
            else if (currentTabIndex == 1) showNotifTab();
            else if (currentTabIndex == 2) showAppearanceTab();
            else if (currentTabIndex == 3) showDataTab();
            
            if (mainScrollPane != null) {
                mainScrollPane.layout();
            }
            
            System.out.println("[SETTINGS] Theme toggled successfully");
            System.out.println("========== [SETTINGS] DARK MODE TOGGLE FINISHED ==========\n");
        });

        VBox body = new VBox(0);
        String bodyBg = ThemeManager.isDarkMode ? "#1a1a1a" : "#ffffff";
        body.setStyle("-fx-background-color: " + bodyBg + ";");
        
        body.getChildren().addAll(
            buildSettingRow("Font Size",
                "Adjust the text size throughout the system",
                fontRow, false),
            buildSettingRow("Dark Mode",
                "Switch between light and dark interface theme",
                darkModeControlRow, true)
        );

        container.getChildren().addAll(header, body);
        return container;
    }

    private Node buildDataTab() {
        VBox container = new VBox(0);
        container.setMaxWidth(Double.MAX_VALUE);
        
        String containerBg = ThemeManager.isDarkMode ? "#1a1a1a" : "#ffffff";
        String containerBorder = ThemeManager.isDarkMode ? "#333333" : "#e8e8e8";
        String headerBg = "#1a1a1a";
        
        container.setStyle(
            "-fx-background-color: " + containerBg + "; -fx-background-radius: 16;" +
            "-fx-border-color: " + containerBorder + "; -fx-border-width: 1;");

        VBox header = new VBox(4);
        header.setStyle(
            "-fx-background-color: " + headerBg + "; -fx-background-radius: 16 16 0 0;" +
            "-fx-padding: 22 28;");
        Label title = new Label("Data and Export");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        Label sub = new Label("Export records and manage system data");
        sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");
        header.getChildren().addAll(title, sub);

        Label resLbl = new Label("");
        resLbl.setStyle("-fx-font-size: 11px;");
        Button exportResBtn = new Button("Export CSV");
        exportResBtn.setStyle(buildActionBtnStyle("#e8f5e9", "#2e7d32", "#c8e6c9"));
        exportResBtn.setOnAction(e -> {
            exportResidents();
            setStatus(resLbl, "✅  Exported!", true);
        });
        HBox resRow = new HBox(12, exportResBtn, resLbl);
        resRow.setStyle("-fx-alignment: CENTER_LEFT;");

        Label payLbl = new Label("");
        payLbl.setStyle("-fx-font-size: 11px;");
        Button exportPayBtn = new Button("Export CSV");
        exportPayBtn.setStyle(buildActionBtnStyle("#e8f5e9", "#2e7d32", "#c8e6c9"));
        exportPayBtn.setOnAction(e -> {
            exportPayments();
            setStatus(payLbl, "✅  Exported!", true);
        });
        HBox payRow = new HBox(12, exportPayBtn, payLbl);
        payRow.setStyle("-fx-alignment: CENTER_LEFT;");

        Label finLbl = new Label("");
        finLbl.setStyle("-fx-font-size: 11px;");
        Button exportFinBtn = new Button("Export CSV");
        exportFinBtn.setStyle(buildActionBtnStyle("#e8f5e9", "#2e7d32", "#c8e6c9"));
        exportFinBtn.setOnAction(e -> {
            exportFinances();
            setStatus(finLbl, "✅  Exported!", true);
        });
        HBox finRow = new HBox(12, exportFinBtn, finLbl);
        finRow.setStyle("-fx-alignment: CENTER_LEFT;");

        Label logLbl = new Label("");
        logLbl.setStyle("-fx-font-size: 11px;");
        Button clearLogsBtn = new Button("Clear Logs");
        clearLogsBtn.setStyle(buildActionBtnStyle("#ffebee", "#c62828", "#ffcdd2"));
        clearLogsBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Clear Logs");
            confirm.setHeaderText("Clear all activity logs?");
            confirm.setContentText("This cannot be undone.");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    try {
                        Connection conn = DatabaseConnection.getConnection();
                        conn.prepareStatement("DELETE FROM logs").executeUpdate();
                        conn.close();
                        setStatus(logLbl, "✅  Logs cleared!", true);
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            });
        });
        HBox logRow = new HBox(12, clearLogsBtn, logLbl);
        logRow.setStyle("-fx-alignment: CENTER_LEFT;");

        VBox body = new VBox(0);
        String bodyBg = ThemeManager.isDarkMode ? "#1a1a1a" : "#ffffff";
        body.setStyle("-fx-background-color: " + bodyBg + ";");
        
        body.getChildren().addAll(
            buildSettingRow("Export Residents",
                "Download all resident records as a CSV file",
                resRow, false),
            buildSettingRow("Export Payments",
                "Download all payment records as a CSV file",
                payRow, false),
            buildSettingRow("Export Finances",
                "Download all income and expense records as a CSV file",
                finRow, false),
            buildSettingRow("Clear Activity Logs",
                "Permanently delete all system logs — this cannot be undone",
                logRow, true)
        );

        container.getChildren().addAll(header, body);
        return container;
    }

    private HBox buildSettingRow(String title, String description,
                                  Node control, boolean isLast) {
        HBox row = new HBox(16);
        
        String rowBg = ThemeManager.isDarkMode ? "#1a1a1a" : "#ffffff";
        String textColor = ThemeManager.isDarkMode ? "#ffffff" : "#1a1a1a";
        String descColor = ThemeManager.isDarkMode ? "#aaaaaa" : "#aaaaaa";
        String borderColor = ThemeManager.isDarkMode ? "#333333" : "#f4f4f4";
        
        row.setStyle(
            "-fx-padding: 20 28; -fx-background-color: " + rowBg + ";" +
            (isLast ? "" : "-fx-border-color: " + borderColor + "; -fx-border-width: 0 0 1 0;"));
        row.setAlignment(Pos.CENTER_LEFT);

        VBox textBox = new VBox(4);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        Label titleLbl = new Label(title);
        titleLbl.setStyle(
            "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");
        Label descLbl = new Label(description);
        descLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + descColor + ";");
        descLbl.setWrapText(true);
        textBox.getChildren().addAll(titleLbl, descLbl);

        row.getChildren().addAll(textBox, control);
        return row;
    }

    private HBox buildToggleRow(String title, String description,
                                 StackPane toggle, boolean isLast) {
        HBox row = new HBox(16);
        
        String rowBg = ThemeManager.isDarkMode ? "#1a1a1a" : "#ffffff";
        String textColor = ThemeManager.isDarkMode ? "#ffffff" : "#1a1a1a";
        String descColor = ThemeManager.isDarkMode ? "#aaaaaa" : "#aaaaaa";
        String borderColor = ThemeManager.isDarkMode ? "#333333" : "#f4f4f4";
        
        row.setStyle(
            "-fx-padding: 20 28; -fx-background-color: " + rowBg + ";" +
            (isLast ? "" : "-fx-border-color: " + borderColor + "; -fx-border-width: 0 0 1 0;"));
        row.setAlignment(Pos.CENTER_LEFT);

        VBox textBox = new VBox(4);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        Label titleLbl = new Label(title);
        titleLbl.setStyle(
            "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");
        Label descLbl = new Label(description);
        descLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + descColor + ";");
        textBox.getChildren().addAll(titleLbl, descLbl);

        row.getChildren().addAll(textBox, toggle);
        return row;
    }

    private Label buildReadOnlyValue(String value) {
        String valueBg = ThemeManager.isDarkMode ? "#242424" : "#f4f4f4";
        String valueText = ThemeManager.isDarkMode ? "#e8e8e8" : "#555555";
        
        Label lbl = new Label(value);
        lbl.setStyle(
            "-fx-font-size: 12px; -fx-text-fill: " + valueText + ";" +
            "-fx-background-color: " + valueBg + "; -fx-background-radius: 8;" +
            "-fx-padding: 8 14;");
        return lbl;
    }

    private String buildActionBtnStyle(String bg, String fg, String border) {
        return "-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";" +
               "-fx-font-size: 12px; -fx-font-weight: bold;" +
               "-fx-background-radius: 8; -fx-border-color: " + border + ";" +
               "-fx-border-width: 1; -fx-padding: 9 18; -fx-cursor: hand;";
    }

    private StackPane buildToggle(boolean initialState) {
        Rectangle track = new Rectangle(50, 26);
        track.setArcWidth(26);
        track.setArcHeight(26);

        Circle thumb = new Circle(11);
        thumb.setStyle("-fx-fill: #ffffff;");

        StackPane toggle = new StackPane(track, thumb);
        toggle.setPrefSize(50, 26);
        toggle.setStyle("-fx-cursor: hand;");
        toggle.setUserData(initialState);

        if (initialState) {
            track.setFill(Color.web("#2e7d32"));
            thumb.setTranslateX(12);
        } else {
            track.setFill(Color.web("#cccccc"));
            thumb.setTranslateX(-12);
        }

        return toggle;
    }

    private boolean isToggleOn(StackPane toggle) {
        return toggle != null && (boolean) toggle.getUserData();
    }

    private void exportResidents() {
        try {
            String fileName = "residents_export_" + LocalDate.now() + ".csv";
            FileWriter writer = new FileWriter(fileName);
            writer.write("ID,Resident ID,Full Name,Age,Address,Status,Date Added\n");
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.prepareStatement(
                "SELECT id, resident_id, full_name, age, address, status, date_added " +
                "FROM residents ORDER BY id DESC").executeQuery();
            while (rs.next()) {
                writer.write(
                    rs.getInt("id") + "," +
                    clean(rs.getString("resident_id")) + "," +
                    clean(rs.getString("full_name")) + "," +
                    rs.getInt("age") + "," +
                    clean(rs.getString("address")) + "," +
                    clean(rs.getString("status")) + "," +
                    clean(rs.getString("date_added")) + "\n");
            }
            rs.close(); conn.close(); writer.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void exportPayments() {
        try {
            String fileName = "payments_export_" + LocalDate.now() + ".csv";
            FileWriter writer = new FileWriter(fileName);
            writer.write("ID,Ref,Resident,Type,Amount,Status,Date\n");
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.prepareStatement(
                "SELECT id, ref_number, resident_id, payment_type, " +
                "amount, status, date_created FROM payments ORDER BY id DESC"
            ).executeQuery();
            while (rs.next()) {
                writer.write(
                    rs.getInt("id") + "," +
                    clean(rs.getString("ref_number")) + "," +
                    rs.getInt("resident_id") + "," +
                    clean(rs.getString("payment_type")) + "," +
                    rs.getDouble("amount") + "," +
                    clean(rs.getString("status")) + "," +
                    clean(rs.getString("date_created")) + "\n");
            }
            rs.close(); conn.close(); writer.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void exportFinances() {
        try {
            String fileName = "finances_export_" + LocalDate.now() + ".csv";
            FileWriter writer = new FileWriter(fileName);
            writer.write("ID,Description,Category,Type,Amount,Status,Date\n");
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.prepareStatement(
                "SELECT id, description, category, type, amount, status, date_recorded " +
                "FROM finances ORDER BY id DESC"
            ).executeQuery();
            while (rs.next()) {
                writer.write(
                    rs.getInt("id") + "," +
                    clean(rs.getString("description")) + "," +
                    clean(rs.getString("category")) + "," +
                    clean(rs.getString("type")) + "," +
                    rs.getDouble("amount") + "," +
                    clean(rs.getString("status")) + "," +
                    clean(rs.getString("date_recorded")) + "\n");
            }
            rs.close(); conn.close(); writer.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private String clean(String s) {
        if (s == null) return "";
        return "\"" + s.replace("\"", "''") + "\"";
    }

    private void setStatus(Label lbl, String msg, boolean isSuccess) {
        lbl.setText(msg);
        lbl.setStyle(isSuccess
            ? "-fx-text-fill: #2e7d32; -fx-font-size: 11px;"
            : "-fx-text-fill: #c62828; -fx-font-size: 11px;");
    }

    @FXML private void handleLogout() {
        SessionManager.logout();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "login.fxml", false, getClass());
    }
    @FXML private void goToDashboard() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "KagawadAdminDashboard.fxml", true, getClass());
    }
    @FXML private void goToResidents() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "KagawadResidents.fxml", true, getClass());
    }
    @FXML private void goToDocuments() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "KagawadDocuments.fxml", true, getClass());
    }
    @FXML private void goToPayments() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "KagawadPayments.fxml", true, getClass());
    }
    @FXML private void goToArchive() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "PaymentArchive.fxml", true, getClass());
    }
    @FXML private void goToComplaints() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "KagawadComplaints.fxml", true, getClass());
    }
    @FXML private void goToAnnouncements() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "KagawadAnnouncements.fxml", true, getClass());
    }
    @FXML private void goToFinances() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Kagawadfinances.fxml", true, getClass());
    }
    @FXML private void goToAdmin() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "KagawadAdmin.fxml", true, getClass());
    }
}