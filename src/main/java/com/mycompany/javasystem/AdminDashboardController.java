package com.mycompany.javasystem;

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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardController {

    private static final int BASE_POPULATION = 6474;

    @FXML private Button logoutButton;
    @FXML private Button alertsButton;
    @FXML private Label alertBadge;
    @FXML private HBox avatarBox;
    @FXML private Circle avatarCircle;
    @FXML private ImageView profileImageView;
    @FXML private Label avatarInitialLabel;
    @FXML private Label greetingLabel;
    @FXML private Label topBarNameLabel;
    @FXML private Label topBarRoleLabel;
    @FXML private Label totalResidentsLabel;
    @FXML private Label pendingRequestsLabel;
    @FXML private Label collectionTodayLabel;
    @FXML private Label openComplaintsLabel;
    @FXML private Label activeResidentsLabel;
    @FXML private Label resolvedComplaintsLabel;
    @FXML private Label totalAnnouncementsLabel;
    @FXML private Label totalPaidLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private VBox recentRequestsBody;
    @FXML private VBox announcementsBody;
    @FXML private VBox activityBody;
    @FXML private ScrollPane mainScrollPane;

    @FXML
    public void initialize() {
        setGreeting();
        loadTopBar();
        loadAvatarPicture();
        loadPrimaryStats();
        loadSecondaryStats();
        loadRecentRequests();
        loadAnnouncements();
        loadRecentActivity();
        syncNotifications();
        refreshAlertBadge();

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

    private void setGreeting() {
        int hour = LocalTime.now().getHour();
        String name = SessionManager.getName();
        String displayName = (name != null && !name.equals("Admin")) ? name : "Admin";
        String greeting;
        if (hour < 12)      greeting = "Good morning, " + displayName + "! 👋";
        else if (hour < 17) greeting = "Good afternoon, " + displayName + "! 👋";
        else                greeting = "Good evening, " + displayName + "! 👋";
        greetingLabel.setText(greeting);
    }

    // ── Notifications ─────────────────────────────────────────────────────────────
    private void cleanupNotifications() {
        String email = SessionManager.getEmail();
        if (email == null) return;
        try {
            Connection conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(true);

            PreparedStatement stmt1 = conn.prepareStatement(
                "DELETE FROM notifications WHERE type = 'announcement' " +
                "AND user_email = ? AND reference_id NOT IN " +
                "(SELECT announcement_id FROM announcements)");
            stmt1.setString(1, email);
            int d1 = stmt1.executeUpdate();
            stmt1.close();

            PreparedStatement stmt2 = conn.prepareStatement(
                "DELETE FROM notifications WHERE type = 'complaint' " +
                "AND user_email = ? AND reference_id NOT IN " +
                "(SELECT complaint_id FROM complaints WHERE status <> 'Resolved')");
            stmt2.setString(1, email);
            int d2 = stmt2.executeUpdate();
            stmt2.close();

            PreparedStatement stmt3 = conn.prepareStatement(
                "DELETE FROM notifications WHERE type = 'payment' " +
                "AND user_email = ? AND reference_id NOT IN " +
                "(SELECT ref_number FROM payments " +
                "WHERE status = 'Pending' AND archived = False)");
            stmt3.setString(1, email);
            int d3 = stmt3.executeUpdate();
            stmt3.close();

            conn.close();
            System.out.println("[Cleanup] Removed " + (d1+d2+d3) + " stale notifications");
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
                String msg = "Pending payment from " +
                    rs1.getString("resident_name") + " (" + refNo + ")";
                insertIfNew(conn, "payment", msg, refNo, email);
            }
            rs1.close();

            ResultSet rs2 = conn.prepareStatement(
                "SELECT complaint_id, complainant_name, incident_type " +
                "FROM complaints WHERE status <> 'Resolved'"
            ).executeQuery();
            while (rs2.next()) {
                String cid = rs2.getString("complaint_id");
                String msg = "Open complaint: " + rs2.getString("incident_type") +
                    " by " + rs2.getString("complainant_name");
                insertIfNew(conn, "complaint", msg, cid, email);
            }
            rs2.close();

            ResultSet rs3 = conn.prepareStatement(
                "SELECT announcement_id, title FROM announcements ORDER BY id DESC"
            ).executeQuery();
            int aCount = 0;
            while (rs3.next() && aCount < 5) {
                String aid = rs3.getString("announcement_id");
                String msg = "Announcement posted: " + rs3.getString("title");
                insertIfNew(conn, "announcement", msg, aid, email);
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
        check.setString(1, refId);
        check.setString(2, email);
        check.setString(3, type);
        ResultSet rs = check.executeQuery();
        boolean exists = rs.next();
        rs.close(); check.close();

        if (!exists) {
            PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO notifications " +
                "(type, message, reference_id, is_read, created_at, user_email) " +
                "VALUES (?, ?, ?, 'false', ?, ?)");
            ins.setString(1, type);
            ins.setString(2, message);
            ins.setString(3, refId);
            ins.setString(4, LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ins.setString(5, email);
            ins.executeUpdate();
            ins.close();
            System.out.println("[Notif] New: " + type + " - " + refId);
        }
    }

    private void markOneAsRead(String notifId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(true);
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE notifications SET is_read = 'true' " +
                "WHERE notif_id = " + notifId);
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
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM notifications " +
                "WHERE user_email = ? AND is_read = 'false'");
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            int count = rs.next() ? rs.getInt(1) : 0;
            rs.close(); stmt.close(); conn.close();
            System.out.println("[Badge] Unread count = " + count);
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
                        rs.getString("notif_id"),
                        rs.getString("type"),
                        rs.getString("message"),
                        rs.getString("is_read"),
                        rs.getString("created_at")
                    });
                }
                rs.close(); conn.close();

                if (items.isEmpty()) {
                    VBox empty = new VBox(8);
                    empty.setStyle("-fx-alignment: CENTER; -fx-padding: 40;");
                    Label emptyLbl = new Label(
                        showingPast[0]
                            ? "No past notifications."
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
        else                             { icon = "📣"; bg = "#e3f2fd"; }

        HBox row = new HBox(14);
        row.setStyle(
            "-fx-padding: 16 24;" +
            "-fx-border-color: #f4f4f4; -fx-border-width: 0 0 1 0;" +
            ("false".equals(isRead)
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
                                  String isRead,
                                  Runnable[] loadNotifsRef, Stage alertStage) {
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

        String goToLabel =
            "complaint".equals(type)   ? "→  Go to Complaints" :
            "payment".equals(type)     ? "→  Go to Payments"   :
                                         "→  Go to Announcements";
        String goToFxml =
            "complaint".equals(type)   ? "Complaints.fxml" :
            "payment".equals(type)     ? "Payments.fxml"   :
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
            detail.close();
            alertStage.close();
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

        Button markBtn = new Button("Mark as Read");
        markBtn.setStyle(
            "-fx-background-color: #1a1a1a; -fx-text-fill: #ffffff;" +
            "-fx-font-size: 12px; -fx-font-weight: bold;" +
            "-fx-background-radius: 8; -fx-padding: 10 24; -fx-cursor: hand;");

        if ("true".equals(isRead)) {
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

    // ── Avatar click ──────────────────────────────────────────────────────────────
    @FXML
    private void handleAvatarClick() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Profile.fxml", true, getClass());
    }

    // ── Primary Stats ─────────────────────────────────────────────────────────────
    private void loadPrimaryStats() {
        int actualResidents = getCount("SELECT COUNT(*) FROM residents");
        int basePop = BASE_POPULATION;
        try {
            String email = SessionManager.getEmail();
            if (email != null) {
                Connection conn = DatabaseConnection.getConnection();
                ResultSet rs = conn.prepareStatement(
                    "SELECT base_population FROM settings WHERE user_email = '" + email + "'"
                ).executeQuery();
                if (rs.next() && rs.getInt("base_population") > 0)
                    basePop = rs.getInt("base_population");
                rs.close(); conn.close();
            }
        } catch (Exception e) { e.printStackTrace(); }
        totalResidentsLabel.setText(String.format("%,d", basePop + actualResidents));
        int pending = getCount(
            "SELECT COUNT(*) FROM payments WHERE status = 'Pending' AND archived = False");
        pendingRequestsLabel.setText(String.valueOf(pending));
        double todayTotal = 0;
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.prepareStatement(
                "SELECT SUM(amount) FROM payments WHERE status = 'Paid'" +
                " AND date_created >= #" + LocalDate.now() +
                "# AND date_created < #" + LocalDate.now().plusDays(1) + "#"
            ).executeQuery();
            if (rs.next()) todayTotal = rs.getDouble(1);
            rs.close(); conn.close();
        } catch (Exception e) { e.printStackTrace(); }
        collectionTodayLabel.setText(String.format("₱%,.2f", todayTotal));
        int open = getCount("SELECT COUNT(*) FROM complaints WHERE status <> 'Resolved'");
        openComplaintsLabel.setText(String.valueOf(open));
    }

    // ── Secondary Stats ───────────────────────────────────────────────────────────
    private void loadSecondaryStats() {
        int active = getCount("SELECT COUNT(*) FROM residents WHERE status = 'Active'");
        activeResidentsLabel.setText(String.format("%,d", BASE_POPULATION + active));
        int resolved = getCount("SELECT COUNT(*) FROM complaints WHERE status = 'Resolved'");
        resolvedComplaintsLabel.setText(String.valueOf(resolved));
        int announcements = getCount("SELECT COUNT(*) FROM announcements");
        totalAnnouncementsLabel.setText(String.valueOf(announcements));
        int paid = getCount("SELECT COUNT(*) FROM payments WHERE status = 'Paid'");
        totalPaidLabel.setText(String.valueOf(paid));
        int expenses = getCount("SELECT COUNT(*) FROM finances WHERE type = 'Expense'");
        totalExpensesLabel.setText(String.valueOf(expenses));
    }

    // ── Recent Requests ───────────────────────────────────────────────────────────
    private void loadRecentRequests() {
        recentRequestsBody.getChildren().clear();
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.prepareStatement(
                "SELECT resident_name, payment_type, status, date_created " +
                "FROM payments ORDER BY ID DESC"
            ).executeQuery();
            int count = 0;
            while (rs.next() && count < 5) {
                String name   = rs.getString("resident_name");
                String type   = rs.getString("payment_type");
                String status = rs.getString("status");
                String date   = rs.getString("date_created");
                if (date != null && date.length() > 10) date = date.substring(0, 10);
                HBox row = new HBox();
                row.setStyle("-fx-padding: 12 0;" +
                    "-fx-border-color: #f8f8f8; -fx-border-width: 0 0 1 0;");
                Label nameLbl = new Label(name != null ? name : "—");
                nameLbl.setPrefWidth(155);
                nameLbl.setStyle(
                    "-fx-font-size: 12px; -fx-text-fill: #333333; -fx-font-weight: bold;");
                Label typeLbl = new Label(type != null ? type : "—");
                typeLbl.setPrefWidth(110);
                typeLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");
                String bg, fg;
                if ("Paid".equals(status)) { bg = "#e8f5e9"; fg = "#2e7d32"; }
                else                       { bg = "#fff8e1"; fg = "#f59e0b"; }
                Label statusLbl = new Label(status != null ? status : "—");
                statusLbl.setPrefWidth(100);
                statusLbl.setStyle(
                    "-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";" +
                    "-fx-font-size: 10px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 20; -fx-padding: 3 10;");
                Label dateLbl = new Label(date != null ? date : "—");
                dateLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaaaaa;");
                row.getChildren().addAll(nameLbl, typeLbl, statusLbl, dateLbl);
                recentRequestsBody.getChildren().add(row);
                count++;
            }
            rs.close(); conn.close();
            if (count == 0) {
                Label empty = new Label("No recent requests found.");
                empty.setStyle(
                    "-fx-font-size: 12px; -fx-text-fill: #aaaaaa; -fx-padding: 16 0;");
                recentRequestsBody.getChildren().add(empty);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Announcements ─────────────────────────────────────────────────────────────
    private void loadAnnouncements() {
        announcementsBody.getChildren().clear();
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.prepareStatement(
                "SELECT title, category, priority, date_posted, content " +
                "FROM announcements ORDER BY id DESC"
            ).executeQuery();
            int count = 0;
            while (rs.next() && count < 3) {
                String title    = rs.getString("title");
                String priority = rs.getString("priority");
                String date     = rs.getString("date_posted");
                String content  = rs.getString("content");
                if (date != null && date.length() > 10) date = date.substring(0, 10);
                String priorityBg, priorityFg;
                if ("Urgent".equalsIgnoreCase(priority)) {
                    priorityBg = "#ffebee"; priorityFg = "#c62828";
                } else if ("High".equalsIgnoreCase(priority)) {
                    priorityBg = "#fff8e1"; priorityFg = "#f57f17";
                } else {
                    priorityBg = "#f4f4f4"; priorityFg = "#777777";
                }
                VBox item = new VBox(6);
                boolean isLast = (count == 2);
                item.setStyle(isLast ? "-fx-padding: 0;"
                    : "-fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0;" +
                      "-fx-padding: 0 0 12 0;");
                HBox topRow = new HBox(8);
                topRow.setStyle("-fx-alignment: CENTER_LEFT;");
                Label priorityBadge = new Label(priority != null ? priority : "General");
                priorityBadge.setStyle(
                    "-fx-background-color: " + priorityBg + ";" +
                    "-fx-text-fill: " + priorityFg + ";" +
                    "-fx-font-size: 9px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 20; -fx-padding: 2 8;");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                Label dateLbl = new Label(date != null ? date : "");
                dateLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #cccccc;");
                topRow.getChildren().addAll(priorityBadge, spacer, dateLbl);
                Label titleLbl = new Label(title != null ? title : "—");
                titleLbl.setStyle(
                    "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
                titleLbl.setWrapText(true);
                Label contentLbl = new Label(
                    content != null && content.length() > 70
                        ? content.substring(0, 70) + "..." : content);
                contentLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #888888;");
                contentLbl.setWrapText(true);
                item.getChildren().addAll(topRow, titleLbl, contentLbl);
                announcementsBody.getChildren().add(item);
                count++;
            }
            rs.close(); conn.close();
            if (count == 0) {
                Label empty = new Label("No announcements yet.");
                empty.setStyle(
                    "-fx-font-size: 12px; -fx-text-fill: #aaaaaa; -fx-padding: 8 0;");
                announcementsBody.getChildren().add(empty);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Recent Activity ───────────────────────────────────────────────────────────
    private void loadRecentActivity() {
        activityBody.getChildren().clear();
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.prepareStatement(
                "SELECT action, performed_by, log_date FROM logs ORDER BY log_id DESC"
            ).executeQuery();
            int count = 0;
            while (rs.next() && count < 5) {
                String action = rs.getString("action");
                String by     = rs.getString("performed_by");
                String date   = rs.getString("log_date");
                if (date != null && date.length() > 10) date = date.substring(0, 10);
                HBox item = new HBox(10);
                item.setStyle("-fx-alignment: CENTER_LEFT;");
                Circle dot = new Circle(4);
                dot.setStyle("-fx-fill: #2d2d2d;");
                VBox textBox = new VBox(2);
                HBox.setHgrow(textBox, Priority.ALWAYS);
                Label actionLbl = new Label(action != null ? action : "—");
                actionLbl.setStyle(
                    "-fx-font-size: 11px; -fx-text-fill: #333333; -fx-font-weight: bold;");
                actionLbl.setWrapText(true);
                Label metaLbl = new Label(
                    (by != null ? by : "Admin") + " • " + (date != null ? date : ""));
                metaLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #aaaaaa;");
                textBox.getChildren().addAll(actionLbl, metaLbl);
                item.getChildren().addAll(dot, textBox);
                VBox wrapper = new VBox(10, item);
                if (count < 4) {
                    wrapper.setStyle(
                        "-fx-border-color: #f4f4f4; -fx-border-width: 0 0 1 0;" +
                        "-fx-padding: 0 0 10 0;");
                }
                activityBody.getChildren().add(wrapper);
                count++;
            }
            rs.close(); conn.close();
            if (count == 0) {
                Label empty = new Label("No activity recorded yet.");
                empty.setStyle(
                    "-fx-font-size: 12px; -fx-text-fill: #aaaaaa; -fx-padding: 8 0;");
                activityBody.getChildren().add(empty);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Helper ────────────────────────────────────────────────────────────────────
    private int getCount(String sql) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.prepareStatement(sql).executeQuery();
            int count = rs.next() ? rs.getInt(1) : 0;
            rs.close(); conn.close();
            return count;
        } catch (Exception e) {
            e.printStackTrace(); return 0;
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────────────
    @FXML private void handleLogout() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "login.fxml", false, getClass());
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
    @FXML private void goToAdmin() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Admin.fxml", true, getClass());
    }
    @FXML private void goToSettings() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Settings.fxml", true, getClass());
    }
}