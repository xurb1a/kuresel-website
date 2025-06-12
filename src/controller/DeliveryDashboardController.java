package controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import model.Client;
import model.Commande;
import model.DeliveryStats;
import util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeliveryDashboardController implements DashboardController.UserAware, DashboardController.DashboardAware {
    private static final Logger LOGGER = Logger.getLogger(DeliveryDashboardController.class.getName());

    @FXML private Label totalEarningsLabel;
    @FXML private Label totalDeliveriesLabel;
    @FXML private Label pendingDeliveriesLabel;
    @FXML private Label successfulDeliveriesLabel;

    @FXML private TableView<Commande> availableOrdersTable;
    @FXML private TableColumn<Commande, Integer> orderIdCol;
    @FXML private TableColumn<Commande, String> clientCol;
    @FXML private TableColumn<Commande, String> cityCol;
    @FXML private TableColumn<Commande, Double> deliveryFeeCol;
    @FXML private TableColumn<Commande, LocalDate> orderDateCol;
    @FXML private TableColumn<Commande, Void> actionCol;

    @FXML private TableView<Commande> myDeliveriesTable;
    @FXML private TableColumn<Commande, Integer> myOrderIdCol;
    @FXML private TableColumn<Commande, String> myClientCol;
    @FXML private TableColumn<Commande, String> myStatusCol;
    @FXML private TableColumn<Commande, LocalDate> myEstimatedDateCol;
    @FXML private TableColumn<Commande, Void> myActionCol;

    private final ObservableList<Commande> availableOrders = FXCollections.observableArrayList();
    private final ObservableList<Commande> myDeliveries = FXCollections.observableArrayList();
    private Client deliveryGuy;
    private DashboardController dashboardController;

    @Override
    public void setUser(Client user) {
        this.deliveryGuy = user;
        initialize();
    }

    @Override
    public void setDashboardController(DashboardController controller) {
        this.dashboardController = controller;
    }

    @FXML
    public void initialize() {
        if (deliveryGuy != null) {
            loadDeliveryStats();
            loadAvailableOrders();
            loadMyDeliveries();
            setupTables();
        }
    }

    private void loadDeliveryStats() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // Get or create delivery stats
            stmt = conn.prepareStatement(
                    "SELECT id, total_deliveries, total_earnings, successful_deliveries FROM delivery_stats WHERE delivery_guy_id = ?");
            stmt.setInt(1, deliveryGuy.getId());
            rs = stmt.executeQuery();

            if (rs.next()) {
                totalDeliveriesLabel.setText(String.valueOf(rs.getInt("total_deliveries")));
                totalEarningsLabel.setText(String.format("%.2f DH", rs.getDouble("total_earnings")));
                successfulDeliveriesLabel.setText(String.valueOf(rs.getInt("successful_deliveries")));
            } else {
                // Create initial stats record
                PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO delivery_stats (delivery_guy_id, total_deliveries, total_earnings, successful_deliveries) VALUES (?, 0, 0.0, 0)");
                insertStmt.setInt(1, deliveryGuy.getId());
                insertStmt.executeUpdate();

                totalDeliveriesLabel.setText("0");
                totalEarningsLabel.setText("0.00 DH");
                successfulDeliveriesLabel.setText("0");
            }

            // Get pending deliveries count
            PreparedStatement pendingStmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM livraison WHERE delivery_guy_id = ? AND statut IN ('En cours', 'Assignée')");
            pendingStmt.setInt(1, deliveryGuy.getId());
            ResultSet pendingRs = pendingStmt.executeQuery();
            if (pendingRs.next()) {
                pendingDeliveriesLabel.setText(String.valueOf(pendingRs.getInt(1)));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading delivery stats", e);
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load delivery statistics.");
        } finally {
            DatabaseConnection.closeResources(rs, stmt);
        }
    }

    private void loadAvailableOrders() {
        availableOrders.clear();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = """
                SELECT c.id, c.date, c.delivery_city, c.delivery_fee, cl.nom as client_name, cl.adresse
                FROM commandes c 
                JOIN clients cl ON c.client_id = cl.id 
                LEFT JOIN livraison l ON c.id = l.commande_id 
                WHERE c.statutLivraison = 'En attente' 
                AND (l.delivery_guy_id IS NULL OR l.statut = 'En attente')
                ORDER BY c.date ASC
                """;

            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Client client = new Client(0, rs.getString("client_name"), "", rs.getString("adresse"), "", "", 1);
                Commande commande = new Commande(
                        rs.getInt("id"),
                        client,
                        null,
                        rs.getDate("date").toLocalDate(),
                        "En attente",
                        null
                );
                commande.setDeliveryCity(rs.getString("delivery_city"));
                commande.setDeliveryFee(rs.getDouble("delivery_fee"));
                availableOrders.add(commande);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading available orders", e);
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load available orders.");
        } finally {
            DatabaseConnection.closeResources(rs, stmt);
        }
    }

    private void loadMyDeliveries() {
        myDeliveries.clear();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = """
                SELECT c.id, c.date, c.delivery_city, cl.nom as client_name, l.statut, l.estimated_delivery_date
                FROM commandes c 
                JOIN clients cl ON c.client_id = cl.id 
                JOIN livraison l ON c.id = l.commande_id 
                WHERE l.delivery_guy_id = ?
                ORDER BY l.estimated_delivery_date ASC
                """;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deliveryGuy.getId());
            rs = stmt.executeQuery();

            while (rs.next()) {
                Client client = new Client(0, rs.getString("client_name"), "", "", "", "", 1);
                Commande commande = new Commande(
                        rs.getInt("id"),
                        client,
                        null,
                        rs.getDate("date").toLocalDate(),
                        rs.getString("statut"),
                        null
                );
                commande.setDeliveryCity(rs.getString("delivery_city"));
                if (rs.getDate("estimated_delivery_date") != null) {
                    commande.setEstimatedDeliveryDate(rs.getDate("estimated_delivery_date").toLocalDate());
                }
                myDeliveries.add(commande);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading my deliveries", e);
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load your deliveries.");
        } finally {
            DatabaseConnection.closeResources(rs, stmt);
        }
    }

    private void setupTables() {
        // Available orders table
        orderIdCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getId()));
        clientCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getClient().getNom()));
        cityCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDeliveryCity()));
        deliveryFeeCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getDeliveryFee()));
        orderDateCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getDate()));

        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button assignBtn = new Button("📦 Assign to Me");

            {
                assignBtn.getStyleClass().add("manager-button");
                assignBtn.setOnAction(e -> {
                    Commande order = getTableView().getItems().get(getIndex());
                    assignOrderToMe(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : assignBtn);
            }
        });

        // My deliveries table
        myOrderIdCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getId()));
        myClientCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getClient().getNom()));
        myStatusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatutLivraison()));
        myEstimatedDateCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getEstimatedDeliveryDate()));

        myActionCol.setCellFactory(col -> new TableCell<>() {
            private final Button deliverBtn = new Button("✅ Mark Delivered");
            private final Button startBtn = new Button("🚚 Start Delivery");
            private final HBox buttonBox = new HBox(5, startBtn, deliverBtn);

            {
                startBtn.getStyleClass().add("manager-button");
                deliverBtn.getStyleClass().add("action-button");

                startBtn.setOnAction(e -> {
                    Commande order = getTableView().getItems().get(getIndex());
                    startDelivery(order);
                });

                deliverBtn.setOnAction(e -> {
                    Commande order = getTableView().getItems().get(getIndex());
                    markAsDelivered(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Commande order = getTableView().getItems().get(getIndex());
                    String status = order.getStatutLivraison();

                    if ("Assignée".equals(status)) {
                        setGraphic(startBtn);
                    } else if ("En cours".equals(status)) {
                        setGraphic(deliverBtn);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        availableOrdersTable.setItems(availableOrders);
        myDeliveriesTable.setItems(myDeliveries);
    }

    private void assignOrderToMe(Commande order) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            stmt = conn.prepareStatement(
                    "UPDATE livraison SET delivery_guy_id = ?, statut = 'Assignée' WHERE commande_id = ?");
            stmt.setInt(1, deliveryGuy.getId());
            stmt.setInt(2, order.getId());
            int updated = stmt.executeUpdate();

            if (updated > 0) {
                conn.commit();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Order #" + order.getId() + " has been assigned to you!");
                loadAvailableOrders();
                loadMyDeliveries();
                loadDeliveryStats();
            } else {
                conn.rollback();
                showAlert(Alert.AlertType.WARNING, "Assignment Failed", "This order may have been assigned to another delivery person.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error assigning order", e);
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error rolling back transaction", ex);
            }
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to assign order.");
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error resetting auto-commit", e);
            }
            DatabaseConnection.closeResources(null, stmt);
        }
    }

    private void startDelivery(Commande order) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            stmt = conn.prepareStatement(
                    "UPDATE livraison SET statut = 'En cours' WHERE commande_id = ? AND delivery_guy_id = ?");
            stmt.setInt(1, order.getId());
            stmt.setInt(2, deliveryGuy.getId());
            int updated = stmt.executeUpdate();

            if (updated > 0) {
                // Update order status
                PreparedStatement orderStmt = conn.prepareStatement(
                        "UPDATE commandes SET statutLivraison = 'En cours' WHERE id = ?");
                orderStmt.setInt(1, order.getId());
                orderStmt.executeUpdate();

                conn.commit();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Delivery started for order #" + order.getId());
                loadMyDeliveries();
            } else {
                conn.rollback();
                showAlert(Alert.AlertType.WARNING, "Update Failed", "Could not start delivery. Please try again.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error starting delivery", e);
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error rolling back transaction", ex);
            }
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to start delivery.");
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error resetting auto-commit", e);
            }
            DatabaseConnection.closeResources(null, stmt);
        }
    }

    private void markAsDelivered(Commande order) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Update delivery status
            stmt = conn.prepareStatement(
                    "UPDATE livraison SET statut = 'Livrée', actual_delivery_date = ? WHERE commande_id = ? AND delivery_guy_id = ?");
            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            stmt.setInt(2, order.getId());
            stmt.setInt(3, deliveryGuy.getId());
            int updated = stmt.executeUpdate();

            if (updated > 0) {
                // Update order status
                PreparedStatement orderStmt = conn.prepareStatement(
                        "UPDATE commandes SET statutLivraison = 'Livrée' WHERE id = ?");
                orderStmt.setInt(1, order.getId());
                orderStmt.executeUpdate();

                // Add earnings
                PreparedStatement earningsStmt = conn.prepareStatement(
                        "INSERT INTO delivery_earnings (delivery_guy_id, commande_id, amount, earned_date, status) VALUES (?, ?, ?, ?, 'paid')");
                earningsStmt.setInt(1, deliveryGuy.getId());
                earningsStmt.setInt(2, order.getId());
                earningsStmt.setDouble(3, order.getDeliveryFee());
                earningsStmt.setDate(4, Date.valueOf(LocalDate.now()));
                earningsStmt.executeUpdate();

                // Update delivery stats
                PreparedStatement statsStmt = conn.prepareStatement(
                        "UPDATE delivery_stats SET total_deliveries = total_deliveries + 1, " +
                                "total_earnings = total_earnings + ?, successful_deliveries = successful_deliveries + 1 " +
                                "WHERE delivery_guy_id = ?");
                statsStmt.setDouble(1, order.getDeliveryFee());
                statsStmt.setInt(2, deliveryGuy.getId());
                statsStmt.executeUpdate();

                conn.commit();
                showAlert(Alert.AlertType.INFORMATION, "Success", String.format("Order #%d marked as delivered! You earned %.2f DH",
                        order.getId(), order.getDeliveryFee()));

                loadMyDeliveries();
                loadDeliveryStats();
            } else {
                conn.rollback();
                showAlert(Alert.AlertType.WARNING, "Update Failed", "Could not mark as delivered. Please try again.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error marking delivery as complete", e);
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error rolling back transaction", ex);
            }
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to mark as delivered.");
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error resetting auto-commit", e);
            }
            DatabaseConnection.closeResources(null, stmt);
        }
    }

    @FXML
    private void handleRefresh() {
        loadDeliveryStats();
        loadAvailableOrders();
        loadMyDeliveries();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type, content, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }
}
