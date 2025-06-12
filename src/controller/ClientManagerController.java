package controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import model.Client;
import util.DatabaseConnection;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientManagerController {
    private static final Logger LOGGER = Logger.getLogger(ClientManagerController.class.getName());

    @FXML private TextField searchField;
    @FXML private TableView<Client> clientTable;
    @FXML private TableColumn<Client, Integer> idCol;
    @FXML private TableColumn<Client, String> nameCol;
    @FXML private TableColumn<Client, String> emailCol;
    @FXML private TableColumn<Client, String> addressCol;
    @FXML private TableColumn<Client, String> phoneCol;
    @FXML private TableColumn<Client, String> typeCol;
    @FXML private TableColumn<Client, Void> actionCol;

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> typeComboBox;

    private final ObservableList<Client> clients = FXCollections.observableArrayList();
    private final FilteredList<Client> filteredClients = new FilteredList<>(clients, p -> true);

    public void initialize() {
        // Add delivery guy option to the type combo box
        typeComboBox.getItems().clear();
        typeComboBox.getItems().addAll("Client", "Admin", "Delivery Guy");

        loadClients();
        setupSearch();
        setupColumns();
        addActionButtons();

        clientTable.setPlaceholder(new Label("📭 No clients found"));
    }

    private void loadClients() {
        clients.clear();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM clients");

            while (rs.next()) {
                clients.add(new Client(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("email"),
                        rs.getString("adresse"),
                        rs.getString("telephone"),
                        rs.getString("motdepasse"),
                        rs.getInt("type")
                ));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading clients", e);
            showAlert("Database Error", "Failed to load clients: " + e.getMessage());
        } finally {
            DatabaseConnection.closeResources(rs, stmt);
        }

        clientTable.setItems(filteredClients);
    }

    private void setupColumns() {
        idCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNom()));
        emailCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        addressCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAdresse()));
        phoneCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTelephone()));
        typeCol.setCellValueFactory(c -> {
            int userType = c.getValue().getUserType();
            String typeText;
            switch (userType) {
                case 1:
                    typeText = "Client";
                    break;
                case 2:
                    typeText = "Admin";
                    break;
                case 3:
                    typeText = "Delivery Guy";
                    break;
                default:
                    typeText = "Unknown";
            }
            return new SimpleStringProperty(typeText);
        });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String lower = newVal.toLowerCase().trim();
            filteredClients.setPredicate(client ->
                    client.getNom().toLowerCase().contains(lower) ||
                            client.getEmail().toLowerCase().contains(lower)
            );
        });
    }

    private void addActionButtons() {
        actionCol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Client, Void> call(TableColumn<Client, Void> param) {
                return new TableCell<>() {
                    final Button editBtn = new Button("✏️");
                    final Button delBtn = new Button("🗑️");
                    final HBox box = new HBox(8, editBtn, delBtn);

                    {
                        editBtn.getStyleClass().add("manager-button");
                        delBtn.getStyleClass().addAll("manager-button", "secondary");

                        editBtn.setOnAction(e -> editClient(getTableView().getItems().get(getIndex())));
                        delBtn.setOnAction(e -> deleteClient(getTableView().getItems().get(getIndex()).getId()));
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : box);
                    }
                };
            }
        });
    }

    private void editClient(Client client) {
        // Populate fields with client data for editing
        nameField.setText(client.getNom());
        emailField.setText(client.getEmail());
        addressField.setText(client.getAdresse());
        phoneField.setText(client.getTelephone());

        // Set the correct user type in the combo box
        switch (client.getUserType()) {
            case 1:
                typeComboBox.setValue("Client");
                break;
            case 2:
                typeComboBox.setValue("Admin");
                break;
            case 3:
                typeComboBox.setValue("Delivery Guy");
                break;
            default:
                typeComboBox.setValue("Client");
        }

        // Create a temporary button for updating
        Button updateBtn = new Button("Update Client");
        updateBtn.getStyleClass().add("manager-button");
        updateBtn.setOnAction(e -> {
            updateClient(client.getId());
            updateBtn.getParent().getChildrenUnmodifiable().remove(updateBtn);
        });

        // Add the update button to the scene
        HBox parent = (HBox) nameField.getParent();
        if (!parent.getChildren().contains(updateBtn)) {
            parent.getChildren().add(updateBtn);
        }
    }

    private void updateClient(int clientId) {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();
        String type = typeComboBox.getValue();

        if (name.isEmpty() || email.isEmpty()) {
            showAlert("Validation Error", "Name and email are required fields.");
            return;
        }

        int userType;
        switch (type) {
            case "Admin":
                userType = 2;
                break;
            case "Delivery Guy":
                userType = 3;
                break;
            default:
                userType = 1; // Default to Client
        }

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(
                    "UPDATE clients SET nom = ?, email = ?, adresse = ?, telephone = ?, type = ? WHERE id = ?");
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, address);
            ps.setString(4, phone);
            ps.setInt(5, userType);
            ps.setInt(6, clientId);

            int result = ps.executeUpdate();
            if (result > 0) {
                showAlert("Success", "Client updated successfully.");
                clearFields();
                loadClients();
            } else {
                showAlert("Error", "Failed to update client.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating client", e);
            showAlert("Database Error", "Could not update client: " + e.getMessage());
        } finally {
            DatabaseConnection.closeResources(null, ps);
        }
    }

    @FXML
    private void handleAdd() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();
        String type = typeComboBox.getValue();

        if (name.isEmpty() || email.isEmpty()) {
            showAlert("Validation Error", "Name and email are required fields.");
            return;
        }

        int userType;
        switch (type) {
            case "Admin":
                userType = 2;
                break;
            case "Delivery Guy":
                userType = 3;
                break;
            default:
                userType = 1; // Default to Client
        }

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(
                    "INSERT INTO clients (nom, email, adresse, telephone, type, motdepasse) VALUES (?, ?, ?, ?, ?, 'password')");
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, address);
            ps.setString(4, phone);
            ps.setInt(5, userType);
            ps.executeUpdate();

            // If adding a delivery guy, create an entry in delivery_stats
            if (userType == 3) {
                // Get the newly created client ID
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    int newClientId = rs.getInt(1);

                    // Create delivery stats entry
                    PreparedStatement statsPs = conn.prepareStatement(
                            "INSERT INTO delivery_stats (delivery_guy_id, total_deliveries, total_earnings, successful_deliveries) VALUES (?, 0, 0.0, 0)");
                    statsPs.setInt(1, newClientId);
                    statsPs.executeUpdate();
                }
            }

            clearFields();
            loadClients();
            showAlert("Success", "Client added successfully.");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding client", e);
            showAlert("Database Error", "Could not add client: " + e.getMessage());
        } finally {
            DatabaseConnection.closeResources(null, ps);
        }
    }

    private void deleteClient(int id) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DatabaseConnection.getConnection();

            // Check if client is a delivery guy
            PreparedStatement checkPs = conn.prepareStatement("SELECT type FROM clients WHERE id = ?");
            checkPs.setInt(1, id);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next() && rs.getInt("type") == 3) {
                // Delete from delivery_stats first
                PreparedStatement statsPs = conn.prepareStatement("DELETE FROM delivery_stats WHERE delivery_guy_id = ?");
                statsPs.setInt(1, id);
                statsPs.executeUpdate();

                // Delete from delivery_earnings
                PreparedStatement earningsPs = conn.prepareStatement("DELETE FROM delivery_earnings WHERE delivery_guy_id = ?");
                earningsPs.setInt(1, id);
                earningsPs.executeUpdate();

                // Update livraison records to remove this delivery guy
                PreparedStatement livraisonPs = conn.prepareStatement("UPDATE livraison SET delivery_guy_id = NULL WHERE delivery_guy_id = ?");
                livraisonPs.setInt(1, id);
                livraisonPs.executeUpdate();
            }

            // Now delete the client
            ps = conn.prepareStatement("DELETE FROM clients WHERE id = ?");
            ps.setInt(1, id);
            ps.executeUpdate();

            loadClients();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting client", e);
            showAlert("Database Error", "Could not delete client: " + e.getMessage());
        } finally {
            DatabaseConnection.closeResources(null, ps);
        }
    }

    @FXML
    private void handleDelete() {
        Client selected = clientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select a client to delete.");
            return;
        }

        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Client");
        confirmAlert.setContentText("Are you sure you want to delete " + selected.getNom() + "?");

        confirmAlert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                deleteClient(selected.getId());
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadClients();
    }

    private void clearFields() {
        nameField.clear();
        emailField.clear();
        addressField.clear();
        phoneField.clear();
        typeComboBox.setValue("Client");
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
