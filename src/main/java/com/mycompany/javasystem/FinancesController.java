package com.mycompany.javasystem;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FinancesController {

    @FXML private TableView<String[]> transactionTable;
    @FXML private TableColumn<String[], String> colDate;
    @FXML private TableColumn<String[], String> colResident;
    @FXML private TableColumn<String[], String> colCategory;
    @FXML private TableColumn<String[], String> colType;
    @FXML private TableColumn<String[], String> colAmount;
    @FXML private TableColumn<String[], String> colPaymentMethod;
    @FXML private TableColumn<String[], String> colStatus;
    @FXML private TableColumn<String[], String> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterType;
    @FXML private ComboBox<String> filterStatus;
    @FXML private DatePicker filterDateFrom;
    @FXML private DatePicker filterDateTo;

    @FXML private Label totalCollectionsLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private Label netBalanceLabel;
    @FXML private Label pendingLabel;
    @FXML private Label recordCountLabel;
    @FXML private Button logoutButton;

    @FXML private BarChart<String, Number> barChart;
    @FXML private PieChart pieChart;
    @FXML private ScrollPane mainScrollPane;

    @FXML
    public void initialize() {
        filterType.getItems().addAll("All", "Income", "Expense");
        filterType.setValue("All");
        filterStatus.getItems().addAll("All", "Paid", "Pending");
        filterStatus.setValue("All");

        setupTableColumns();
        transactionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        loadSummary();
        loadTransactions();
        loadBarChart();
        loadPieChart();

        barChart.setCache(true);
        barChart.setCacheHint(CacheHint.SPEED);
        pieChart.setCache(true);
        pieChart.setCacheHint(CacheHint.SPEED);

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

    // ── Table Columns ─────────────────────────────────────────────────────────────
    private void setupTableColumns() {
        colDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[0]));
        colResident.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[1]));
        colCategory.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[2]));
        colAmount.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[4]));
        colPaymentMethod.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[5]));

        colType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[3]));
        colType.setCellFactory(col -> new TableCell<String[], String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setGraphic(null); setStyle(""); return;
                }
                Label badge = new Label(item);
                badge.setStyle(item.equals("Income")
                    ? "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32;" +
                      "-fx-font-size: 11px; -fx-font-weight: bold;" +
                      "-fx-background-radius: 20; -fx-padding: 4 14;"
                    : "-fx-background-color: #ffebee; -fx-text-fill: #c62828;" +
                      "-fx-font-size: 11px; -fx-font-weight: bold;" +
                      "-fx-background-radius: 20; -fx-padding: 4 14;");
                setGraphic(badge);
                setText(null);
            }
        });

        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[6]));
        colStatus.setCellFactory(col -> new TableCell<String[], String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setGraphic(null); setStyle(""); return;
                }
                Label badge = new Label(item);
                badge.setStyle(item.equals("Paid")
                    ? "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32;" +
                      "-fx-font-size: 11px; -fx-font-weight: bold;" +
                      "-fx-background-radius: 20; -fx-padding: 4 14;"
                    : "-fx-background-color: #fff8e1; -fx-text-fill: #f57f17;" +
                      "-fx-font-size: 11px; -fx-font-weight: bold;" +
                      "-fx-background-radius: 20; -fx-padding: 4 14;");
                setGraphic(badge);
                setText(null);
            }
        });

        transactionTable.setRowFactory(tv -> new TableRow<String[]>() {
            @Override
            protected void updateItem(String[] item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setStyle(getIndex() % 2 == 0
                        ? "-fx-background-color: #ffffff; -fx-pref-height: 52px;"
                        : "-fx-background-color: #fafbfc; -fx-pref-height: 52px;");
                }
            }
        });

        colActions.setCellFactory(col -> new TableCell<String[], String>() {
            final Button viewBtn   = new Button("View");
            final Button editBtn   = new Button("Edit");
            final Button deleteBtn = new Button("Delete");
            final HBox   box       = new HBox(6, viewBtn, editBtn, deleteBtn);
            {
                box.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 0 0 0 4;");

                viewBtn.setStyle(
                    "-fx-background-color: #e3f2fd; -fx-text-fill: #1565c0;" +
                    "-fx-font-size: 11px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 6; -fx-border-color: #bbdefb;" +
                    "-fx-border-width: 1; -fx-padding: 5 10; -fx-cursor: hand;");
                editBtn.setStyle(
                    "-fx-background-color: #f4f4f4; -fx-text-fill: #444444;" +
                    "-fx-font-size: 11px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 6; -fx-border-color: #e0e0e0;" +
                    "-fx-border-width: 1; -fx-padding: 5 10; -fx-cursor: hand;");
                deleteBtn.setStyle(
                    "-fx-background-color: #ffebee; -fx-text-fill: #c62828;" +
                    "-fx-font-size: 11px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 6; -fx-border-color: #ffcdd2;" +
                    "-fx-border-width: 1; -fx-padding: 5 10; -fx-cursor: hand;");

                viewBtn.setOnAction(e -> {
                    String[] row = getTableView().getItems().get(getIndex());
                    openViewModal(row);
                });

                editBtn.setOnAction(e -> {
                    String[] row = getTableView().getItems().get(getIndex());
                    if (row[7].equals("finances")) {
                        openExpenseModal(row[8], row[1], row[2], row[4], row[6]);
                    } else {
                        openIncomeEditModal(row[8], row[1], row[2], row[4], row[5], row[6], row[0]);
                    }
                });

                deleteBtn.setOnAction(e -> {
                    String[] row = getTableView().getItems().get(getIndex());
                    if (row[7].equals("finances")) {
                        deleteExpense(row[8]);
                    } else {
                        showInfo("Income records can only be deleted from the Payments page.");
                    }
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    // ── Summary ───────────────────────────────────────────────────────────────────
    private void loadSummary() {
        double totalIncome = 0, totalExpenses = 0;
        int pending = 0;
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs1 = conn.prepareStatement(
                "SELECT SUM(amount) AS total FROM payments WHERE status = 'Paid'"
            ).executeQuery();
            if (rs1.next()) totalIncome = rs1.getDouble("total");
            rs1.close();

            ResultSet rs2 = conn.prepareStatement(
                "SELECT COUNT(*) AS cnt FROM payments WHERE status = 'Pending'"
            ).executeQuery();
            if (rs2.next()) pending = rs2.getInt("cnt");
            rs2.close();

            ResultSet rs3 = conn.prepareStatement(
                "SELECT SUM(amount) AS total FROM finances WHERE type = 'Expense'"
            ).executeQuery();
            if (rs3.next()) totalExpenses = rs3.getDouble("total");
            rs3.close();
            conn.close();
        } catch (Exception e) { e.printStackTrace(); }

        double net = totalIncome - totalExpenses;
        totalCollectionsLabel.setText(String.format("₱%,.2f", totalIncome));
        totalExpensesLabel.setText(String.format("₱%,.2f", totalExpenses));
        netBalanceLabel.setText(String.format("₱%,.2f", net));
        netBalanceLabel.setStyle(
            "-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: " +
            (net >= 0 ? "#2e7d32" : "#c62828") + ";");
        pendingLabel.setText(String.valueOf(pending));
    }

    // ── Bar Chart ─────────────────────────────────────────────────────────────────
    private void loadBarChart() {
        barChart.getData().clear();
        barChart.setLegendVisible(true);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yy");

        Map<String, double[]> incomeMap  = new LinkedHashMap<>();
        Map<String, double[]> expenseMap = new LinkedHashMap<>();

        for (int i = 5; i >= 0; i--) {
            String label = LocalDate.now().minusMonths(i).format(fmt);
            incomeMap.put(label,  new double[]{0, 0});
            expenseMap.put(label, new double[]{0, 0});
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs1 = conn.prepareStatement(
                "SELECT date_created, amount FROM payments WHERE status = 'Paid'"
            ).executeQuery();
            while (rs1.next()) {
                try {
                    String raw = rs1.getString("date_created");
                    if (raw == null) continue;
                    String label = LocalDate.parse(raw.substring(0, 10)).format(fmt);
                    if (incomeMap.containsKey(label)) {
                        incomeMap.get(label)[0] += rs1.getDouble("amount");
                        incomeMap.get(label)[1]++;
                    }
                } catch (Exception ignored) {}
            }
            rs1.close();

            ResultSet rs2 = conn.prepareStatement(
                "SELECT date_recorded, amount FROM finances WHERE type = 'Expense'"
            ).executeQuery();
            while (rs2.next()) {
                try {
                    String raw = rs2.getString("date_recorded");
                    if (raw == null) continue;
                    String label = LocalDate.parse(raw.substring(0, 10)).format(fmt);
                    if (expenseMap.containsKey(label)) {
                        expenseMap.get(label)[0] += rs2.getDouble("amount");
                        expenseMap.get(label)[1]++;
                    }
                } catch (Exception ignored) {}
            }
            rs2.close();
            conn.close();
        } catch (Exception e) { e.printStackTrace(); }

        XYChart.Series<String, Number> incomeSeries  = new XYChart.Series<>();
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        expenseSeries.setName("Expenses");

        for (String month : incomeMap.keySet()) {
            double[] inc = incomeMap.get(month);
            double[] exp = expenseMap.get(month);

            XYChart.Data<String, Number> incData = new XYChart.Data<>(month, inc[0]);
            XYChart.Data<String, Number> expData = new XYChart.Data<>(month, exp[0]);

            final double incAmt = inc[0]; final int incCnt = (int) inc[1];
            final double expAmt = exp[0]; final int expCnt = (int) exp[1];

            incData.nodeProperty().addListener((obs, old, node) -> {
                if (node != null) {
                    node.setStyle("-fx-bar-fill: #43a047;");
                    Tooltip tip = new Tooltip(
                        "📈 Income: ₱" + String.format("%,.2f", incAmt) +
                        "\n👥 Payments made: " + incCnt);
                    tip.setStyle("-fx-font-size: 12px; -fx-background-color: #1a1a1a;" +
                        "-fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 12;");
                    tip.setShowDelay(Duration.millis(80));
                    tip.setHideDelay(Duration.millis(200));
                    Tooltip.install(node, tip);
                    node.setOnMouseEntered(e -> node.setStyle("-fx-bar-fill: #2e7d32;"));
                    node.setOnMouseExited(e  -> node.setStyle("-fx-bar-fill: #43a047;"));
                }
            });

            expData.nodeProperty().addListener((obs, old, node) -> {
                if (node != null) {
                    node.setStyle("-fx-bar-fill: #e53935;");
                    Tooltip tip = new Tooltip(
                        "📉 Expenses: ₱" + String.format("%,.2f", expAmt) +
                        "\n📋 Records: " + expCnt);
                    tip.setStyle("-fx-font-size: 12px; -fx-background-color: #1a1a1a;" +
                        "-fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 12;");
                    tip.setShowDelay(Duration.millis(80));
                    tip.setHideDelay(Duration.millis(200));
                    Tooltip.install(node, tip);
                    node.setOnMouseEntered(e -> node.setStyle("-fx-bar-fill: #b71c1c;"));
                    node.setOnMouseExited(e  -> node.setStyle("-fx-bar-fill: #e53935;"));
                }
            });

            incomeSeries.getData().add(incData);
            expenseSeries.getData().add(expData);
        }
        barChart.getData().addAll(incomeSeries, expenseSeries);
    }

    // ── Pie Chart ─────────────────────────────────────────────────────────────────
    private void loadPieChart() {
        pieChart.getData().clear();
        Map<String, double[]> typeMap = new LinkedHashMap<>();
        typeMap.put("Clearance", new double[]{0, 0});
        typeMap.put("Indigency",  new double[]{0, 0});
        typeMap.put("Residency",  new double[]{0, 0});

        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.prepareStatement(
                "SELECT payment_type, SUM(amount) AS total, COUNT(*) AS cnt " +
                "FROM payments WHERE status = 'Paid' GROUP BY payment_type"
            ).executeQuery();
            while (rs.next()) {
                String type = rs.getString("payment_type");
                if (typeMap.containsKey(type)) {
                    typeMap.get(type)[0] = rs.getDouble("total");
                    typeMap.get(type)[1] = rs.getInt("cnt");
                }
            }
            rs.close();
            conn.close();
        } catch (Exception e) { e.printStackTrace(); }

        String[] colors = {"#43a047", "#1e88e5", "#f59e0b"};
        int i = 0;
        for (Map.Entry<String, double[]> entry : typeMap.entrySet()) {
            double amt = entry.getValue()[0];
            int    cnt = (int) entry.getValue()[1];
            if (amt > 0) {
                PieChart.Data slice = new PieChart.Data(entry.getKey(), amt);
                pieChart.getData().add(slice);
                final String color = colors[i % colors.length];
                final String tipText = "📄 " + entry.getKey() +
                    "\n₱" + String.format("%,.2f", amt) +
                    "\n" + cnt + " payment" + (cnt != 1 ? "s" : "");
                slice.nodeProperty().addListener((obs, old, node) -> {
                    if (node != null) {
                        node.setStyle("-fx-pie-color: " + color + ";");
                        Tooltip tip = new Tooltip(tipText);
                        tip.setStyle("-fx-font-size: 12px; -fx-background-color: #1a1a1a;" +
                            "-fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 12;");
                        tip.setShowDelay(Duration.millis(80));
                        tip.setHideDelay(Duration.millis(200));
                        Tooltip.install(node, tip);
                        node.setOnMouseEntered(e -> node.setStyle(
                            "-fx-pie-color: " + color + "; -fx-opacity: 0.8;"));
                        node.setOnMouseExited(e -> node.setStyle(
                            "-fx-pie-color: " + color + "; -fx-opacity: 1.0;"));
                    }
                });
                i++;
            }
        }
        if (pieChart.getData().isEmpty())
            pieChart.getData().add(new PieChart.Data("No data yet", 1));
    }

    // ── Load Transactions ─────────────────────────────────────────────────────────
    private void loadTransactions() {
        String    search = searchField.getText().trim().toLowerCase();
        String    type   = filterType.getValue();
        String    status = filterStatus.getValue();
        LocalDate from   = filterDateFrom.getValue();
        LocalDate to     = filterDateTo.getValue();

        List<String[]> rows = new ArrayList<>();
        DateTimeFormatter displayFmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");

        try {
            Connection conn = DatabaseConnection.getConnection();

            if (type.equals("All") || type.equals("Income")) {
                StringBuilder sql = new StringBuilder(
                    "SELECT payment_id, ref_number, resident_name, payment_type, " +
                    "amount, status, date_created FROM payments WHERE 1=1");
                if (!status.equals("All"))
                    sql.append(" AND status = '").append(status).append("'");
                if (from != null)
                    sql.append(" AND date_created >= #").append(from).append("#");
                if (to != null)
                    sql.append(" AND date_created <= #").append(to).append("#");
                sql.append(" ORDER BY date_created DESC");

                ResultSet rs = conn.prepareStatement(sql.toString()).executeQuery();
                while (rs.next()) {
                    String resident = rs.getString("resident_name");
                    String ref      = rs.getString("ref_number");
                    String cat      = rs.getString("payment_type");
                    String amt      = String.format("₱%,.2f", rs.getDouble("amount"));
                    String stat     = rs.getString("status");
                    String rawDate  = rs.getString("date_created");
                    String id       = rs.getString("payment_id");
                    String method   = (ref != null && ref.toUpperCase().startsWith("CASH"))
                                        ? "Cash" : "GCash";
                    String date = rawDate;
                    try {
                        date = LocalDate.parse(rawDate.substring(0, 10)).format(displayFmt);
                    } catch (Exception ignored) {}

                    if (!search.isEmpty()
                        && !resident.toLowerCase().contains(search)
                        && !ref.toLowerCase().contains(search)) continue;

                    rows.add(new String[]{
                        date, resident, cat, "Income", amt, method, stat, "payments", id
                    });
                }
                rs.close();
            }

            if (type.equals("All") || type.equals("Expense")) {
                StringBuilder sql = new StringBuilder(
                    "SELECT finance_id, description, category, amount, " +
                    "status, date_recorded FROM finances WHERE type = 'Expense'");
                if (!status.equals("All"))
                    sql.append(" AND status = '").append(status).append("'");
                if (from != null)
                    sql.append(" AND date_recorded >= #").append(from).append("#");
                if (to != null)
                    sql.append(" AND date_recorded <= #").append(to).append("#");
                sql.append(" ORDER BY date_recorded DESC");

                ResultSet rs = conn.prepareStatement(sql.toString()).executeQuery();
                while (rs.next()) {
                    String desc    = rs.getString("description");
                    String cat     = rs.getString("category");
                    String amt     = String.format("₱%,.2f", rs.getDouble("amount"));
                    String stat    = rs.getString("status");
                    String rawDate = rs.getString("date_recorded");
                    String id      = String.valueOf(rs.getInt("finance_id"));
                    String date = rawDate;
                    try {
                        date = LocalDate.parse(rawDate.substring(0, 10)).format(displayFmt);
                    } catch (Exception ignored) {}

                    if (!search.isEmpty()
                        && !desc.toLowerCase().contains(search)
                        && !cat.toLowerCase().contains(search)) continue;

                    rows.add(new String[]{
                        date, desc, cat, "Expense", amt, "N/A", stat, "finances", id
                    });
                }
                rs.close();
            }
            conn.close();
        } catch (Exception e) { e.printStackTrace(); }

        transactionTable.setItems(FXCollections.observableArrayList(rows));
        if (recordCountLabel != null)
            recordCountLabel.setText(rows.size() + " record" + (rows.size() != 1 ? "s" : ""));
    }

    // ── MODAL HELPERS ─────────────────────────────────────────────────────────────

    private VBox buildModalRoot() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #ffffff; -fx-min-width: 460; -fx-max-width: 460;");
        return root;
    }

    private VBox buildModalHeader(String title, String subtitle) {
        VBox header = new VBox(6);
        header.setStyle(
            "-fx-background-color: #1a1a1a; -fx-padding: 24 28 22 28;");
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
            "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        Label subLabel = new Label(subtitle);
        subLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");
        header.getChildren().addAll(titleLabel, subLabel);
        return header;
    }

    private VBox buildModalBody() {
        VBox body = new VBox(14);
        body.setStyle("-fx-padding: 24 28 10 28; -fx-background-color: #ffffff;");
        return body;
    }

    private VBox buildFieldGroup(String labelText, javafx.scene.Node field) {
        VBox group = new VBox(6);
        Label lbl = new Label(labelText);
        lbl.setStyle(
            "-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #999999;");
        group.getChildren().addAll(lbl, field);
        return group;
    }

    private TextField buildReadOnlyField(String value) {
        TextField field = new TextField(value != null ? value : "");
        field.setEditable(false);
        field.setStyle(
            "-fx-font-size: 13px; -fx-padding: 10 14; -fx-background-radius: 8;" +
            "-fx-border-color: #eeeeee; -fx-border-width: 1; -fx-border-radius: 8;" +
            "-fx-background-color: #f8f8f8; -fx-text-fill: #555555;");
        return field;
    }

    private TextField buildEditableField(String value) {
        TextField field = new TextField(value != null ? value : "");
        field.setStyle(
            "-fx-font-size: 13px; -fx-padding: 10 14; -fx-background-radius: 8;" +
            "-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8;" +
            "-fx-background-color: #fafafa; -fx-text-fill: #1a1a1a;");
        return field;
    }

    private HBox buildModalFooter(Button... buttons) {
        HBox footer = new HBox(10);
        footer.setStyle(
            "-fx-padding: 16 28 24 28; -fx-alignment: CENTER_RIGHT;" +
            "-fx-border-color: #f0f0f0; -fx-border-width: 1 0 0 0;");
        footer.getChildren().addAll(buttons);
        return footer;
    }

    private Button buildCancelButton(Stage stage) {
        Button btn = new Button("Cancel");
        btn.setStyle(
            "-fx-background-color: #f4f4f4; -fx-text-fill: #555555;" +
            "-fx-font-size: 12px; -fx-font-weight: bold;" +
            "-fx-background-radius: 8; -fx-border-color: #e0e0e0;" +
            "-fx-border-width: 1; -fx-padding: 10 20; -fx-cursor: hand;");
        btn.setOnAction(e -> stage.close());
        return btn;
    }

    private Button buildPrimaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: #1a1a1a; -fx-text-fill: #ffffff;" +
            "-fx-font-size: 12px; -fx-font-weight: bold;" +
            "-fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;");
        return btn;
    }

    // ── VIEW MODAL ────────────────────────────────────────────────────────────────
    private void openViewModal(String[] row) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.initOwner(logoutButton.getScene().getWindow());
        modal.setTitle("Transaction Details");
        modal.setResizable(false);

        boolean isIncome = row[7].equals("payments");

        VBox root   = buildModalRoot();
        VBox header = buildModalHeader(
            isIncome ? "Income Details" : "Expense Details",
            isIncome ? "Payment record from the Payments page"
                     : "Expense record from the Finance ledger");
        VBox body   = buildModalBody();

        HBox row1 = new HBox(16);
        VBox dateGroup = buildFieldGroup("DATE", buildReadOnlyField(row[0]));
        HBox.setHgrow(dateGroup, Priority.ALWAYS);
        VBox typeGroup = buildFieldGroup("TYPE", buildReadOnlyField(row[3]));
        HBox.setHgrow(typeGroup, Priority.ALWAYS);
        row1.getChildren().addAll(dateGroup, typeGroup);

        VBox residentGroup = buildFieldGroup(
            isIncome ? "RESIDENT NAME" : "DESCRIPTION",
            buildReadOnlyField(row[1]));

        VBox categoryGroup = buildFieldGroup("CATEGORY", buildReadOnlyField(row[2]));

        HBox row2 = new HBox(16);
        VBox amountGroup = buildFieldGroup("AMOUNT", buildReadOnlyField(row[4]));
        HBox.setHgrow(amountGroup, Priority.ALWAYS);
        VBox methodGroup = buildFieldGroup("PAYMENT METHOD", buildReadOnlyField(row[5]));
        HBox.setHgrow(methodGroup, Priority.ALWAYS);
        row2.getChildren().addAll(amountGroup, methodGroup);

        // Status badge
        Label statusBadge = new Label(row[6]);
        statusBadge.setStyle(row[6].equals("Paid")
            ? "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32;" +
              "-fx-font-size: 12px; -fx-font-weight: bold;" +
              "-fx-background-radius: 20; -fx-padding: 6 16;"
            : "-fx-background-color: #fff8e1; -fx-text-fill: #f57f17;" +
              "-fx-font-size: 12px; -fx-font-weight: bold;" +
              "-fx-background-radius: 20; -fx-padding: 6 16;");
        VBox statusGroup = buildFieldGroup("STATUS", statusBadge);

        body.getChildren().addAll(row1, residentGroup, categoryGroup, row2, statusGroup);

        Button closeBtn = buildPrimaryButton("Close");
        closeBtn.setOnAction(e -> modal.close());
        HBox footer = buildModalFooter(closeBtn);

        root.getChildren().addAll(header, body, footer);
        modal.setScene(new Scene(root));
        Platform.runLater(() -> root.requestFocus());
        modal.showAndWait();
    }

    // ── ADD / EDIT EXPENSE MODAL ──────────────────────────────────────────────────
    @FXML
    private void handleAddExpense() {
        openExpenseModal(null, null, null, null, null);
    }

    private void openExpenseModal(String financeId, String description,
                                   String category, String amount, String status) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.initOwner(logoutButton.getScene().getWindow());
        modal.setTitle(financeId == null ? "Add Expense" : "Edit Expense");
        modal.setResizable(false);

        boolean isEdit = financeId != null;

        VBox root   = buildModalRoot();
        VBox header = buildModalHeader(
            isEdit ? "Edit Expense" : "Add Expense",
            isEdit ? "Update this expense record"
                   : "Record a new barangay expense");
        VBox body   = buildModalBody();

        Label descLbl = new Label("DESCRIPTION");
        descLbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #999999;");
        TextField descField = buildEditableField(description);
        descField.setPromptText("e.g. Electricity bill, Office supplies");
        VBox descGroup = new VBox(6, descLbl, descField);

        Label catLbl = new Label("CATEGORY");
        catLbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #999999;");
        ComboBox<String> catBox = new ComboBox<>();
        catBox.getItems().addAll("Utilities", "Events", "Supplies", "Salaries", "Maintenance", "Other");
        catBox.setValue(category != null ? category : "Utilities");
        catBox.setMaxWidth(Double.MAX_VALUE);
        catBox.setStyle(
            "-fx-font-size: 13px; -fx-background-radius: 8;" +
            "-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8;");
        VBox catGroup = new VBox(6, catLbl, catBox);

        HBox row1 = new HBox(16);
        Label amtLbl = new Label("AMOUNT (₱)");
        amtLbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #999999;");
        TextField amountField = buildEditableField(
            amount != null ? amount.replace("₱", "").replace(",", "") : "");
        amountField.setPromptText("e.g. 500.00");
        VBox amtGroup = new VBox(6, amtLbl, amountField);
        HBox.setHgrow(amtGroup, Priority.ALWAYS);

        Label statLbl = new Label("STATUS");
        statLbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #999999;");
        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Paid", "Pending");
        statusBox.setValue(status != null ? status : "Paid");
        statusBox.setMaxWidth(Double.MAX_VALUE);
        statusBox.setStyle(
            "-fx-font-size: 13px; -fx-background-radius: 8;" +
            "-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8;");
        VBox statGroup = new VBox(6, statLbl, statusBox);
        HBox.setHgrow(statGroup, Priority.ALWAYS);
        row1.getChildren().addAll(amtGroup, statGroup);

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #c62828; -fx-font-size: 11px;");

        body.getChildren().addAll(descGroup, catGroup, row1, errorLabel);

        Button cancelBtn = buildCancelButton(modal);
        Button saveBtn   = buildPrimaryButton(isEdit ? "Save Changes" : "Add Expense");

        String finalId = financeId;
        saveBtn.setOnAction(e -> {
            String desc = descField.getText().trim();
            String cat  = catBox.getValue();
            String amt  = amountField.getText().trim();
            String stat = statusBox.getValue();

            if (desc.isEmpty() || amt.isEmpty()) {
                errorLabel.setText("⚠  Please fill in all fields.");
                return;
            }
            double parsedAmt;
            try {
                parsedAmt = Double.parseDouble(amt);
                if (parsedAmt <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                errorLabel.setText("⚠  Enter a valid amount greater than 0.");
                return;
            }
            try {
                Connection conn = DatabaseConnection.getConnection();
                if (finalId == null) {
                    PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO finances " +
                        "(description, category, type, amount, status, date_recorded, recorded_by) " +
                        "VALUES (?, ?, 'Expense', ?, ?, Date(), 'Admin')");
                    stmt.setString(1, desc);
                    stmt.setString(2, cat);
                    stmt.setDouble(3, parsedAmt);
                    stmt.setString(4, stat);
                    stmt.executeUpdate();
                    stmt.close();
                } else {
                    PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE finances SET description=?, category=?, amount=?, status=? " +
                        "WHERE finance_id=?");
                    stmt.setString(1, desc);
                    stmt.setString(2, cat);
                    stmt.setDouble(3, parsedAmt);
                    stmt.setString(4, stat);
                    stmt.setInt(5, Integer.parseInt(finalId));
                    stmt.executeUpdate();
                    stmt.close();
                }
                conn.close();
                modal.close();
                loadSummary();
                loadTransactions();
                loadBarChart();
                loadPieChart();
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("⚠  Database error. Please try again.");
            }
        });

        HBox footer = buildModalFooter(cancelBtn, saveBtn);
        root.getChildren().addAll(header, body, footer);
        modal.setScene(new Scene(root));
        Platform.runLater(() -> root.requestFocus());
        modal.showAndWait();
    }

    // ── EDIT INCOME MODAL (status only) ──────────────────────────────────────────
    private void openIncomeEditModal(String paymentId, String resident, String category,
                                      String amount, String method, String status, String date) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.initOwner(logoutButton.getScene().getWindow());
        modal.setTitle("Edit Payment Status");
        modal.setResizable(false);

        VBox root   = buildModalRoot();
        VBox header = buildModalHeader(
            "Edit Payment",
            "Only the payment status can be changed here");
        VBox body   = buildModalBody();

        HBox row1 = new HBox(16);
        VBox dateGroup = buildFieldGroup("DATE", buildReadOnlyField(date));
        HBox.setHgrow(dateGroup, Priority.ALWAYS);
        VBox catGroup  = buildFieldGroup("CATEGORY", buildReadOnlyField(category));
        HBox.setHgrow(catGroup, Priority.ALWAYS);
        row1.getChildren().addAll(dateGroup, catGroup);

        VBox residentGroup = buildFieldGroup("RESIDENT NAME", buildReadOnlyField(resident));

        HBox row2 = new HBox(16);
        VBox amountGroup = buildFieldGroup("AMOUNT", buildReadOnlyField(amount));
        HBox.setHgrow(amountGroup, Priority.ALWAYS);
        VBox methodGroup = buildFieldGroup("PAYMENT METHOD", buildReadOnlyField(method));
        HBox.setHgrow(methodGroup, Priority.ALWAYS);
        row2.getChildren().addAll(amountGroup, methodGroup);

        // Only editable field
        Label statLbl = new Label("STATUS");
        statLbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #999999;");
        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Paid", "Pending");
        statusBox.setValue(status);
        statusBox.setMaxWidth(Double.MAX_VALUE);
        statusBox.setStyle(
            "-fx-font-size: 13px; -fx-background-radius: 8;" +
            "-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8;");
        VBox statGroup = new VBox(6, statLbl, statusBox);

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #c62828; -fx-font-size: 11px;");

        body.getChildren().addAll(row1, residentGroup, row2, statGroup, errorLabel);

        Button cancelBtn = buildCancelButton(modal);
        Button saveBtn   = buildPrimaryButton("Save Changes");

        saveBtn.setOnAction(e -> {
            String newStatus = statusBox.getValue();
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE payments SET status = ? WHERE payment_id = ?");
                stmt.setString(1, newStatus);
                stmt.setString(2, paymentId);
                stmt.executeUpdate();
                stmt.close();
                conn.close();
                modal.close();
                loadSummary();
                loadTransactions();
                loadBarChart();
                loadPieChart();
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("⚠  Database error. Please try again.");
            }
        });

        HBox footer = buildModalFooter(cancelBtn, saveBtn);
        root.getChildren().addAll(header, body, footer);
        modal.setScene(new Scene(root));
        Platform.runLater(() -> root.requestFocus());
        modal.showAndWait();
    }

    // ── Delete Expense ────────────────────────────────────────────────────────────
    private void deleteExpense(String financeId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Expense");
        confirm.setHeaderText("Delete this expense record?");
        confirm.setContentText("This action cannot be undone.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM finances WHERE finance_id = ?");
                    stmt.setInt(1, Integer.parseInt(financeId));
                    stmt.executeUpdate();
                    stmt.close();
                    conn.close();
                    loadSummary();
                    loadTransactions();
                    loadBarChart();
                    loadPieChart();
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    // ── Export CSV ────────────────────────────────────────────────────────────────
    @FXML
    private void handleExportCSV() {
        try {
            String fileName = "finances_export_" + LocalDate.now() + ".csv";
            FileWriter writer = new FileWriter(fileName);
            writer.write("Date,Resident/Description,Category,Type,Amount,Method,Status\n");
            for (String[] row : transactionTable.getItems()) {
                writer.write(String.join(",",
                    row[0], row[1], row[2], row[3], row[4], row[5], row[6]) + "\n");
            }
            writer.close();
            showInfo("✅  Exported successfully!\nFile saved as: " + fileName);
        } catch (Exception e) {
            e.printStackTrace();
            showInfo("Export failed. Please try again.");
        }
    }

    // ── Filters ───────────────────────────────────────────────────────────────────
    @FXML private void handleSearch() { loadTransactions(); }
    @FXML private void handleFilter() { loadTransactions(); }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        filterType.setValue("All");
        filterStatus.setValue("All");
        filterDateFrom.setValue(null);
        filterDateTo.setValue(null);
        loadTransactions();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────────
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
    @FXML private void handleLogout() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "login.fxml", false, getClass());
    }
}