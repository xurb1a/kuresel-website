package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Produit;
import util.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URL;

public class ProductManagerController {
    private static final Logger LOGGER = Logger.getLogger(ProductManagerController.class.getName());

    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextField stockField;
    @FXML private TextField descriptionField;
    @FXML private TextField searchField;
    @FXML private FlowPane productFlow;

    private String selectedImagePath = "";
    private final ObservableList<Produit> productList = FXCollections.observableArrayList();

    private static final String IMAGE_DIR = "product_images/";
    private static final String DEFAULT_IMAGE = "/images/default-product.png";
    private static final int IMAGE_WIDTH = 200;
    private static final int IMAGE_HEIGHT = 150;

    @FXML
    public void initialize() {
        loadProducts();
        setupSearch();
    }

    private void loadProducts() {
        productList.clear();
        productFlow.getChildren().clear();

        String query = "SELECT id, nom, prix, stock, image_url, description FROM produits";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Produit produit = new Produit(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getDouble("prix"),
                        rs.getInt("stock"),
                        rs.getString("image_url"),
                        rs.getString("description")
                );
                productList.add(produit);
                addProductCard(produit);
            }

        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load products: " + e.getMessage());
        }
    }

    private void addProductCard(Produit produit) {
        VBox card = new VBox();
        card.getStyleClass().add("product-card");
        card.setPrefWidth(250);
        card.setMaxWidth(250);
        card.setSpacing(10);

        // Product Image Container
        VBox imageContainer = new VBox();
        imageContainer.getStyleClass().add("product-image-container");
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setPrefHeight(180);

        ImageView imageView = new ImageView();
        imageView.getStyleClass().add("product-image");
        loadProductImage(imageView, produit);
        imageView.setFitWidth(IMAGE_WIDTH);
        imageView.setFitHeight(IMAGE_HEIGHT);
        imageView.setPreserveRatio(true);

        imageContainer.getChildren().add(imageView);

        // Product Details
        Label nameLabel = new Label(produit.getNom());
        nameLabel.getStyleClass().add("product-name");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(230);

        Label descLabel = new Label(produit.getDescription() != null ? produit.getDescription() : "No description");
        descLabel.getStyleClass().add("product-description");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(230);
        descLabel.setMaxHeight(60);

        Label stockLabel = new Label("Stock: " + produit.getStock());
        stockLabel.getStyleClass().add(produit.getStock() > 0 ? "in-stock" : "out-of-stock");

        Label priceLabel = new Label(String.format("%.2f DH", produit.getPrix()));
        priceLabel.getStyleClass().add("product-price");

        // Action Buttons
        Button viewBtn = new Button("View");
        viewBtn.getStyleClass().add("view-button");
        viewBtn.setOnAction(e -> showDescription(produit.getDescription()));

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("edit-button");
        editBtn.setOnAction(e -> openEditProduct(produit));

        Button delBtn = new Button("Delete");
        delBtn.getStyleClass().add("delete-button");
        delBtn.setOnAction(e -> deleteProduct(produit));

        HBox actionBox = new HBox();
        actionBox.getStyleClass().add("action-box");
        actionBox.setSpacing(10);
        actionBox.setAlignment(Pos.CENTER);
        actionBox.getChildren().addAll(viewBtn, editBtn, delBtn);

        // Add all elements to card
        card.getChildren().addAll(imageContainer, nameLabel, descLabel, stockLabel, priceLabel, actionBox);
        productFlow.getChildren().add(card);
    }

    private void showDescription(String description) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Product Description");
        alert.setHeaderText(null);
        alert.setContentText(description != null ? description : "No description available");

        // Try to apply styling, but don't fail if CSS is missing
        try {
            URL cssUrl = getClass().getResource("/view/styles.css");
            if (cssUrl != null) {
                alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not load CSS for alert dialog", e);
        }

        alert.showAndWait();
    }

    private void loadProductImage(ImageView imageView, Produit produit) {
        String imagePath = produit.getImageUrl();
        try {
            if (imagePath == null || imagePath.trim().isEmpty()) {
                imageView.setImage(loadDefaultImage());
                return;
            }

            // Force filesystem-based loading
            File file = new File(imagePath);
            if (!file.exists()) {
                // If relative, try loading from project directory
                file = new File(System.getProperty("user.dir") + File.separator + imagePath);
            }

            if (file.exists()) {
                imageView.setImage(new Image(file.toURI().toString()));
                return;
            }

            // Fallback to default
            imageView.setImage(loadDefaultImage());

        } catch (Exception e) {
            System.err.println("Error loading image for product: " + produit.getNom());
            e.printStackTrace();
            imageView.setImage(loadDefaultImage());
        }
    }



    private Image loadDefaultImage() {
        try {
            return new Image(getClass().getResource(DEFAULT_IMAGE).toExternalForm());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Default image missing: " + DEFAULT_IMAGE, e);
            return null;
        }
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selected = fileChooser.showOpenDialog(new Stage());
        if (selected == null) return;

        try {
            // Save to working directory under 'product_images'
            Path targetDir = Paths.get(System.getProperty("user.dir"), IMAGE_DIR);
            if (!Files.exists(targetDir)) Files.createDirectories(targetDir);

            String filename = selected.getName();
            Path targetPath = targetDir.resolve(filename);
            Files.copy(selected.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Save just the relative path to DB
            selectedImagePath = IMAGE_DIR + filename;
            showAlert("Success", "Image selected: " + filename);

        } catch (IOException e) {
            showAlert("Image Error", "Failed to save image: " + e.getMessage());
        }
    }


    @FXML
    private void handleAddProduct() {
        if (!validateInputs()) return;

        try {
            Produit p = createProductFromInputs();
            saveProductToDatabase(p);
            resetForm();
            loadProducts();
            showAlert("Success", "Product added successfully!");

        } catch (NumberFormatException e) {
            showAlert("Format Error", "Price and Stock must be valid numbers.");
        } catch (SQLException e) {
            showAlert("Database Error", "Unable to add product: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        if (nameField.getText().isBlank()) {
            showAlert("Validation Error", "Product name is required.");
            return false;
        }
        if (priceField.getText().isBlank()) {
            showAlert("Validation Error", "Product price is required.");
            return false;
        }
        if (stockField.getText().isBlank()) {
            showAlert("Validation Error", "Product stock is required.");
            return false;
        }
        return true;
    }

    private Produit createProductFromInputs() {
        String name = nameField.getText().trim();
        double price = Double.parseDouble(priceField.getText().trim());
        int stock = Integer.parseInt(stockField.getText().trim());
        String desc = descriptionField.getText().trim();
        String img = selectedImagePath.isEmpty() ? DEFAULT_IMAGE : selectedImagePath;

        return new Produit(0, name, price, stock, img, desc.isEmpty() ? "No description provided" : desc);
    }

    private void saveProductToDatabase(Produit p) throws SQLException {
        String sql = "INSERT INTO produits(nom, prix, stock, image_url, description) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setDouble(2, p.getPrix());
            ps.setInt(3, p.getStock());
            ps.setString(4, p.getImageUrl());
            ps.setString(5, p.getDescription());
            ps.executeUpdate();
        }
    }

    private void resetForm() {
        nameField.clear();
        priceField.clear();
        stockField.clear();
        descriptionField.clear();
        selectedImagePath = "";
    }

    private void openEditProduct(Produit produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/edit_product.fxml"));
            Parent root = loader.load();

            EditProductController controller = loader.getController();
            controller.setProduit(produit);
            controller.setProductManagerController(this);

            Stage stage = new Stage();
            stage.setTitle("Edit Product - " + produit.getNom());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            showAlert("Load Error", "Unable to open edit window: " + e.getMessage());
        }
    }

    private void deleteProduct(Produit produit) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Product");
        confirmAlert.setHeaderText("Are you sure?");
        confirmAlert.setContentText("Do you want to delete '" + produit.getNom() + "'? This action cannot be undone.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement("DELETE FROM produits WHERE id = ?");
                    ps.setInt(1, produit.getId());
                    ps.executeUpdate();
                    loadProducts();
                    showAlert("Success", "Product deleted successfully!");
                } catch (SQLException e) {
                    showAlert("Database Error", "Could not delete product: " + e.getMessage());
                }
            }
        });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, keyword) -> {
            String lower = keyword.toLowerCase().trim();
            productFlow.getChildren().clear();

            if (lower.isEmpty()) {
                // Show all products if search is empty
                productList.forEach(this::addProductCard);
            } else {
                // Filter products
                productList.stream()
                        .filter(p -> p.getNom().toLowerCase().contains(lower) ||
                                String.valueOf(p.getId()).contains(lower) ||
                                (p.getDescription() != null && p.getDescription().toLowerCase().contains(lower)))
                        .forEach(this::addProductCard);
            }
        });
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);

        // Try to apply styling, but don't fail if CSS is missing
        try {
            URL cssUrl = getClass().getResource("/view/styles.css");
            if (cssUrl != null) {
                alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
                alert.getDialogPane().getStyleClass().add("alert-dialog");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not load CSS for alert dialog", e);
        }

        alert.showAndWait();
    }

    public void refreshProducts() {
        loadProducts();
    }
}
