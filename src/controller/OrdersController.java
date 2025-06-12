package controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import model.Client;
import model.Commande;
import model.Paiement;
import util.DatabaseConnection;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrdersController implements DashboardController.UserAware {
    private static final Logger LOGGER = Logger.getLogger(OrdersController.class.getName());

    @FXML private TextField searchField;
    @FXML private DatePicker dateFilter;
    @FXML private ComboBox<String> statusFilter;

    @FXML private TableView<Commande> ordersTable;
    @FXML private TableColumn<Commande, Integer> idCol;
    @FXML private TableColumn<Commande, LocalDate> dateCol;
    @FXML private TableColumn<Commande, String> statusCol;
    @FXML private TableColumn<Commande, String> clientCol;
    @FXML private TableColumn<Commande, String> paymentCol;
    @FXML private TableColumn<Commande, LocalDate> estimatedDeliveryCol;
    @FXML private TableColumn<Commande, String> deliveryCityCol;
    @FXML private TableColumn<Commande, Void> actionCol;

    private final ObservableList<Commande> allOrders = FXCollections.observableArrayList();
    private final FilteredList<Commande> filteredOrders = new FilteredList<>(allOrders, p -> true);

    private Client currentUser;
    private boolean isAdmin = false;

    @Override
    public void setUser(Client user) {
        this.currentUser = user;
        this.isAdmin = user.getUserType() == 2;
        loadOrders();
        setupSearchFilters();
    }

    private void createDeliveryIfConfirmed(Commande commande) {
        if (!"Confirmée".equalsIgnoreCase(commande.getStatutLivraison())) return;

        Connection conn = null;
        PreparedStatement check = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            check = conn.prepareStatement("SELECT COUNT(*) FROM livraison WHERE commande_id = ?");
            check.setInt(1, commande.getId());
            rs = check.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) return;

            PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO livraison (commande_id, adresse_livraison, statut, date_livraison, estimated_delivery_date) VALUES (?, ?, ?, ?, ?)"
            );
            insert.setInt(1, commande.getId());
            insert.setString(2, commande.getClient().getAdresse());
            insert.setString(3, "En attente");
            insert.setDate(4, Date.valueOf(LocalDate.now()));

            // Calculate estimated delivery date based on city
            LocalDate estimatedDate = LocalDate.now().plusDays(1); // Default: 1 day
            if (commande.getDeliveryCity() != null) {
                // In a real app, we would look up the city's delivery time
                // For now, we'll use a simple rule based on the city name
                if (commande.getDeliveryCity().equals("Casablanca") ||
                        commande.getDeliveryCity().equals("Rabat")) {
                    estimatedDate = LocalDate.now().plusDays(2);
                } else if (commande.getDeliveryCity().equals("Marrakech") ||
                        commande.getDeliveryCity().equals("Fès")) {
                    estimatedDate = LocalDate.now().plusDays(3);
                } else if (commande.getDeliveryCity().equals("Tangier") ||
                        commande.getDeliveryCity().equals("Agadir")) {
                    estimatedDate = LocalDate.now().plusDays(4);
                } else if (commande.getDeliveryCity().equals("Oujda")) {
                    estimatedDate = LocalDate.now().plusDays(5);
                }
            }

            insert.setDate(5, Date.valueOf(estimatedDate));
            insert.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating delivery record", e);
        } finally {
            DatabaseConnection.closeResources(rs, check);
        }
    }

    private void loadOrders() {
        allOrders.clear();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = isAdmin
                    ? "SELECT c.id, c.date, c.statutLivraison, c.delivery_city, c.delivery_fee, " +
                    "cl.nom, cl.id AS client_id, p.id AS pid, p.methode, p.montant, " +
                    "l.estimated_delivery_date " +
                    "FROM commandes c " +
                    "JOIN clients cl ON c.client_id = cl.id " +
                    "LEFT JOIN paiements p ON c.id = p.commande_id " +
                    "LEFT JOIN livraison l ON c.id = l.commande_id"
                    : "SELECT c.id, c.date, c.statutLivraison, c.delivery_city, c.delivery_fee, " +
                    "p.id AS pid, p.methode, p.montant, " +
                    "l.estimated_delivery_date " +
                    "FROM commandes c " +
                    "LEFT JOIN paiements p ON c.id = p.commande_id " +
                    "LEFT JOIN livraison l ON c.id = l.commande_id " +
                    "WHERE c.client_id = ?";

            ps = conn.prepareStatement(sql);
            if (!isAdmin) ps.setInt(1, currentUser.getId());

            rs = ps.executeQuery();
            while (rs.next()) {
                Paiement paiement = null;
                int pid = rs.getInt("pid");
                if (!rs.wasNull()) {
                    paiement = new Paiement(pid, rs.getString("methode"), rs.getDouble("montant"));
                }

                Client orderClient;
                if (isAdmin) {
                    orderClient = new Client(rs.getInt("client_id"), rs.getString("nom"), null, null, null, "", 1);
                } else {
                    orderClient = currentUser;
                }

                Commande cmd = new Commande(
                        rs.getInt("id"),
                        orderClient,
                        null,
                        rs.getDate("date").toLocalDate(),
                        rs.getString("statutLivraison"),
                        paiement
                );

                // Set delivery information
                cmd.setDeliveryCity(rs.getString("delivery_city"));
                cmd.setDeliveryFee(rs.getDouble("delivery_fee"));

                Date estimatedDate = rs.getDate("estimated_delivery_date");
                if (estimatedDate != null) {
                    cmd.setEstimatedDeliveryDate(estimatedDate.toLocalDate());
                }

                allOrders.add(cmd);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading orders", e);
        } finally {
            DatabaseConnection.closeResources(rs, ps);
        }

        idCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getId()));
        dateCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getDate()));

        // Color-coded status column
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatutLivraison()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "En attente" -> setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
                        case "Confirmée" -> setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
                        case "En cours" -> setStyle("-fx-text-fill: #9C27B0; -fx-font-weight: bold;");
                        case "Livrée" -> setStyle("-fx-text-fill: #795548; -fx-font-weight: bold;");
                        case "Reçue" -> setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                        default -> setStyle("");
                    }
                }
            }
        });

        if (isAdmin && clientCol != null) {
            clientCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getClient().getNom()));
            clientCol.setVisible(true);
        } else if (clientCol != null) {
            clientCol.setVisible(false);
        }

        paymentCol.setCellValueFactory(c -> {
            Paiement p = c.getValue().getPaiement();
            return new SimpleStringProperty(p != null ? p.getMontant() + " (" + p.getMethode() + ")" : "N/A");
        });

        // Setup delivery columns
        if (deliveryCityCol != null) {
            deliveryCityCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDeliveryCity()));
        }

        if (estimatedDeliveryCol != null) {
            estimatedDeliveryCol.setCellValueFactory(c -> {
                LocalDate date = c.getValue().getEstimatedDeliveryDate();
                return new SimpleObjectProperty<>(date);
            });
        }

        // Setup action column for clients
        if (!isAdmin && actionCol != null) {
            addClientActionButtons();
        } else if (isAdmin && actionCol != null) {
            addAdminActionButtons();
        }

        ordersTable.setItems(filteredOrders);
        ordersTable.setPlaceholder(new Label("🧾 No orders to show."));
    }

    private void addClientActionButtons() {
        actionCol.setCellFactory(col -> new TableCell<>() {
            final Button confirmBtn = new Button("✅ Confirm Receipt");
            final Label waitingLabel = new Label("⏳ Waiting for delivery");
            final Label completedLabel = new Label("✅ Completed");

            {
                confirmBtn.getStyleClass().add("action-button");
                waitingLabel.setStyle("-fx-text-fill: #9C27B0; -fx-font-weight: bold;");
                completedLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");

                confirmBtn.setOnAction(e -> {
                    Commande order = getTableView().getItems().get(getIndex());
                    confirmReceipt(order);
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

                    if ("Livrée".equals(status)) {
                        setGraphic(confirmBtn);
                    } else if ("Reçue".equals(status)) {
                        setGraphic(completedLabel);
                    } else {
                        setGraphic(waitingLabel);
                    }
                }
            }
        });
    }

    private void addAdminActionButtons() {
        actionCol.setCellFactory(col -> new TableCell<>() {
            final Button editBtn = new Button("✏️");
            final Button delBtn = new Button("🗑️");
            final HBox pane = new HBox(6, editBtn, delBtn);

            {
                editBtn.getStyleClass().add("manager-button");
                delBtn.getStyleClass().add("remove-button");

                editBtn.setOnAction(e -> openEditDialog(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> deleteOrder(getTableView().getItems().get(getIndex()).getId()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void confirmReceipt(Commande order) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Receipt");
        confirmAlert.setHeaderText("Confirm Package Receipt");
        confirmAlert.setContentText(String.format(
                "Did you receive your package for Order #%d?\n\n" +
                        "Order Date: %s\n" +
                        "Delivery City: %s\n" +
                        "Delivery Fee: %.2f DH\n\n" +
                        "⚠️ Only confirm if you actually received the package!\n" +
                        "This will complete the delivery and pay the delivery guy.",
                order.getId(),
                order.getDate(),
                order.getDeliveryCity(),
                order.getDeliveryFee()
        ));

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            LocalDate today = LocalDate.now();

            // Update order status to 'Reçue' (received)
            stmt = conn.prepareStatement(
                    "UPDATE commandes SET statutLivraison = 'Reçue' WHERE id = ? AND client_id = ?");
            stmt.setInt(1, order.getId());
            stmt.setInt(2, currentUser.getId());
            int orderUpdated = stmt.executeUpdate();

            if (orderUpdated > 0) {
                // Update delivery status to 'Livrée' (delivered)
                PreparedStatement deliveryStmt = conn.prepareStatement(
                        "UPDATE livraison SET statut = 'Livrée' WHERE commande_id = ?");
                deliveryStmt.setInt(1, order.getId());
                deliveryStmt.executeUpdate();

                // Get delivery guy ID
                PreparedStatement getDeliveryGuyStmt = conn.prepareStatement(
                        "SELECT delivery_guy_id FROM livraison WHERE commande_id = ?");
                getDeliveryGuyStmt.setInt(1, order.getId());
                ResultSet deliveryRs = getDeliveryGuyStmt.executeQuery();

                if (deliveryRs.next()) {
                    int deliveryGuyId = deliveryRs.getInt("delivery_guy_id");

                    // Add earnings for delivery guy
                    PreparedStatement earningsStmt = conn.prepareStatement(
                            "INSERT INTO delivery_earnings (delivery_guy_id, commande_id, amount, earned_date, status) VALUES (?, ?, ?, ?, 'paid')");
                    earningsStmt.setInt(1, deliveryGuyId);
                    earningsStmt.setInt(2, order.getId());
                    earningsStmt.setDouble(3, order.getDeliveryFee());
                    earningsStmt.setDate(4, Date.valueOf(today));
                    earningsStmt.executeUpdate();

                    // Update delivery stats
                    PreparedStatement checkStatsStmt = conn.prepareStatement(
                            "SELECT id FROM delivery_stats WHERE delivery_guy_id = ?");
                    checkStatsStmt.setInt(1, deliveryGuyId);
                    ResultSet statsRs = checkStatsStmt.executeQuery();

                    if (statsRs.next()) {
                        // Update existing stats
                        PreparedStatement updateStatsStmt = conn.prepareStatement(
                                "UPDATE delivery_stats SET total_deliveries = total_deliveries + 1, " +
                                        "total_earnings = total_earnings + ?, successful_deliveries = successful_deliveries + 1 " +
                                        "WHERE delivery_guy_id = ?");
                        updateStatsStmt.setDouble(1, order.getDeliveryFee());
                        updateStatsStmt.setInt(2, deliveryGuyId);
                        updateStatsStmt.executeUpdate();
                    } else {
                        // Create new stats record
                        PreparedStatement insertStatsStmt = conn.prepareStatement(
                                "INSERT INTO delivery_stats (delivery_guy_id, total_deliveries, total_earnings, successful_deliveries) " +
                                        "VALUES (?, 1, ?, 1)");
                        insertStatsStmt.setInt(1, deliveryGuyId);
                        insertStatsStmt.setDouble(2, order.getDeliveryFee());
                        insertStatsStmt.executeUpdate();
                    }
                }

                conn.commit();

                showAlert(Alert.AlertType.INFORMATION, "Receipt Confirmed",
                        String.format("✅ Thank you for confirming receipt!\n\n" +
                                        "Order #%d is now complete.\n" +
                                        "The delivery guy has been paid %.2f DH.\n\n" +
                                        "We hope you enjoyed your purchase!",
                                order.getId(),
                                order.getDeliveryFee()));

                loadOrders(); // Refresh the table
            } else {
                conn.rollback();
                showAlert(Alert.AlertType.WARNING, "Update Failed",
                        "Could not confirm receipt. Please try again.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error confirming receipt", e);
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error rolling back transaction", ex);
            }
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to confirm receipt: " + e.getMessage());
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

    private void openEditDialog(Commande cmd) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/edit_order.fxml"));
            Parent root = loader.load();

            EditOrderController controller = loader.getController();
            controller.setCommande(cmd);
            controller.setParentController(this);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edit Order");
            dialog.getDialogPane().setContent(root);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    createDeliveryIfConfirmed(cmd);
                    refreshCurrentPage();
                }
            });

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error opening edit dialog", e);
        }
    }

    private void deleteOrder(int id) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // First delete related records in other tables
            String[] relatedTables = {"delivery_earnings", "livraison", "ligne_commande", "paiements"};
            for (String table : relatedTables) {
                PreparedStatement relatedPs = conn.prepareStatement("DELETE FROM " + table + " WHERE commande_id = ?");
                relatedPs.setInt(1, id);
                relatedPs.executeUpdate();
            }

            // Then delete the order
            ps = conn.prepareStatement("DELETE FROM commandes WHERE id = ?");
            ps.setInt(1, id);
            ps.executeUpdate();

            conn.commit();
            allOrders.removeIf(c -> c.getId() == id);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting order", e);
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error rolling back transaction", ex);
            }
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error resetting auto-commit", e);
            }
            DatabaseConnection.closeResources(null, ps);
        }
    }

    private void setupSearchFilters() {
        statusFilter.getItems().addAll("", "En attente", "Confirmée", "En cours", "Livrée", "Reçue", "Annulée");
        statusFilter.valueProperty().addListener((obs, old, selected) -> applyFilters());
        dateFilter.valueProperty().addListener((obs, old, selected) -> applyFilters());
        searchField.textProperty().addListener((obs, old, text) -> applyFilters());
    }

    private void applyFilters() {
        String keyword = searchField.getText().toLowerCase().trim();
        String status = statusFilter.getValue();
        LocalDate date = dateFilter.getValue();

        filteredOrders.setPredicate(cmd -> {
            boolean matchesText = keyword.isEmpty() ||
                    String.valueOf(cmd.getId()).contains(keyword) ||
                    cmd.getStatutLivraison().toLowerCase().contains(keyword) ||
                    (isAdmin && cmd.getClient().getNom().toLowerCase().contains(keyword));

            boolean matchesStatus = status == null || status.isEmpty() || cmd.getStatutLivraison().equalsIgnoreCase(status);
            boolean matchesDate = date == null || cmd.getDate().isEqual(date);

            return matchesText && matchesStatus && matchesDate;
        });
    }

    public void refreshCurrentPage() {
        loadOrders();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
