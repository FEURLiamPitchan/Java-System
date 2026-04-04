package com.mycompany.javasystem;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MyDocuments_resident {
    @FXML
    private Button backButton;
    @FXML
    private VBox documentRowsContainer;

    private List<DocumentRequest> documents;

    @FXML
    public void initialize() {
        loadDocuments();
        displayDocuments();
    }

    private void loadDocuments() {
        documents = new ArrayList<>();
        documents.add(new DocumentRequest(
            "#BR-2024-001", "Barangay Clearance", "Employment",
            LocalDate.of(2024, 6, 13), "In Progress", 0.6,
            "Processing (60%)", "#e3f2fd;#2196f3", "#2196f3"
        ));
        documents.add(new DocumentRequest(
            "#BR-2024-002", "Certificate of Residency", "School Requirements",
            LocalDate.of(2024, 6, 12), "Ready", 1.0,
            "Ready for Pickup (100%)", "#e8f5e9;#4caf50", "#4caf50"
        ));
        documents.add(new DocumentRequest(
            "#BR-2024-003", "Indigency Certificate", "Medical Assistance",
            LocalDate.of(2024, 6, 10), "Released", 1.0,
            "Completed (100%)", "#f3e5f5;#9c27b0", "#9c27b0"
        ));
        documents.add(new DocumentRequest(
            "#BR-2024-004", "Business Permit", "New Business",
            LocalDate.of(2024, 6, 8), "Pending", 0.2,
            "Submitted (20%)", "#fff8e1;#f59e0b", "#f59e0b"
        ));
        documents.add(new DocumentRequest(
            "#BR-2024-005", "Barangay ID", "Identification",
            LocalDate.of(2024, 6, 5), "Active", 0.4,
            "Under Review (40%)", "#e1f5fe;#0288d1", "#0288d1"
        ));
    }

    private void displayDocuments() {
        if (documentRowsContainer == null) return;
        
        documentRowsContainer.getChildren().clear();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");

        for (DocumentRequest doc : documents) {
            HBox row = createDocumentRow(doc, formatter);
            documentRowsContainer.getChildren().add(row);
        }
    }

    private HBox createDocumentRow(DocumentRequest doc, DateTimeFormatter formatter) {
        HBox row = new HBox();
        row.setStyle("-fx-padding: 16 8; -fx-background-color: #ffffff; " +
                    "-fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0;");

        Label idLabel = new Label(doc.getRequestId());
        idLabel.setPrefWidth(100);
        idLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2196f3; -fx-font-weight: bold;");

        Label typeLabel = new Label(doc.getDocumentType());
        typeLabel.setPrefWidth(180);
        typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #1a1a1a; -fx-font-weight: bold;");

        Label purposeLabel = new Label(doc.getPurpose());
        purposeLabel.setPrefWidth(200);
        purposeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

        Label dateLabel = new Label(doc.getRequestDate().format(formatter));
        dateLabel.setPrefWidth(120);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

        VBox statusBox = new VBox();
        statusBox.setPrefWidth(120);
        Label statusLabel = new Label(doc.getStatus());
        String[] colors = doc.getStatusColor().split(";");
        statusLabel.setStyle("-fx-background-color: " + colors[0] + "; " +
                           "-fx-text-fill: " + colors[1] + "; " +
                           "-fx-font-size: 11px; -fx-font-weight: bold; " +
                           "-fx-background-radius: 4; -fx-padding: 4 10;");
        statusBox.getChildren().add(statusLabel);

        VBox progressBox = new VBox(4);
        progressBox.setPrefWidth(150);
        Label progressLabel = new Label(doc.getProgressText());
        progressLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #555555;");
        ProgressBar progressBar = new ProgressBar(doc.getProgress());
        progressBar.setPrefWidth(140);
        progressBar.setPrefHeight(6);
        progressBar.setStyle("-fx-accent: " + doc.getProgressColor() + ";");
        progressBox.getChildren().addAll(progressLabel, progressBar);

        row.getChildren().addAll(idLabel, typeLabel, purposeLabel, dateLabel, statusBox, progressBox);
        return row;
    }

    @FXML
    private void goBackToDashboard() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "ResidentDashboard_resident.fxml", true, getClass());
    }

    @FXML
    private void goToRequestDocument() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "RequestDocument_resident.fxml", true, getClass());
    }
}
