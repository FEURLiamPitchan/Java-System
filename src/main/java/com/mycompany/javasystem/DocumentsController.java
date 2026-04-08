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
import java.io.File;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DocumentsController {

    @FXML private VBox documentsTableBody;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatus;
    @FXML private ComboBox<String> filterType;
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
        filterStatus.getItems().addAll("All", "Pending", "Processing", "Released", "Rejected");
        filterStatus.setValue("All");
        filterType.getItems().addAll("All", "Barangay Clearance", "Certificate of Residency",
                "Certificate of Indigency");
        filterType.setValue("All");
        loadDocuments("", "All", "All");
        syncNotifications();
        refreshAlertBadge();
        Platform.runLater(() -> documentsTableBody.requestFocus());
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

    private void loadAvatarPicture() {
        ProfilePictureManager.loadAvatarPicture(
            SessionManager.getEmail(),
            avatarBox, avatarCircle, profileImageView, avatarInitialLabel
        );
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    // ── Load Documents ────────────────────────────────────────────────────────────
    private void loadDocuments(String search, String status, String type) {
        documentsTableBody.getChildren().clear();
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM document_requests ORDER BY id DESC");
            ResultSet rs = stmt.executeQuery();
            boolean hasData = false;

            while (rs.next()) {
                String requestId = rs.getString("request_id");
                String fullName  = rs.getString("full_name");
                String docType   = rs.getString("document_type");
                String dateReq   = rs.getString("date_requested");
                String docStatus = rs.getString("status");

                // Search filter
                if (!search.isEmpty()) {
                    String lSearch = search.toLowerCase();
                    if (!fullName.toLowerCase().contains(lSearch) &&
                        !requestId.toLowerCase().contains(lSearch)) continue;
                }
                // Status filter
                if (!status.equals("All") && !docStatus.equalsIgnoreCase(status)) continue;
                // Type filter
                if (!type.equals("All") && !docType.equalsIgnoreCase(type)) continue;

                hasData = true;

                HBox row = new HBox();
                row.setStyle("-fx-padding: 14 16; -fx-border-color: #f8f8f8;" +
                             "-fx-border-width: 0 0 1 0;");

                Label refLabel = new Label(requestId != null ? requestId : "—");
                refLabel.setPrefWidth(125);
                refLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

                Label nameLabel = new Label(fullName != null ? fullName : "—");
                nameLabel.setPrefWidth(220);
                nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333333;");

                Label typeLabel = new Label(docType != null ? docType : "—");
                typeLabel.setPrefWidth(180);
                typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

                Label dateLabel = new Label(dateReq != null ? dateReq : "N/A");
                dateLabel.setPrefWidth(150);
                dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

                // Status badge
                String statusBg, statusFg;
                if ("Released".equalsIgnoreCase(docStatus)) {
                    statusBg = "#e8f5e9"; statusFg = "#4caf50";
                } else if ("Processing".equalsIgnoreCase(docStatus)) {
                    statusBg = "#e3f2fd"; statusFg = "#1e88e5";
                } else if ("Rejected".equalsIgnoreCase(docStatus)) {
                    statusBg = "#ffebee"; statusFg = "#e53935";
                } else {
                    statusBg = "#fff8e1"; statusFg = "#f59e0b";
                }
                Label statusLabel = new Label(docStatus != null ? docStatus : "Pending");
                statusLabel.setStyle(
                    "-fx-background-color: " + statusBg + ";" +
                    "-fx-text-fill: " + statusFg + ";" +
                    "-fx-font-size: 11px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 4; -fx-padding: 3 8;");
                HBox statusBox = new HBox(statusLabel);
                statusBox.setPrefWidth(130);
                statusBox.setAlignment(Pos.CENTER_LEFT);

                // Action buttons
                HBox actionBox = new HBox(6);
                actionBox.setPrefWidth(200);
                actionBox.setAlignment(Pos.CENTER_LEFT);

                final String fRequestId = requestId;
                final String fName      = fullName;
                final String fType      = docType;
                final String fDate      = dateReq;

                // View button — always visible
                Button viewBtn = createActionBtn("View", "#f4f4f4", "#333333");
                viewBtn.setOnAction(e -> openViewModal(fRequestId));
                actionBox.getChildren().add(viewBtn);

                if ("Pending".equalsIgnoreCase(docStatus)) {
                    Button approveBtn = createActionBtn("Approve", "#2d2d2d", "#ffffff");
                    Button rejectBtn  = createActionBtn("Reject",  "#fff0f0", "#e53935");
                    approveBtn.setOnAction(e -> updateStatus(fRequestId, "Processing"));
                    rejectBtn.setOnAction(e  -> updateStatus(fRequestId, "Rejected"));
                    actionBox.getChildren().addAll(approveBtn, rejectBtn);
                } else if ("Processing".equalsIgnoreCase(docStatus)) {
                    Button releaseBtn = createActionBtn("Release", "#2d2d2d", "#ffffff");
                    Button rejectBtn  = createActionBtn("Reject",  "#fff0f0", "#e53935");
                    releaseBtn.setOnAction(e -> updateStatus(fRequestId, "Released"));
                    rejectBtn.setOnAction(e  -> updateStatus(fRequestId, "Rejected"));
                    actionBox.getChildren().addAll(releaseBtn, rejectBtn);
                } else if ("Released".equalsIgnoreCase(docStatus)) {
                    Button printBtn = createActionBtn("Print", "#e8f5e9", "#4caf50");
                    printBtn.setOnAction(e -> handlePrint(fRequestId));
                    actionBox.getChildren().add(printBtn);
                }

                row.getChildren().addAll(refLabel, nameLabel, typeLabel, dateLabel, statusBox, actionBox);
                documentsTableBody.getChildren().add(row);
            }

            if (!hasData) {
                Label empty = new Label("No document requests found.");
                empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #aaaaaa; -fx-padding: 20 0;");
                VBox.setMargin(empty, new Insets(20, 0, 20, 16));
                documentsTableBody.getChildren().add(empty);
            }
            rs.close(); stmt.close(); conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            Label error = new Label("Error loading requests: " + e.getMessage());
            error.setStyle("-fx-font-size: 12px; -fx-text-fill: #e53935;");
            documentsTableBody.getChildren().add(error);
        }
    }

    // ── View Modal ────────────────────────────────────────────────────────────────
    private void openViewModal(String requestId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM document_requests WHERE request_id = ?");
            stmt.setString(1, requestId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) { rs.close(); stmt.close(); conn.close(); return; }

            String fullName       = rs.getString("full_name");
            String docType        = rs.getString("document_type");
            String age            = rs.getString("age");
            String gender         = rs.getString("gender");
            String birthPlace     = rs.getString("birth_place");
            String birthDate      = rs.getString("birth_date");
            String civilStatus    = rs.getString("civil_status");
            String address        = rs.getString("address");
            String yearsResidency = rs.getString("years_residency");
            String contactNumber  = rs.getString("contact_number");
            String emailAddress   = rs.getString("email_address");
            String purpose        = rs.getString("purpose");
            String occupation     = rs.getString("occupation");
            String headOfFamily   = rs.getString("head_of_family");
            String familyMembers  = rs.getString("family_members");
            String monthlyIncome  = rs.getString("monthly_income");
            String incomeSource   = rs.getString("income_source");
            String validIdPath    = rs.getString("valid_id_path");
            String proofResPath   = rs.getString("proof_of_residency_path");
            String proofIncPath   = rs.getString("proof_of_income_path");
            String status         = rs.getString("status");
            String dateRequested  = rs.getString("date_requested");
            String dateCompleted  = rs.getString("date_completed");
            rs.close(); stmt.close(); conn.close();

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(logoutButton.getScene().getWindow());
            modal.setTitle("Document Request — " + requestId);
            modal.setResizable(false);

            VBox root = new VBox(0);
            root.setStyle("-fx-background-color: #ffffff; -fx-min-width: 640; -fx-max-width: 640;");

            // ── Header ──
            String hBg, hFg;
            if ("Released".equalsIgnoreCase(status))        { hBg = "#e8f5e9"; hFg = "#4caf50"; }
            else if ("Processing".equalsIgnoreCase(status)) { hBg = "#e3f2fd"; hFg = "#1e88e5"; }
            else if ("Rejected".equalsIgnoreCase(status))   { hBg = "#ffebee"; hFg = "#e53935"; }
            else                                             { hBg = "#fff8e1"; hFg = "#f59e0b"; }

            Label statusBadge = new Label(status != null ? status : "Pending");
            statusBadge.setStyle(
                "-fx-background-color: " + hBg + "; -fx-text-fill: " + hFg + ";" +
                "-fx-font-size: 11px; -fx-font-weight: bold;" +
                "-fx-background-radius: 4; -fx-padding: 4 10;");

            VBox headerText = new VBox(4);
            HBox.setHgrow(headerText, Priority.ALWAYS);
            Label titleLbl = new Label(docType != null ? docType : "Document Request");
            titleLbl.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
            Label subLbl = new Label("Request ID: " + requestId + "   •   Filed: " +
                (dateRequested != null ? dateRequested : "N/A"));
            subLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaaaaa;");
            headerText.getChildren().addAll(titleLbl, subLbl);

            HBox headerRow = new HBox(12);
            headerRow.setAlignment(Pos.CENTER_LEFT);
            headerRow.getChildren().addAll(headerText, statusBadge);

            VBox header = new VBox(4);
            header.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 22 28;");
            header.getChildren().add(headerRow);

            // ── Scrollable Body ──
            ScrollPane scroll = new ScrollPane();
            scroll.setFitToWidth(true);
            scroll.setPrefHeight(460);
            scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;" +
                            "-fx-border-color: transparent;");

            VBox body = new VBox(20);
            body.setStyle("-fx-padding: 24 28;");

            // Personal Information
            body.getChildren().add(sectionLabel("Personal Information"));
            GridPane personalGrid = new GridPane();
            personalGrid.setHgap(16); personalGrid.setVgap(12);
            personalGrid.getColumnConstraints().addAll(cloneCol(50), cloneCol(50));
            addField(personalGrid, "Full Name",    fullName,    0, 0, true);
            addField(personalGrid, "Age",          age,         1, 0, false);
            addField(personalGrid, "Gender",       gender,      1, 1, false);
            addField(personalGrid, "Birth Place",  birthPlace,  2, 0, false);
            addField(personalGrid, "Birth Date",   birthDate,   2, 1, false);
            addField(personalGrid, "Civil Status", civilStatus, 3, 0, false);
            body.getChildren().add(personalGrid);

            // Contact & Address
            body.getChildren().add(sectionLabel("Contact & Address"));
            GridPane contactGrid = new GridPane();
            contactGrid.setHgap(16); contactGrid.setVgap(12);
            contactGrid.getColumnConstraints().addAll(cloneCol(50), cloneCol(50));
            addField(contactGrid, "Address",            address,        0, 0, true);
            addField(contactGrid, "Years of Residency", yearsResidency, 1, 0, false);
            addField(contactGrid, "Contact Number",     contactNumber,  1, 1, false);
            addField(contactGrid, "Email Address",      emailAddress,   2, 0, true);
            body.getChildren().add(contactGrid);

            // Request Details
            body.getChildren().add(sectionLabel("Request Details"));
            GridPane reqGrid = new GridPane();
            reqGrid.setHgap(16); reqGrid.setVgap(12);
            reqGrid.getColumnConstraints().addAll(cloneCol(50), cloneCol(50));
            addField(reqGrid, "Purpose of Request", purpose, 0, 0, true);
            body.getChildren().add(reqGrid);

            // Indigency Details (only for Certificate of Indigency)
            boolean isIndigency = docType != null && docType.toLowerCase().contains("indigency");
            if (isIndigency) {
                body.getChildren().add(sectionLabel("Indigency Details"));
                GridPane indGrid = new GridPane();
                indGrid.setHgap(16); indGrid.setVgap(12);
                indGrid.getColumnConstraints().addAll(cloneCol(50), cloneCol(50));
                addField(indGrid, "Occupation",     occupation,    0, 0, false);
                addField(indGrid, "Head of Family", headOfFamily,  0, 1, false);
                addField(indGrid, "Family Members", familyMembers, 1, 0, false);
                addField(indGrid, "Monthly Income",
                    monthlyIncome != null && !monthlyIncome.isEmpty()
                        ? "₱ " + monthlyIncome : null,             1, 1, false);
                addField(indGrid, "Income Source",  incomeSource,  2, 0, true);
                body.getChildren().add(indGrid);
            }

            // Uploaded Documents
            boolean hasValidId  = validIdPath  != null && !validIdPath.isEmpty();
            boolean hasResProof = proofResPath != null && !proofResPath.isEmpty();
            boolean hasIncProof = proofIncPath != null && !proofIncPath.isEmpty();
            if (hasValidId || hasResProof || hasIncProof) {
                body.getChildren().add(sectionLabel("Uploaded Documents"));
                VBox docsBox = new VBox(8);
                if (hasValidId)  docsBox.getChildren().add(fileChip("Valid ID",           validIdPath));
                if (hasResProof) docsBox.getChildren().add(fileChip("Proof of Residency", proofResPath));
                if (hasIncProof) docsBox.getChildren().add(fileChip("Proof of Income",    proofIncPath));
                body.getChildren().add(docsBox);
            }

            // Completion
            if (dateCompleted != null && !dateCompleted.isEmpty()) {
                body.getChildren().add(sectionLabel("Completion"));
                GridPane compGrid = new GridPane();
                compGrid.setHgap(16); compGrid.setVgap(12);
                compGrid.getColumnConstraints().addAll(cloneCol(50), cloneCol(50));
                addField(compGrid, "Date Completed", dateCompleted, 0, 0, false);
                body.getChildren().add(compGrid);
            }

            scroll.setContent(body);

            // Footer
            HBox footer = new HBox(10);
            footer.setStyle(
                "-fx-padding: 16 28; -fx-alignment: CENTER_RIGHT;" +
                "-fx-border-color: #f0f0f0; -fx-border-width: 1 0 0 0;");
            Button closeBtn = new Button("Close");
            closeBtn.setStyle(
                "-fx-background-color: #f4f4f4; -fx-text-fill: #555555;" +
                "-fx-font-size: 12px; -fx-background-radius: 8;" +
                "-fx-border-color: #e0e0e0; -fx-border-width: 1;" +
                "-fx-padding: 10 24; -fx-cursor: hand;");
            closeBtn.setOnAction(e -> modal.close());
            footer.getChildren().add(closeBtn);

            root.getChildren().addAll(header, scroll, footer);
            modal.setScene(new Scene(root));
            Platform.runLater(() -> root.requestFocus());
            modal.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Modal Helpers ─────────────────────────────────────────────────────────────
    private Label sectionLabel(String text) {
        Label lbl = new Label(text);
        lbl.setMaxWidth(Double.MAX_VALUE);
        lbl.setStyle(
            "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;" +
            "-fx-border-color: #eeeeee; -fx-border-width: 0 0 1 0; -fx-padding: 0 0 8 0;");
        VBox.setMargin(lbl, new Insets(4, 0, 0, 0));
        return lbl;
    }

    private void addField(GridPane grid, String label, String value,
                           int row, int col, boolean fullWidth) {
        VBox cell = new VBox(4);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #aaaaaa;");
        Label val = new Label(value != null && !value.isEmpty() ? value : "—");
        val.setWrapText(true);
        val.setStyle(
            "-fx-font-size: 13px; -fx-text-fill: #1a1a1a;" +
            "-fx-background-color: #f8f8f8; -fx-background-radius: 6;" +
            "-fx-border-color: #eeeeee; -fx-border-width: 1; -fx-border-radius: 6;" +
            "-fx-padding: 8 12;");
        val.setMaxWidth(Double.MAX_VALUE);
        cell.getChildren().addAll(lbl, val);
        if (fullWidth) GridPane.setColumnSpan(cell, 2);
        grid.add(cell, col, row);
    }

    private HBox fileChip(String label, String path) {
        HBox chip = new HBox(10);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setStyle(
            "-fx-background-color: #f8f8f8; -fx-background-radius: 6;" +
            "-fx-border-color: #eeeeee; -fx-border-width: 1; -fx-border-radius: 6;" +
            "-fx-padding: 10 14;");
        Label icon = new Label("📎");
        icon.setStyle("-fx-font-size: 14px;");
        VBox info = new VBox(2);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #aaaaaa;");
        Label pathLbl = new Label(path);
        pathLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #555555;");
        pathLbl.setWrapText(true);
        info.getChildren().addAll(lbl, pathLbl);
        HBox.setHgrow(info, Priority.ALWAYS);
        chip.getChildren().addAll(icon, info);
        return chip;
    }

    private ColumnConstraints cloneCol(double percent) {
        ColumnConstraints c = new ColumnConstraints();
        c.setPercentWidth(percent);
        return c;
    }

    // ── Action Helpers ────────────────────────────────────────────────────────────
    private Button createActionBtn(String text, String bg, String fg) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";" +
            "-fx-font-size: 11px; -fx-background-radius: 6;" +
            "-fx-border-radius: 6; -fx-padding: 5 12; -fx-cursor: hand;");
        return btn;
    }

    private void updateStatus(String requestId, String newStatus) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Action");
        confirm.setHeaderText("Change status to " + newStatus + "?");
        confirm.setContentText("This will update the request status for " + requestId + ".");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE document_requests SET status = ? WHERE request_id = ?");
                    stmt.setString(1, newStatus);
                    stmt.setString(2, requestId);
                    stmt.executeUpdate();
                    if ("Released".equals(newStatus)) {
                        PreparedStatement stmtDate = conn.prepareStatement(
                            "UPDATE document_requests SET date_completed = ? WHERE request_id = ?");
                        stmtDate.setString(1, LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                        stmtDate.setString(2, requestId);
                        stmtDate.executeUpdate();
                        stmtDate.close();
                    }
                    stmt.close(); conn.close();
                    loadDocuments(
                        searchField.getText().trim(),
                        filterStatus.getValue(),
                        filterType.getValue());
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    private void handlePrint(String requestId) {
      try {
          // Fetch complete data from database
          Connection conn = DatabaseConnection.getConnection();
          PreparedStatement stmt = conn.prepareStatement(
              "SELECT * FROM document_requests WHERE request_id = ?");
          stmt.setString(1, requestId);
          ResultSet rs = stmt.executeQuery();

          if (!rs.next()) { 
              rs.close(); 
              stmt.close(); 
              conn.close(); 
              return; 
          }

          String fullName = rs.getString("full_name");
          int age = rs.getInt("age");
          String address = rs.getString("address");
          String yearsResidency = rs.getString("years_residency");
          String purpose = rs.getString("purpose");
          String monthlyIncome = rs.getString("monthly_income");
          String docType = rs.getString("document_type");
          String barangayCity = rs.getString("barangay_city");
          String barangayProvince = rs.getString("barangay_province");

          rs.close(); 
          stmt.close();

          String orNumber = null;

          // Only generate O.R. number for Barangay Clearance
          if ("Barangay Clearance".equalsIgnoreCase(docType)) {
              orNumber = generateORNumber(conn, docType);

              // Store O.R. number in database
              try {
                  PreparedStatement updateStmt = conn.prepareStatement(
                      "UPDATE document_requests SET or_number = ? WHERE request_id = ?");
                  updateStmt.setString(1, orNumber);
                  updateStmt.setString(2, requestId);
                  updateStmt.executeUpdate();
                  updateStmt.close();
                  System.out.println("✅ O.R. Number stored: " + orNumber);
              } catch (Exception e) {
                  System.out.println("⚠️ Could not store O.R. number: " + e.getMessage());
              }
          }

          conn.close();

          // Generate certificate
          String certificatePath = CertificateGenerator.generateCertificate(
              docType,
              requestId,
              fullName,
              age,
              address,
              yearsResidency,
              purpose,
              monthlyIncome,
              barangayCity,
              barangayProvince,
              orNumber  // Will be null for Residency and Indigency
          );

          if (certificatePath != null && new File(certificatePath).exists()) {
              openPDFInViewer(certificatePath);
              showSuccess("Certificate generated and opened successfully!");
          } else {
              showError("Failed to generate certificate.");
          }

      } catch (Exception e) {
          e.printStackTrace();
          showError("Error: " + e.getMessage());
      }
  }
 // ── Generate O.R. Number ──────────────────────────────────────────────────
private String generateORNumber(Connection conn, String docType) {
    try {
        String year = String.valueOf(LocalDateTime.now().getYear());
        
        // Get count of released documents for this type (simpler query for Access)
        PreparedStatement countStmt = conn.prepareStatement(
            "SELECT COUNT(*) FROM document_requests WHERE document_type = ? " +
            "AND or_number IS NOT NULL");
        countStmt.setString(1, docType);
        ResultSet rs = countStmt.executeQuery();
        
        int count = 0;
        if (rs.next()) {
            count = rs.getInt(1);
        }
        rs.close(); 
        countStmt.close();
        
        // Generate O.R. number: OR-2026-001, OR-2026-002, etc.
        String orNumber = String.format("OR-%s-%03d", year, count + 1);
        return orNumber;
        
    } catch (Exception e) {
        e.printStackTrace();
        return "OR-ERR";
    }
}

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setContentText(message);

        // Auto-close after 3 seconds
        Timeline timeline = new Timeline(new KeyFrame(
            Duration.seconds(3),
            event -> alert.close()
        ));
        timeline.setCycleCount(1);

        alert.show();
        timeline.play();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void openPDFInViewer(String filePath) {
    try {
        File pdfFile = new File(filePath);
        if (!pdfFile.exists()) {
            showError("PDF file not found: " + filePath);
            return;
        }
        
        String os = System.getProperty("os.name").toLowerCase();
        
        try {
            if (os.contains("win")) {
                // Windows
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "", filePath});
            } else if (os.contains("mac")) {
                // macOS
                Runtime.getRuntime().exec(new String[]{"open", filePath});
            } else {
                // Linux
                Runtime.getRuntime().exec(new String[]{"xdg-open", filePath});
            }
            System.out.println("✅ PDF opened: " + filePath);
        } catch (Exception e) {
            System.out.println("⚠️ Could not open PDF with default viewer: " + e.getMessage());
            showError("Could not open PDF. File saved at:\n" + filePath);
        }
    } catch (Exception e) {
        e.printStackTrace();
        showError("Error: " + e.getMessage());
    }
}

    // ── Notifications ─────────────────────────────────────────────────────────────
    private void cleanupNotifications() {
        String email = SessionManager.getEmail();
        if (email == null) return;
        try {
            Connection conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(true);
            PreparedStatement s1 = conn.prepareStatement(
                "DELETE FROM notifications WHERE type = 'announcement' " +
                "AND user_email = ? AND reference_id NOT IN " +
                "(SELECT announcement_id FROM announcements)");
            s1.setString(1, email); s1.executeUpdate(); s1.close();
            PreparedStatement s2 = conn.prepareStatement(
                "DELETE FROM notifications WHERE type = 'complaint' " +
                "AND user_email = ? AND reference_id NOT IN " +
                "(SELECT complaint_id FROM complaints WHERE status <> 'Resolved')");
            s2.setString(1, email); s2.executeUpdate(); s2.close();
            PreparedStatement s3 = conn.prepareStatement(
                "DELETE FROM notifications WHERE type = 'payment' " +
                "AND user_email = ? AND reference_id NOT IN " +
                "(SELECT ref_number FROM payments " +
                "WHERE status = 'Pending' AND archived = False)");
            s3.setString(1, email); s3.executeUpdate(); s3.close();
            conn.close();
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
                insertIfNew(conn, "payment",
                    "Pending payment from " + rs1.getString("resident_name") +
                    " (" + refNo + ")", refNo, email);
            }
            rs1.close();
            ResultSet rs2 = conn.prepareStatement(
                "SELECT complaint_id, complainant_name, incident_type " +
                "FROM complaints WHERE status <> 'Resolved'"
            ).executeQuery();
            while (rs2.next()) {
                String cid = rs2.getString("complaint_id");
                insertIfNew(conn, "complaint",
                    "Open complaint: " + rs2.getString("incident_type") +
                    " by " + rs2.getString("complainant_name"), cid, email);
            }
            rs2.close();
            ResultSet rs3 = conn.prepareStatement(
                "SELECT announcement_id, title FROM announcements ORDER BY id DESC"
            ).executeQuery();
            int aCount = 0;
            while (rs3.next() && aCount < 5) {
                String aid = rs3.getString("announcement_id");
                insertIfNew(conn, "announcement",
                    "Announcement posted: " + rs3.getString("title"), aid, email);
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
        check.setString(1, refId); check.setString(2, email); check.setString(3, type);
        ResultSet rs = check.executeQuery();
        boolean exists = rs.next();
        rs.close(); check.close();
        if (!exists) {
            PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO notifications " +
                "(type, message, reference_id, is_read, created_at, user_email) " +
                "VALUES (?, ?, ?, 'false', ?, ?)");
            ins.setString(1, type); ins.setString(2, message);
            ins.setString(3, refId);
            ins.setString(4, LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ins.setString(5, email);
            ins.executeUpdate(); ins.close();
        }
    }

    private void markOneAsRead(String notifId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(true);
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE notifications SET is_read = 'true' WHERE notif_id = " + notifId);
            stmt.executeUpdate(); stmt.close(); conn.close();
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

    // ── Alerts Popup ──────────────────────────────────────────────────────────────
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
                        rs.getString("notif_id"), rs.getString("type"),
                        rs.getString("message"), rs.getString("is_read"),
                        rs.getString("created_at")
                    });
                }
                rs.close(); conn.close();
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
        String message = item[2]; String isRead = item[3];
        String dateStr = item[4];
        if (dateStr != null && dateStr.length() > 16) dateStr = dateStr.substring(0, 16);

        String icon, bg;
        if ("complaint".equals(type))    { icon = "📢"; bg = "#ffebee"; }
        else if ("payment".equals(type)) { icon = "💳"; bg = "#fff8e1"; }
        else                             { icon = "📣"; bg = "#e3f2fd"; }

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

        String goToLabel =
            "complaint".equals(type) ? "→  Go to Complaints" :
            "payment".equals(type)   ? "→  Go to Payments"   :
                                       "→  Go to Announcements";
        String goToFxml =
            "complaint".equals(type) ? "Complaints.fxml" :
            "payment".equals(type)   ? "Payments.fxml"   :
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

    // ── Search / Filter ───────────────────────────────────────────────────────────
    @FXML
    private void handleSearch() {
        loadDocuments(searchField.getText().trim(),
            filterStatus.getValue(), filterType.getValue());
    }

    @FXML
    private void handleFilter() {
        loadDocuments(searchField.getText().trim(),
            filterStatus.getValue(), filterType.getValue());
    }

    // ── Navigation ────────────────────────────────────────────────────────────────
    @FXML private void goToDashboard() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "AdminDashboard.fxml", true, getClass());
    }
    @FXML private void goToResidents() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "Residents.fxml", true, getClass());
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
    @FXML private void handleLogout() {
        SessionManager.logout();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "login.fxml", false, getClass());
    }
}