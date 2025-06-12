package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDashboardController {

    @FXML private Label totalOrdersLabel;
    @FXML private Label totalSalesLabel;
    @FXML private Label lowStockLabel;
    @FXML private Label totalClientsLabel;
    @FXML private Label pendingDeliveriesLabel;

    @FXML private PieChart orderStatusPie;
    @FXML private BarChart<String, Number> monthlySalesChart;
    @FXML private BarChart<String, Number> topProductsChart;
    @FXML private PieChart revenuePerProductChart;
    @FXML private PieChart paymentMethodChart;

    public void initialize() {
        // Apply CSS classes to all labels
        applyLabelStyling();

        // Apply CSS classes to all charts
        applyChartStyling();

        // Load data
        loadSummary();
        loadOrderStatusPie();
        loadMonthlySalesChart();
        loadTopProductsChart();
        loadRevenuePerProductChart();
        loadPaymentMethodChart();
    }

    private void applyLabelStyling() {
        // Apply dashboard stat value styling to all summary labels
        totalOrdersLabel.getStyleClass().add("stats-value");
        totalSalesLabel.getStyleClass().add("stats-value");
        lowStockLabel.getStyleClass().add("stats-value");
        totalClientsLabel.getStyleClass().add("stats-value");
        pendingDeliveriesLabel.getStyleClass().add("stats-value");
    }

    private void applyChartStyling() {
        // Apply chart styling to all charts
        orderStatusPie.getStyleClass().add("chart");
        monthlySalesChart.getStyleClass().add("chart");
        topProductsChart.getStyleClass().add("chart");
        revenuePerProductChart.getStyleClass().add("chart");
        paymentMethodChart.getStyleClass().add("chart");

        // Set chart titles with proper styling
        orderStatusPie.setTitle("Order Status Distribution");
        monthlySalesChart.setTitle("Monthly Sales Trend");
        topProductsChart.setTitle("Top Selling Products");
        revenuePerProductChart.setTitle("Revenue by Product");
        paymentMethodChart.setTitle("Payment Methods");

        // Configure chart properties for better dark theme appearance
        configureChartAppearance();
    }

    private void configureChartAppearance() {
        // Configure pie charts
        configurePieChart(orderStatusPie);
        configurePieChart(revenuePerProductChart);
        configurePieChart(paymentMethodChart);

        // Configure bar charts
        configureBarChart(monthlySalesChart);
        configureBarChart(topProductsChart);
    }

    private void configurePieChart(PieChart chart) {
        chart.setLegendVisible(true);
        chart.setLabelsVisible(true);
        chart.setStartAngle(90);
        chart.setClockwise(true);

        // Set animated property for smooth transitions
        chart.setAnimated(true);
    }

    private void configureBarChart(BarChart<String, Number> chart) {
        chart.setLegendVisible(true);
        chart.setAnimated(true);
        chart.setCategoryGap(10);
        chart.setBarGap(5);

        // Configure axes
        if (chart.getXAxis() != null) {
            chart.getXAxis().setAnimated(true);
            chart.getXAxis().setAutoRanging(true);
        }

        if (chart.getYAxis() != null) {
            chart.getYAxis().setAnimated(true);
            chart.getYAxis().setAutoRanging(true);
        }
    }

    private void loadSummary() {
        try (Connection conn = DatabaseConnection.getConnection()) {

            // Total Orders
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM commandes");
            if (rs.next()) {
                totalOrdersLabel.setText(String.valueOf(rs.getInt(1)));
            }

            // Total Sales
            rs = conn.createStatement().executeQuery("SELECT SUM(montant) FROM paiements");
            if (rs.next()) {
                double totalSales = rs.getDouble(1);
                totalSalesLabel.setText(String.format("%.2f DH", totalSales));
            }

            // Low Stock Items
            rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM produits WHERE stock < 5");
            if (rs.next()) {
                lowStockLabel.setText(String.valueOf(rs.getInt(1)));
            }

            // Total Clients
            rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM clients WHERE type = 1");
            if (rs.next()) {
                totalClientsLabel.setText(String.valueOf(rs.getInt(1)));
            }

            // Pending Deliveries
            rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM livraison WHERE statut IN ('En attente', 'En cours')");
            if (rs.next()) {
                pendingDeliveriesLabel.setText(String.valueOf(rs.getInt(1)));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Set default values on error
            setDefaultValues();
        }
    }

    private void setDefaultValues() {
        totalOrdersLabel.setText("0");
        totalSalesLabel.setText("0.00 DH");
        lowStockLabel.setText("0");
        totalClientsLabel.setText("0");
        pendingDeliveriesLabel.setText("0");
    }

    private void loadOrderStatusPie() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT statutLivraison, COUNT(*) FROM commandes GROUP BY statutLivraison");
            ResultSet rs = ps.executeQuery();

            var data = FXCollections.<PieChart.Data>observableArrayList();
            while (rs.next()) {
                String status = rs.getString(1);
                int count = rs.getInt(2);

                // Create pie chart data with formatted labels
                PieChart.Data pieData = new PieChart.Data(
                        String.format("%s (%d)", status, count), count);
                data.add(pieData);
            }

            orderStatusPie.setData(data);

            // Apply custom colors to pie chart slices after data is set
            orderStatusPie.applyCss();
            orderStatusPie.layout();
            applyPieChartColors(orderStatusPie);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadMonthlySalesChart() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT MONTH(date_paiement), SUM(montant) FROM paiements GROUP BY MONTH(date_paiement) ORDER BY MONTH(date_paiement)");
            ResultSet rs = ps.executeQuery();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Sales (DH)");
            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

            // Initialize all months with 0 values
            for (String month : months) {
                series.getData().add(new XYChart.Data<>(month, 0));
            }

            // Update with actual data
            while (rs.next()) {
                int monthNum = rs.getInt(1);
                double sales = rs.getDouble(2);
                if (monthNum >= 1 && monthNum <= 12) {
                    // Update the existing data point
                    series.getData().get(monthNum - 1).setYValue(sales);
                }
            }

            monthlySalesChart.getData().clear();
            monthlySalesChart.getData().add(series);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTopProductsChart() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                    SELECT p.nom, SUM(lc.quantite) AS total_qty
                    FROM ligne_commande lc
                    JOIN produits p ON lc.produit_id = p.id
                    GROUP BY p.nom
                    ORDER BY total_qty DESC
                    LIMIT 5
                    """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Quantity Sold");

            while (rs.next()) {
                String productName = rs.getString(1);
                int quantitySold = rs.getInt(2);

                // Truncate long product names for better display
                String displayName = productName.length() > 15 ?
                        productName.substring(0, 12) + "..." : productName;

                series.getData().add(new XYChart.Data<>(displayName, quantitySold));
            }

            topProductsChart.getData().clear();
            topProductsChart.getData().add(series);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadRevenuePerProductChart() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                    SELECT p.nom, SUM(p.prix * lc.quantite) AS revenue
                    FROM ligne_commande lc
                    JOIN produits p ON lc.produit_id = p.id
                    GROUP BY p.nom
                    ORDER BY revenue DESC
                    LIMIT 8
                    """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            var data = FXCollections.<PieChart.Data>observableArrayList();
            while (rs.next()) {
                String productName = rs.getString(1);
                double revenue = rs.getDouble(2);

                // Format the label with revenue amount
                String label = String.format("%s (%.0f DH)",
                        productName.length() > 12 ? productName.substring(0, 9) + "..." : productName,
                        revenue);

                data.add(new PieChart.Data(label, revenue));
            }

            revenuePerProductChart.setData(data);

            // Apply custom colors after data is set
            revenuePerProductChart.applyCss();
            revenuePerProductChart.layout();
            applyPieChartColors(revenuePerProductChart);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadPaymentMethodChart() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT methode, SUM(montant) FROM paiements GROUP BY methode";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            var data = FXCollections.<PieChart.Data>observableArrayList();
            while (rs.next()) {
                String method = rs.getString(1);
                double amount = rs.getDouble(2);

                // Format the label with amount
                String label = String.format("%s (%.0f DH)", method, amount);
                data.add(new PieChart.Data(label, amount));
            }

            paymentMethodChart.setData(data);

            // Apply custom colors after data is set
            paymentMethodChart.applyCss();
            paymentMethodChart.layout();
            applyPieChartColors(paymentMethodChart);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void applyPieChartColors(PieChart chart) {
        // Define custom colors that match the dark theme
        String[] colors = {
                "#00d4ff", "#5a67d8", "#48bb78", "#ff4757",
                "#ffd93d", "#a0aec0", "#e2e8f0", "#718096"
        };

        // Apply colors to pie chart slices
        int colorIndex = 0;
        for (PieChart.Data data : chart.getData()) {
            if (data.getNode() != null) {
                String color = colors[colorIndex % colors.length];
                data.getNode().setStyle("-fx-pie-color: " + color + ";");
                colorIndex++;
            }
        }
    }

    // Method to refresh all dashboard data
    public void refreshDashboard() {
        loadSummary();
        loadOrderStatusPie();
        loadMonthlySalesChart();
        loadTopProductsChart();
        loadRevenuePerProductChart();
        loadPaymentMethodChart();
    }
}
