package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import model.Client;
import model.RecentOrder;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class ClientDashboardController implements DashboardController.DashboardAware, DashboardController.UserAware {

    private DashboardController dashboardController;
    private Client client;

    @Override
    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @FXML private Label welcomeLabel;
    @FXML private Label memberSinceLabel;
    @FXML private Label totalOrdersLabel;
    @FXML private Label activeOrdersLabel;
    @FXML private Label cartItemsLabel;
    @FXML private TableView<RecentOrder> recentOrdersTable;
    @FXML private TableColumn<RecentOrder, Integer> orderIdCol;
    @FXML private TableColumn<RecentOrder, LocalDate> orderDateCol;
    @FXML private TableColumn<RecentOrder, String> orderStatusCol;

    @Override
    public void setUser(Client client) {
        this.client = client;
        initializeDashboard();
    }

    private void initializeDashboard() {
        if (client != null) {
            // Apply CSS classes to dynamically set labels
            welcomeLabel.setText("Welcome back, " + client.getNom() + " 👋");
            welcomeLabel.getStyleClass().add("dashboard-welcome-label");

            memberSinceLabel.setText("Member since: " + fetchMemberSinceDate());
            memberSinceLabel.getStyleClass().add("dashboard-member-since");
        } else {
            welcomeLabel.setText("Welcome!");
            memberSinceLabel.setText("");
        }

        loadStats();
        loadRecentOrders();
    }

    private String fetchMemberSinceDate() {
        String dateStr = "...";
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT MIN(date) AS firstOrder FROM commandes WHERE client_id = ?");
            ps.setInt(1, client.getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getDate("firstOrder") != null) {
                dateStr = rs.getDate("firstOrder").toLocalDate().toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateStr;
    }

    private void loadStats() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Total Orders
            PreparedStatement totalPs = conn.prepareStatement(
                    "SELECT COUNT(*) FROM commandes WHERE client_id = ?");
            totalPs.setInt(1, client.getId());
            ResultSet totalRs = totalPs.executeQuery();
            if (totalRs.next()) {
                totalOrdersLabel.setText(String.valueOf(totalRs.getInt(1)));
                totalOrdersLabel.getStyleClass().add("dashboard-stat-value");
            }

            // Active Orders
            PreparedStatement activePs = conn.prepareStatement(
                    "SELECT COUNT(*) FROM commandes WHERE client_id = ? AND statutLivraison != 'Livrée'");
            activePs.setInt(1, client.getId());
            ResultSet activeRs = activePs.executeQuery();
            if (activeRs.next()) {
                activeOrdersLabel.setText(String.valueOf(activeRs.getInt(1)));
                activeOrdersLabel.getStyleClass().add("dashboard-stat-value");
            }

            // Cart Items
            cartItemsLabel.setText("0");
            cartItemsLabel.getStyleClass().add("dashboard-stat-value");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadRecentOrders() {
        ObservableList<RecentOrder> recentOrders = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, date, statutLivraison FROM commandes WHERE client_id = ? ORDER BY date DESC LIMIT 5");
            ps.setInt(1, client.getId());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                recentOrders.add(new RecentOrder(
                        rs.getInt("id"),
                        rs.getDate("date").toLocalDate(),
                        rs.getString("statutLivraison")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        orderIdCol.setCellValueFactory(data -> data.getValue().orderIdProperty().asObject());
        orderDateCol.setCellValueFactory(data -> data.getValue().orderDateProperty());
        orderStatusCol.setCellValueFactory(data -> data.getValue().orderStatusProperty());

        recentOrdersTable.setItems(recentOrders);
        recentOrdersTable.getStyleClass().add("table-view");
    }

    @FXML
    private void goToProducts() {
        if (dashboardController != null) {
            dashboardController.loadPage("products.fxml");
        }
    }

    @FXML
    private void goToCart() {
        if (dashboardController != null) {
            dashboardController.loadPage("cart.fxml");
        }
    }

    @FXML
    private void goToOrders() {
        if (dashboardController != null) {
            dashboardController.loadPage("orders.fxml");
        }
    }
}
