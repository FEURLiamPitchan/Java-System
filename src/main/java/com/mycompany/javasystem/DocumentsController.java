package com.mycompany.javasystem;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DocumentsController {

    @FXML private VBox documentsTableBody;
    @FXML private VBox sidebarVBox;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatus;
    @FXML private ComboBox<String> filterType;
    @FXML private Button logoutButton;

    @FXML
    public void initialize() {
        filterStatus.getItems().addAll("All", "Pending", "Processing", "Released", "Rejected");
        filterStatus.setValue("All");

        filterType.getItems().addAll("All", "Clearance", "Residency", "Indigency");
        filterType.setValue("All");

        loadDocuments("", "All", "All");

        // Force remove hover effect completely from sidebar buttons
        for (Node node : sidebarVBox.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                btn.skinProperty().addListener((obs, oldSkin, newSkin) -> {
                    if (newSkin != null) {
                        btn.setScaleX(1.0);
                        btn.setScaleY(1.0);
                    }
                });
                btn.hoverProperty().addListener((obs, wasHovered, isHovered) -> {
                    btn.setScaleX(1.0);
                    btn.setScaleY(1.0);
                });
                btn.pressedProperty().addListener((obs, wasPressed, isPressed) -> {
                    btn.setScaleX(1.0);
                    btn.setScaleY(1.0);
                });
                btn.armedProperty().addListener((obs, wasArmed, isArmed) -> {
                    btn.setScaleX(1.0);
                    btn.setScaleY(1.0);
                });
                btn.setOnMouseEntered(e -> { btn.setScaleX(1.0); btn.setScaleY(1.0); });
                btn.setOnMouseExited(e -> { btn.setScaleX(1.0); btn.setScaleY(1.0); });
            }
        }

        // Remove focus from sidebar buttons on load
        Platform.runLater(() -> documentsTableBody.requestFocus());
    }

    private void loadDocuments(String search, String status, String type) {
        documentsTableBody.getChildren().clear();

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM document_requests ORDER BY id");
            ResultSet rs = stmt.executeQuery();

            boolean hasData = false;

            while (rs.next()) {
                String refNumber = rs.getString("ref_number");
                String residentName = rs.getString("resident_name");
                String docType = rs.getString("document_type");
                String dateFiled = rs.getString("date_filed");
                String docStatus = rs.getString("status");

                // Apply filters
                if (!search.isEmpty()) {
                    if (!residentName.toLowerCase().contains(search.toLowerCase()) &&
                        !refNumber.toLowerCase().contains(search.toLowerCase())) {
                        continue;
                    }
                }
                if (!status.equals("All") && !docStatus.equals(status)) continue;
                if (!type.equals("All") && !docType.equals(type)) continue;

                hasData = true;

                HBox row = new HBox();
                row.setStyle("-fx-padding: 14 16; -fx-border-color: #f8f8f8; -fx-border-width: 0 0 1 0;");

                Label refLabel = new Label(refNumber);
                refLabel.setPrefWidth(120);
                refLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

                Label nameLabel = new Label(residentName);
                nameLabel.setPrefWidth(220);
                nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333333;");

                Label typeLabel = new Label(docType);
                typeLabel.setPrefWidth(180);
                typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

                Label dateLabel = new Label(dateFiled != null ? dateFiled : "N/A");
                dateLabel.setPrefWidth(150);
                dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

                String statusBg, statusFg;
                switch (docStatus) {
                    case "Released":
                        statusBg = "#e8f5e9"; statusFg = "#4caf50"; break;
                    case "Processing":
                        statusBg = "#e3f2fd"; statusFg = "#1e88e5"; break;
                    case "Rejected":
                        statusBg = "#ffebee"; statusFg = "#e53935"; break;
                    default:
                        statusBg = "#fff8e1"; statusFg = "#f59e0b"; break;
                }
                Label statusLabel = new Label(docStatus);
                statusLabel.setStyle("-fx-background-color: " + statusBg + ";" +
                        "-fx-text-fill: " + statusFg + ";" +
                        "-fx-font-size: 11px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 4; -fx-padding: 3 8;");

                HBox statusBox = new HBox(statusLabel);
                statusBox.setPrefWidth(130);
                statusBox.setAlignment(Pos.CENTER_LEFT);

                HBox actionBox = new HBox(8);
                actionBox.setPrefWidth(200);
                actionBox.setAlignment(Pos.CENTER_LEFT);

                final String fRefNumber = refNumber;
                final String fResidentName = residentName;
                final String fDocType = docType;
                final String fDateFiled = dateFiled;

                if (docStatus.equals("Pending")) {
                    Button approveBtn = createActionButton("Approve", "#2d2d2d", "#ffffff");
                    Button rejectBtn = createActionButton("Reject", "#fff0f0", "#e53935");
                    approveBtn.setOnAction(e -> updateStatus(fRefNumber, "Processing"));
                    rejectBtn.setOnAction(e -> updateStatus(fRefNumber, "Rejected"));
                    actionBox.getChildren().addAll(approveBtn, rejectBtn);

                } else if (docStatus.equals("Processing")) {
                    Button releaseBtn = createActionButton("Release", "#2d2d2d", "#ffffff");
                    Button rejectBtn = createActionButton("Reject", "#fff0f0", "#e53935");
                    releaseBtn.setOnAction(e -> updateStatus(fRefNumber, "Released"));
                    rejectBtn.setOnAction(e -> updateStatus(fRefNumber, "Rejected"));
                    actionBox.getChildren().addAll(releaseBtn, rejectBtn);

                } else if (docStatus.equals("Released")) {
                    Button printBtn = createActionButton("Print", "#f4f4f4", "#333333");
                    printBtn.setOnAction(e -> handlePrint(fRefNumber, fResidentName, fDocType, fDateFiled));
                    actionBox.getChildren().add(printBtn);

                } else if (docStatus.equals("Rejected")) {
                    Label rejectedLabel = new Label("No actions available");
                    rejectedLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaaaaa;");
                    actionBox.getChildren().add(rejectedLabel);
                }

                row.getChildren().addAll(refLabel, nameLabel, typeLabel, dateLabel, statusBox, actionBox);
                documentsTableBody.getChildren().add(row);
            }

            if (!hasData) {
                Label empty = new Label("No document requests found.");
                empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #aaaaaa; -fx-padding: 20 0;");
                VBox.setMargin(empty, new Insets(20, 0, 20, 0));
                documentsTableBody.getChildren().add(empty);
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            Label error = new Label("Error loading requests: " + e.getMessage());
            error.setStyle("-fx-font-size: 12px; -fx-text-fill: #e53935;");
            documentsTableBody.getChildren().add(error);
        }
    }

    private Button createActionButton(String text, String bgColor, String textColor) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + bgColor + ";" +
                "-fx-text-fill: " + textColor + ";" +
                "-fx-font-size: 11px;" +
                "-fx-background-radius: 6;" +
                "-fx-border-radius: 6;" +
                "-fx-padding: 5 12;" +
                "-fx-cursor: hand;");
        return btn;
    }

    private void updateStatus(String refNumber, String newStatus) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Action");
        confirm.setHeaderText("Change status to " + newStatus + "?");
        confirm.setContentText("This will update the request status for " + refNumber + ".");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    Connection conn = DatabaseConnection.getConnection();
                    String sql = "UPDATE document_requests SET status = ? WHERE ref_number = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, newStatus);
                    stmt.setString(2, refNumber);
                    stmt.executeUpdate();
                    stmt.close();
                    conn.close();
                    loadDocuments(
                        searchField.getText().trim(),
                        filterStatus.getValue(),
                        filterType.getValue()
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void handlePrint(String refNumber, String residentName, String docType, String dateFiled) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Print Document");
        info.setHeaderText("Print: " + docType);
        info.setContentText("Ref: " + refNumber + "\nResident: " + residentName + "\nDate Filed: " + dateFiled);
        info.showAndWait();
    }

    @FXML
    private void handleSearch() {
        loadDocuments(searchField.getText().trim(), filterStatus.getValue(), filterType.getValue());
    }

    @FXML
    private void handleFilter() {
        loadDocuments(searchField.getText().trim(), filterStatus.getValue(), filterType.getValue());
    }

    @FXML
    private void goToDashboard() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "AdminDashboard.fxml", true, getClass());
    }

    @FXML
    private void goToResidents() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Residents.fxml", true, getClass());
    }

    @FXML
    private void handleLogout() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "login.fxml", false, getClass());
    }
}