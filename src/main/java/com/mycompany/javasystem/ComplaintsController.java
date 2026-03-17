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
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

// Java
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ComplaintsController {

    @FXML private VBox complaintsTableBody;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatus;
    @FXML private ComboBox<String> filterType;
    @FXML private DatePicker filterDateFrom;
    @FXML private DatePicker filterDateTo;
    @FXML private Button logoutButton;
    @FXML private Button alertsButton;
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
        loadComplaints();
        loadSummary();
        updateAlertBadge();
    }

    private void updateAlertBadge() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.prepareStatement(
                "SELECT COUNT(*) FROM complaints WHERE is_read = False").executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                if (count > 0) {
                    alertBadge.setText(String.valueOf(count));
                    alertBadge.setVisible(true);
                } else {
                    alertBadge.setVisible(false);
                }
            }
            rs.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAlerts() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            conn.prepareStatement("UPDATE complaints SET is_read = True").executeUpdate();
            conn.close();
            alertBadge.setVisible(false);
            loadComplaints();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

                // Search filter
                if (!search.isEmpty() &&
                    !name.toLowerCase().contains(search.toLowerCase()) &&
                    !complaintId.toLowerCase().contains(search.toLowerCase())) continue;

                // Status filter
                if (!status.equals("All") && !complaintStatus.equals(status)) continue;

                // Type filter
                if (!type.equals("All") && !incidentType.equals(type)) continue;

                // Date range filter
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

            // Clickable name opens resident profile
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

            // Date + status changed timestamp
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
                // Get resident_id from complaints table first
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

                // Now look up resident by ID — no duplicate name problem
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
                // Update status_changed_at when status changes
                updateStatusTimestamp(complaintId);
                loadComplaints();
                loadSummary();
                updateAlertBadge();
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
                    updateAlertBadge();
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

            // Title
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

            // Table
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
    @FXML private void handleLogout() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "login.fxml", false, getClass());
    }
}