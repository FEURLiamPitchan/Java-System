package com.mycompany.javasystem;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AnnouncementsController {

    @FXML private VBox announcementsPane;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterPriority;
    @FXML private ComboBox<String> filterCategory;
    @FXML private DatePicker filterDateFrom;
    @FXML private DatePicker filterDateTo;
    @FXML private Label totalLabel;
    @FXML private Label emergencyLabel;
    @FXML private Label urgentLabel;
    @FXML private Label normalLabel;
    @FXML private Button logoutButton;

    @FXML
    public void initialize() {
        filterPriority.getItems().addAll("All", "Emergency", "Urgent", "Normal", "Low");
        filterPriority.setValue("All");
        filterCategory.getItems().addAll("All", "Health", "Safety & Security",
                "Environment", "Events", "Government Services", "Other");
        filterCategory.setValue("All");
        loadAnnouncements();
        loadSummary();
    }

    private void loadSummary() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.prepareStatement(
                "SELECT priority, COUNT(*) as cnt FROM announcements GROUP BY priority"
            ).executeQuery();
            int total = 0, emergency = 0, urgent = 0, normal = 0;
            while (rs.next()) {
                int cnt = rs.getInt("cnt");
                total += cnt;
                switch (rs.getString("priority")) {
                    case "Emergency": emergency = cnt; break;
                    case "Urgent": urgent = cnt; break;
                    case "Normal": case "Low": normal += cnt; break;
                }
            }
            totalLabel.setText(String.valueOf(total));
            emergencyLabel.setText(String.valueOf(emergency));
            urgentLabel.setText(String.valueOf(urgent));
            normalLabel.setText(String.valueOf(normal));
            rs.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAnnouncements() {
        announcementsPane.getChildren().clear();
        String search = searchField.getText().trim();
        String priority = filterPriority.getValue();
        String category = filterCategory.getValue();
        LocalDate dateFrom = filterDateFrom.getValue();
        LocalDate dateTo = filterDateTo.getValue();

        List<String[]> todayList = new ArrayList<>();
        List<String[]> yesterdayList = new ArrayList<>();
        List<String[]> thisWeekList = new ArrayList<>();
        List<String[]> olderList = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate weekStart = today.minusDays(7);

        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.prepareStatement(
                "SELECT * FROM announcements ORDER BY id DESC").executeQuery();

            while (rs.next()) {
                String annId = rs.getString("announcement_id");
                String title = rs.getString("title");
                String content = rs.getString("content");
                String prio = rs.getString("priority");
                String cat = rs.getString("category");
                String postedBy = rs.getString("posted_by");
                String datePosted = rs.getString("date_posted");
                String previewLen = rs.getString("preview_length");

                if (!search.isEmpty() &&
                    !title.toLowerCase().contains(search.toLowerCase()) &&
                    !content.toLowerCase().contains(search.toLowerCase())) continue;
                if (!priority.equals("All") && !prio.equals(priority)) continue;
                if (!category.equals("All") && !cat.equals(category)) continue;

                LocalDate posted = null;
                if (datePosted != null && !datePosted.isEmpty()) {
                    try {
                        posted = LocalDate.parse(datePosted.substring(0, 10));
                    } catch (Exception ignored) {}
                }

                if (dateFrom != null && posted != null && posted.isBefore(dateFrom)) continue;
                if (dateTo != null && posted != null && posted.isAfter(dateTo)) continue;

                String[] data = {annId, title, content, prio, cat, postedBy, datePosted, previewLen};

                if (posted == null || posted.isBefore(weekStart)) {
                    olderList.add(data);
                } else if (posted.isBefore(yesterday)) {
                    thisWeekList.add(data);
                } else if (posted.equals(yesterday)) {
                    yesterdayList.add(data);
                } else {
                    todayList.add(data);
                }
            }
            rs.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean hasAny = false;

        if (!todayList.isEmpty()) {
            hasAny = true;
            announcementsPane.getChildren().add(
                buildSectionHeader("Today", today.toString()));
            for (String[] c : todayList)
                announcementsPane.getChildren().add(
                    buildCard(c[0], c[1], c[2], c[3], c[4], c[5], c[6], c[7]));
        }
        if (!yesterdayList.isEmpty()) {
            hasAny = true;
            announcementsPane.getChildren().add(
                buildSectionHeader("Yesterday", yesterday.toString()));
            for (String[] c : yesterdayList)
                announcementsPane.getChildren().add(
                    buildCard(c[0], c[1], c[2], c[3], c[4], c[5], c[6], c[7]));
        }
        if (!thisWeekList.isEmpty()) {
            hasAny = true;
            announcementsPane.getChildren().add(
                buildSectionHeader("This Week", ""));
            for (String[] c : thisWeekList)
                announcementsPane.getChildren().add(
                    buildCard(c[0], c[1], c[2], c[3], c[4], c[5], c[6], c[7]));
        }
        if (!olderList.isEmpty()) {
            hasAny = true;
            announcementsPane.getChildren().add(
                buildSectionHeader("Older", ""));
            for (String[] c : olderList)
                announcementsPane.getChildren().add(
                    buildCard(c[0], c[1], c[2], c[3], c[4], c[5], c[6], c[7]));
        }

        if (!hasAny) {
            Label empty = new Label("No announcements found.");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #aaaaaa; -fx-padding: 20;");
            announcementsPane.getChildren().add(empty);
        }
    }

    private VBox buildSectionHeader(String title, String subtitle) {
        VBox header = new VBox(4);
        header.setMaxWidth(Double.MAX_VALUE);
        header.setStyle("-fx-padding: 12 0 6 0;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
        header.getChildren().add(titleLabel);

        if (!subtitle.isEmpty()) {
            Label subLabel = new Label(subtitle);
            subLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaaaaa;");
            header.getChildren().add(subLabel);
        }

        Separator sep = new Separator();
        VBox.setMargin(sep, new javafx.geometry.Insets(4, 0, 0, 0));
        header.getChildren().add(sep);

        return header;
    }

    private VBox buildCard(String annId, String title, String content,
            String priority, String category, String postedBy,
            String datePosted, String previewLen) {

        String prioBg, prioFg, borderColor;
        switch (priority) {
            case "Emergency":
                prioBg = "#fdecea"; prioFg = "#e53935"; borderColor = "#ffcdd2"; break;
            case "Urgent":
                prioBg = "#fff8e1"; prioFg = "#f59e0b"; borderColor = "#ffe082"; break;
            case "Normal":
                prioBg = "#e3f2fd"; prioFg = "#1e88e5"; borderColor = "#bbdefb"; break;
            default:
                prioBg = "#e8f5e9"; prioFg = "#4caf50"; borderColor = "#c8e6c9"; break;
        }

        // 0 or null = Auto 
        int maxLen;
        if (previewLen == null || previewLen.isEmpty() ||
                previewLen.equals("0") || previewLen.equals("0.0")) {
            int smart = (int) (content.length() * 0.3);
            maxLen = Math.min(300, Math.max(80, smart));
        } else {
            try { maxLen = (int) Double.parseDouble(previewLen.trim()); }
            catch (Exception e) { maxLen = 180; }
        }

        VBox card = new VBox(14);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinHeight(180);
        card.setStyle("-fx-background-color: #ffffff;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: " + borderColor + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 12;" +
                "-fx-padding: 24;");

        HBox topRow = new HBox(8);
        topRow.setStyle("-fx-alignment: CENTER_LEFT;");

        Label prioBadge = new Label(priority);
        prioBadge.setStyle("-fx-background-color: " + prioBg + ";" +
                "-fx-text-fill: " + prioFg + ";" +
                "-fx-font-size: 11px; -fx-font-weight: bold;" +
                "-fx-background-radius: 4; -fx-padding: 4 10;");

        Label catBadge = new Label(category);
        catBadge.setStyle("-fx-background-color: #f4f4f4;" +
                "-fx-text-fill: #777777;" +
                "-fx-font-size: 11px;" +
                "-fx-background-radius: 4; -fx-padding: 4 10;");

        topRow.getChildren().addAll(prioBadge, catBadge);

        Label titleLabel = new Label(title);
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");

        String preview = content != null ? smartTruncate(content, maxLen) : "";
        Label contentLabel = new Label(preview);
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        HBox footer = new HBox();
        footer.setStyle("-fx-alignment: CENTER_LEFT;");

        VBox metaBox = new VBox(3);
        HBox.setHgrow(metaBox, javafx.scene.layout.Priority.ALWAYS);

        Label postedByLabel = new Label("Posted by: " + (postedBy != null ? postedBy : "N/A"));
        postedByLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");

        Label dateLabel = new Label(datePosted != null ? datePosted : "N/A");
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");

        metaBox.getChildren().addAll(postedByLabel, dateLabel);

        HBox actions = new HBox(8);
        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #f4f4f4; -fx-text-fill: #333333;" +
                "-fx-font-size: 12px; -fx-background-radius: 6;" +
                "-fx-border-color: #e0e0e0; -fx-border-width: 1;" +
                "-fx-padding: 6 14; -fx-cursor: hand;");
        editBtn.setOnAction(e -> openEditModal(annId, title, content,
                priority, category, postedBy, previewLen));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #fff0f0; -fx-text-fill: #e53935;" +
                "-fx-font-size: 12px; -fx-background-radius: 6;" +
                "-fx-border-color: #ffcdd2; -fx-border-width: 1;" +
                "-fx-padding: 6 14; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> deleteAnnouncement(annId));

        actions.getChildren().addAll(editBtn, deleteBtn);
        footer.getChildren().addAll(metaBox, actions);
        card.getChildren().addAll(topRow, titleLabel, contentLabel, footer);

        return card;
    }

    private String smartTruncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        int cutAt = text.lastIndexOf(' ', maxLength);
        if (cutAt == -1) cutAt = maxLength;
        return text.substring(0, cutAt) + "...";
    }

    @FXML
    private void handleNewAnnouncement() {
        openModal(null, null, null, null, null, null, null);
    }

    private void openEditModal(String annId, String title, String content,
            String priority, String category, String postedBy, String previewLen) {
        openModal(annId, title, content, priority, category, postedBy, previewLen);
    }

    private void openModal(String annId, String title, String content,
            String priority, String category, String postedBy, String previewLen) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("AnnouncementsModal.fxml"));
            Parent root = loader.load();
            AnnouncementsModalController ctrl = loader.getController();
            ctrl.setData(annId, title, content, priority, category, postedBy, previewLen);
            ctrl.setOnSave(() -> {
                loadAnnouncements();
                loadSummary();
            });

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(logoutButton.getScene().getWindow());
            stage.setTitle(annId == null ? "New Announcement" : "Edit Announcement");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteAnnouncement(String annId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Announcement");
        confirm.setHeaderText("Delete this announcement?");
        confirm.setContentText("This action cannot be undone.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM announcements WHERE announcement_id = ?");
                    stmt.setString(1, annId);
                    stmt.executeUpdate();
                    stmt.close();
                    conn.close();
                    loadAnnouncements();
                    loadSummary();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML private void handleSearch() { loadAnnouncements(); }
    @FXML private void handleFilter() { loadAnnouncements(); }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        filterPriority.setValue("All");
        filterCategory.setValue("All");
        filterDateFrom.setValue(null);
        filterDateTo.setValue(null);
        loadAnnouncements();
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
    @FXML private void goToFinances() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Finances.fxml", true, getClass());
    }
    @FXML private void handleLogout() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "login.fxml", false, getClass());
    }
}