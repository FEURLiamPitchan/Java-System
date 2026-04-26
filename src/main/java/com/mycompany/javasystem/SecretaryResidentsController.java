package com.mycompany.javasystem;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SecretaryResidentsController {

    @FXML private VBox residentsTableBody;
    @FXML private TextField searchField;
    @FXML private Button logoutButton;
    @FXML private Button alertsButton;
    @FXML private Label alertBadge;
    @FXML private HBox avatarBox;
    @FXML private Circle avatarCircle;
    @FXML private ImageView profileImageView;
    @FXML private Label avatarInitialLabel;
    @FXML private Label topBarNameLabel;
    @FXML private Label topBarRoleLabel;

    @FXML
    public void initialize() {
        loadTopBar();
        loadAvatarPicture();
        loadResidents("");
        syncNotifications();
        refreshAlertBadge();
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

// ── Load Residents ────────────────────────────────────────────────────────────
private void loadResidents(String search) {
    residentsTableBody.getChildren().clear();

    try {
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt;

        if (search.isEmpty()) {
            stmt = conn.prepareStatement("SELECT * FROM residents ORDER BY id");
        } else {
            stmt = conn.prepareStatement(
                "SELECT * FROM residents WHERE full_name LIKE ? " +
                "OR address LIKE ? OR contact_number LIKE ?"
            );
            stmt.setString(1, "%" + search + "%");
            stmt.setString(2, "%" + search + "%");
            stmt.setString(3, "%" + search + "%");
        }

        ResultSet rs = stmt.executeQuery();
        boolean hasData = false;
        int rowNumber = 1;

        while (rs.next()) {
            hasData = true;

            int id = rs.getInt("id");
            String fullName = rs.getString("full_name");
            int age = rs.getInt("age");
            String address = rs.getString("address");
            String status = rs.getString("status");
            String dateAdded = rs.getString("date_added") != null
                    ? rs.getString("date_added") : "N/A";

            // NEW FIELDS
            String gender = rs.getString("gender") != null ? rs.getString("gender") : "N/A";
            String birthPlace = rs.getString("birth_place") != null ? rs.getString("birth_place") : "N/A";
            String birthDate = rs.getString("birth_date") != null ? rs.getString("birth_date") : "N/A";
            String civilStatus = rs.getString("civil_status") != null ? rs.getString("civil_status") : "N/A";
            String contactNumber = rs.getString("contact_number") != null ? rs.getString("contact_number") : "N/A";

            // ── ROW ─────────────────────────────────────────────
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle(
                "-fx-padding: 14 16;" +
                "-fx-border-color: #f8f8f8;" +
                "-fx-border-width: 0 0 1 0;"
            );

            // ── ID ─────────────────────────────────────────────
            Label idLabel = new Label(String.valueOf(rowNumber));
            idLabel.setPrefWidth(100);
            idLabel.setMinWidth(100);
            idLabel.setMaxWidth(100);
            idLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

            // ── NAME ───────────────────────────────────────────
            Label nameLabel = new Label(fullName);
            nameLabel.setPrefWidth(180);
            nameLabel.setMinWidth(180);
            nameLabel.setMaxWidth(180);
            nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333333;");

            // ── AGE ────────────────────────────────────────────
            Label ageLabel = new Label(String.valueOf(age));
            ageLabel.setPrefWidth(100);
            ageLabel.setMinWidth(100);
            ageLabel.setMaxWidth(100);
            ageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

            // ── ADDRESS ────────────────────────────────────────
            Label addressLabel = new Label(address);
            addressLabel.setPrefWidth(250);
            addressLabel.setMinWidth(250);
            addressLabel.setMaxWidth(250);
            addressLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

            // ── STATUS ─────────────────────────────────────────
            String statusColor = status.equals("Active") ? "#e8f5e9" : "#fff8e1";
            String statusTextColor = status.equals("Active") ? "#4caf50" : "#f59e0b";

            Label statusLabel = new Label(status);
            statusLabel.setPrefWidth(100);
            statusLabel.setMinWidth(100);
            statusLabel.setMaxWidth(100);
            statusLabel.setStyle(
                "-fx-background-color: " + statusColor + ";" +
                "-fx-text-fill: " + statusTextColor + ";" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 4;" +
                "-fx-padding: 3 8;"
            );

            // ── BUTTON ─────────────────────────────────────────
            Button viewBtn = new Button("View");
            viewBtn.setPrefWidth(80);
            viewBtn.setMinWidth(80);
            viewBtn.setMaxWidth(80);
            viewBtn.setStyle(
                "-fx-background-color: #f4f4f4;" +
                "-fx-text-fill: #333333;" +
                "-fx-font-size: 11px;" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 5 12;" +
                "-fx-cursor: hand;"
            );

            final int fId = id;
            final String fFullName = fullName;
            final int fAge = age;
            final String fAddress = address;
            final String fStatus = status;
            final String fDateAdded = dateAdded;
            final String fGender = gender;
            final String fBirthPlace = birthPlace;
            final String fBirthDate = birthDate;
            final String fCivilStatus = civilStatus;
            final String fContactNumber = contactNumber;

            viewBtn.setOnAction(e -> {
                try {
                    FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("SecretaryViewResidentModal.fxml")
                    );
                    Parent modalRoot = loader.load();

                    SecretaryViewResidentController viewController = loader.getController();
                    viewController.setResident(
                        fId, fFullName, fAge, fAddress, fStatus, fDateAdded,
                        fGender, fBirthPlace, fBirthDate, fCivilStatus, fContactNumber
                    );

                    viewController.setOnDelete(() -> loadResidents(""));

                    Stage modalStage = new Stage();
                    modalStage.initModality(Modality.APPLICATION_MODAL);
                    modalStage.initOwner(logoutButton.getScene().getWindow());
                    modalStage.setTitle("View Resident");
                    modalStage.setScene(new Scene(modalRoot));
                    modalStage.setResizable(false);
                    modalStage.showAndWait();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            row.getChildren().addAll(
                idLabel, nameLabel, ageLabel, addressLabel, statusLabel, viewBtn
            );

            residentsTableBody.getChildren().add(row);
            rowNumber++;
        }

        if (!hasData) {
            Label empty = new Label("No residents found.");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #aaaaaa; -fx-padding: 20 0;");
            VBox.setMargin(empty, new Insets(20, 0, 20, 0));
            residentsTableBody.getChildren().add(empty);
        }

        rs.close();
        stmt.close();
        conn.close();

    } catch (Exception e) {
        e.printStackTrace();
        Label error = new Label("Error loading residents: " + e.getMessage());
        error.setStyle("-fx-font-size: 12px; -fx-text-fill: #e53935;");
        residentsTableBody.getChildren().add(error);
    }
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

    // ── Avatar Click ──────────────────────────────────────────────────────────────
    @FXML
    private void handleAvatarClick() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Profile.fxml", true, getClass());
    }

    @FXML private void handleSearch() {
        loadResidents(searchField.getText().trim());
    }

    @FXML
    private void handleAddResident() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("SecretaryAddResidentModal.fxml"));
            Parent modalRoot = loader.load();
            SecretaryAddResidentController modalController = loader.getController();
            modalController.setOnSuccess(() -> loadResidents(""));
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(logoutButton.getScene().getWindow());
            modalStage.setTitle("Add Resident");
            modalStage.setScene(new Scene(modalRoot));
            modalStage.setResizable(false);
            modalStage.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Navigation ────────────────────────────────────────────────────────────────
    @FXML private void goToDashboard() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "AdminDashboard.fxml", true, getClass());
    }
    @FXML private void handleLogout() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "login.fxml", false, getClass());
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