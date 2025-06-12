package controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import model.Client;
import model.Commande;
import util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AvailableOrdersController implements DashboardController.UserAware, DashboardController.DashboardAware {
    private static final Logger LOGGER = Logger.getLogger(AvailableOrdersController.class.getName());

    @FXML private TextField searchField;
    @FXML private Label orderCountLabel;

    @FXML private TableView<Commande> ordersTable;
    @FXML private TableColumn<Commande, Integer> orderIdCol;
    @FXML private TableColumn<Commande, String> clientCol;
    @FXML private TableColumn<Commande, String> addressCol;
    @FXML private TableColumn<Commande, String> cityCol;
    @FXML private TableColumn<Commande, LocalDate> orderDateCol;
    @FXML private TableColumn<Commande, Double> deliveryFeeCol;
    @FXML private TableColumn<Commande, Void> actionCol;

    private Client deliveryGuy;
    private DashboardController dashboardController;
    private final ObservableList<Commande> availableOrders = FXCollections.observableArrayList();
    private final FilteredList<Commande> filteredOrders = new FilteredList<>(availableOrders);

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
            setupSearch();
            loadAvailableOrders();
            setupTable();
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            String searchText = newText.toLowerCase().trim();
            filteredOrders.setPredicate(order -> {
                if (searchText.isEmpty()) {
                    return true;
                }

                return String.valueOf(order.getId()).contains(searchText) ||
                        order.getClient().getNom().toLowerCase().contains(searchText) ||
                        order.getClient().getAdresse().toLowerCase().contains(searchText) ||
                        order.getDeliveryCity().toLowerCase().contains(searchText);
            });

            updateOrderCount();
        });
    }

    private void loadAvailableOrders() {
        availableOrders.clear();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT c.id, c.date, c.delivery_city, c.delivery_fee, 
                       cl.id as client_id, cl.nom as client_name, cl.adresse, cl.email, cl.telephone
                FROM commandes c 
                JOIN clients cl ON c.client_id = cl.id 
                LEFT JOIN livraison l ON c.id = l.commande_id 
                WHERE c.statutLivraison = 'En attente' 
                AND (l.delivery_guy_id IS NULL OR l.statut = 'En attente')
                ORDER BY c.date ASC
                """;

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Client client = new Client(
                        rs.getInt("client_id"),
                        rs.getString("client_name"),
                        rs.getString("email"),
                        rs.getString("adresse"),
                        rs.getString("telephone"),
                        "",  // Password is not needed here
                        1    // User type is not relevant here
                );

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

            ordersTable.setItems(filteredOrders);
            updateOrderCount();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading available orders", e);
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load available orders.");
        }
    }

    private void setupTable() {
        orderIdCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
        clientCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getClient().getNom()));
        addressCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getClient().getAdresse()));
        cityCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDeliveryCity()));
        orderDateCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getDate()));
        deliveryFeeCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getDeliveryFee()).asObject());
        deliveryFeeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double fee, boolean empty) {
                super.updateItem(fee, empty);
                if (empty || fee == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DH", fee));
                }
            }
        });

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
    }

    private void assignOrderToMe(Commande order) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Check if the order already has a delivery record
            String checkSql = "SELECT id, statut FROM livraison WHERE commande_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, order.getId());
            ResultSet rs = checkStmt.executeQuery();

            LocalDate estimatedDeliveryDate = calculateEstimatedDeliveryDate(order.getDeliveryCity());

            if (rs.next()) {
                // Update existing delivery record
                int livraisonId = rs.getInt("id");
                String currentStatus = rs.getString("statut");

                if (!"En attente".equals(currentStatus)) {
                    showAlert(Alert.AlertType.WARNING, "Assignment Failed",
                            "This order is already being processed by another delivery person.");
                    conn.rollback();
                    return;
                }

                stmt = conn.prepareStatement(
                        "UPDATE livraison SET delivery_guy_id = ?, statut = 'Confirmée', estimated_delivery_date = ? WHERE id = ?");
                stmt.setInt(1, deliveryGuy.getId());
                stmt.setDate(2, Date.valueOf(estimatedDeliveryDate));
                stmt.setInt(3, livraisonId);
            } else {
                // Create new delivery record
                stmt = conn.prepareStatement(
                        "INSERT INTO livraison (commande_id, adresse_livraison, statut, date_livraison, delivery_guy_id, estimated_delivery_date) " +
                                "VALUES (?, ?, 'Confirmée', ?, ?, ?)");
                stmt.setInt(1, order.getId());
                stmt.setString(2, order.getClient().getAdresse());
                stmt.setDate(3, Date.valueOf(LocalDate.now()));
                stmt.setInt(4, deliveryGuy.getId());
                stmt.setDate(5, Date.valueOf(estimatedDeliveryDate));
            }

            int updated = stmt.executeUpdate();

            if (updated > 0) {
                // Update order status
                PreparedStatement orderStmt = conn.prepareStatement(
                        "UPDATE commandes SET statutLivraison = 'Confirmée' WHERE id = ?");
                orderStmt.setInt(1, order.getId());
                orderStmt.executeUpdate();

                conn.commit();
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Order #" + order.getId() + " has been assigned to you!\n" +
                                "Estimated delivery date: " + estimatedDeliveryDate);

                loadAvailableOrders();
            } else {
                conn.rollback();
                showAlert(Alert.AlertType.WARNING, "Assignment Failed",
                        "This order may have been assigned to another delivery person.");
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
            if (conn != null) {
                try {
                    if (!conn.isClosed()) {
                        conn.setAutoCommit(true);
                    }
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error resetting auto-commit", e);
                }
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing connection", e);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing statement", e);
                }
            }
        }
    }

    private LocalDate calculateEstimatedDeliveryDate(String city) {
        LocalDate today = LocalDate.now();

        // Simple logic:
        // - Same city: 1-2 days
        // - Major cities: 2-3 days
        // - Other cities: 3-5 days

        // Get delivery guy's city from address (assuming first part of address is city)
        String deliveryGuyCity = "Unknown";
        if (deliveryGuy.getAdresse() != null && !deliveryGuy.getAdresse().isEmpty()) {
            String[] addressParts = deliveryGuy.getAdresse().split(",");
            if (addressParts.length > 0) {
                deliveryGuyCity = addressParts[0].trim();
            }
        }

        if (city.equalsIgnoreCase(deliveryGuyCity)) {
            // Same city delivery: 1-2 days
            return today.plusDays(1 + (int)(Math.random() * 2));
        } else if (isMajorCity(city)) {
            // Major city: 2-3 days
            return today.plusDays(2 + (int)(Math.random() * 2));
        } else {
            // Other cities: 3-5 days
            return today.plusDays(3 + (int)(Math.random() * 3));
        }
    }

    private boolean isMajorCity(String city) {
        // List of major cities in Morocco
        String[] majorCities = {
                "Casablanca", "Rabat", "Fès", "Marrakech", "Tanger", "Agadir",
                "Meknès", "Oujda", "Kénitra", "Tétouan"
        };

        for (String majorCity : majorCities) {
            if (city.equalsIgnoreCase(majorCity)) {
                return true;
            }
        }

        return false;
    }

    @FXML
    private void handleRefresh() {
        loadAvailableOrders();
    }

    private void updateOrderCount() {
        int count = filteredOrders.size();
        orderCountLabel.setText(count + (count == 1 ? " order available" : " orders available"));
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
