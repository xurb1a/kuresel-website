package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Produit;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EditProductController {

    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextField stockField;
    @FXML private TextField descriptionField;

    private Produit produit;
    private ProductManagerController productManagerController;

    public void setProduit(Produit produit) {
        this.produit = produit;
        nameField.setText(produit.getNom());
        priceField.setText(String.valueOf(produit.getPrix()));
        stockField.setText(String.valueOf(produit.getStock()));
        descriptionField.setText(produit.getDescription());
    }

    public void setProductManagerController(ProductManagerController controller) {
        this.productManagerController = controller;
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();
        String priceStr = priceField.getText().trim();
        String stockStr = stockField.getText().trim();
        String description = descriptionField.getText().trim();

        if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
            showAlert("Validation Error", "Please fill all required fields.");
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            int stock = Integer.parseInt(stockStr);

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE produits SET nom = ?, prix = ?, stock = ?, description = ? WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, name);
                ps.setDouble(2, price);
                ps.setInt(3, stock);
                ps.setString(4, description);
                ps.setInt(5, produit.getId());
                ps.executeUpdate();
            }

            showAlert("Success", "Product updated successfully.");
            if (productManagerController != null) {
                productManagerController.refreshProducts();
            }
            closeWindow();

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Price and stock must be numeric values.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Could not update product.");
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
