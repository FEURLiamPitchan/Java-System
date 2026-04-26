package com.mycompany.javasystem;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableRow;
import javafx.scene.control.Separator;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class KagawadController {

    @FXML private BorderPane rootPane;
    @FXML private ScrollPane mainScrollPane;
    @FXML private Button logoutButton;
    @FXML private Button alertsButton;
    @FXML private HBox avatarBox;
    @FXML private Circle avatarCircle;
    @FXML private ImageView profileImageView;
    @FXML private Label avatarInitialLabel;
    @FXML private Label topBarNameLabel;
    @FXML private Label topBarRoleLabel;
    @FXML private Label alertBadge;
    @FXML private StackPane tabContent;
    @FXML private Button tabUsersBtn;
    @FXML private Button tabLogsBtn;
    @FXML private Button tabPasswordBtn;
    @FXML private Button tabStatsBtn;

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
        syncNotifications();
        refreshAlertBadge();
        showUsersTab();
        
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

        // Apply theme with delay
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                if (stage != null && stage.getScene() != null) {
                    System.out.println("[AdminController] Applying theme - isDarkMode: " + ThemeManager.isDarkMode);
                    ThemeManager.applyTheme(stage);

                    if (ThemeManager.isDarkMode) {
                        Platform.runLater(() -> {
                            applyThemeToRoot();
                            applyDarkModeToCurrentTab();
                        });
                    }
                }
            } catch (Exception e) {
                System.out.println("[AdminController] Error applying theme: " + e.getMessage());
            }
        });
    }

    // Apply theme colors to entire page
    private void applyThemeToRoot() {
        if (!ThemeManager.isDarkMode) return;
        
        Platform.runLater(() -> {
            try {
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
                System.out.println("[AdminController] Error applying theme to root: " + e.getMessage());
            }
        });
    }

    // Apply dark mode to current tab content
    private void applyDarkModeToCurrentTab() {
        try {
            if (!ThemeManager.isDarkMode || tabContent == null) return;
            
            for (javafx.scene.Node node : tabContent.getChildren()) {
                if (node instanceof VBox) {
                    VBox tabVBox = (VBox) node;
                    styleNodeDarkMode(tabVBox);
                }
            }
        } catch (Exception e) {
            System.out.println("[AdminController] Error applying dark mode to tab: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Recursively style all nodes with stricter logic
    private void styleNodeDarkMode(javafx.scene.Node node) {
        if (node instanceof Label) {
            Label lbl = (Label) node;
            String style = lbl.getStyle();
            if (style != null) {
                style = style.replace("-fx-text-fill: #1a1a1a;", "-fx-text-fill: #ffffff;");
                style = style.replace("-fx-text-fill: #333333;", "-fx-text-fill: #e8e8e8;");
                style = style.replace("-fx-text-fill: #555555;", "-fx-text-fill: #b0b0b0;");
                style = style.replace("-fx-text-fill: #666666;", "-fx-text-fill: #aaaaaa;");
                style = style.replace("-fx-text-fill: #777777;", "-fx-text-fill: #999999;");
                style = style.replace("-fx-text-fill: #999999;", "-fx-text-fill: #888888;");
                style = style.replace("-fx-text-fill: #aaaaaa;", "-fx-text-fill: #888888;");
                lbl.setStyle(style);
            }
        } else if (node instanceof Button) {
            Button btn = (Button) node;
            String style = btn.getStyle();
            if (style != null && !style.contains("#e8f5e9") && !style.contains("#ffebee") && 
                !style.contains("#e3f2fd") && !style.contains("#fce4ec")) {
                if (style.contains("-fx-background-color: #f4f4f4") ||
                    style.contains("-fx-background-color: #f8f9fa")) {
                    style = style.replace("-fx-background-color: #f4f4f4;", "-fx-background-color: #2a2a2a;");
                    style = style.replace("-fx-background-color: #f8f9fa;", "-fx-background-color: #2a2a2a;");
                    style = style.replace("-fx-text-fill: #333333;", "-fx-text-fill: #e8e8e8;");
                    style = style.replace("-fx-text-fill: #555555;", "-fx-text-fill: #b0b0b0;");
                    btn.setStyle(style);
                }
            }
        } else if (node instanceof VBox) {
            VBox vbox = (VBox) node;
            String style = vbox.getStyle();

            // Skip colored stat cards
            if (style != null && (style.contains("#e8f5e9") || style.contains("#ffebee") || 
                style.contains("#e3f2fd") || style.contains("#fce4ec") || 
                style.contains("#fff8e1") || style.contains("#f3e5f5") ||
                style.contains("#1a1a1a"))) {
                for (javafx.scene.Node child : vbox.getChildren()) {
                    styleNodeDarkMode(child);
                }
                return;
            }

            // Convert white containers to dark
            if (style != null && style.contains("-fx-background-color: #ffffff")) {
                vbox.setStyle(
                    "-fx-background-color: #1a1a1a;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 12, 0, 0, 3);");
            }

            for (javafx.scene.Node child : vbox.getChildren()) {
                styleNodeDarkMode(child);
            }
        } else if (node instanceof HBox) {
            HBox hbox = (HBox) node;
            String style = hbox.getStyle();
            if (style != null) {
                if (style.contains("#e8f5e9") || style.contains("#ffebee") || 
                    style.contains("#e3f2fd") || style.contains("#fce4ec") ||
                    style.contains("#fff8e1") || style.contains("#f3e5f5") ||
                    style.contains("#1a1a1a")) {
                    for (javafx.scene.Node child : hbox.getChildren()) {
                        styleNodeDarkMode(child);
                    }
                    return;
                }

                if (style.contains("-fx-background-color: #ffffff")) {
                    hbox.setStyle("-fx-background-color: #1a1a1a;");
                }
                if (style.contains("-fx-background-color: #f8f9fa")) {
                    style = style.replace("-fx-background-color: #f8f9fa;", "-fx-background-color: #2a2a2a;");
                    hbox.setStyle(style);
                }
            }
            for (javafx.scene.Node child : hbox.getChildren()) {
                styleNodeDarkMode(child);
            }
        } else if (node instanceof TableView) {
            TableView<?> table = (TableView<?>) node;
            String style = table.getStyle();
            if (style != null) {
                style = style.replace("-fx-background-color: transparent;", "-fx-background-color: #1a1a1a;");
                table.setStyle(style);
            }
        } else if (node instanceof TextField) {
            TextField tf = (TextField) node;
            String style = tf.getStyle();
            if (style != null) {
                style = style.replace("-fx-background-color: #f8f8f8;", "-fx-background-color: #2a2a2a;");
                style = style.replace("-fx-background-color: #f8f9fa;", "-fx-background-color: #2a2a2a;");
                style = style.replace("-fx-text-fill: #333333;", "-fx-text-fill: #e8e8e8;");
                tf.setStyle(style);
            }
        } else if (node instanceof PasswordField) {
            PasswordField pf = (PasswordField) node;
            String style = pf.getStyle();
            if (style != null) {
                style = style.replace("-fx-background-color: #f8f8f8;", "-fx-background-color: #2a2a2a;");
                style = style.replace("-fx-background-color: #f8f9fa;", "-fx-background-color: #2a2a2a;");
                style = style.replace("-fx-text-fill: #333333;", "-fx-text-fill: #e8e8e8;");
                pf.setStyle(style);
            }
        } else if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                styleNodeDarkMode(child);
            }
        }
    }

    // TOP BAR
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

    @FXML
    private void handleAvatarClick() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Profile.fxml", true, getClass());
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
        "complaint".equals(type) ? "Complaints.fxml" :
        "payment".equals(type)   ? "Payments.fxml"   :
        "document".equals(type)  ? "Documents.fxml"  : "Announcements.fxml";

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
    // TAB SWITCHING
    private void setActiveTab(Button active) {
        for (Button btn : new Button[]{tabUsersBtn, tabLogsBtn, tabPasswordBtn, tabStatsBtn}) {
            btn.setStyle(inactiveTabStyle);
        }
        active.setStyle(activeTabStyle);
    }

    @FXML private void showUsersTab() {
        setActiveTab(tabUsersBtn);
        tabContent.getChildren().setAll(buildUsersTab());
        Platform.runLater(this::applyDarkModeToCurrentTab);
    }

    @FXML private void showLogsTab() {
        setActiveTab(tabLogsBtn);
        tabContent.getChildren().setAll(buildLogsTab());
        Platform.runLater(this::applyDarkModeToCurrentTab);
    }

    @FXML private void showPasswordTab() {
        setActiveTab(tabPasswordBtn);
        tabContent.getChildren().setAll(buildPasswordTab());
        Platform.runLater(this::applyDarkModeToCurrentTab);
    }

    @FXML private void showStatsTab() {
        setActiveTab(tabStatsBtn);
        tabContent.getChildren().setAll(buildStatsTab());
        Platform.runLater(this::applyDarkModeToCurrentTab);
    }
    
    // ── USERS TAB ─────────────────────────────────────────────────────────────────
    private Node buildUsersTab() {
        VBox container = new VBox(20);
        container.setMaxWidth(Double.MAX_VALUE);
        container.setStyle(
            "-fx-background-color: #ffffff; -fx-background-radius: 16;" +
            "-fx-border-color: #e8e8e8; -fx-border-width: 1; -fx-padding: 28;");

        // Mini summary cards
        int[] counts = getUserCounts();
        HBox cards = new HBox(14);
        cards.setMaxWidth(Double.MAX_VALUE);
        
        String totalUsersBg = ThemeManager.isDarkMode ? "#CCCCCC" : "#1a1a1a";
        
        cards.getChildren().addAll(
            buildMiniCard("Total Users", String.valueOf(counts[0]), totalUsersBg, "#ffffff", "👥"),
            buildMiniCard("Active",      String.valueOf(counts[1]), "#e8f5e9", "#2e7d32", "✅"),
            buildMiniCard("Inactive",    String.valueOf(counts[2]), "#ffebee", "#c62828", "🚫"),
            buildMiniCard("Admins",      String.valueOf(counts[3]), "#e3f2fd", "#1565c0", "⚙️")
        );

        // Header row
        HBox header = new HBox(10);
        header.setStyle("-fx-alignment: CENTER_LEFT;");
        VBox titleBox = new VBox(4);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        Label title = new Label("User Accounts");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
        Label subtitle = new Label("Manage roles, status and accounts");
        subtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaaaaa;");
        titleBox.getChildren().addAll(title, subtitle);

        TextField searchField = new TextField();
        searchField.setPromptText("Search by name or email...");
        searchField.setPrefWidth(240);
        searchField.setStyle(
            "-fx-font-size: 12px; -fx-padding: 9 14; -fx-background-radius: 8;" +
            "-fx-border-color: #e8e8e8; -fx-border-width: 1; -fx-border-radius: 8;" +
            "-fx-background-color: #f8f9fa;");

        Button exportBtn = new Button("Export CSV");
        exportBtn.setStyle(
            "-fx-background-color: #ffffff; -fx-text-fill: #555555;" +
            "-fx-font-size: 12px; -fx-background-radius: 8;" +
            "-fx-border-color: #e0e0e0; -fx-border-width: 1;" +
            "-fx-padding: 9 16; -fx-cursor: hand;");

        Label countLabel = new Label();
        countLabel.setStyle(
            "-fx-font-size: 11px; -fx-text-fill: #888888;" +
            "-fx-background-color: #f4f4f4; -fx-background-radius: 20; -fx-padding: 5 14;");
        header.getChildren().addAll(titleBox, searchField, exportBtn, countLabel);

        // Table
        TableView<String[]> table = new TableView<>();
        table.setMaxWidth(Double.MAX_VALUE);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(
            "-fx-background-color: transparent; -fx-border-color: #f0f0f0;" +
            "-fx-border-width: 1; -fx-border-radius: 10;" +
            "-fx-table-cell-border-color: #f8f8f8;");
        table.setPrefHeight(400);
        
        table.setRowFactory(tv -> new TableRow<String[]>() {
            @Override
            protected void updateItem(String[] item, boolean empty) {
                super.updateItem(item, empty);
                if (ThemeManager.isDarkMode) {
                    setStyle(empty || item == null
                        ? "-fx-background-color: transparent;"
                        : getIndex() % 2 == 0
                            ? "-fx-background-color: #1a1a1a; -fx-pref-height: 54px;"
                            : "-fx-background-color: #242424; -fx-pref-height: 54px;");
                } else {
                    setStyle(empty || item == null
                        ? "-fx-background-color: transparent;"
                        : getIndex() % 2 == 0
                            ? "-fx-background-color: #ffffff; -fx-pref-height: 54px;"
                            : "-fx-background-color: #fafbfc; -fx-pref-height: 54px;");
                }
            }
        });

        TableColumn<String[], String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[0]));
        colId.setPrefWidth(40);
        colId.setMinWidth(40);
        colId.setMaxWidth(40);
        colId.setCellFactory(col -> new TableCell<String[], String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                if (ThemeManager.isDarkMode) {
                    setStyle("-fx-text-fill: white;");
                } else {
                    setStyle("");
                }
            }
        });

        TableColumn<String[], String> colName = new TableColumn<>("Full Name");
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[1]));
        colName.setPrefWidth(130);
        colName.setCellFactory(col -> new TableCell<String[], String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                if (ThemeManager.isDarkMode) {
                    setStyle("-fx-text-fill: white;");
                } else {
                    setStyle("");
                }
            }
        });

        TableColumn<String[], String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[2]));
        colEmail.setPrefWidth(190);
        colEmail.setCellFactory(col -> new TableCell<String[], String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                if (ThemeManager.isDarkMode) {
                    setStyle("-fx-text-fill: white;");
                } else {
                    setStyle("");
                }
            }
        });

        TableColumn<String[], String> colRole = new TableColumn<>("Role");
        colRole.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[3]));
        colRole.setPrefWidth(140);
        colRole.setCellFactory(col -> new TableCell<String[], String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); return; }
                Label badge = new Label(item);
                String bg, fg;
                
                // ✅ UNIQUE COLORS FOR EACH ROLE
                switch(item.toLowerCase()) {
                    case "admin":
                        bg = "#1a1a1a"; fg = "#ffffff";
                        break;
                    case "barangay_captain":
                        bg = "#e8f5e9"; fg = "#1b5e20";
                        break;
                    case "kagawad":
                        bg = "#e3f2fd"; fg = "#0d47a1";
                        break;
                    case "secretary":
                        bg = "#fff3e0"; fg = "#e65100";
                        break;
                    case "treasurer":
                        bg = "#fce4ec"; fg = "#880e4f";
                        break;
                    case "resident":
                        bg = "#f3e5f5"; fg = "#4a148c";
                        break;
                    default:
                        bg = "#f4f4f4"; fg = "#555555";
                }
                
                badge.setStyle(
                    "-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";" +
                    "-fx-font-size: 11px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 20; -fx-padding: 6 14;");
                setGraphic(badge);
                setText(null);
            }
        });
        TableColumn<String[], String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[4]));
        colStatus.setPrefWidth(100);
        colStatus.setCellFactory(col -> new TableCell<String[], String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                // ✅ FIXED: Don't show anything for empty cells
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                String trimmedItem = item.trim();

                // ✅ FIXED: Only show badge if item is not empty
                if (trimmedItem.isEmpty()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label badge = new Label(trimmedItem);

                if (trimmedItem.equalsIgnoreCase("Active")) {
                    badge.setStyle(
                        "-fx-background-color: #e8f5e9; -fx-text-fill: #1b5e20;" +
                        "-fx-font-size: 11px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 20; -fx-padding: 6 14;");
                } else if (trimmedItem.equalsIgnoreCase("Inactive")) {
                    badge.setStyle(
                        "-fx-background-color: #ffebee; -fx-text-fill: #b71c1c;" +
                        "-fx-font-size: 11px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 20; -fx-padding: 6 14;");
                } else {
                    badge.setStyle(
                        "-fx-background-color: #f4f4f4; -fx-text-fill: #555555;" +
                        "-fx-font-size: 11px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 20; -fx-padding: 6 14;");
                }

                setGraphic(badge);
                setText(null);
            }
        });

        TableColumn<String[], String> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(350);
        colActions.setCellFactory(col -> new TableCell<String[], String>() {
            final Button roleBtn   = new Button("Role");
            final Button statusBtn = new Button("Status");
            final Button resetBtn  = new Button("Reset");
            final Button deleteBtn = new Button("Delete");
            final HBox   box       = new HBox(5, roleBtn, statusBtn, resetBtn, deleteBtn);
            {
                box.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 0 0 0 4;");

                String btnBase =
                    "-fx-font-size: 11px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 6; -fx-border-width: 1;" +
                    "-fx-padding: 5 10; -fx-cursor: hand;";

                roleBtn.setStyle(btnBase +
                    "-fx-background-color: #e3f2fd; -fx-text-fill: #1565c0;" +
                    "-fx-border-color: #bbdefb;");
                statusBtn.setStyle(btnBase +
                    "-fx-background-color: #fff8e1; -fx-text-fill: #f57f17;" +
                    "-fx-border-color: #ffe082;");
                resetBtn.setStyle(btnBase +
                    "-fx-background-color: #f4f4f4; -fx-text-fill: #444444;" +
                    "-fx-border-color: #e0e0e0;");
                deleteBtn.setStyle(btnBase +
                    "-fx-background-color: #ffebee; -fx-text-fill: #c62828;" +
                    "-fx-border-color: #ffcdd2;");

                roleBtn.setOnAction(e -> {
                    String[] row = getTableView().getItems().get(getIndex());
                    openChangeRoleModal(row[0], row[1], row[2], row[3], table, countLabel);
                });
                
                statusBtn.setOnAction(e -> {
                    String[] row = getTableView().getItems().get(getIndex());
                    toggleUserStatus(row[0], row[4], table, countLabel);
                });
                
                resetBtn.setOnAction(e -> {
                    String[] row = getTableView().getItems().get(getIndex());
                    resetPassword(row[0], row[2]);
                });
                deleteBtn.setOnAction(e -> {
                    String[] row = getTableView().getItems().get(getIndex());
                    deleteUser(row[0], row[2], table, countLabel);
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(colId, colName, colEmail, colRole, colStatus, colActions);

        Platform.runLater(() -> {
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        });
        
        new Thread(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> {
                if (ThemeManager.isDarkMode) {
                    table.lookupAll(".column-header").forEach(hdr -> {
                        hdr.setStyle(
                            "-fx-background-color: #2a2a2a; " +
                            "-fx-text-fill: #ffffff;");
                    });

                    table.lookupAll(".column-header-background").forEach(hdr -> {
                        hdr.setStyle("-fx-background-color: #2a2a2a;");
                    });
                }
            });
        }).start();

        loadUsers(table, countLabel, "");
        searchField.setOnKeyReleased(e ->
            loadUsers(table, countLabel, searchField.getText().trim()));
        exportBtn.setOnAction(e -> exportUsersCSV(table));

        container.getChildren().addAll(cards, header, table);
        return container;
    }

    // ✅ FIXED: Correctly count Active and Inactive users
    private int[] getUserCounts() {
        int total = 0, active = 0, inactive = 0, admins = 0;
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.prepareStatement(
                "SELECT COALESCE(status, 'Active') as status, role FROM users"
            ).executeQuery();
            while (rs.next()) {
                total++;
                String s = rs.getString("status");
                String r = rs.getString("role");
                
                // ✅ Properly count Active vs Inactive
                if (s != null && s.trim().equalsIgnoreCase("Inactive")) {
                    inactive++;
                } else {
                    active++;
                }
                
                if (r != null && r.equalsIgnoreCase("admin")) {
                    admins++;
                }
            }
            rs.close();
            conn.close();
            
            System.out.println("[Stats] Total: " + total + " | Active: " + active + " | Inactive: " + inactive + " | Admins: " + admins);
        } catch (Exception e) { 
            System.out.println("[Stats] Error: " + e.getMessage());
            e.printStackTrace(); 
        }
        return new int[]{total, active, inactive, admins};
    }

    private void loadUsers(TableView<String[]> table, Label countLabel, String search) {
        List<String[]> rows = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT id, full_name, email, role, COALESCE(status, 'Active') as status FROM users WHERE 1=1";

            if (!search.isEmpty()) {
                sql += " AND (full_name LIKE '%" + search + "%' OR email LIKE '%" + search + "%')";
            }

            sql += " ORDER BY id DESC";

            System.out.println("[Users] Loading with query: " + sql);

            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                String id     = String.valueOf(rs.getInt("id"));
                String name   = rs.getString("full_name") != null ? rs.getString("full_name") : "—";
                String email  = rs.getString("email") != null ? rs.getString("email") : "—";
                String role   = rs.getString("role") != null ? rs.getString("role") : "resident";
                String status = rs.getString("status") != null ? rs.getString("status").trim() : "Active";

                rows.add(new String[]{id, name, email, role, status});
                count++;
                System.out.println("[Users] Row " + count + ": ID=" + id + " | Name=" + name + " | Status=" + status);
            }
            rs.close();
            pstmt.close();
            conn.close();

            System.out.println("[Users] Total rows loaded: " + count);
        } catch (Exception e) { 
            System.out.println("[Users] Error: " + e.getMessage());
            e.printStackTrace(); 
        }

        // ✅ Clear and refresh table
        table.getItems().clear();
        table.setItems(FXCollections.observableArrayList(rows));
        countLabel.setText(rows.size() + " user" + (rows.size() != 1 ? "s" : ""));

        System.out.println("[Users] Table displayed with " + rows.size() + " records");
    }
    private void exportUsersCSV(TableView<String[]> table) {
        try {
            String fileName = "users_export_" + LocalDate.now() + ".csv";
            FileWriter writer = new FileWriter(fileName);
            writer.write("ID,Full Name,Email,Role,Status\n");
            for (String[] row : table.getItems())
                writer.write(String.join(",", row[0], row[1], row[2], row[3], row[4]) + "\n");
            writer.close();
            showInfo("✅  Exported successfully!\nSaved as: " + fileName);
        } catch (Exception e) {
            e.printStackTrace();
            showInfo("Export failed. Please try again.");
        }
    }

    private void openChangeRoleModal(String id, String name, String email,
                                      String currentRole,
                                      TableView<String[]> table, Label countLabel) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.initOwner(logoutButton.getScene().getWindow());
        modal.setTitle("Change Role");
        modal.setResizable(false);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #ffffff; -fx-min-width: 420;");

        VBox header = new VBox(6);
        header.setFocusTraversable(true);
        header.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 24 28 22 28;");
        Label titleLbl = new Label("Change Role");
        titleLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        Label subLbl = new Label("Assign a new role to this user");
        subLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");
        header.getChildren().addAll(titleLbl, subLbl);

        VBox body = new VBox(14);
        body.setStyle("-fx-padding: 24 28 16 28;");

        Label userLbl = new Label("USER");
        userLbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #999999;");
        TextField nameField = new TextField(name.equals("—") ? email : name);
        nameField.setEditable(false);
        nameField.setStyle(
            "-fx-font-size: 13px; -fx-padding: 10 14; -fx-background-radius: 8;" +
            "-fx-border-color: #eeeeee; -fx-border-width: 1; -fx-border-radius: 8;" +
            "-fx-background-color: #f8f8f8; -fx-text-fill: #555555;");
        VBox nameGroup = new VBox(6, userLbl, nameField);

        Label roleLbl = new Label("NEW ROLE");
        roleLbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #999999;");
        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll(
            "admin", "barangay_captain", "kagawad", "secretary", "treasurer", "resident");
        roleBox.setValue(currentRole);
        roleBox.setMaxWidth(Double.MAX_VALUE);
        roleBox.setStyle(
            "-fx-font-size: 13px; -fx-background-radius: 8;" +
            "-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8;");
        VBox roleGroup = new VBox(6, roleLbl, roleBox);

        Label errorLbl = new Label("");
        errorLbl.setStyle("-fx-text-fill: #c62828; -fx-font-size: 11px;");
        body.getChildren().addAll(nameGroup, roleGroup, errorLbl);

        HBox footer = new HBox(10);
        footer.setStyle(
            "-fx-padding: 16 28 24 28; -fx-alignment: CENTER_RIGHT;" +
            "-fx-border-color: #f0f0f0; -fx-border-width: 1 0 0 0;");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(
            "-fx-background-color: #f4f4f4; -fx-text-fill: #555555;" +
            "-fx-font-size: 12px; -fx-font-weight: bold;" +
            "-fx-background-radius: 8; -fx-border-color: #e0e0e0;" +
            "-fx-border-width: 1; -fx-padding: 10 20; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> modal.close());

        Button saveBtn = new Button("Save Role");
        saveBtn.setStyle(
            "-fx-background-color: #1a1a1a; -fx-text-fill: #ffffff;" +
            "-fx-font-size: 12px; -fx-font-weight: bold;" +
            "-fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> {
            String newRole = roleBox.getValue();
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE users SET role = ? WHERE id = ?");
                stmt.setString(1, newRole);
                stmt.setInt(2, Integer.parseInt(id));
                stmt.executeUpdate();
                stmt.close();
                logAction("Changed role of " + email + " to " + newRole);
                conn.close();
                modal.close();
                loadUsers(table, countLabel, "");
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLbl.setText("⚠ Database error.");
            }
        });

        footer.getChildren().addAll(cancelBtn, saveBtn);
        root.getChildren().addAll(header, body, footer);
        modal.setScene(new Scene(root));
        Platform.runLater(() -> root.requestFocus());
        modal.showAndWait();
    }

private void toggleUserStatus(String id, String currentStatus,
                               TableView<String[]> table, Label countLabel) {
    // ✅ Handle NULL or empty status - default to Active
    String statusValue = currentStatus;
    if (statusValue == null || statusValue.trim().isEmpty()) {
        statusValue = "Active";
    }
    
    statusValue = statusValue.trim();
    
    // ✅ Create FINAL variables for lambda
    final String finalCurrentStatus = statusValue;
    final String finalNewStatus = statusValue.equalsIgnoreCase("Active") ? "Inactive" : "Active";
    final String finalId = id;
    
    System.out.println("[Toggle] User ID: " + finalId + " | Current: " + finalCurrentStatus + " | New: " + finalNewStatus);
    
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Toggle Status");
    confirm.setHeaderText("Set user to " + finalNewStatus + "?");
    confirm.setContentText("User ID " + finalId + " will be set to: " + finalNewStatus);
    
    confirm.showAndWait().ifPresent(r -> {
        if (r == ButtonType.OK) {
            try {
                Connection conn = DatabaseConnection.getConnection();
                
                // ✅ Update database
                String updateSQL = "UPDATE users SET status = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(updateSQL);
                stmt.setString(1, finalNewStatus);
                stmt.setInt(2, Integer.parseInt(finalId));
                
                int rowsUpdated = stmt.executeUpdate();
                System.out.println("[Toggle] SQL executed. Rows updated: " + rowsUpdated);
                
                stmt.close();
                
                // ✅ Log the action
                logAction("Changed user ID " + finalId + " status from " + finalCurrentStatus + " to " + finalNewStatus);
                
                conn.close();
                
                // ✅ Refresh the table
                loadUsers(table, countLabel, "");
                
                System.out.println("[Toggle] Success! Table refreshed.");
                
                // ✅ Show confirmation
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Success");
                success.setHeaderText("Status Updated");
                success.setContentText("User status changed to: " + finalNewStatus);
                success.showAndWait();
                
            } catch (Exception e) { 
                System.out.println("[Toggle] Error: " + e.getMessage());
                e.printStackTrace();
                
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Error");
                error.setHeaderText("Failed to update status");
                error.setContentText(e.getMessage());
                error.showAndWait();
            }
        }
    });
}
    private void resetPassword(String id, String email) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reset Password");
        confirm.setHeaderText("Reset password for " + email + "?");
        confirm.setContentText("Password will be reset to: barangay123");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE users SET password = ? WHERE id = ?");
                    stmt.setString(1, "barangay123");
                    stmt.setInt(2, Integer.parseInt(id));
                    stmt.executeUpdate();
                    stmt.close();
                    logAction("Reset password for " + email);
                    conn.close();
                    showInfo("✅ Password reset to: barangay123");
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    private void deleteUser(String id, String email,
                             TableView<String[]> table, Label countLabel) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete User");
        confirm.setHeaderText("Delete account for " + email + "?");
        confirm.setContentText("This action cannot be undone.");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM users WHERE id = ?");
                    stmt.setInt(1, Integer.parseInt(id));
                    stmt.executeUpdate();
                    stmt.close();
                    logAction("Deleted account: " + email);
                    conn.close();
                    loadUsers(table, countLabel, "");
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }
// ── LOGS TAB ───────────────────────────────────────────────────────────────────
private Node buildLogsTab() {
    VBox container = new VBox(20);
    container.setMaxWidth(Double.MAX_VALUE);
    container.setStyle(
        "-fx-background-color: #ffffff; -fx-background-radius: 16;" +
        "-fx-border-color: #e8e8e8; -fx-border-width: 1; -fx-padding: 28;");

    HBox headerRow = new HBox(10);
    headerRow.setStyle("-fx-alignment: CENTER_LEFT;");
    VBox titleBox = new VBox(4);
    HBox.setHgrow(titleBox, Priority.ALWAYS);
    Label title = new Label("Activity Logs");
    title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
    Label subtitle = new Label("Recent system actions and changes");
    subtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaaaaa;");
    titleBox.getChildren().addAll(title, subtitle);
    
    Label recordCount = new Label();
    recordCount.setStyle(
        "-fx-font-size: 11px; -fx-text-fill: #888888;" +
        "-fx-background-color: #f4f4f4; -fx-background-radius: 20; -fx-padding: 5 14;");
    
    headerRow.getChildren().addAll(titleBox, recordCount);

    HBox filterRow = new HBox(10);
    filterRow.setStyle(
        "-fx-alignment: CENTER_LEFT; -fx-background-color: #f8f9fa;" +
        "-fx-background-radius: 10; -fx-padding: 14 18;" +
        "-fx-border-color: #ebebeb; -fx-border-width: 1; -fx-border-radius: 10;");
    
    Label fromLbl = new Label("From:");
    fromLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555; -fx-font-weight: bold;");
    DatePicker fromPicker = new DatePicker();
    fromPicker.setPromptText("Start date");
    fromPicker.setPrefWidth(145);
    
    Label toLbl = new Label("To:");
    toLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555; -fx-font-weight: bold;");
    DatePicker toPicker = new DatePicker();
    toPicker.setPromptText("End date");
    toPicker.setPrefWidth(145);
    
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    
    Button filterBtn = new Button("Apply Filter");
    filterBtn.setStyle(
        "-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff;" +
        "-fx-font-size: 12px; -fx-font-weight: bold;" +
        "-fx-background-radius: 8; -fx-padding: 9 18; -fx-cursor: hand;");
    
    Button clearFilterBtn = new Button("Clear");
    clearFilterBtn.setStyle(
        "-fx-background-color: #f4f4f4; -fx-text-fill: #555555;" +
        "-fx-font-size: 12px; -fx-background-radius: 8;" +
        "-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 9 14; -fx-cursor: hand;");
    
    filterRow.getChildren().addAll(
        fromLbl, fromPicker, toLbl, toPicker, spacer, filterBtn, clearFilterBtn);

    // ✅ Table
    TableView<String[]> table = new TableView<>();
    table.setMaxWidth(Double.MAX_VALUE);
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    table.setStyle(
        "-fx-background-color: transparent; -fx-border-color: #f0f0f0;" +
        "-fx-border-width: 1; -fx-border-radius: 10;" +
        "-fx-table-cell-border-color: #f8f8f8;");
    table.setPrefHeight(400);
    table.setFixedCellSize(50);
    
    table.setRowFactory(tv -> new TableRow<String[]>() {
        @Override
        protected void updateItem(String[] item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setStyle("-fx-background-color: transparent;");
                setVisible(false);
            } else {
                setVisible(true);
                if (ThemeManager.isDarkMode) {
                    setStyle(getIndex() % 2 == 0
                        ? "-fx-background-color: #1a1a1a; -fx-pref-height: 50px;"
                        : "-fx-background-color: #242424; -fx-pref-height: 50px;");
                } else {
                    setStyle(getIndex() % 2 == 0
                        ? "-fx-background-color: #ffffff; -fx-pref-height: 50px;"
                        : "-fx-background-color: #fafbfc; -fx-pref-height: 50px;");
                }
            }
        }
    });

    // ✅ Column: ID
    TableColumn<String[], String> colId = new TableColumn<>("#");
    colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[0]));
    colId.setPrefWidth(60);
    colId.setMinWidth(60);
    colId.setMaxWidth(60);
    colId.setCellFactory(col -> new TableCell<String[], String>() {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                return;
            }
            setText(item);
            setStyle(ThemeManager.isDarkMode ? "-fx-text-fill: white;" : "");
        }
    });

    // ✅ Column: Action
    TableColumn<String[], String> colAction = new TableColumn<>("Action");
    colAction.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[1]));
    colAction.setCellFactory(col -> new TableCell<String[], String>() {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                return;
            }
            setText(item);
            setWrapText(true);
            setStyle(ThemeManager.isDarkMode ? "-fx-text-fill: white;" : "");
        }
    });

    // ✅ Column: Performed By
    TableColumn<String[], String> colBy = new TableColumn<>("Performed By");
    colBy.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[2]));
    colBy.setPrefWidth(140);
    colBy.setMinWidth(140);
    colBy.setCellFactory(col -> new TableCell<String[], String>() {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                return;
            }
            setText(item);
            setStyle(ThemeManager.isDarkMode ? "-fx-text-fill: white;" : "");
        }
    });

    // ✅ Column: Date & Time (FIXED)
    TableColumn<String[], String> colDate = new TableColumn<>("Date & Time");
    colDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[3]));
    colDate.setPrefWidth(200);
    colDate.setMinWidth(200);
    colDate.setCellFactory(col -> new TableCell<String[], String>() {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                return;
            }
            setText(item);
            setStyle(ThemeManager.isDarkMode ? "-fx-text-fill: white;" : "");
        }
    });

    table.getColumns().addAll(colId, colAction, colBy, colDate);

    // ✅ Load logs
    loadLogs(table, recordCount, null, null);
    
    // ✅ Filter button
    filterBtn.setOnAction(e -> loadLogs(table, recordCount, fromPicker.getValue(), toPicker.getValue()));
    
    // ✅ Clear filter button
    clearFilterBtn.setOnAction(e -> {
        fromPicker.setValue(null);
        toPicker.setValue(null);
        loadLogs(table, recordCount, null, null);
    });

    // ✅ Clear all logs button
    Button clearBtn = new Button("Clear All Logs");
    clearBtn.setStyle(
        "-fx-background-color: #ffebee; -fx-text-fill: #c62828;" +
        "-fx-font-size: 12px; -fx-font-weight: bold;" +
        "-fx-background-radius: 8; -fx-border-color: #ffcdd2;" +
        "-fx-border-width: 1; -fx-padding: 9 16; -fx-cursor: hand;");
    clearBtn.setOnAction(e -> {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear Logs");
        confirm.setHeaderText("Clear all activity logs?");
        confirm.setContentText("This action cannot be undone.");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement("DELETE FROM logs");
                    int rowsDeleted = stmt.executeUpdate();
                    stmt.close();
                    conn.close();
                    
                    System.out.println("[Logs] Cleared " + rowsDeleted + " rows");
                    loadLogs(table, recordCount, null, null);
                    
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Success");
                    success.setHeaderText("Logs Cleared");
                    success.setContentText("All activity logs have been cleared.");
                    success.showAndWait();
                } catch (Exception ex) { 
                    System.out.println("[Logs] Error clearing: " + ex.getMessage());
                    ex.printStackTrace();
                    
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Error");
                    error.setHeaderText("Failed to clear logs");
                    error.setContentText(ex.getMessage());
                    error.showAndWait();
                }
            }
        });
    });
    
    HBox footer = new HBox();
    footer.setStyle("-fx-alignment: CENTER_RIGHT; -fx-padding: 4 0 0 0;");
    footer.getChildren().add(clearBtn);

    container.getChildren().addAll(headerRow, filterRow, table, footer);
    return container;
}
// ✅ COMPLETE FIXED: Use log_id instead of id
private void loadLogs(TableView<String[]> table, Label recordCount, LocalDate from, LocalDate to) {
    List<String[]> rows = new ArrayList<>();
    try {
        Connection conn = DatabaseConnection.getConnection();
        
        // ✅ Build query - changed 'id' to 'log_id'
        StringBuilder sql = new StringBuilder(
            "SELECT log_id, action, performed_by, log_date FROM logs WHERE 1=1");
        
        if (from != null) {
            sql.append(" AND DATE(log_date) >= '").append(from).append("'");
        }
        if (to != null) {
            sql.append(" AND DATE(log_date) <= '").append(to).append("'");
        }
        
        sql.append(" ORDER BY log_id DESC");
        
        System.out.println("[Logs] SQL Query: " + sql.toString());
        
        // ✅ Execute query
        PreparedStatement pstmt = conn.prepareStatement(sql.toString());
        ResultSet rs = pstmt.executeQuery();
        
        int count = 0;
        while (rs.next()) {
            String id  = String.valueOf(rs.getInt("log_id"));  // ✅ Changed from 'id' to 'log_id'
            String act = rs.getString("action") != null ? rs.getString("action").trim() : "—";
            String by  = rs.getString("performed_by") != null ? rs.getString("performed_by").trim() : "System";
            
            // ✅ Format the date properly (YYYY-MM-DD HH:MM:SS)
            String dt = "—";
            try {
                String rawDate = rs.getString("log_date");
                if (rawDate != null && !rawDate.isEmpty()) {
                    dt = rawDate.substring(0, Math.min(19, rawDate.length())); // Get YYYY-MM-DD HH:MM:SS
                }
            } catch (Exception dateEx) {
                System.out.println("[Logs] Date parse error: " + dateEx.getMessage());
            }
            
            rows.add(new String[]{id, act, by, dt});
            count++;
            System.out.println("[Logs] Row " + count + ": log_id=" + id + " | Action=" + act + " | By=" + by + " | Date=" + dt);
        }
        rs.close();
        pstmt.close();
        conn.close();
        
        System.out.println("[Logs] ✅ Successfully loaded " + count + " records");
        
    } catch (Exception e) { 
        System.out.println("[Logs] ❌ Error: " + e.getMessage());
        e.printStackTrace();
        
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle("Error");
        error.setHeaderText("Failed to load logs");
        error.setContentText(e.getMessage());
        error.showAndWait();
    }
    
    // ✅ Clear and set table items
    table.getItems().clear();
    table.setItems(FXCollections.observableArrayList(rows));
    
    if (recordCount != null) {
        recordCount.setText(rows.size() + " record" + (rows.size() != 1 ? "s" : ""));
    }
    
    System.out.println("[Logs] Table updated with " + rows.size() + " items");
}
    // ── PASSWORD TAB ───────────────────────────────────────────────────────────────
    private Node buildPasswordTab() {
        HBox wrapper = new HBox();
        wrapper.setMaxWidth(Double.MAX_VALUE);
        wrapper.setStyle("-fx-alignment: CENTER;");

        VBox container = new VBox(22);
        container.setMaxWidth(520);
        container.setMinWidth(520);
        
        String containerBg = ThemeManager.isDarkMode ? "#1a1a1a" : "#ffffff";
        String containerBorder = ThemeManager.isDarkMode ? "#333333" : "#e8e8e8";
        String fieldBg = ThemeManager.isDarkMode ? "#242424" : "#f8f9fa";
        String fieldBorder = ThemeManager.isDarkMode ? "#444444" : "#e8e8e8";
        String fieldText = ThemeManager.isDarkMode ? "#ffffff" : "#000000";
        String labelText = ThemeManager.isDarkMode ? "#cccccc" : "#999999";
        
        container.setStyle(
            "-fx-background-color: " + containerBg + "; -fx-background-radius: 16;" +
            "-fx-border-color: " + containerBorder + "; -fx-border-width: 1; -fx-padding: 36;");

        VBox bannerBox = new VBox(6);
        bannerBox.setMaxWidth(Double.MAX_VALUE);
        bannerBox.setStyle(
            "-fx-background-color: #1a1a1a; -fx-background-radius: 12; -fx-padding: 22 26;");
        Label bannerTitle = new Label("Change Password");
        bannerTitle.setStyle(
            "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        Label bannerSub = new Label("Keep your account secure by updating your password regularly");
        bannerSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");
        bannerBox.getChildren().addAll(bannerTitle, bannerSub);

        String fieldStyle =
            "-fx-font-size: 13px; -fx-padding: 12 16; -fx-background-radius: 10;" +
            "-fx-border-color: " + fieldBorder + "; -fx-border-width: 1; -fx-border-radius: 10;" +
            "-fx-background-color: " + fieldBg + "; -fx-text-fill: " + fieldText + ";";
        String labelStyle =
            "-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: " + labelText + ";";

        Label currentLbl = new Label("CURRENT PASSWORD");
        currentLbl.setStyle(labelStyle);
        PasswordField currentField = new PasswordField();
        currentField.setPromptText("Enter your current password");
        currentField.setMaxWidth(Double.MAX_VALUE);
        currentField.setStyle(fieldStyle);
        VBox currentGroup = new VBox(8, currentLbl, currentField);

        Label newLbl = new Label("NEW PASSWORD");
        newLbl.setStyle(labelStyle);
        PasswordField newField = new PasswordField();
        newField.setPromptText("Enter new password (min. 6 characters)");
        newField.setMaxWidth(Double.MAX_VALUE);
        newField.setStyle(fieldStyle);
        VBox newGroup = new VBox(8, newLbl, newField);

        Label confirmLbl = new Label("CONFIRM NEW PASSWORD");
        confirmLbl.setStyle(labelStyle);
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Re-enter new password");
        confirmField.setMaxWidth(Double.MAX_VALUE);
        confirmField.setStyle(fieldStyle);
        VBox confirmGroup = new VBox(8, confirmLbl, confirmField);

        Label errorLbl = new Label("");
        errorLbl.setStyle("-fx-text-fill: #c62828; -fx-font-size: 12px;");

        Button saveBtn = new Button("Update Password");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setStyle(
            "-fx-background-color: #1a1a1a; -fx-text-fill: #ffffff;" +
            "-fx-font-size: 13px; -fx-font-weight: bold;" +
            "-fx-background-radius: 10; -fx-padding: 14; -fx-cursor: hand;");

        saveBtn.setOnAction(e -> {
            String current = currentField.getText().trim();
            String newPass = newField.getText().trim();
            String confirm = confirmField.getText().trim();

            if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                errorLbl.setStyle("-fx-text-fill: #c62828; -fx-font-size: 12px;");
                errorLbl.setText("⚠  Please fill in all fields.");
                return;
            }
            if (!newPass.equals(confirm)) {
                errorLbl.setStyle("-fx-text-fill: #c62828; -fx-font-size: 12px;");
                errorLbl.setText("⚠  New passwords do not match.");
                return;
            }
            if (newPass.length() < 6) {
                errorLbl.setStyle("-fx-text-fill: #c62828; -fx-font-size: 12px;");
                errorLbl.setText("⚠  Password must be at least 6 characters.");
                return;
            }
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT id FROM users WHERE role = 'admin' AND password = ?");
                checkStmt.setString(1, current);
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next()) {
                    errorLbl.setStyle("-fx-text-fill: #c62828; -fx-font-size: 12px;");
                    errorLbl.setText("⚠  Current password is incorrect.");
                    rs.close(); checkStmt.close(); conn.close();
                    return;
                }
                String adminId = rs.getString("id");
                rs.close(); checkStmt.close();

                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE users SET password = ? WHERE id = ?");
                stmt.setString(1, newPass);
                stmt.setInt(2, Integer.parseInt(adminId));
                stmt.executeUpdate();
                stmt.close();
                logAction("Admin changed their password");
                conn.close();

                currentField.clear();
                newField.clear();
                confirmField.clear();
                errorLbl.setStyle("-fx-text-fill: #2e7d32; -fx-font-size: 12px;");
                errorLbl.setText("✅  Password updated successfully!");
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLbl.setStyle("-fx-text-fill: #c62828; -fx-font-size: 12px;");
                errorLbl.setText("⚠  Database error. Please try again.");
            }
        });

        container.getChildren().addAll(
            bannerBox, currentGroup, newGroup, confirmGroup, errorLbl, saveBtn);
        wrapper.getChildren().add(container);
        return wrapper;
    }

    // ── STATISTICS TAB ──────────────────────────────────────────────────────────────
    private Node buildStatsTab() {
        VBox container = new VBox(24);
        container.setMaxWidth(Double.MAX_VALUE);
        
        String containerBg = ThemeManager.isDarkMode ? "#0f0f0f" : "#ffffff";
        String containerBorder = ThemeManager.isDarkMode ? "#2a2a2a" : "#e8e8e8";
        
        container.setStyle(
            "-fx-background-color: " + containerBg + "; -fx-background-radius: 16;" +
            "-fx-border-color: " + containerBorder + "; -fx-border-width: 1; -fx-padding: 28;");

        HBox headerRow = new HBox(10);
        headerRow.setStyle("-fx-alignment: CENTER_LEFT;");
        VBox titleBox = new VBox(4);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        Label title = new Label("System Statistics");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + 
            (ThemeManager.isDarkMode ? "#ffffff" : "#1a1a1a") + ";");
        Label subtitle = new Label("Live overview of all data across the system");
        subtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaaaaa;");
        titleBox.getChildren().addAll(title, subtitle);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle(
            "-fx-background-color: #f4f4f4; -fx-text-fill: #555555;" +
            "-fx-font-size: 12px; -fx-background-radius: 8;" +
            "-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 9 16; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> {
            setActiveTab(tabStatsBtn);
            tabContent.getChildren().setAll(buildStatsTab());
        });
        headerRow.getChildren().addAll(titleBox, refreshBtn);

        int residents          = getCount("SELECT COUNT(*) FROM residents WHERE status = 'Active'");
        int activeResidents    = getCount("SELECT COUNT(*) FROM residents WHERE status = 'Active'");
        int inactiveResidents  = getCount("SELECT COUNT(*) FROM residents WHERE status = 'Inactive'");
        int totalPayments      = getCount("SELECT COUNT(*) FROM payments");
        int paidPayments       = getCount("SELECT COUNT(*) FROM payments WHERE status = 'Paid'");
        int pendingPayments    = getCount("SELECT COUNT(*) FROM payments WHERE status = 'Pending'");
        int totalComplaints    = getCount("SELECT COUNT(*) FROM complaints");
        int pendingComplaints  = getCount("SELECT COUNT(*) FROM complaints WHERE status = 'Pending'");
        int resolvedComplaints = getCount("SELECT COUNT(*) FROM complaints WHERE status = 'Resolved'");
        int underReview        = getCount("SELECT COUNT(*) FROM complaints WHERE status = 'Under Review'");
        int announcements      = getCount("SELECT COUNT(*) FROM announcements");
        int totalUsers         = getCount("SELECT COUNT(*) FROM users");
        int expenses           = getCount("SELECT COUNT(*) FROM finances WHERE type = 'Expense'");

        VBox resSection = buildStatSection("Residents", new String[][]{
            {"Total Residents", String.valueOf(residents),                
                ThemeManager.isDarkMode ? "#404040" : "#1a1a1a", "#ffffff"},
            {"Active",          String.valueOf(activeResidents),          "#e8f5e9", "#2e7d32"},
            {"Inactive",        String.valueOf(inactiveResidents),        "#ffebee", "#c62828"}
        });
        VBox paySection = buildStatSection("Payments", new String[][]{
            {"Total",   String.valueOf(totalPayments),   "#e3f2fd", "#1565c0"},
            {"Paid",    String.valueOf(paidPayments),    "#e8f5e9", "#2e7d32"},
            {"Pending", String.valueOf(pendingPayments), "#fff8e1", "#f57f17"}
        });
        VBox cmpSection = buildStatSection("Complaints", new String[][]{
            {"Total",        String.valueOf(totalComplaints),    "#f3e5f5", "#7b1fa2"},
            {"Pending",      String.valueOf(pendingComplaints),  "#fff8e1", "#f57f17"},
            {"Under Review", String.valueOf(underReview),        "#e3f2fd", "#1565c0"},
            {"Resolved",     String.valueOf(resolvedComplaints), "#e8f5e9", "#2e7d32"}
        });
        VBox otherSection = buildStatSection("Other", new String[][]{
            {"Announcements", String.valueOf(announcements), "#fce4ec", "#c62828"},
            {"System Users",  String.valueOf(totalUsers),    "#e3f2fd", "#1565c0"},
            {"Expenses",      String.valueOf(expenses),      "#ffebee", "#c62828"}
        });

        container.getChildren().addAll(
            headerRow, resSection, paySection, cmpSection, otherSection);
        return container;
    }

    private VBox buildStatSection(String sectionTitle, String[][] items) {
        VBox section = new VBox(12);
        section.setMaxWidth(Double.MAX_VALUE);

        Label sectionLbl = new Label(sectionTitle);
        String sectionLblColor = ThemeManager.isDarkMode ? "#bbbbbb" : "#999999";
        sectionLbl.setStyle(
            "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + sectionLblColor + ";" +
            "-fx-padding: 0 0 2 0;");

        HBox row = new HBox(14);
        row.setMaxWidth(Double.MAX_VALUE);
        for (String[] item : items) {
            VBox card = new VBox(10);
            HBox.setHgrow(card, Priority.ALWAYS);
            card.setStyle(
                "-fx-background-color: " + item[2] + "; -fx-background-radius: 14;" +
                "-fx-padding: 24 28; -fx-min-height: 110;");
            Label valLbl = new Label(item[1]);
            valLbl.setStyle(
                "-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: " + item[3] + ";");
            Label nameLbl = new Label(item[0]);
            nameLbl.setStyle(
                "-fx-font-size: 12px; -fx-text-fill: " + item[3] + "; -fx-opacity: 0.7;");
            card.getChildren().addAll(valLbl, nameLbl);
            row.getChildren().add(card);
        }

        Separator sep = new Separator();
        String sepOpacity = ThemeManager.isDarkMode ? "-fx-opacity: 0.3;" : "-fx-opacity: 0.2;";
        sep.setStyle(sepOpacity);
        section.getChildren().addAll(sectionLbl, row, sep);
        return section;
    }

    private int getCount(String sql) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.prepareStatement(sql).executeQuery();
            int count = rs.next() ? rs.getInt(1) : 0;
            rs.close();
            conn.close();
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private VBox buildMiniCard(String label, String value, String bg, String fg, String icon) {
        VBox card = new VBox(6);
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setStyle(
            "-fx-background-color: " + bg + "; -fx-background-radius: 12; -fx-padding: 18 20;");
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 18px;");
        Label valLbl = new Label(value);
        valLbl.setStyle(
            "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + fg + ";");
        Label nameLbl = new Label(label);
        nameLbl.setStyle(
            "-fx-font-size: 11px; -fx-text-fill: " + fg + "; -fx-opacity: 0.75;");
        card.getChildren().addAll(iconLbl, valLbl, nameLbl);
        return card;
    }

    private void logAction(String action) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO logs (action, performed_by, log_date) VALUES (?, ?, ?)");
            stmt.setString(1, action);
            stmt.setString(2, "Admin");
            stmt.setString(3, LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            stmt.executeUpdate();
            stmt.close();
            conn.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // NAVIGATION
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
    @FXML private void goToFinances() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Finances.fxml", true, getClass());
    }
    @FXML private void goToSettings() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Settings.fxml", true, getClass());
    }
    @FXML private void handleLogout() {
        SessionManager.logout();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "login.fxml", false, getClass());
    }
}