package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Commande;
import model.Paiement;
import util.DatabaseConnection;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EditOrderController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(EditOrderController.class.getName());

    @FXML private Label orderIdLabel;
    @FXML private Label clientLabel;
    @FXML private Label dateLabel;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Label paymentMethodLabel;
    @FXML private Label paymentAmountLabel;
    @FXML private Label estimatedDeliveryLabel;
    @FXML private Label deliveryCityLabel;

    private Commande order;
    private OrdersController parentController;

    public void setCommande(Commande order) {
        this.order = order;

        orderIdLabel.setText(String.valueOf(order.getId()));
        clientLabel.setText(order.getClient().getNom());
        dateLabel.setText(order.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        statusComboBox.setValue(order.getStatutLivraison());

        // Display delivery information
        if (order.getDeliveryCity() != null) {
            deliveryCityLabel.setText(order.getDeliveryCity());
        } else {
            deliveryCityLabel.setText("N/A");
        }

        if (order.getEstimatedDeliveryDate() != null) {
            estimatedDeliveryLabel.setText(order.getEstimatedDeliveryDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } else {
            estimatedDeliveryLabel.setText("Not scheduled");
        }

        Paiement payment = order.getPaiement();
        if (payment != null) {
            paymentMethodLabel.setText(payment.getMethode());
            paymentAmountLabel.setText(String.format("%.2f MAD", payment.getMontant()));
        } else {
            paymentMethodLabel.setText("N/A");
            paymentAmountLabel.setText("0.00 MAD");
        }
    }

    public void setParentController(OrdersController controller) {
        this.parentController = controller;
    }

    @FXML
    private void handleSave() {
        String newStatus = statusComboBox.getValue();
        order.setStatutLivraison(newStatus);

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement("UPDATE commandes SET statutLivraison = ? WHERE id = ?");
            stmt.setString(1, newStatus);
            stmt.setInt(2, order.getId());
            stmt.executeUpdate();

            // Also update the status in livraison table to keep consistency
            PreparedStatement livraisonStmt = conn.prepareStatement(
                    "UPDATE livraison SET statut = ? WHERE commande_id = ?");
            livraisonStmt.setString(1, newStatus);
            livraisonStmt.setInt(2, order.getId());
            livraisonStmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Order updated successfully.");

            if (parentController != null) {
                parentController.refreshCurrentPage();
            }

            ((Stage) statusComboBox.getScene().getWindow()).close();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating order", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Could not update the order: " + e.getMessage());
        } finally {
            DatabaseConnection.closeResources(null, stmt);
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) statusComboBox.getScene().getWindow()).close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        statusComboBox.getItems().addAll("En attente", "Confirmée", "En cours", "Livrée", "Annulée");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
