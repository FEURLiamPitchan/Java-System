package com.mycompany.javasystem;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ProfileController {

    // TOP BAR AVATAR
    @FXML private Button logoutButton;
    @FXML private Button alertsButton;
    @FXML private Label alertBadge;
    @FXML private HBox avatarBox;
    @FXML private Circle avatarCircle;
    @FXML private ImageView profileImageView;
    @FXML private Label avatarInitialLabel;
    @FXML private Label topBarNameLabel;
    @FXML private Label topBarRoleLabel;
    @FXML private ScrollPane mainScrollPane;

    // PROFILE PAGE LEFT PANEL AVATAR (renamed to avoid conflicts)
    @FXML private StackPane avatarContainer;
    @FXML private Circle profileAvatarCircleBg;
    @FXML private Label profileAvatarInitialLabel;
    @FXML private ImageView profileAvatarImageView;
    @FXML private Label profileNameDisplay;
    @FXML private Label profileEmailDisplay;
    @FXML private Label picErrorLabel;
    @FXML private Label statusBadge;
    @FXML private Label roleBadge;
    @FXML private Label memberSinceLabel;

    // RIGHT PANEL
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField roleField;
    @FXML private TextField statusField;
    @FXML private TextField memberSinceField;
    @FXML private Label profileErrorLabel;
    @FXML private Button editToggleBtn;
    @FXML private HBox saveButtonRow;

    private boolean isEditing = false;
    private String originalName;
    private String originalEmail;

    @FXML
    public void initialize() {
        loadTopBar();
        loadAvatarPicture();
        loadProfileData();
        generateNotifications();
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

    // ── Load Profile Data ─────────────────────────────────────────────────────────
    private void loadProfileData() {
        String email = SessionManager.getEmail();
        String name  = SessionManager.getName();
        String role  = SessionManager.getRole();

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT full_name, email, role, status, date_created, profile_picture " +
                "FROM users WHERE email = ?");
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                name  = rs.getString("full_name") != null ? rs.getString("full_name") : "—";
                email = rs.getString("email")     != null ? rs.getString("email")     : "—";
                role  = rs.getString("role")      != null ? rs.getString("role")      : "admin";
                String status = rs.getString("status") != null ? rs.getString("status") : "Active";
                String dc     = rs.getString("date_created");
                String pic    = rs.getString("profile_picture");

                String memberSince = "—";
                if (dc != null && dc.length() >= 10) {
                    try {
                        LocalDate ld = LocalDate.parse(dc.substring(0, 10));
                        memberSince = ld.format(
                            DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
                    } catch (Exception ignored) {
                        memberSince = dc.substring(0, 10);
                    }
                }

                fullNameField.setText(name);
                emailField.setText(email);
                roleField.setText(capitalize(role));
                statusField.setText(status);
                memberSinceField.setText(memberSince);
                memberSinceLabel.setText(memberSince);
                profileNameDisplay.setText(name);
                profileEmailDisplay.setText(email);
                profileAvatarInitialLabel.setText(
                    !name.isEmpty() ? String.valueOf(name.charAt(0)).toUpperCase() : "A");

                statusBadge.setText(status);
                statusBadge.setStyle("Active".equals(status)
                    ? "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32;" +
                      "-fx-font-size: 10px; -fx-font-weight: bold;" +
                      "-fx-background-radius: 20; -fx-padding: 3 10;"
                    : "-fx-background-color: #ffebee; -fx-text-fill: #c62828;" +
                      "-fx-font-size: 10px; -fx-font-weight: bold;" +
                      "-fx-background-radius: 20; -fx-padding: 3 10;");

                roleBadge.setText(capitalize(role));

                if (pic != null && !pic.isEmpty()) {
                    loadProfileImage(pic);
                } else {
                    showInitials(name);
                }
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) { e.printStackTrace(); }

        originalName  = fullNameField.getText();
        originalEmail = emailField.getText();
        setFieldsEditable(false);
    }

    private void loadProfileImage(String base64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            Image img = new Image(new ByteArrayInputStream(bytes));
            Circle clip = new Circle(55, 55, 55);
            profileAvatarImageView.setClip(clip);
            profileAvatarImageView.setImage(img);
            profileAvatarImageView.setVisible(true);
            profileAvatarInitialLabel.setVisible(false);
            profileAvatarCircleBg.setVisible(false);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showInitials(String name) {
        profileAvatarImageView.setVisible(false);
        profileAvatarInitialLabel.setVisible(true);
        profileAvatarCircleBg.setVisible(true);
        profileAvatarInitialLabel.setText(
            name != null && !name.isEmpty()
                ? String.valueOf(name.charAt(0)).toUpperCase() : "A");
    }

    @FXML
    private void handleEditToggle() {
        isEditing = !isEditing;
        setFieldsEditable(isEditing);
        editToggleBtn.setText(isEditing ? "Cancel Edit" : "Edit");
        editToggleBtn.setStyle(isEditing
            ? "-fx-background-color: #c62828; -fx-text-fill: #ffffff;" +
              "-fx-font-size: 11px; -fx-background-radius: 6;" +
              "-fx-padding: 6 16; -fx-cursor: hand;"
            : "-fx-background-color: #3d3d3d; -fx-text-fill: #ffffff;" +
              "-fx-font-size: 11px; -fx-background-radius: 6;" +
              "-fx-padding: 6 16; -fx-cursor: hand;");
        saveButtonRow.setVisible(isEditing);
        saveButtonRow.setManaged(isEditing);
        profileErrorLabel.setText("");
        if (!isEditing) {
            fullNameField.setText(originalName);
            emailField.setText(originalEmail);
        }
    }

    @FXML
    private void handleCancelEdit() {
        isEditing = false;
        setFieldsEditable(false);
        editToggleBtn.setText("Edit");
        editToggleBtn.setStyle(
            "-fx-background-color: #3d3d3d; -fx-text-fill: #ffffff;" +
            "-fx-font-size: 11px; -fx-background-radius: 6;" +
            "-fx-padding: 6 16; -fx-cursor: hand;");
        saveButtonRow.setVisible(false);
        saveButtonRow.setManaged(false);
        fullNameField.setText(originalName);
        emailField.setText(originalEmail);
        profileErrorLabel.setText("");
    }

    private void setFieldsEditable(boolean editable) {
        String editableStyle =
            "-fx-font-size: 13px; -fx-padding: 10 14; -fx-background-radius: 8;" +
            "-fx-border-color: #e8e8e8; -fx-border-width: 1; -fx-border-radius: 8;" +
            "-fx-background-color: #f8f9fa; -fx-text-fill: #1a1a1a;";
        String readOnlyStyle =
            "-fx-font-size: 13px; -fx-padding: 10 14; -fx-background-radius: 8;" +
            "-fx-border-color: #eeeeee; -fx-border-width: 1; -fx-border-radius: 8;" +
            "-fx-background-color: #f4f4f4; -fx-text-fill: #777777;";
        fullNameField.setEditable(editable);
        emailField.setEditable(editable);
        fullNameField.setStyle(editable ? editableStyle : readOnlyStyle);
        emailField.setStyle(editable ? editableStyle : readOnlyStyle);
    }

    @FXML
    private void handleSaveProfile() {
        String newName  = fullNameField.getText().trim();
        String newEmail = emailField.getText().trim();
        String oldEmail = SessionManager.getEmail();

        if (newName.isEmpty() || newEmail.isEmpty()) {
            setError("⚠  Please fill in all fields.", false);
            return;
        }
        if (!newEmail.contains("@")) {
            setError("⚠  Enter a valid email address.", false);
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
                PreparedStatement s2 = conn.prepareStatement(
                    "UPDATE settings SET user_email = ? WHERE user_email = ?");
                s2.setString(1, newEmail); s2.setString(2, oldEmail);
                s2.executeUpdate(); s2.close();

                PreparedStatement s3 = conn.prepareStatement(
                    "UPDATE notifications SET user_email = ? WHERE user_email = ?");
                s3.setString(1, newEmail); s3.setString(2, oldEmail);
                s3.executeUpdate(); s3.close();
            }
            conn.close();

            SessionManager.login(newEmail, SessionManager.getRole(), newName);
            originalName  = newName;
            originalEmail = newEmail;
            profileNameDisplay.setText(newName);
            profileEmailDisplay.setText(newEmail);
            profileAvatarInitialLabel.setText(
                String.valueOf(newName.charAt(0)).toUpperCase());
            loadTopBar();
            loadAvatarPicture();
            handleCancelEdit();
            setError("✅  Profile updated successfully!", true);
        } catch (Exception e) {
            e.printStackTrace();
            setError("⚠  Database error. Please try again.", false);
        }
    }

    @FXML
    private void handleChangePicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter(
                "Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File file = fileChooser.showOpenDialog(logoutButton.getScene().getWindow());
        if (file == null) return;

        if (file.length() > 2 * 1024 * 1024) {
            picErrorLabel.setText("⚠  File too large. Max 2MB.");
            return;
        }

        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] bytes = fis.readAllBytes();
            fis.close();
            String base64 = Base64.getEncoder().encodeToString(bytes);

            ImageCropDialog cropDialog = new ImageCropDialog();
            String croppedBase64 = cropDialog.showCropDialog(
                (Stage) logoutButton.getScene().getWindow(), base64);

            if (croppedBase64 == null || croppedBase64.isEmpty()) {
                picErrorLabel.setText("⚠  Crop cancelled.");
                return;
            }

            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE users SET profile_picture = ? WHERE email = ?");
            stmt.setString(1, croppedBase64);
            stmt.setString(2, SessionManager.getEmail());
            stmt.executeUpdate();
            stmt.close();
            conn.close();

            loadProfileImage(croppedBase64);
            loadAvatarPicture();
            picErrorLabel.setText("");
            picErrorLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-size: 10px;");
            picErrorLabel.setText("✅  Picture updated!");

        } catch (Exception e) {
            e.printStackTrace();
            picErrorLabel.setText("⚠  Failed to upload. Try again.");
        }
    }

    @FXML
    private void handleRemovePicture() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Remove Picture");
        confirm.setHeaderText("Remove your profile picture?");
        confirm.setContentText("Your initials will be shown instead.");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE users SET profile_picture = NULL WHERE email = ?");
                    stmt.setString(1, SessionManager.getEmail());
                    stmt.executeUpdate();
                    stmt.close();
                    conn.close();
                    showInitials(fullNameField.getText());
                    loadAvatarPicture();
                    picErrorLabel.setText("");
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
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
            stmt1.executeUpdate();
            stmt1.close();

            PreparedStatement stmt2 = conn.prepareStatement(
                "DELETE FROM notifications WHERE type = 'complaint' " +
                "AND user_email = ? AND reference_id NOT IN " +
                "(SELECT complaint_id FROM complaints WHERE status <> 'Resolved')");
            stmt2.setString(1, email);
            stmt2.executeUpdate();
            stmt2.close();

            PreparedStatement stmt3 = conn.prepareStatement(
                "DELETE FROM notifications WHERE type = 'payment' " +
                "AND user_email = ? AND reference_id NOT IN " +
                "(SELECT ref_number FROM payments " +
                "WHERE status = 'Pending' AND archived = False)");
            stmt3.setString(1, email);
            stmt3.executeUpdate();
            stmt3.close();

            conn.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void generateNotifications() {
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
                String msg = "Pending payment from " + rs1.getString("resident_name") +
                    " (" + refNo + ")";
                insertNotifIfNew(conn, "payment", msg, refNo, email);
            }
            rs1.close();

            ResultSet rs2 = conn.prepareStatement(
                "SELECT complaint_id, complainant_name, incident_type FROM complaints " +
                "WHERE status <> 'Resolved'"
            ).executeQuery();
            while (rs2.next()) {
                String cid = rs2.getString("complaint_id");
                String msg = "Open complaint: " + rs2.getString("incident_type") +
                    " by " + rs2.getString("complainant_name");
                insertNotifIfNew(conn, "complaint", msg, cid, email);
            }
            rs2.close();

            ResultSet rs3 = conn.prepareStatement(
                "SELECT announcement_id, title FROM announcements ORDER BY id DESC"
            ).executeQuery();
            int aCount = 0;
            while (rs3.next() && aCount < 5) {
                String aid = rs3.getString("announcement_id");
                String msg = "Announcement posted: " + rs3.getString("title");
                insertNotifIfNew(conn, "announcement", msg, aid, email);
                aCount++;
            }
            rs3.close();
            conn.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void insertNotifIfNew(Connection conn, String type,
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
        }
    }

    private void markOneAsRead(String notifId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(true);
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE notifications SET is_read = 'true' " +
                "WHERE notif_id = " + notifId);
            stmt.executeUpdate();
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

    @FXML
    private void handleAvatarClick() {
        // Already on profile page
    }

    private void setError(String msg, boolean isSuccess) {
        profileErrorLabel.setText(msg);
        profileErrorLabel.setStyle(isSuccess
            ? "-fx-text-fill: #2e7d32; -fx-font-size: 11px;"
            : "-fx-text-fill: #c62828; -fx-font-size: 11px;");
    }

    @FXML private void handleLogout() {
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
    @FXML private void goToSettings() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Settings.fxml", true, getClass());
    }
}