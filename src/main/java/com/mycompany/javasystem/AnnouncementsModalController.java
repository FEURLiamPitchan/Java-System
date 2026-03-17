package com.mycompany.javasystem;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AnnouncementsModalController {

    @FXML private Label modalTitle;
    @FXML private TextField titleField;
    @FXML private TextArea contentField;
    @FXML private ComboBox<String> priorityField;
    @FXML private ComboBox<String> categoryField;
    @FXML private TextField postedByField;
    @FXML private TextField previewLengthField;
    @FXML private CheckBox autoCheckBox;
    @FXML private Label previewLabel;
    @FXML private Button submitButton;

    private String announcementId;
    private Runnable onSave;

    @FXML
    public void initialize() {
        priorityField.getItems().addAll("Emergency", "Urgent", "Normal", "Low");
        categoryField.getItems().addAll("Health", "Safety & Security",
                "Environment", "Events", "Government Services", "Other");

        autoCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            previewLengthField.setDisable(newVal);
            previewLengthField.setEditable(!newVal);
            previewLengthField.setOpacity(newVal ? 0.5 : 1.0);
            previewLengthField.setStyle(
                "-fx-background-color: " + (newVal ? "#eeeeee" : "#f9f9f9") + ";" +
                "-fx-border-color: #eeeeee;" +
                "-fx-border-width: 1; -fx-border-radius: 8;" +
                "-fx-background-radius: 8; -fx-padding: 10 14; -fx-font-size: 13px;");
            updatePreview();
        });

        contentField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        previewLengthField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
    }

    private void updatePreview() {
        String content = contentField.getText();
        if (content == null || content.isEmpty()) {
            previewLabel.setText("Start typing content to see preview...");
            return;
        }
        int maxLen = computePreviewLength(content);
        String preview = smartTruncate(content, maxLen);
        previewLabel.setText(preview);
    }

    private int computePreviewLength(String content) {
        if (autoCheckBox.isSelected()) {
            int smart = (int) (content.length() * 0.3);
            return Math.min(300, Math.max(80, smart));
        }
        try {
            return (int) Double.parseDouble(previewLengthField.getText().trim());
        } catch (Exception e) {
            return 180;
        }
    }

    private String smartTruncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        int cutAt = text.lastIndexOf(' ', maxLength);
        if (cutAt == -1) cutAt = maxLength;
        return text.substring(0, cutAt) + "...";
    }

    public void setData(String annId, String title, String content,
            String priority, String category, String postedBy, String previewLength) {
        this.announcementId = annId;

        if (annId != null) {
            modalTitle.setText("Edit Announcement");
            submitButton.setText("Save Changes");
            titleField.setText(title);
            contentField.setText(content);
            priorityField.setValue(priority);
            categoryField.setValue(category);
            postedByField.setText(postedBy);
            if (previewLength == null || previewLength.isEmpty() ||
                    previewLength.equals("0") || previewLength.equals("0.0")) {
                autoCheckBox.setSelected(true);
                previewLengthField.setDisable(true);
                previewLengthField.setEditable(false);
                previewLengthField.setOpacity(0.5);
            } else {
                autoCheckBox.setSelected(false);
                previewLengthField.setDisable(false);
                previewLengthField.setEditable(true);
                previewLengthField.setOpacity(1.0);
                try {
                    int val = (int) Double.parseDouble(previewLength);
                    previewLengthField.setText(String.valueOf(val));
                } catch (Exception e) {
                    previewLengthField.setText(previewLength);
                }
            }
        } else {
            modalTitle.setText("New Announcement");
            submitButton.setText("Post Announcement");
            autoCheckBox.setSelected(true);
            previewLengthField.setDisable(true);
            previewLengthField.setEditable(false);
            previewLengthField.setOpacity(0.5);
        }
        // Prevent auto-selection of title field
        javafx.application.Platform.runLater(() -> {
            titleField.deselect();
            titleField.end();
        });
        updatePreview();
    }

    public void setOnSave(Runnable callback) {
        this.onSave = callback;
    }

    @FXML
    private void handleSubmit() {
        String title = titleField.getText().trim();
        String content = contentField.getText().trim();
        String priority = priorityField.getValue();
        String category = categoryField.getValue();
        String postedBy = postedByField.getText().trim();

        if (title.isEmpty() || content.isEmpty() ||
                priority == null || category == null || postedBy.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Incomplete");
            alert.setHeaderText("All fields are required");
            alert.setContentText("Please fill in all fields before submitting.");
            alert.showAndWait();
            return;
        }

        int previewLen = 0;
        if (!autoCheckBox.isSelected()) {
            try {
                previewLen = (int) Double.parseDouble(previewLengthField.getText().trim());
                if (previewLen <= 0) previewLen = 0;
            } catch (Exception e) {
                previewLen = 0;
            }
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            String dateNow = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            if (announcementId == null) {
                String newId = generateId(conn);
                PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO announcements (announcement_id, title, content, " +
                    "priority, category, posted_by, date_posted, preview_length) " +
                    "VALUES (?,?,?,?,?,?,?,?)");
                stmt.setString(1, newId);
                stmt.setString(2, title);
                stmt.setString(3, content);
                stmt.setString(4, priority);
                stmt.setString(5, category);
                stmt.setString(6, postedBy);
                stmt.setString(7, dateNow);
                stmt.setInt(8, previewLen);
                stmt.executeUpdate();
                stmt.close();
            } else {
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE announcements SET title=?, content=?, priority=?, " +
                    "category=?, posted_by=?, preview_length=? WHERE announcement_id=?");
                stmt.setString(1, title);
                stmt.setString(2, content);
                stmt.setString(3, priority);
                stmt.setString(4, category);
                stmt.setString(5, postedBy);
                stmt.setInt(6, previewLen);
                stmt.setString(7, announcementId);
                stmt.executeUpdate();
                stmt.close();
            }

            conn.close();
            if (onSave != null) onSave.run();
            handleCancel();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Could not save announcement: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private String generateId(Connection conn) throws Exception {
        ResultSet rs = conn.prepareStatement(
            "SELECT COUNT(*) FROM announcements").executeQuery();
        int count = rs.next() ? rs.getInt(1) : 0;
        rs.close();
        return String.format("ANN-%04d", count + 1);
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }
}