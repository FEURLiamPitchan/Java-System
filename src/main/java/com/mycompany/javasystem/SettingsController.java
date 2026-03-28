package com.mycompany.javasystem;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class SettingsController {

    @FXML private Button logoutButton;
    @FXML private Label topBarNameLabel;
    @FXML private Label topBarRoleLabel;
    @FXML private HBox avatarBox;
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

    // Toggle switch nodes for notifications
    private StackPane complaintsToggle;
    private StackPane paymentsToggle;
    private StackPane announcementsToggle;

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
        loadSettingsFromDB();
        showGeneralTab();

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

    // ── Top Bar ───────────────────────────────────────────────────────────────────
    private void loadTopBar() {
        String name = SessionManager.getName();
        String role = SessionManager.getRole();
        if (topBarNameLabel != null)
            topBarNameLabel.setText(name != null ? name : "Administrator");
        if (topBarRoleLabel != null)
            topBarRoleLabel.setText(role != null ? capitalize(role) : "Admin");
    }

    // ── Load Settings ─────────────────────────────────────────────────────────────
    private void loadSettingsFromDB() {
        String email = SessionManager.getEmail();
        if (email == null) return;
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.prepareStatement(
                "SELECT * FROM settings WHERE user_email = '" + email + "'"
            ).executeQuery();
            if (rs.next()) {
                notifComplaints    = "true".equals(rs.getString("notif_complaints"));
                notifPayments      = "true".equals(rs.getString("notif_payments"));
                notifAnnouncements = "true".equals(rs.getString("notif_announcements"));
                String fs = rs.getString("font_size");
                if (fs != null) currentFontSize = fs;
            } else {
                insertDefaultSettings(email);
            }
            rs.close();
            conn.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void insertDefaultSettings(String email) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO settings (user_email, dark_mode, font_size, " +
                "notif_complaints, notif_payments, notif_announcements, base_population) " +
                "VALUES (?, 'false', 'Medium', 'true', 'true', 'true', 6474)");
            stmt.setString(1, email);
            stmt.executeUpdate();
            stmt.close();
            conn.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Tab Switching ─────────────────────────────────────────────────────────────
    private void setActiveTab(Button active) {
        for (Button btn : new Button[]{
            tabGeneralBtn, tabNotifBtn, tabAppearanceBtn, tabDataBtn}) {
            btn.setStyle(inactiveTabStyle);
        }
        active.setStyle(activeTabStyle);
    }

    @FXML private void showGeneralTab() {
        setActiveTab(tabGeneralBtn);
        tabContent.getChildren().setAll(buildGeneralTab());
    }
    @FXML private void showNotifTab() {
        setActiveTab(tabNotifBtn);
        tabContent.getChildren().setAll(buildNotifTab());
    }
    @FXML private void showAppearanceTab() {
        setActiveTab(tabAppearanceBtn);
        tabContent.getChildren().setAll(buildAppearanceTab());
    }
    @FXML private void showDataTab() {
        setActiveTab(tabDataBtn);
        tabContent.getChildren().setAll(buildDataTab());
    }

    // ── GENERAL TAB ───────────────────────────────────────────────────────────────
    private Node buildGeneralTab() {
        VBox container = new VBox(0);
        container.setMaxWidth(Double.MAX_VALUE);
        container.setStyle(
            "-fx-background-color: #ffffff; -fx-background-radius: 16;" +
            "-fx-border-color: #e8e8e8; -fx-border-width: 1;");

        // Header
        VBox header = new VBox(4);
        header.setStyle(
            "-fx-background-color: #1a1a1a; -fx-background-radius: 16 16 0 0;" +
            "-fx-padding: 22 28;");
        Label title = new Label("General Settings");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        Label sub = new Label("Configure system-wide preferences");
        sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");
        header.getChildren().addAll(title, sub);

        VBox body = new VBox(0);
        body.setStyle("-fx-padding: 0;");

        // Population base row
        TextField popField = new TextField();
        popField.setStyle(
            "-fx-font-size: 13px; -fx-padding: 10 14; -fx-background-radius: 10;" +
            "-fx-border-color: #e8e8e8; -fx-border-width: 1; -fx-border-radius: 10;" +
            "-fx-background-color: #f8f9fa; -fx-max-width: 200;");
        popField.setPromptText("e.g. 6474");

        Label popErrorLbl = new Label("");
        popErrorLbl.setStyle("-fx-font-size: 11px;");

        // Load current value
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.prepareStatement(
                "SELECT base_population FROM settings WHERE user_email = '" +
                SessionManager.getEmail() + "'"
            ).executeQuery();
            if (rs.next()) popField.setText(String.valueOf(rs.getInt("base_population")));
            rs.close();
            conn.close();
        } catch (Exception e) { e.printStackTrace(); }

        Button savePopBtn = new Button("Save");
        savePopBtn.setStyle(
            "-fx-background-color: #1a1a1a; -fx-text-fill: #ffffff;" +
            "-fx-font-size: 12px; -fx-font-weight: bold;" +
            "-fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;");
        savePopBtn.setOnAction(e -> {
            String val = popField.getText().trim();
            try {
                int pop = Integer.parseInt(val);
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE settings SET base_population = ? WHERE user_email = ?");
                stmt.setInt(1, pop);
                stmt.setString(2, SessionManager.getEmail());
                stmt.executeUpdate();
                stmt.close();
                conn.close();
                setStatus(popErrorLbl, "✅  Saved!", true);
            } catch (Exception ex) {
                setStatus(popErrorLbl, "⚠  Enter a valid number.", false);
            }
        });

        HBox popRow = new HBox(12);
        popRow.setStyle("-fx-alignment: CENTER_LEFT;");
        popRow.getChildren().addAll(popField, savePopBtn, popErrorLbl);

        body.getChildren().addAll(
            buildSettingRow("Population Base",
                "The base census count added to your DB resident count on the dashboard",
                popRow, false),
            buildSettingRow("System Version",
                "Current version of the Barangay Management System",
                buildReadOnlyValue("v1.0.0"), false),
            buildSettingRow("Database",
                "Connected to Microsoft Access database",
                buildReadOnlyValue("MS Access — Connected ✅"), true)
        );

        container.getChildren().addAll(header, body);
        return container;
    }

    // ── NOTIFICATIONS TAB ─────────────────────────────────────────────────────────
    private Node buildNotifTab() {
        VBox container = new VBox(0);
        container.setMaxWidth(Double.MAX_VALUE);
        container.setStyle(
            "-fx-background-color: #ffffff; -fx-background-radius: 16;" +
            "-fx-border-color: #e8e8e8; -fx-border-width: 1;");

        VBox header = new VBox(4);
        header.setStyle(
            "-fx-background-color: #1a1a1a; -fx-background-radius: 16 16 0 0;" +
            "-fx-padding: 22 28;");
        Label title = new Label("Notification Settings");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        Label sub = new Label("Choose which alerts you want to receive");
        sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");
        header.getChildren().addAll(title, sub);

        // Build toggles
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
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE settings SET notif_complaints=?, notif_payments=?," +
                    " notif_announcements=? WHERE user_email=?");
                stmt.setString(1, isToggleOn(complaintsToggle)    ? "true" : "false");
                stmt.setString(2, isToggleOn(paymentsToggle)      ? "true" : "false");
                stmt.setString(3, isToggleOn(announcementsToggle) ? "true" : "false");
                stmt.setString(4, SessionManager.getEmail());
                stmt.executeUpdate();
                stmt.close();
                conn.close();
                setStatus(errorLbl, "✅  Notification settings saved!", true);
            } catch (Exception ex) {
                ex.printStackTrace();
                setStatus(errorLbl, "⚠  Database error.", false);
            }
        });

        HBox footer = new HBox(10);
        footer.setStyle("-fx-padding: 20 28; -fx-alignment: CENTER_RIGHT;");
        footer.getChildren().addAll(errorLbl, saveBtn);

        VBox body = new VBox(0);
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

    // ── APPEARANCE TAB ────────────────────────────────────────────────────────────
    private Node buildAppearanceTab() {
        VBox container = new VBox(0);
        container.setMaxWidth(Double.MAX_VALUE);
        container.setStyle(
            "-fx-background-color: #ffffff; -fx-background-radius: 16;" +
            "-fx-border-color: #e8e8e8; -fx-border-width: 1;");

        VBox header = new VBox(4);
        header.setStyle(
            "-fx-background-color: #1a1a1a; -fx-background-radius: 16 16 0 0;" +
            "-fx-padding: 22 28;");
        Label title = new Label("Appearance");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        Label sub = new Label("Customize how the system looks and feels");
        sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");
        header.getChildren().addAll(title, sub);

        // Font size buttons
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
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE settings SET font_size = ? WHERE user_email = ?");
                stmt.setString(1, currentFontSize);
                stmt.setString(2, SessionManager.getEmail());
                stmt.executeUpdate();
                stmt.close();
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

        // Dark mode — coming soon
        Label soonBadge = new Label("Coming Soon");
        soonBadge.setStyle(
            "-fx-background-color: #e3f2fd; -fx-text-fill: #1565c0;" +
            "-fx-font-size: 10px; -fx-font-weight: bold;" +
            "-fx-background-radius: 20; -fx-padding: 4 12;");

        VBox body = new VBox(0);
        body.getChildren().addAll(
            buildSettingRow("Font Size",
                "Adjust the text size throughout the system",
                fontRow, false),
            buildSettingRow("Dark Mode",
                "Switch between light and dark interface theme",
                soonBadge, true)
        );

        container.getChildren().addAll(header, body);
        return container;
    }

    // ── DATA TAB ──────────────────────────────────────────────────────────────────
    private Node buildDataTab() {
        VBox container = new VBox(0);
        container.setMaxWidth(Double.MAX_VALUE);
        container.setStyle(
            "-fx-background-color: #ffffff; -fx-background-radius: 16;" +
            "-fx-border-color: #e8e8e8; -fx-border-width: 1;");

        VBox header = new VBox(4);
        header.setStyle(
            "-fx-background-color: #1a1a1a; -fx-background-radius: 16 16 0 0;" +
            "-fx-padding: 22 28;");
        Label title = new Label("Data and Export");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        Label sub = new Label("Export records and manage system data");
        sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");
        header.getChildren().addAll(title, sub);

        // Export residents
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

        // Export payments
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

        // Export finances
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

        // Clear logs
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

    // ── ROW BUILDERS ─────────────────────────────────────────────────────────────
    private HBox buildSettingRow(String title, String description,
                                  Node control, boolean isLast) {
        HBox row = new HBox(16);
        row.setStyle(
            "-fx-padding: 20 28;" +
            (isLast ? "" : "-fx-border-color: #f4f4f4; -fx-border-width: 0 0 1 0;"));
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox textBox = new VBox(4);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        Label titleLbl = new Label(title);
        titleLbl.setStyle(
            "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
        Label descLbl = new Label(description);
        descLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaaaaa;");
        descLbl.setWrapText(true);
        textBox.getChildren().addAll(titleLbl, descLbl);

        row.getChildren().addAll(textBox, control);
        return row;
    }

    private HBox buildToggleRow(String title, String description,
                                 StackPane toggle, boolean isLast) {
        HBox row = new HBox(16);
        row.setStyle(
            "-fx-padding: 20 28;" +
            (isLast ? "" : "-fx-border-color: #f4f4f4; -fx-border-width: 0 0 1 0;"));
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox textBox = new VBox(4);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        Label titleLbl = new Label(title);
        titleLbl.setStyle(
            "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
        Label descLbl = new Label(description);
        descLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaaaaa;");
        textBox.getChildren().addAll(titleLbl, descLbl);

        row.getChildren().addAll(textBox, toggle);
        return row;
    }

    private Label buildReadOnlyValue(String value) {
        Label lbl = new Label(value);
        lbl.setStyle(
            "-fx-font-size: 12px; -fx-text-fill: #555555;" +
            "-fx-background-color: #f4f4f4; -fx-background-radius: 8;" +
            "-fx-padding: 8 14;");
        return lbl;
    }

    private String buildActionBtnStyle(String bg, String fg, String border) {
        return "-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";" +
               "-fx-font-size: 12px; -fx-font-weight: bold;" +
               "-fx-background-radius: 8; -fx-border-color: " + border + ";" +
               "-fx-border-width: 1; -fx-padding: 9 18; -fx-cursor: hand;";
    }

    // ── TOGGLE SWITCH ─────────────────────────────────────────────────────────────
    private StackPane buildToggle(boolean initialState) {
        // Track with background
        Rectangle track = new Rectangle(46, 24);
        track.setArcWidth(24);
        track.setArcHeight(24);

        // Thumb (circle)
        Circle thumb = new Circle(10);
        thumb.setStyle("-fx-fill: #ffffff;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 1);");

        StackPane toggle = new StackPane(track, thumb);
        toggle.setPrefSize(46, 24);
        toggle.setStyle("-fx-cursor: hand;");

        // Store state as userData
        toggle.setUserData(initialState);
        updateToggleVisual(toggle, track, thumb, initialState);

        toggle.setOnMouseClicked(e -> {
            boolean current = (boolean) toggle.getUserData();
            boolean newState = !current;
            toggle.setUserData(newState);
            updateToggleVisual(toggle, track, thumb, newState);
        });

        return toggle;
    }

    private void updateToggleVisual(StackPane toggle, Rectangle track,
                                     Circle thumb, boolean isOn) {
        if (isOn) {
            track.setStyle("-fx-fill: #2e7d32;");
            StackPane.setAlignment(thumb, javafx.geometry.Pos.CENTER_RIGHT);
            thumb.setTranslateX(-3);
        } else {
            track.setStyle("-fx-fill: #cccccc;");
            StackPane.setAlignment(thumb, javafx.geometry.Pos.CENTER_LEFT);
            thumb.setTranslateX(3);
        }
    }

    private boolean isToggleOn(StackPane toggle) {
        return toggle != null && (boolean) toggle.getUserData();
    }

    // ── AVATAR / PROFILE MODAL ────────────────────────────────────────────────────
    @FXML
    private void handleAvatarClick() {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.initOwner(logoutButton.getScene().getWindow());
        modal.setTitle("My Profile");
        modal.setResizable(false);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #ffffff; -fx-min-width: 440;");

        // Header
        VBox header = new VBox(6);
        header.setFocusTraversable(true);
        header.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 24 28 22 28;");
        Label titleLbl = new Label("My Profile");
        titleLbl.setStyle(
            "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        Label subLbl = new Label("View and update your account information");
        subLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");
        header.getChildren().addAll(titleLbl, subLbl);

        VBox body = new VBox(16);
        body.setStyle("-fx-padding: 24 28;");

        // Avatar row
        String name  = SessionManager.getName();
        String email = SessionManager.getEmail();
        String role  = SessionManager.getRole();

        HBox avatarRow = new HBox(16);
        avatarRow.setStyle("-fx-alignment: CENTER_LEFT;");
        StackPane avatarCircle = new StackPane();
        avatarCircle.setStyle(
            "-fx-background-color: #2d2d2d; -fx-background-radius: 35;" +
            "-fx-min-width: 70; -fx-min-height: 70;" +
            "-fx-max-width: 70; -fx-max-height: 70;");
        Label avatarLbl = new Label(
            name != null && !name.isEmpty()
                ? String.valueOf(name.charAt(0)).toUpperCase() : "A");
        avatarLbl.setStyle(
            "-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        avatarCircle.getChildren().add(avatarLbl);

        VBox infoBox = new VBox(4);
        Label nameLbl = new Label(name != null ? name : "—");
        nameLbl.setStyle(
            "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
        Label emailLbl = new Label(email != null ? email : "—");
        emailLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #777777;");
        Label roleBadge = new Label(role != null ? capitalize(role) : "—");
        roleBadge.setStyle(
            "-fx-background-color: #e3f2fd; -fx-text-fill: #1565c0;" +
            "-fx-font-size: 10px; -fx-font-weight: bold;" +
            "-fx-background-radius: 20; -fx-padding: 3 10;");
        infoBox.getChildren().addAll(nameLbl, emailLbl, roleBadge);
        avatarRow.getChildren().addAll(avatarCircle, infoBox);

        Separator sep = new Separator();
        sep.setStyle("-fx-opacity: 0.2;");

        // Edit fields
        String labelStyle =
            "-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #999999;";
        String fieldStyle =
            "-fx-font-size: 13px; -fx-padding: 10 14; -fx-background-radius: 8;" +
            "-fx-border-color: #e8e8e8; -fx-border-width: 1; -fx-border-radius: 8;" +
            "-fx-background-color: #f8f9fa;";

        Label nameFldLbl = new Label("FULL NAME");
        nameFldLbl.setStyle(labelStyle);
        TextField nameField = new TextField(name != null ? name : "");
        nameField.setStyle(fieldStyle);
        nameField.setMaxWidth(Double.MAX_VALUE);
        VBox nameGroup = new VBox(6, nameFldLbl, nameField);

        Label emailFldLbl = new Label("EMAIL ADDRESS");
        emailFldLbl.setStyle(labelStyle);
        TextField emailField = new TextField(email != null ? email : "");
        emailField.setStyle(fieldStyle);
        emailField.setMaxWidth(Double.MAX_VALUE);
        VBox emailGroup = new VBox(6, emailFldLbl, emailField);

        Label errorLbl = new Label("");
        errorLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #c62828;");

        body.getChildren().addAll(avatarRow, sep, nameGroup, emailGroup, errorLbl);

        // Footer
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

        Button saveBtn = new Button("Save Changes");
        saveBtn.setStyle(
            "-fx-background-color: #1a1a1a; -fx-text-fill: #ffffff;" +
            "-fx-font-size: 12px; -fx-font-weight: bold;" +
            "-fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> {
            String newName  = nameField.getText().trim();
            String newEmail = emailField.getText().trim();
            String oldEmail = SessionManager.getEmail();

            if (newName.isEmpty() || newEmail.isEmpty()) {
                errorLbl.setText("⚠  Please fill in all fields.");
                return;
            }
            if (!newEmail.contains("@")) {
                errorLbl.setText("⚠  Enter a valid email address.");
                return;
            }
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE users SET full_name = ?, email = ? WHERE email = ?");
                stmt.setString(1, newName);
                stmt.setString(2, newEmail);
                stmt.setString(3, oldEmail);
                stmt.executeUpdate();
                stmt.close();

                if (!newEmail.equals(oldEmail)) {
                    PreparedStatement stmt2 = conn.prepareStatement(
                        "UPDATE settings SET user_email = ? WHERE user_email = ?");
                    stmt2.setString(1, newEmail);
                    stmt2.setString(2, oldEmail);
                    stmt2.executeUpdate();
                    stmt2.close();
                }
                conn.close();

                SessionManager.login(newEmail, SessionManager.getRole(), newName);
                loadTopBar();
                modal.close();
                showInfo("✅  Profile updated successfully!");
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLbl.setText("⚠  Database error.");
            }
        });

        footer.getChildren().addAll(cancelBtn, saveBtn);
        root.getChildren().addAll(header, body, footer);
        modal.setScene(new Scene(root));
        Platform.runLater(() -> root.requestFocus());
        modal.showAndWait();
    }

    // ── EXPORT HELPERS ────────────────────────────────────────────────────────────
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
            writer.write("Payment ID,Ref,Resident,Type,Amount,Status,Date\n");
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.prepareStatement(
                "SELECT payment_id, ref_number, resident_name, payment_type, " +
                "amount, status, date_created FROM payments ORDER BY ID DESC"
            ).executeQuery();
            while (rs.next()) {
                writer.write(
                    clean(rs.getString("payment_id")) + "," +
                    clean(rs.getString("ref_number")) + "," +
                    clean(rs.getString("resident_name")) + "," +
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
                "SELECT finance_id, description, category, type, amount, status, date_recorded " +
                "FROM finances ORDER BY finance_id DESC"
            ).executeQuery();
            while (rs.next()) {
                writer.write(
                    rs.getInt("finance_id") + "," +
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

    // ── HELPERS ───────────────────────────────────────────────────────────────────
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

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ── NAVIGATION ────────────────────────────────────────────────────────────────
    @FXML private void handleLogout() {
        SessionManager.logout();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "login.fxml", false, getClass());
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
}