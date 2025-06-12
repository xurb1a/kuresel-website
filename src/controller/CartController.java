package controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import model.*;
import util.DatabaseConnection;
import util.CityLoader;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CartController implements DashboardController.UserAware {
    private static final Logger LOGGER = Logger.getLogger(CartController.class.getName());

    @FXML private TableView<LigneCommande> cartTable;
    @FXML private TableColumn<LigneCommande, String> nameCol;
    @FXML private TableColumn<LigneCommande, Double> priceCol;
    @FXML private TableColumn<LigneCommande, Integer> quantityCol;
    @FXML private TableColumn<LigneCommande, Double> totalCol;
    @FXML private TableColumn<LigneCommande, Void> actionCol;

    @FXML private Label totalAmountLabel;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private ComboBox<City> cityComboBox;
    @FXML private Label deliveryFeeLabel;
    @FXML private Label estimatedDeliveryLabel;
    @FXML private Label subtotalLabel;

    private final ObservableList<LigneCommande> cartItems = FXCollections.observableArrayList();
    private Client client;
    private DashboardController dashboardController;

    @Override
    public void setUser(Client client) {
        this.client = client;
        initialize();
    }

    public void setDashboardController(DashboardController controller) {
        this.dashboardController = controller;
    }

    @FXML
    public void initialize() {
        if (client != null) {
            loadCities();
            loadCartFromDatabase();
            setupTable();
        }
    }

    private void loadCities() {
        try {
            List<City> cities = CityLoader.loadCities();
            cityComboBox.setItems(FXCollections.observableArrayList(cities));

            // Set default city (Khouribga)
            City defaultCity = cities.stream()
                    .filter(city -> "Khouribga".equals(city.getName()))
                    .findFirst()
                    .orElse(cities.get(0));

            cityComboBox.setValue(defaultCity);
            cityComboBox.setConverter(new javafx.util.StringConverter<>() {
                @Override
                public String toString(City city) {
                    return city == null ? "" : city.getName() + " (" + city.getDeliveryFee() + " DH)";
                }

                @Override
                public City fromString(String string) {
                    return null; // Not needed for ComboBox
                }
            });

            // Add listener for city selection changes
            cityComboBox.setOnAction(e -> updateDeliveryInfo());
            updateDeliveryInfo();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading cities", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load city data. Using default values.");
        }
    }

    private void updateDeliveryInfo() {
        City selectedCity = cityComboBox.getValue();
        if (selectedCity != null) {
            deliveryFeeLabel.setText(String.format("%.1f DH", selectedCity.getDeliveryFee()));
            estimatedDeliveryLabel.setText(selectedCity.getDeliveryDays() + " days");
            updateTotal();
        }
    }

    private void loadCartFromDatabase() {
        cartItems.clear();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(
                    "SELECT c.id, p.id AS produit_id, p.nom, p.prix, p.stock, p.image_url, p.description, c.quantite " +
                            "FROM cart c JOIN produits p ON c.produit_id = p.id WHERE c.client_id = ?");
            stmt.setInt(1, client.getId());
            rs = stmt.executeQuery();

            while (rs.next()) {
                Produit produit = new Produit(
                        rs.getInt("produit_id"),
                        rs.getString("nom"),
                        rs.getDouble("prix"),
                        rs.getInt("stock"),
                        rs.getString("image_url"),
                        rs.getString("description")
                );
                LigneCommande ligne = new LigneCommande(
                        rs.getInt("id"),
                        produit,
                        rs.getInt("quantite")
                );
                cartItems.add(ligne);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading cart items", e);
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load cart items.");
        } finally {
            DatabaseConnection.closeResources(rs, stmt);
        }

        updateTotal();
    }

    private void setupTable() {
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProduit().getNom()));
        priceCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getProduit().getPrix()));
        quantityCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getQuantite()));
        totalCol.setCellValueFactory(c -> new SimpleObjectProperty<>(
                c.getValue().getProduit().getPrix() * c.getValue().getQuantite()
        ));

        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button removeBtn = new Button("🗑️");

            {
                removeBtn.getStyleClass().add("remove-button");
                removeBtn.setOnAction(e -> {
                    LigneCommande ligne = getTableView().getItems().get(getIndex());
                    removeFromCart(ligne);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeBtn);
            }
        });

        cartTable.setPlaceholder(new Label("🛒 Your cart is empty"));
        cartTable.setItems(cartItems);
    }

    private void updateTotal() {
        double subtotal = cartItems.stream()
                .mapToDouble(item -> item.getProduit().getPrix() * item.getQuantite())
                .sum();

        double deliveryFee = 0.0;
        if (cityComboBox.getValue() != null) {
            deliveryFee = cityComboBox.getValue().getDeliveryFee();
        }

        double total = subtotal + deliveryFee;

        if (subtotalLabel != null) {
            subtotalLabel.setText(String.format("%.1f DH", subtotal));
        }
        totalAmountLabel.setText(String.format("%.1f DH", total));
    }

    private void removeFromCart(LigneCommande ligne) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement("DELETE FROM cart WHERE id = ?");
            stmt.setInt(1, ligne.getId());
            stmt.executeUpdate();
            cartItems.remove(ligne);
            updateTotal();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error removing item from cart", e);
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to remove item from cart.");
        } finally {
            DatabaseConnection.closeResources(null, stmt);
        }
    }

    @FXML
    private void handleCheckout() {
        if (cartItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cart is empty", "Please add products to your cart before checkout.");
            return;
        }

        String selectedPayment = paymentMethodCombo.getValue();
        if (selectedPayment == null || selectedPayment.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Payment Method Required", "Please select a payment method.");
            return;
        }

        City selectedCity = cityComboBox.getValue();
        if (selectedCity == null) {
            showAlert(Alert.AlertType.WARNING, "City Required", "Please select your delivery city.");
            return;
        }

        double subtotal = cartItems.stream()
                .mapToDouble(item -> item.getProduit().getPrix() * item.getQuantite())
                .sum();
        double deliveryFee = selectedCity.getDeliveryFee();
        double totalAmount = subtotal + deliveryFee;

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Insert Commande with delivery info
            PreparedStatement cmdStmt = conn.prepareStatement(
                    "INSERT INTO commandes (client_id, date, statutLivraison, delivery_city, delivery_fee) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            cmdStmt.setInt(1, client.getId());
            cmdStmt.setDate(2, Date.valueOf(LocalDate.now()));
            cmdStmt.setString(3, "En attente");
            cmdStmt.setString(4, selectedCity.getName());
            cmdStmt.setDouble(5, deliveryFee);
            cmdStmt.executeUpdate();

            ResultSet rs = cmdStmt.getGeneratedKeys();
            int commandeId = 0;
            if (rs.next()) {
                commandeId = rs.getInt(1);
            } else {
                throw new SQLException("Failed to get generated commande ID");
            }

            // Insert LigneCommande
            PreparedStatement ligneStmt = conn.prepareStatement(
                    "INSERT INTO ligne_commande (commande_id, produit_id, quantite, prix_unitaire) VALUES (?, ?, ?, ?)"
            );
            for (LigneCommande item : cartItems) {
                ligneStmt.setInt(1, commandeId);
                ligneStmt.setInt(2, item.getProduit().getId());
                ligneStmt.setInt(3, item.getQuantite());
                ligneStmt.setDouble(4, item.getProduit().getPrix());
                ligneStmt.addBatch();

                // Update product stock
                PreparedStatement stockStmt = conn.prepareStatement(
                        "UPDATE produits SET stock = stock - ? WHERE id = ? AND stock >= ?"
                );
                stockStmt.setInt(1, item.getQuantite());
                stockStmt.setInt(2, item.getProduit().getId());
                stockStmt.setInt(3, item.getQuantite());
                int updated = stockStmt.executeUpdate();
                if (updated == 0) {
                    throw new SQLException("Insufficient stock for product: " + item.getProduit().getNom());
                }
            }
            ligneStmt.executeBatch();

            // Insert Paiement
            PreparedStatement payStmt = conn.prepareStatement(
                    "INSERT INTO paiements (commande_id, montant, date_paiement, methode, statut) VALUES (?, ?, ?, ?, ?)"
            );
            payStmt.setInt(1, commandeId);
            payStmt.setDouble(2, totalAmount);
            payStmt.setDate(3, Date.valueOf(LocalDate.now()));
            payStmt.setString(4, selectedPayment);
            payStmt.setString(5, "En attente");
            payStmt.executeUpdate();

            // Create initial delivery record
            PreparedStatement deliveryStmt = conn.prepareStatement(
                    "INSERT INTO livraison (commande_id, adresse_livraison, statut, date_livraison, estimated_delivery_date) VALUES (?, ?, ?, ?, ?)"
            );
            deliveryStmt.setInt(1, commandeId);
            deliveryStmt.setString(2, client.getAdresse() + ", " + selectedCity.getName());
            deliveryStmt.setString(3, "En attente");
            deliveryStmt.setDate(4, Date.valueOf(LocalDate.now()));
            deliveryStmt.setDate(5, Date.valueOf(LocalDate.now().plusDays(selectedCity.getDeliveryDays())));
            deliveryStmt.executeUpdate();

            // Clear cart from DB
            PreparedStatement clearStmt = conn.prepareStatement("DELETE FROM cart WHERE client_id = ?");
            clearStmt.setInt(1, client.getId());
            clearStmt.executeUpdate();

            conn.commit();

            showAlert(Alert.AlertType.INFORMATION, "Checkout Successful",
                    String.format("Your order has been placed!\nSubtotal: %.1f DH\nDelivery: %.1f DH\nTotal: %.1f DH\nEstimated delivery: %d days",
                            subtotal, deliveryFee, totalAmount, selectedCity.getDeliveryDays()));

            cartItems.clear();
            updateTotal();
            paymentMethodCombo.getSelectionModel().clearSelection();

            // Navigate to orders page
            if (dashboardController != null) {
                dashboardController.loadPage("orders.fxml");
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error during checkout", e);
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error rolling back transaction", ex);
            }

            if (e.getMessage().contains("Insufficient stock")) {
                showAlert(Alert.AlertType.ERROR, "Checkout Error", e.getMessage());
            } else {
                showAlert(Alert.AlertType.ERROR, "Checkout Error", "Failed to complete checkout. Please try again.");
            }
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error resetting auto-commit", e);
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type, content, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }
}
