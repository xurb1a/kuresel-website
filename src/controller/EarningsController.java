package controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import model.Client;
import model.DeliveryEarning;
import util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EarningsController implements DashboardController.UserAware {
    private static final Logger LOGGER = Logger.getLogger(EarningsController.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @FXML private Label totalEarningsLabel;
    @FXML private Label monthlyEarningsLabel;
    @FXML private Label weeklyEarningsLabel;
    @FXML private Label totalDeliveriesLabel;

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> statusFilter;

    @FXML private BarChart<String, Number> earningsChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    @FXML private TableView<DeliveryEarning> earningsTable;
    @FXML private TableColumn<DeliveryEarning, LocalDate> dateCol;
    @FXML private TableColumn<DeliveryEarning, Integer> orderIdCol;
    @FXML private TableColumn<DeliveryEarning, String> clientCol;
    @FXML private TableColumn<DeliveryEarning, String> cityCol;
    @FXML private TableColumn<DeliveryEarning, Double> amountCol;
    @FXML private TableColumn<DeliveryEarning, String> statusCol;

    private Client deliveryGuy;
    private final ObservableList<DeliveryEarning> allEarnings = FXCollections.observableArrayList();
    private final FilteredList<DeliveryEarning> filteredEarnings = new FilteredList<>(allEarnings);
    private final Map<Integer, String> clientNames = new HashMap<>();
    private final Map<Integer, String> orderCities = new HashMap<>();

    @Override
    public void setUser(Client user) {
        this.deliveryGuy = user;
        initialize();
    }

    @FXML
    public void initialize() {
        if (deliveryGuy != null) {
            setupControls();
            loadEarningsData();
            setupTable();
            updateStatistics();
            updateChart();
        }
    }

    private void setupControls() {
        // Set default date range to current month
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());

        startDatePicker.setValue(firstDayOfMonth);
        endDatePicker.setValue(now);

        // Setup status filter
        statusFilter.setItems(FXCollections.observableArrayList("All", "Paid", "Pending"));
        statusFilter.getSelectionModel().selectFirst();

        // Setup chart
        xAxis.setLabel("Date");
        yAxis.setLabel("Amount (DH)");
    }

    private void loadEarningsData() {
        allEarnings.clear();
        clientNames.clear();
        orderCities.clear();

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Load client names and cities for orders
            String clientSql = "SELECT co.id, cl.nom, co.delivery_city FROM commandes co " +
                    "JOIN clients cl ON co.client_id = cl.id " +
                    "JOIN livraison l ON co.id = l.commande_id " +
                    "WHERE l.delivery_guy_id = ?";

            PreparedStatement clientStmt = conn.prepareStatement(clientSql);
            clientStmt.setInt(1, deliveryGuy.getId());
            ResultSet clientRs = clientStmt.executeQuery();

            while (clientRs.next()) {
                int orderId = clientRs.getInt("id");
                clientNames.put(orderId, clientRs.getString("nom"));
                orderCities.put(orderId, clientRs.getString("delivery_city"));
            }

            // Load earnings data
            String sql = "SELECT e.id, e.commande_id, e.amount, e.earned_date, e.status " +
                    "FROM delivery_earnings e " +
                    "WHERE e.delivery_guy_id = ? " +
                    "ORDER BY e.earned_date DESC";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deliveryGuy.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                int commandeId = rs.getInt("commande_id");
                double amount = rs.getDouble("amount");
                LocalDate earnedDate = rs.getDate("earned_date").toLocalDate();
                String status = rs.getString("status");

                DeliveryEarning earning = new DeliveryEarning(
                        id, deliveryGuy.getId(), commandeId, amount, earnedDate, status
                );

                allEarnings.add(earning);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading earnings data", e);
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load earnings data.");
        }

        earningsTable.setItems(filteredEarnings);
    }

    private void setupTable() {
        dateCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getEarnedDate()));
        dateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DISPLAY_DATE_FORMATTER));
                }
            }
        });

        orderIdCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getCommandeId()));

        clientCol.setCellValueFactory(data -> {
            String clientName = clientNames.getOrDefault(data.getValue().getCommandeId(), "Unknown");
            return new SimpleStringProperty(clientName);
        });

        cityCol.setCellValueFactory(data -> {
            String city = orderCities.getOrDefault(data.getValue().getCommandeId(), "Unknown");
            return new SimpleStringProperty(city);
        });

        amountCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getAmount()).asObject());
        amountCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DH", amount));
                }
            }
        });

        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
    }

    private void updateStatistics() {
        double totalEarnings = 0;
        double monthlyEarnings = 0;
        double weeklyEarnings = 0;
        int totalDeliveries = allEarnings.size();

        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.minusDays(now.getDayOfWeek().getValue() - 1);
        LocalDate monthStart = now.withDayOfMonth(1);

        for (DeliveryEarning earning : allEarnings) {
            double amount = earning.getAmount();
            LocalDate date = earning.getEarnedDate();

            totalEarnings += amount;

            if (!date.isBefore(monthStart)) {
                monthlyEarnings += amount;
            }

            if (!date.isBefore(weekStart)) {
                weeklyEarnings += amount;
            }
        }

        totalEarningsLabel.setText(String.format("%.2f DH", totalEarnings));
        monthlyEarningsLabel.setText(String.format("%.2f DH", monthlyEarnings));
        weeklyEarningsLabel.setText(String.format("%.2f DH", weeklyEarnings));
        totalDeliveriesLabel.setText(String.valueOf(totalDeliveries));
    }

    private void updateChart() {
        earningsChart.getData().clear();

        // Get date range
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null) {
            return;
        }

        // Limit to reasonable number of days to display
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween > 60) {
            showAlert(Alert.AlertType.WARNING, "Date Range Too Large",
                    "Please select a date range of 60 days or less for the chart.");
            return;
        }

        // Create series for the chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Earnings");

        // Group earnings by date
        Map<LocalDate, Double> dailyEarnings = new HashMap<>();

        for (DeliveryEarning earning : filteredEarnings) {
            LocalDate date = earning.getEarnedDate();
            if ((date.isEqual(startDate) || date.isAfter(startDate)) &&
                    (date.isEqual(endDate) || date.isBefore(endDate))) {

                dailyEarnings.merge(date, earning.getAmount(), Double::sum);
            }
        }

        // Add data points for each day in range
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            String dateStr = current.format(DateTimeFormatter.ofPattern("MMM dd"));
            double amount = dailyEarnings.getOrDefault(current, 0.0);
            series.getData().add(new XYChart.Data<>(dateStr, amount));
            current = current.plusDays(1);
        }

        earningsChart.getData().add(series);
    }

    @FXML
    private void handleApplyFilter() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String status = statusFilter.getValue();

        if (startDate == null || endDate == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Dates", "Please select both start and end dates.");
            return;
        }

        if (endDate.isBefore(startDate)) {
            showAlert(Alert.AlertType.WARNING, "Invalid Date Range", "End date must be after start date.");
            return;
        }

        filteredEarnings.setPredicate(earning -> {
            boolean matchesDate = (earning.getEarnedDate().isEqual(startDate) || earning.getEarnedDate().isAfter(startDate)) &&
                    (earning.getEarnedDate().isEqual(endDate) || earning.getEarnedDate().isBefore(endDate));

            boolean matchesStatus = "All".equals(status) || status.equalsIgnoreCase(earning.getStatus());

            return matchesDate && matchesStatus;
        });

        updateChart();
    }

    @FXML
    private void handleResetFilter() {
        // Reset date pickers to current month
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());

        startDatePicker.setValue(firstDayOfMonth);
        endDatePicker.setValue(now);

        // Reset status filter
        statusFilter.getSelectionModel().selectFirst();

        // Clear filter
        filteredEarnings.setPredicate(earning -> true);

        updateChart();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
