package controller;

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
import model.Livraison;
import util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyDeliveriesController implements DashboardController.UserAware, DashboardController.DashboardAware {
    private static final Logger LOGGER = Logger.getLogger(MyDeliveriesController.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label deliveryCountLabel;

    @FXML private TableView<Livraison> deliveriesTable;
    @FXML private TableColumn<Livraison, Integer> orderIdCol;
    @FXML private TableColumn<Livraison, String> clientCol;
    @FXML private TableColumn<Livraison, String> addressCol;
    @FXML private TableColumn<Livraison, String> cityCol;
    @FXML private TableColumn<Livraison, String> statusCol;
    @FXML private TableColumn<Livraison, LocalDate> estimatedDateCol;
    @FXML private TableColumn<Livraison, Void> actionCol;

    private Client deliveryGuy;
    private DashboardController dashboardController;
    private final ObservableList<Livraison> myDeliveries = FXCollections.observableArrayList();
    private final FilteredList<Livraison> filteredDeliveries = new FilteredList<>(myDeliveries);

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
            setupControls();
            loadMyDeliveries();
            setupTable();
        }
    }

    private void setupControls() {
        // Setup status filter with new statuses
        statusFilter.setItems(FXCollections.observableArrayList(
                "All", "Assignée", "En cours", "Livrée", "Confirmée", "Annulée"
        ));
        statusFilter.getSelectionModel().selectFirst();

        statusFilter.setOnAction(e -> applyFilters());

        // Setup search
        searchField.textProperty().addListener((obs, oldText, newText) -> applyFilters());
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase().trim();
        String status = statusFilter.getValue();

        filteredDeliveries.setPredicate(delivery -> {
            boolean matchesSearch = searchText.isEmpty() ||
                    String.valueOf(delivery.getCommande().getId()).contains(searchText) ||
                    delivery.getClientNom().toLowerCase().contains(searchText) ||
                    delivery.getAdresseLivraison().toLowerCase().contains(searchText) ||
                    delivery.getCommande().getDeliveryCity().toLowerCase().contains(searchText);

            boolean matchesStatus = "All".equals(status) || status.equals(delivery.getStatut());

            return matchesSearch && matchesStatus;
        });

        updateDeliveryCount();
    }

    private void loadMyDeliveries() {
        myDeliveries.clear();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT l.id, l.commande_id, l.adresse_livraison, l.statut, l.date_livraison, 
                       l.estimated_delivery_date, l.actual_delivery_date,
                       c.date as order_date, c.delivery_city, c.delivery_fee, c.statutLivraison,
                       cl.id as client_id, cl.nom as client_name, cl.adresse, cl.email, cl.telephone
                FROM livraison l
                JOIN commandes c ON l.commande_id = c.id
                JOIN clients cl ON c.client_id = cl.id
                WHERE l.delivery_guy_id = ?
                ORDER BY 
                    CASE l.statut 
                        WHEN 'En cours' THEN 1
                        WHEN 'Assignée' THEN 2
                        WHEN 'Livrée' THEN 3
                        WHEN 'Confirmée' THEN 4
                        WHEN 'Annulée' THEN 5
                        ELSE 6
                    END,
                    l.estimated_delivery_date ASC
                """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deliveryGuy.getId());
            ResultSet rs = stmt.executeQuery();

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
                        rs.getInt("commande_id"),
                        client,
                        null,
                        rs.getDate("order_date").toLocalDate(),
                        rs.getString("statutLivraison"), // Use order status from commandes table
                        null
                );

                commande.setDeliveryCity(rs.getString("delivery_city"));
                commande.setDeliveryFee(rs.getDouble("delivery_fee"));

                LocalDate estimatedDate = null;
                if (rs.getDate("estimated_delivery_date") != null) {
                    estimatedDate = rs.getDate("estimated_delivery_date").toLocalDate();
                }

                LocalDate actualDate = null;
                if (rs.getDate("actual_delivery_date") != null) {
                    actualDate = rs.getDate("actual_delivery_date").toLocalDate();
                }

                Livraison livraison = new Livraison(
                        rs.getInt("id"),
                        commande,
                        rs.getString("adresse_livraison"),
                        rs.getString("statut"), // Use delivery status from livraison table
                        rs.getDate("date_livraison").toLocalDate()
                );

                livraison.setClientNom(rs.getString("client_name"));
                livraison.setEstimatedDeliveryDate(estimatedDate);
                livraison.setActualDeliveryDate(actualDate);

                myDeliveries.add(livraison);
            }

            deliveriesTable.setItems(filteredDeliveries);
            updateDeliveryCount();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading my deliveries", e);
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load your deliveries.");
        }
    }

    private void setupTable() {
        orderIdCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getCommande().getId()));
        clientCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getClientNom()));
        addressCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAdresseLivraison()));
        cityCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCommande().getDeliveryCity()));

        // Show delivery status with color coding
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatut()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    // Color code the status
                    switch (status) {
                        case "Assignée" -> setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
                        case "En cours" -> setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
                        case "Livrée" -> setStyle("-fx-text-fill: #9C27B0; -fx-font-weight: bold;");
                        case "Confirmée" -> setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                        case "Annulée" -> setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
                        default -> setStyle("");
                    }
                }
            }
        });

        estimatedDateCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getEstimatedDeliveryDate()));
        estimatedDateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DATE_FORMATTER));
                }
            }
        });

        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button startBtn = new Button("🚚 Start Delivery");
            private final Button deliverBtn = new Button("📦 Mark as Delivered");
            private final Button viewBtn = new Button("👁️ View Details");
            private final Label waitingLabel = new Label("⏳ Waiting for client confirmation");
            private final Label completedLabel = new Label("✅ Completed & Paid");
            private final HBox buttonBox = new HBox(5);

            {
                startBtn.getStyleClass().add("manager-button");
                deliverBtn.getStyleClass().add("action-button");
                viewBtn.getStyleClass().add("view-button");
                waitingLabel.setStyle("-fx-text-fill: #9C27B0; -fx-font-weight: bold;");
                completedLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");

                startBtn.setOnAction(e -> {
                    Livraison delivery = getTableView().getItems().get(getIndex());
                    startDelivery(delivery);
                });

                deliverBtn.setOnAction(e -> {
                    Livraison delivery = getTableView().getItems().get(getIndex());
                    markAsDelivered(delivery);
                });

                viewBtn.setOnAction(e -> {
                    Livraison delivery = getTableView().getItems().get(getIndex());
                    viewDeliveryDetails(delivery);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    buttonBox.getChildren().clear();

                    Livraison delivery = getTableView().getItems().get(getIndex());
                    String status = delivery.getStatut();

                    switch (status) {
                        case "Assignée" -> buttonBox.getChildren().addAll(startBtn, viewBtn);
                        case "En cours" -> buttonBox.getChildren().addAll(deliverBtn, viewBtn);
                        case "Livrée" -> buttonBox.getChildren().addAll(waitingLabel, viewBtn);
                        case "Confirmée" -> buttonBox.getChildren().addAll(completedLabel, viewBtn);
                        default -> buttonBox.getChildren().add(viewBtn);
                    }

                    setGraphic(buttonBox);
                }
            }
        });
    }

    private void startDelivery(Livraison delivery) {
        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Start Delivery");
        confirmAlert.setHeaderText("Start Delivery Process");
        confirmAlert.setContentText(String.format(
                "Are you sure you want to start delivery for Order #%d?\n\n" +
                        "Client: %s\n" +
                        "Address: %s\n" +
                        "City: %s\n\n" +
                        "This will change the status to 'En cours' (In Progress).",
                delivery.getCommande().getId(),
                delivery.getClientNom(),
                delivery.getAdresseLivraison(),
                delivery.getCommande().getDeliveryCity()
        ));

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Update delivery status to 'En cours'
            stmt = conn.prepareStatement(
                    "UPDATE livraison SET statut = 'En cours' WHERE id = ? AND delivery_guy_id = ?");
            stmt.setInt(1, delivery.getId());
            stmt.setInt(2, deliveryGuy.getId());
            int updated = stmt.executeUpdate();

            if (updated > 0) {
                // Update order status to 'En cours'
                PreparedStatement orderStmt = conn.prepareStatement(
                        "UPDATE commandes SET statutLivraison = 'En cours' WHERE id = ?");
                orderStmt.setInt(1, delivery.getCommande().getId());
                orderStmt.executeUpdate();

                conn.commit();

                // Show success message with clear status explanation
                showAlert(Alert.AlertType.INFORMATION, "Delivery Started",
                        String.format("✅ Delivery started successfully!\n\n" +
                                        "Order #%d status changed to: 'En cours' (In Progress)\n\n" +
                                        "Next step: When you deliver the package, click 'Mark as Delivered' " +
                                        "to notify the client.",
                                delivery.getCommande().getId()));

                loadMyDeliveries(); // Refresh the table
            } else {
                conn.rollback();
                showAlert(Alert.AlertType.WARNING, "Update Failed",
                        "Could not start delivery. The order may have been modified by another user.");
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
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to start delivery: " + e.getMessage());
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

    private void markAsDelivered(Livraison delivery) {
        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Mark as Delivered");
        confirmAlert.setHeaderText("Package Delivered");
        confirmAlert.setContentText(String.format(
                "Are you sure you delivered the package for Order #%d?\n\n" +
                        "Client: %s\n" +
                        "Address: %s\n\n" +
                        "This will:\n" +
                        "• Change status to 'Livrée' (Delivered)\n" +
                        "• Notify the client to confirm receipt\n" +
                        "• You will receive %.2f DH after client confirmation",
                delivery.getCommande().getId(),
                delivery.getClientNom(),
                delivery.getAdresseLivraison(),
                delivery.getCommande().getDeliveryFee()
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

            // Update delivery status to 'Livrée' (waiting for client confirmation)
            stmt = conn.prepareStatement(
                    "UPDATE livraison SET statut = 'Livrée', actual_delivery_date = ? WHERE id = ? AND delivery_guy_id = ?");
            stmt.setDate(1, Date.valueOf(today));
            stmt.setInt(2, delivery.getId());
            stmt.setInt(3, deliveryGuy.getId());
            int updated = stmt.executeUpdate();

            if (updated > 0) {
                // Update order status to 'Livrée'
                PreparedStatement orderStmt = conn.prepareStatement(
                        "UPDATE commandes SET statutLivraison = 'Livrée' WHERE id = ?");
                orderStmt.setInt(1, delivery.getCommande().getId());
                orderStmt.executeUpdate();

                conn.commit();

                // Show success message
                showAlert(Alert.AlertType.INFORMATION, "Package Delivered",
                        String.format("📦 Package marked as delivered!\n\n" +
                                        "Order #%d status: 'Livrée' (Delivered)\n" +
                                        "Delivery date: %s\n\n" +
                                        "⏳ Waiting for client confirmation...\n" +
                                        "You will receive %.2f DH once the client confirms receipt.",
                                delivery.getCommande().getId(),
                                today.format(DATE_FORMATTER),
                                delivery.getCommande().getDeliveryFee()));

                loadMyDeliveries(); // Refresh the table
            } else {
                conn.rollback();
                showAlert(Alert.AlertType.WARNING, "Update Failed",
                        "Could not mark as delivered. The delivery may have been modified by another user.");
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
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to mark as delivered: " + e.getMessage());
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

    private void viewDeliveryDetails(Livraison delivery) {
        StringBuilder details = new StringBuilder();
        details.append("📦 DELIVERY DETAILS\n");
        details.append("═══════════════════════════════════\n\n");

        details.append("Order Information:\n");
        details.append("• Order ID: #").append(delivery.getCommande().getId()).append("\n");
        details.append("• Order Date: ").append(delivery.getCommande().getDate().format(DATE_FORMATTER)).append("\n");
        details.append("• Current Status: ").append(delivery.getStatut()).append("\n\n");

        details.append("Client Information:\n");
        details.append("• Name: ").append(delivery.getClientNom()).append("\n");
        details.append("• Address: ").append(delivery.getAdresseLivraison()).append("\n");
        details.append("• City: ").append(delivery.getCommande().getDeliveryCity()).append("\n\n");

        details.append("Delivery Information:\n");
        if (delivery.getEstimatedDeliveryDate() != null) {
            details.append("• Estimated Delivery: ").append(delivery.getEstimatedDeliveryDate().format(DATE_FORMATTER)).append("\n");
        }
        if (delivery.getActualDeliveryDate() != null) {
            details.append("• Actual Delivery: ").append(delivery.getActualDeliveryDate().format(DATE_FORMATTER)).append("\n");
        }
        details.append("• Delivery Fee: ").append(String.format("%.2f DH", delivery.getCommande().getDeliveryFee())).append("\n\n");

        details.append("Status Explanation:\n");
        details.append("• Assignée: Order assigned to you\n");
        details.append("• En cours: Delivery in progress\n");
        details.append("• Livrée: Package delivered, waiting for client confirmation\n");
        details.append("• Confirmée: Client confirmed receipt, payment processed\n");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Delivery Details");
        alert.setHeaderText("Order #" + delivery.getCommande().getId());
        alert.setContentText(details.toString());
        alert.showAndWait();
    }

    @FXML
    private void handleRefresh() {
        loadMyDeliveries();
    }

    private void updateDeliveryCount() {
        int count = filteredDeliveries.size();
        deliveryCountLabel.setText(count + (count == 1 ? " delivery" : " deliveries"));
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
