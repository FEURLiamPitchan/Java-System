package com.mycompany.javasystem;

// iText
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

// JavaFX
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

// Java
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ComplaintsController {

    @FXML private BorderPane rootPane;
    @FXML private ScrollPane mainScrollPane;
    @FXML private VBox complaintsTableBody;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatus;
    @FXML private ComboBox<String> filterType;
    @FXML private DatePicker filterDateFrom;
    @FXML private DatePicker filterDateTo;
    @FXML private Button logoutButton;
    @FXML private Button alertsButton;
    @FXML private HBox avatarBox;
    @FXML private Circle avatarCircle;
    @FXML private ImageView profileImageView;
    @FXML private Label avatarInitialLabel;
    @FXML private Label topBarNameLabel;
    @FXML private Label topBarRoleLabel;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label alertBadge;
    @FXML private Label totalLabel;
    @FXML private Label pendingLabel;
    @FXML private Label underReviewLabel;
    @FXML private Label resolvedLabel;
    @FXML private Label pageLabel;

    // Pagination
    private static final int PAGE_SIZE = 10;
    private int currentPage = 1;

    // Full complaint list for sorting/pagination
    private List<String[]> allComplaints = new ArrayList<>();

    // Sort state: field and ascending/descending
    private String sortField = "id";
    private boolean sortAsc = false;

    @FXML
    public void initialize() {
        filterStatus.getItems().addAll("All", "Pending", "Under Review", "Resolved");
        filterStatus.setValue("All");
        filterType.getItems().addAll("All", "Noise Complaint", "Property Dispute",
                "Public Disturbance", "Infrastructure Issue", "Other");
        filterType.setValue("All");
        
        loadTopBar();
        loadAvatarPicture();
        loadComplaints();
        loadSummary();
        syncNotifications();
        refreshAlertBadge();

        // Apply theme with delay
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                if (stage != null && stage.getScene() != null) {
                    System.out.println("[ComplaintsController] Applying theme - isDarkMode: " + ThemeManager.isDarkMode);
                    ThemeManager.applyTheme(stage);

                    if (ThemeManager.isDarkMode) {
                        Platform.runLater(() -> {
                            applyThemeToRoot();
                            applyDarkModeOverrides();
                        });
                    }
                }
            } catch (Exception e) {
                System.out.println("[ComplaintsController] Error applying theme: " + e.getMessage());
            }
        });
    }

    // ✅ Apply theme colors to entire page - DARK MODE (SIMPLIFIED)
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
                System.out.println("[ComplaintsController] Error applying theme to root: " + e.getMessage());
            }
        });
    }

    // ✅ Apply dark mode specific overrides - TABLES & CARDS ONLY
    private void applyDarkModeOverrides() {
        try {
            // Style all VBox cards (summary cards)
            for (javafx.scene.Node node : rootPane.lookupAll(".vbox")) {
                if (node instanceof VBox) {
                    VBox vbox = (VBox) node;
                    String style = vbox.getStyle();
                    if (style != null && style.contains("-fx-background-color: #ffffff")) {
                        vbox.setStyle(
                            "-fx-background-color: #1a1a1a;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 12, 0, 0, 3);");
                    }
                }
            }

            // Style labels in summary cards
            for (javafx.scene.Node node : rootPane.lookupAll(".label")) {
                if (node instanceof Label) {
                    Label label = (Label) node;
                    String style = label.getStyle();
                    if (style != null && !style.isEmpty()) {
                        style = style.replace("-fx-text-fill: #1a1a1a;", "-fx-text-fill: #ffffff;");
                        style = style.replace("-fx-text-fill: #333333;", "-fx-text-fill: #e8e8e8;");
                        style = style.replace("-fx-text-fill: #555555;", "-fx-text-fill: #b0b0b0;");
                        style = style.replace("-fx-text-fill: #aaaaaa;", "-fx-text-fill: #888888;");
                        style = style.replace("-fx-text-fill: #bbbbbb;", "-fx-text-fill: #777777;");
                        label.setStyle(style);
                    }
                }
            }

            // Style buttons
            for (javafx.scene.Node node : rootPane.lookupAll(".button")) {
                if (node instanceof Button) {
                    Button btn = (Button) node;
                    String style = btn.getStyle();
                    if (style != null && !style.isEmpty()) {
                        if (style.contains("-fx-background-color: #f4f4f4") ||
                            style.contains("-fx-background-color: #f8f9fa") ||
                            style.contains("-fx-background-color: #ffffff")) {
                            style = style.replace("-fx-background-color: #f4f4f4;", "-fx-background-color: #2a2a2a;");
                            style = style.replace("-fx-background-color: #f8f9fa;", "-fx-background-color: #2a2a2a;");
                            style = style.replace("-fx-background-color: #ffffff;", "-fx-background-color: #2a2a2a;");
                            style = style.replace("-fx-text-fill: #333333;", "-fx-text-fill: #e8e8e8;");
                            style = style.replace("-fx-text-fill: #555555;", "-fx-text-fill: #b0b0b0;");
                            btn.setStyle(style);
                        }
                    }
                }
            }

            // Style table body rows
            for (javafx.scene.Node node : complaintsTableBody.getChildren()) {
                if (node instanceof HBox) {
                    HBox row = (HBox) node;
                    String style = row.getStyle();
                    if (style != null) {
                        style = style.replace("-fx-background-color: transparent", "-fx-background-color: #1a1a1a");
                        style = style.replace("-fx-border-color: #f8f8f8", "-fx-border-color: #404040");
                    }
                    row.setStyle(style != null ? style : "-fx-background-color: #1a1a1a; -fx-border-color: #404040;");
                    
                    // Style text in row
                    for (javafx.scene.Node child : row.getChildren()) {
                        if (child instanceof Label) {
                            Label lbl = (Label) child;
                            String lblStyle = lbl.getStyle();
                            if (lblStyle != null) {
                                lblStyle = lblStyle.replace("-fx-text-fill: #333333;", "-fx-text-fill: #e8e8e8;");
                                lblStyle = lblStyle.replace("-fx-text-fill: #555555;", "-fx-text-fill: #b0b0b0;");
                                lblStyle = lblStyle.replace("-fx-text-fill: #1a1a1a;", "-fx-text-fill: #ffffff;");
                                lbl.setStyle(lblStyle);
                            }
                        } else if (child instanceof Hyperlink) {
                            Hyperlink link = (Hyperlink) child;
                            link.setStyle("-fx-text-fill: #64b5f6; -fx-padding: 0; -fx-border-color: transparent;");
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("[ComplaintsController] Error applying dark mode overrides: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── TOP BAR ────────────────────────────────────────────────────────────────────
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

    // ── NOTIFICATIONS ──────────────────────────────────────────────────────────────
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

    // ── EXISTING METHODS ───────────────────────────────────────────────────────────

    private void loadSummary() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.prepareStatement(
                "SELECT status, COUNT(*) as cnt FROM complaints GROUP BY status").executeQuery();
            int total = 0, pending = 0, underReview = 0, resolved = 0;
            while (rs.next()) {
                int cnt = rs.getInt("cnt");
                total += cnt;
                switch (rs.getString("status")) {
                    case "Pending": pending = cnt; break;
                    case "Under Review": underReview = cnt; break;
                    case "Resolved": resolved = cnt; break;
                }
            }
            totalLabel.setText(String.valueOf(total));
            pendingLabel.setText(String.valueOf(pending));
            underReviewLabel.setText(String.valueOf(underReview));
            resolvedLabel.setText(String.valueOf(resolved));
            rs.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadComplaints() {
        allComplaints.clear();
        String search = searchField.getText().trim();
        String status = filterStatus.getValue();
        String type = filterType.getValue();
        LocalDate dateFrom = filterDateFrom.getValue();
        LocalDate dateTo = filterDateTo.getValue();

        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.prepareStatement(
                "SELECT * FROM complaints ORDER BY id DESC").executeQuery();

            while (rs.next()) {
                String complaintId = rs.getString("complaint_id");
                String name = rs.getString("complainant_name");
                String incidentType = rs.getString("incident_type");
                String location = rs.getString("location");
                String dateFiled = rs.getString("date_filed");
                String complaintStatus = rs.getString("status");
                String details = rs.getString("incident_details");
                String photoPath = rs.getString("photo_path");
                String adminResponse = rs.getString("admin_response");
                String statusChangedAt = rs.getString("status_changed_at");
                boolean isRead = rs.getBoolean("is_read");

                if (!search.isEmpty() &&
                    !name.toLowerCase().contains(search.toLowerCase()) &&
                    !complaintId.toLowerCase().contains(search.toLowerCase())) continue;

                if (!status.equals("All") && !complaintStatus.equals(status)) continue;

                if (!type.equals("All") && !incidentType.equals(type)) continue;

                if (dateFiled != null && !dateFiled.isEmpty()) {
                    try {
                        LocalDate filed = null;
                        try {
                            filed = LocalDate.parse(dateFiled); 
                        } catch (Exception e1) {
                            try {
                                filed = LocalDate.parse(dateFiled,
                                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                                } catch (Exception e2) {
                                    try {
                                        filed = LocalDate.parse(dateFiled,
                                            java.time.format.DateTimeFormatter.ofPattern("d/M/yyyy"));
                                } catch (Exception e3) {}
                            }
                        }
                        if (filed != null) {
                            if (dateFrom != null && filed.isBefore(dateFrom)) continue;
                            if (dateTo != null && filed.isAfter(dateTo)) continue;
                        }
                    } catch (Exception ignored) {}
                }

                allComplaints.add(new String[]{
                    complaintId, name, incidentType, location,
                    dateFiled, complaintStatus, details,
                    photoPath, adminResponse,
                    isRead ? "true" : "false",
                    statusChangedAt != null ? statusChangedAt : ""
                });
            }
            rs.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        applySortAndRender();
    }

    private void applySortAndRender() {
        Comparator<String[]> comparator;
        switch (sortField) {
            case "name":       comparator = Comparator.comparing(r -> r[1]); break;
            case "type":       comparator = Comparator.comparing(r -> r[2]); break;
            case "date":       comparator = Comparator.comparing(r -> r[4] != null ? r[4] : ""); break;
            case "status":     comparator = Comparator.comparing(r -> r[5]); break;
            default:           comparator = Comparator.comparing(r -> r[0]); break;
        }
        if (!sortAsc) comparator = comparator.reversed();
        allComplaints.sort(comparator);

        int totalPages = Math.max(1, (int) Math.ceil((double) allComplaints.size() / PAGE_SIZE));
        if (currentPage > totalPages) currentPage = totalPages;
        pageLabel.setText("Page " + currentPage + " of " + totalPages);
        prevButton.setDisable(currentPage <= 1);
        nextButton.setDisable(currentPage >= totalPages);

        int from = (currentPage - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, allComplaints.size());
        List<String[]> pageData = allComplaints.subList(from, to);

        renderRows(pageData);
    }

    private void renderRows(List<String[]> data) {
        complaintsTableBody.getChildren().clear();

        if (data.isEmpty()) {
            Label empty = new Label("No complaints found.");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #aaaaaa; -fx-padding: 20 0;");
            VBox.setMargin(empty, new Insets(20, 0, 20, 0));
            complaintsTableBody.getChildren().add(empty);
            return;
        }

        for (String[] c : data) {
            String complaintId = c[0], name = c[1], incidentType = c[2];
            String location = c[3], dateFiled = c[4], complaintStatus = c[5];
            String details = c[6], photoPath = c[7], adminResponse = c[8];
            boolean isRead = "true".equals(c[9]);
            String statusChangedAt = c[10];

            HBox row = new HBox();
            row.setStyle("-fx-padding: 14 0; -fx-border-color: #f8f8f8;" +
                    "-fx-border-width: 0 0 1 0; -fx-background-color: " +
                    (!isRead ? "#fffde7" : "transparent") + ";");

            Label idLabel = new Label(complaintId);
            idLabel.setPrefWidth(100);
            idLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

            Hyperlink nameLink = new Hyperlink(name);
            nameLink.setPrefWidth(180);
            nameLink.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;" +
                    "-fx-text-fill: #1e88e5; -fx-padding: 0; -fx-border-color: transparent;");
            final String fName = name;
            nameLink.setOnAction(e -> openResidentProfile(complaintId, fName));

            Label typeLabel = new Label(incidentType);
            typeLabel.setPrefWidth(160);
            typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

            Label locationLabel = new Label(location);
            locationLabel.setPrefWidth(160);
            locationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

            String dateDisplay = dateFiled != null ? dateFiled : "N/A";
            if (!statusChangedAt.isEmpty()) {
                dateDisplay += "\n⏱ " + statusChangedAt;
            }
            Label dateLabel = new Label(dateDisplay);
            dateLabel.setPrefWidth(120);
            dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #555555;");

            String statusBg, statusFg;
            switch (complaintStatus) {
                case "Resolved":    statusBg = "#e8f5e9"; statusFg = "#4caf50"; break;
                case "Under Review": statusBg = "#e3f2fd"; statusFg = "#1e88e5"; break;
                default:            statusBg = "#fff8e1"; statusFg = "#f59e0b"; break;
            }
            Label statusLabel = new Label(complaintStatus);
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

            final String fId = complaintId, fType = incidentType;
            final String fLoc = location, fDate = dateFiled;
            final String fStatus = complaintStatus, fDetails = details;
            final String fPhoto = photoPath, fResponse = adminResponse;

            Button viewBtn = new Button("View");
            viewBtn.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff;" +
                    "-fx-font-size: 11px; -fx-background-radius: 6;" +
                    "-fx-padding: 5 10; -fx-cursor: hand;");
            viewBtn.setOnAction(e -> openComplaintModal(fId, fName, fType,
                    fLoc, fDate, fStatus, fDetails, fPhoto, fResponse));

            Button deleteBtn = new Button("Delete");
            deleteBtn.setStyle("-fx-background-color: #fff0f0; -fx-text-fill: #e53935;" +
                    "-fx-font-size: 11px; -fx-background-radius: 6;" +
                    "-fx-border-color: #ffcdd2; -fx-border-width: 1;" +
                    "-fx-padding: 5 10; -fx-cursor: hand;");
            deleteBtn.setOnAction(e -> deleteComplaint(fId));

            actionBox.getChildren().addAll(viewBtn, deleteBtn);
            row.getChildren().addAll(idLabel, nameLink, typeLabel,
                    locationLabel, dateLabel, statusBox, actionBox);
            complaintsTableBody.getChildren().add(row);
        }
    }

    private void openResidentProfile(String complaintId, String name) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT resident_id FROM complaints WHERE complaint_id = ?");
            stmt.setString(1, complaintId);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                rs.close(); stmt.close(); conn.close();
                showNotFound(name);
                return;
            }

            String residentId = rs.getString("resident_id");
            rs.close(); stmt.close();

            if (residentId == null || residentId.isEmpty()) {
                conn.close();
                showNotFound(name);
                return;
            }

            PreparedStatement stmt2 = conn.prepareStatement(
                "SELECT resident_id, full_name, age, address, status, date_added FROM residents WHERE resident_id = ?");
            stmt2.setString(1, residentId);
            ResultSet rs2 = stmt2.executeQuery();

            if (rs2.next()) {
                String fullName = rs2.getString("full_name");
                int age = rs2.getInt("age");
                String address = rs2.getString("address");
                String status = rs2.getString("status");
                String dateAdded = rs2.getString("date_added");
                rs2.close(); stmt2.close(); conn.close();

                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("ViewResidentModal.fxml"));
                Parent root = loader.load();
                ViewResidentController ctrl = loader.getController();
                ctrl.setResident(residentId, fullName, age, address, status, dateAdded);

                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initOwner(logoutButton.getScene().getWindow());
                stage.setTitle("Resident Profile");
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.showAndWait();
            } else {
                rs2.close(); stmt2.close(); conn.close();
                showNotFound(name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showNotFound(String name) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Not Found");
        alert.setHeaderText("Resident not found");
        alert.setContentText("No resident record found for: " + name);
        alert.showAndWait();
    }

    private void openComplaintModal(String complaintId, String name, String type,
            String location, String date, String status, String details,
            String photoPath, String adminResponse) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("ViewComplaintModal.fxml"));
            Parent modalRoot = loader.load();
            ViewComplaintController ctrl = loader.getController();
            ctrl.setComplaint(complaintId, name, type, location, date,
                    status, details, photoPath, adminResponse);
            ctrl.setOnUpdate(() -> {
                updateStatusTimestamp(complaintId);
                loadComplaints();
                loadSummary();
                refreshAlertBadge();
            });
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(logoutButton.getScene().getWindow());
            modalStage.setTitle("View Complaint");
            modalStage.setScene(new Scene(modalRoot));
            modalStage.setResizable(false);
            modalStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateStatusTimestamp(String complaintId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE complaints SET status_changed_at = Now() WHERE complaint_id = ?");
            stmt.setString(1, complaintId);
            stmt.executeUpdate();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteComplaint(String complaintId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Complaint");
        confirm.setHeaderText("Delete complaint " + complaintId + "?");
        confirm.setContentText("This action cannot be undone.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM complaints WHERE complaint_id = ?");
                    stmt.setString(1, complaintId);
                    stmt.executeUpdate();
                    stmt.close();
                    conn.close();
                    loadComplaints();
                    loadSummary();
                    refreshAlertBadge();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML private void handleSearch() { currentPage = 1; loadComplaints(); }
    @FXML private void handleFilter() { currentPage = 1; loadComplaints(); }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        filterStatus.setValue("All");
        filterType.setValue("All");
        filterDateFrom.setValue(null);
        filterDateTo.setValue(null);
        currentPage = 1;
        loadComplaints();
    }

    @FXML private void handlePrev() { if (currentPage > 1) { currentPage--; applySortAndRender(); } }
    @FXML private void handleNext() {
        int totalPages = Math.max(1, (int) Math.ceil((double) allComplaints.size() / PAGE_SIZE));
        if (currentPage < totalPages) { currentPage++; applySortAndRender(); }
    }

    @FXML private void sortById()     { toggleSort("id");     }
    @FXML private void sortByName()   { toggleSort("name");   }
    @FXML private void sortByType()   { toggleSort("type");   }
    @FXML private void sortByDate()   { toggleSort("date");   }
    @FXML private void sortByStatus() { toggleSort("status"); }

    private void toggleSort(String field) {
        if (sortField.equals(field)) sortAsc = !sortAsc;
        else { sortField = field; sortAsc = true; }
        currentPage = 1;
        applySortAndRender();
    }

    @FXML
    private void handleExportPDF() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save PDF");
        chooser.setInitialFileName("complaints_report.pdf");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = chooser.showSaveDialog(logoutButton.getScene().getWindow());
        if (file == null) return;

        try {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Paragraph title = new Paragraph("Barangay San Isidro - Complaints Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            Paragraph date = new Paragraph("Generated: " + LocalDate.now(),
                new Font(Font.FontFamily.HELVETICA, 9));
            date.setAlignment(Element.ALIGN_CENTER);
            date.setSpacingAfter(16);
            document.add(date);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.2f, 2f, 1.8f, 1.8f, 1.4f, 1.4f, 2.5f});

            Font headerFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE);
            BaseColor headerBg = new BaseColor(45, 45, 45);
            String[] headers = {"ID", "Complainant", "Incident Type",
                                 "Location", "Date Filed", "Status", "Admin Response"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(headerBg);
                cell.setPadding(7);
                table.addCell(cell);
            }

            Font cellFont = new Font(Font.FontFamily.HELVETICA, 8);
            for (String[] c : allComplaints) {
                table.addCell(new PdfPCell(new Phrase(c[0], cellFont)));
                table.addCell(new PdfPCell(new Phrase(c[1], cellFont)));
                table.addCell(new PdfPCell(new Phrase(c[2], cellFont)));
                table.addCell(new PdfPCell(new Phrase(c[3], cellFont)));
                table.addCell(new PdfPCell(new Phrase(c[4] != null ? c[4] : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(c[5], cellFont)));
                table.addCell(new PdfPCell(new Phrase(c[8] != null ? c[8] : "", cellFont)));
            }

            document.add(table);
            document.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Successful");
            alert.setHeaderText(null);
            alert.setContentText("PDF saved to: " + file.getAbsolutePath());
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Failed");
            alert.setContentText("Could not export PDF: " + e.getMessage());
            alert.showAndWait();
        }
    }

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
    @FXML private void handleLogout() {
        SessionManager.logout();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "login.fxml", false, getClass());
    }
}