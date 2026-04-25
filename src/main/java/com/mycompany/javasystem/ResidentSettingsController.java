package com.mycompany.javasystem;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ResidentSettingsController {

    @FXML private Button logoutButton;
    @FXML private Button alertsButton;
    @FXML private Label alertBadge;
    @FXML private javafx.scene.shape.Circle topBarProfileCircle;
    @FXML private Label topBarProfileInitials;
    @FXML private Label residentNameLabel;
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

    private boolean notifDocuments = true;
    private boolean notifComplaints = true;
    private boolean notifAnnouncements = true;
    private String currentFontSize = "Medium";
    private boolean isDarkModeState = false;

    private StackPane documentsToggle;
    private StackPane complaintsToggle;
    private StackPane announcementsToggle;
    private StackPane darkModeToggle;

    private final String activeTabStyle =
        "-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff;" +
        "-fx-font-size: 13px; -fx-font-weight: bold;" +
        "-fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;";
    private final String inactiveTabStyle =
        "-fx-background-color: #ffffff; -fx-text-fill: #555555;" +
        "-fx-font-size: 13px; -fx-background-radius: 8;" +
        "-fx-border-color: #e0e0e0; -fx-border-width: 1;" +
        "-fx-padding: 10 20; -fx-cursor: hand;";
    private final String inactiveTabHoverStyle =
        "-fx-background-color: #f8f8f8; -fx-text-fill: #333333;" +
        "-fx-font-size: 13px; -fx-background-radius: 8;" +
        "-fx-border-color: #d0d0d0; -fx-border-width: 1;" +
        "-fx-padding: 10 20; -fx-cursor: hand;";

    @FXML
    public void initialize() {
        loadUserProfile();
        loadTopBarProfilePicture();
        loadAvatarPicture();
        loadSettingsFromDB();
        
        // Add hover effects to tab buttons
        setupTabButtonHoverEffects();
        
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                if (stage != null && stage.getScene() != null) {
                    ThemeManager.applyTheme(stage);
                }
            } catch (Exception e) {
                System.out.println("[ResidentSettings] Error applying theme: " + e.getMessage());
            }
        });
        
        showGeneralTab();
        refreshAlertBadge();

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

    private void setupTabButtonHoverEffects() {
        for (Button btn : new Button[]{tabGeneralBtn, tabNotifBtn, tabAppearanceBtn}) {
            btn.setOnMouseEntered(e -> {
                if (!btn.getStyle().contains("#2d2d2d")) {
                    btn.setStyle(inactiveTabHoverStyle);
                }
            });
            btn.setOnMouseExited(e -> {
                if (!btn.getStyle().contains("#2d2d2d")) {
                    btn.setStyle(inactiveTabStyle);
                }
            });
        }
    }

    private void loadUserProfile() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                PreparedStatement stmt = conn.prepareStatement(
                    "SELECT full_name FROM users WHERE email = ?");
                stmt.setString(1, UserSession.getCurrentUserEmail());
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next() && residentNameLabel != null) {
                    String fullName = rs.getString("full_name");
                    residentNameLabel.setText(fullName != null && !fullName.trim().isEmpty() ? fullName : "Resident Name");
                }
                
                rs.close();
                stmt.close();
                conn.close();
            }
        } catch (Exception e) {
            System.err.println("Error loading user profile: " + e.getMessage());
        }
    }
    
    private void loadTopBarProfilePicture() {
        if (topBarProfileCircle != null && topBarProfileInitials != null) {
            ProfilePictureLoader.loadProfilePicture(topBarProfileCircle, topBarProfileInitials, UserSession.getCurrentUserEmail());
        }
    }

    private void loadTopBar() {
        String name = UserSession.getCurrentUserName();
        String role = UserSession.getCurrentUserRole();
        if (topBarNameLabel != null)
            topBarNameLabel.setText(name != null ? name : "Resident");
        if (topBarRoleLabel != null)
            topBarRoleLabel.setText(role != null ? capitalize(role) : "Resident");
    }

    private void loadAvatarPicture() {
        Resident_ProfilePictureManager.loadAvatarPicture(
            UserSession.getCurrentUserEmail(),
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

    private void refreshAlertBadge() {
        int count = ResidentNotifications.getUnreadCount(UserSession.getCurrentUserEmail());
        if (count > 0) {
            alertBadge.setText(count > 99 ? "99+" : String.valueOf(count));
            alertBadge.setVisible(true);
        } else {
            alertBadge.setVisible(false);
        }
    }

    @FXML
    private void handleAlertsClick() {
        // Use ResidentNotifications popup
        ResidentNotifications.showNotificationsPopup(logoutButton.getScene().getWindow(), this::refreshAlertBadge);
    }

    @FXML
    private void handleAvatarClick() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "MyProfile.fxml", true, getClass());
    }

    private void loadSettingsFromDB() {
        String email = UserSession.getCurrentUserEmail();
        if (email == null) return;
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.prepareStatement(
                "SELECT * FROM settings WHERE user_email = '" + email + "'"
            ).executeQuery();
            if (rs.next()) {
                notifDocuments = "true".equals(rs.getString("notif_documents"));
                notifComplaints = "true".equals(rs.getString("notif_complaints"));
                notifAnnouncements = "true".equals(rs.getString("notif_announcements"));
                String fs = rs.getString("font_size");
                if (fs != null) currentFontSize = fs;
                String dm = rs.getString("dark_mode");
                isDarkModeState = "true".equalsIgnoreCase(dm);
                ThemeManager.isDarkMode = isDarkModeState;
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
                "notif_documents, notif_complaints, notif_announcements) " +
                "VALUES (?, 'false', 'Medium', 'true', 'true', 'true')");
            stmt.setString(1, email);
            stmt.executeUpdate();
            stmt.close();
            conn.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setActiveTab(Button active) {
        for (Button btn : new Button[]{tabGeneralBtn, tabNotifBtn, tabAppearanceBtn}) {
            btn.setStyle(inactiveTabStyle);
        }
        active.setStyle(activeTabStyle);
        // Re-setup hover effects after changing styles
        setupTabButtonHoverEffects();
    }

    @FXML private void showGeneralTab() {
        setActiveTab(tabGeneralBtn);
        Node content = buildGeneralTab();
        tabContent.getChildren().setAll(content);
        applyThemeToNewContent();
    }

    @FXML private void showNotifTab() {
        setActiveTab(tabNotifBtn);
        Node content = buildNotifTab();
        tabContent.getChildren().setAll(content);
        applyThemeToNewContent();
    }

    @FXML private void showAppearanceTab() {
        setActiveTab(tabAppearanceBtn);
        Node content = buildAppearanceTab();
        tabContent.getChildren().setAll(content);
        applyThemeToNewContent();
    }

    private void applyThemeToNewContent() {
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                if (stage != null && stage.getScene() != null) {
                    ThemeManager.applyTheme(stage);
                }
            } catch (Exception e) {
                System.out.println("[ResidentSettings] Error applying theme: " + e.getMessage());
            }
        });
    }

    private Node buildGeneralTab() {
        VBox container = new VBox(0);
        container.setMaxWidth(Double.MAX_VALUE);
        container.setStyle(
            "-fx-background-color: #ffffff; -fx-background-radius: 16;" +
            "-fx-border-color: #e8e8e8; -fx-border-width: 1;");

        VBox header = new VBox(4);
        header.setStyle(
            "-fx-background-color: #1a1a1a; -fx-background-radius: 16 16 0 0;" +
            "-fx-padding: 22 28;");
        Label title = new Label("General Settings");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        Label sub = new Label("Configure your preferences");
        sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");
        header.getChildren().addAll(title, sub);

        VBox body = new VBox(0);
        body.setStyle("-fx-padding: 0;");

        body.getChildren().addAll(
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

        documentsToggle = buildToggle(notifDocuments);
        complaintsToggle = buildToggle(notifComplaints);
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
                    "UPDATE settings SET notif_documents=?, notif_complaints=?," +
                    " notif_announcements=? WHERE user_email=?");
                stmt.setString(1, isToggleOn(documentsToggle) ? "true" : "false");
                stmt.setString(2, isToggleOn(complaintsToggle) ? "true" : "false");
                stmt.setString(3, isToggleOn(announcementsToggle) ? "true" : "false");
                stmt.setString(4, UserSession.getCurrentUserEmail());
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
            buildToggleRow("Document Alerts",
                "Get notified when your document requests are updated",
                documentsToggle, false),
            buildToggleRow("Complaint Alerts",
                "Get notified about your complaint status updates",
                complaintsToggle, false),
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

        Button fontSmallBtn = new Button("Small");
        Button fontMediumBtn = new Button("Medium");
        Button fontLargeBtn = new Button("Large");

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
            fontSmallBtn.setStyle(currentFontSize.equals("Small") ? active : inactive);
            fontMediumBtn.setStyle(currentFontSize.equals("Medium") ? active : inactive);
            fontLargeBtn.setStyle(currentFontSize.equals("Large") ? active : inactive);
        };
        updateFontBtns.run();

        fontSmallBtn.setOnAction(e -> { currentFontSize = "Small"; updateFontBtns.run(); });
        fontMediumBtn.setOnAction(e -> { currentFontSize = "Medium"; updateFontBtns.run(); });
        fontLargeBtn.setOnAction(e -> { currentFontSize = "Large"; updateFontBtns.run(); });

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
                stmt.setString(2, UserSession.getCurrentUserEmail());
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

        darkModeToggle = buildToggle(isDarkModeState);
        
        Label darkModeErrorLbl = new Label("");
        darkModeErrorLbl.setStyle("-fx-font-size: 11px;");

        HBox darkModeControlRow = new HBox(12);
        darkModeControlRow.setStyle("-fx-alignment: CENTER_LEFT;");
        darkModeControlRow.getChildren().addAll(darkModeToggle, darkModeErrorLbl);

        darkModeToggle.setOnMouseClicked(e -> {
            boolean currentState = (boolean) darkModeToggle.getUserData();
            boolean newDarkMode = !currentState;
            
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
            
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE settings SET dark_mode = ? WHERE user_email = ?");
                stmt.setString(1, newDarkMode ? "true" : "false");
                stmt.setString(2, UserSession.getCurrentUserEmail());
                stmt.executeUpdate();
                stmt.close();
                conn.close();
                setStatus(darkModeErrorLbl, "✅  Theme saved!", true);
            } catch (Exception ex) {
                setStatus(darkModeErrorLbl, "⚠  Error saving theme.", false);
            }
            
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            ThemeManager.isDarkMode = newDarkMode;
            ThemeManager.applyTheme(stage);
        });

        VBox body = new VBox(0);
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

    private HBox buildSettingRow(String title, String description, Node control, boolean isLast) {
        HBox row = new HBox(16);
        row.setStyle(
            "-fx-padding: 20 28;" +
            (isLast ? "" : "-fx-border-color: #f4f4f4; -fx-border-width: 0 0 1 0;"));
        row.setAlignment(Pos.CENTER_LEFT);

        VBox textBox = new VBox(4);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
        Label descLbl = new Label(description);
        descLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");
        descLbl.setWrapText(true);
        textBox.getChildren().addAll(titleLbl, descLbl);

        row.getChildren().addAll(textBox, control);
        return row;
    }

    private HBox buildToggleRow(String title, String description, StackPane toggle, boolean isLast) {
        HBox row = new HBox(16);
        row.setStyle(
            "-fx-padding: 20 28;" +
            (isLast ? "" : "-fx-border-color: #f4f4f4; -fx-border-width: 0 0 1 0;"));
        row.setAlignment(Pos.CENTER_LEFT);

        VBox textBox = new VBox(4);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
        Label descLbl = new Label(description);
        descLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");
        textBox.getChildren().addAll(titleLbl, descLbl);

        row.getChildren().addAll(textBox, toggle);
        return row;
    }

    private Label buildReadOnlyValue(String value) {
        Label lbl = new Label(value);
        lbl.setStyle(
            "-fx-font-size: 13px; -fx-text-fill: #555555;" +
            "-fx-background-color: #f4f4f4; -fx-background-radius: 8;" +
            "-fx-padding: 8 14;");
        return lbl;
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

        toggle.setOnMouseClicked(e -> {
            boolean current = (boolean) toggle.getUserData();
            boolean newState = !current;
            toggle.setUserData(newState);
            
            TranslateTransition tt = new TranslateTransition(Duration.millis(200), thumb);
            if (newState) {
                track.setFill(Color.web("#2e7d32"));
                tt.setToX(12);
            } else {
                track.setFill(Color.web("#cccccc"));
                tt.setToX(-12);
            }
            tt.play();
        });

        return toggle;
    }

    private boolean isToggleOn(StackPane toggle) {
        return toggle != null && (boolean) toggle.getUserData();
    }

    private void setStatus(Label lbl, String msg, boolean isSuccess) {
        lbl.setText(msg);
        lbl.setStyle(isSuccess
            ? "-fx-text-fill: #2e7d32; -fx-font-size: 11px;"
            : "-fx-text-fill: #c62828; -fx-font-size: 11px;");
    }

    @FXML private void handleLogout() {
        UserSession.clearSession();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "login.fxml", false, getClass());
    }

    @FXML private void goToDashboard() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "ResidentDashboard.fxml", true, getClass());
    }

    @FXML private void goToMyDocuments() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "MyDocuments.fxml", true, getClass());
    }

    @FXML private void goToRequestDocument() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "RequestDocument.fxml", true, getClass());
    }

    @FXML private void goToAnnouncements() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "ResidentAnnouncements.fxml", true, getClass());
    }

    @FXML private void goToComplaints() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Complaints_Resident.fxml", true, getClass());
    }

    @FXML private void goToPayments() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "ResidentPayments.fxml", true, getClass());
    }

    @FXML private void goToMyProfile() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "MyProfile.fxml", true, getClass());
    }

    @FXML private void handleMouseEntered(javafx.scene.input.MouseEvent event) {
        Button btn = (Button) event.getSource();
        if (!btn.getStyle().contains("#2d2d2d")) {
            btn.setStyle(btn.getStyle() + "-fx-background-color: #f0f0f0;");
        }
    }

    @FXML private void handleMouseExited(javafx.scene.input.MouseEvent event) {
        Button btn = (Button) event.getSource();
        if (!btn.getStyle().contains("#2d2d2d")) {
            btn.setStyle(btn.getStyle().replace("-fx-background-color: #f0f0f0;", "-fx-background-color: transparent;"));
        }
    }
}
