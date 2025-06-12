package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Client;
import model.Produit;
import util.DatabaseConnection;

import java.io.File;
import java.io.InputStream;
import java.sql.*;

public class ProductsController implements DashboardController.UserAware {

    @FXML private TextField searchField;
    @FXML private FlowPane productFlow;

    private final ObservableList<Produit> products = FXCollections.observableArrayList();
    private final FilteredList<Produit> filteredProducts = new FilteredList<>(products, p -> true);
    private Client client;

    private static final String DEFAULT_IMAGE = "/images/default-product.png";

    @Override
    public void setUser(Client client) {
        this.client = client;
        initialize();
    }

    @FXML
    public void initialize() {
        loadProducts();
        setupSearch();
    }

    private void loadProducts() {
        products.clear();
        productFlow.getChildren().clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM produits")) {

            while (rs.next()) {
                Produit produit = new Produit(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getDouble("prix"),
                        rs.getInt("stock"),
                        rs.getString("image_url"),
                        rs.getString("description")
                );
                products.add(produit);
                addProductCard(produit);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load products: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String lower = newVal.toLowerCase().trim();
            filteredProducts.setPredicate(prod ->
                    prod.getNom().toLowerCase().contains(lower) ||
                            String.valueOf(prod.getId()).contains(lower)
            );
            productFlow.getChildren().clear();
            filteredProducts.forEach(this::addProductCard);
        });
    }

    private void addProductCard(Produit produit) {
        VBox card = new VBox();
        card.getStyleClass().add("product-card");
        card.setPrefWidth(280);

        // Product Image Container for centering
        VBox imageContainer = new VBox();
        imageContainer.getStyleClass().add("product-image-container");

        ImageView imageView = new ImageView();
        imageView.getStyleClass().add("product-image");
        loadProductImage(imageView, produit);
        imageView.setFitWidth(260);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);

        imageContainer.getChildren().add(imageView);

        // Product Body Container
        VBox body = new VBox();
        body.getStyleClass().add("product-body");

        // Product Name
        Label nameLabel = new Label(produit.getNom());
        nameLabel.getStyleClass().add("product-name");

        // Product Description
        Label descLabel = new Label(produit.getDescription());
        descLabel.getStyleClass().add("product-description");
        descLabel.setWrapText(true);

        // Stock Status
        Label stockLabel = new Label(produit.getStock() > 0 ? "In stock" : "Out of stock");
        stockLabel.getStyleClass().add("product-stock");
        if (produit.getStock() > 0) {
            stockLabel.getStyleClass().add("in-stock");
        } else {
            stockLabel.getStyleClass().add("out-of-stock");
        }

        // Price Box
        HBox priceBox = new HBox();
        priceBox.getStyleClass().add("product-price-box");

        Label currentPrice = new Label(String.format("%.2f DH", produit.getPrix()));
        currentPrice.getStyleClass().add("product-current-price");

        Label oldPrice = new Label(String.format("%.2f DH", produit.getPrix() * 1.25));
        oldPrice.getStyleClass().add("product-old-price");

        priceBox.getChildren().addAll(currentPrice, oldPrice);

        // Labels Box (HIT, SALE)
        HBox labelBox = new HBox();
        labelBox.getStyleClass().add("product-labels-box");

        Label hitLabel = new Label("HIT");
        hitLabel.getStyleClass().add("label-hit");

        Label saleLabel = new Label("SALE -25%");
        saleLabel.getStyleClass().add("label-sale");

        labelBox.getChildren().addAll(hitLabel, saleLabel);

        // Quantity Spinner
        Spinner<Integer> quantitySpinner = new Spinner<>();
        quantitySpinner.getStyleClass().add("quantity-spinner");
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
                1, Math.max(1, produit.getStock()), 1);
        quantitySpinner.setValueFactory(valueFactory);
        quantitySpinner.setPrefWidth(80);

        // Add to Cart Button
        Button addToCartBtn = new Button("Add to Cart");
        addToCartBtn.getStyleClass().add("add-to-cart-button");
        addToCartBtn.setOnAction(e -> {
            int quantity = quantitySpinner.getValue();
            addToCart(produit, quantity);
        });

        // Controls Box
        HBox controlsBox = new HBox();
        controlsBox.getStyleClass().add("product-controls-box");
        controlsBox.getChildren().addAll(quantitySpinner, addToCartBtn);

        // Add all elements to body
        body.getChildren().addAll(nameLabel, descLabel, stockLabel, priceBox, labelBox, controlsBox);

        // Add image container and body to card
        card.getChildren().addAll(imageContainer, body);
        productFlow.getChildren().add(card);
    }

    private void loadProductImage(ImageView imageView, Produit produit) {
        String imagePath = produit.getImageUrl();

        try {
            if (imagePath == null || imagePath.trim().isEmpty()) {
                imageView.setImage(loadDefaultImage());
                return;
            }

            Image image = null;

            // 1. If path starts with "file:/", load as URI directly
            if (imagePath.startsWith("file:/")) {
                image = new Image(imagePath);
            }
            // 2. Try as file from filesystem
            else {
                File file = new File(imagePath);
                if (!file.exists()) {
                    file = new File(System.getProperty("user.dir") + File.separator + imagePath);
                }
                if (file.exists()) {
                    image = new Image(file.toURI().toString());
                } else {
                    // 3. Try loading as resource (classpath)
                    InputStream is = getClass().getResourceAsStream("/" + imagePath.replace("\\", "/"));
                    if (is != null) {
                        image = new Image(is);
                    }
                }
            }

            imageView.setImage(image != null ? image : loadDefaultImage());

        } catch (Exception e) {
            System.err.println("Error loading image: " + imagePath);
            e.printStackTrace();
            imageView.setImage(loadDefaultImage());
        }
    }




    private Image loadDefaultImage() {
        try {
            return new Image(getClass().getResource(DEFAULT_IMAGE).toExternalForm());
        } catch (Exception e) {
            System.err.println("CRITICAL: Default image missing!");
            return null;
        }
    }

    private void addToCart(Produit produit, int quantity) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO cart (client_id, produit_id, quantite) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE quantite = quantite + ?")) {
            stmt.setInt(1, client.getId());
            stmt.setInt(2, produit.getId());
            stmt.setInt(3, quantity);
            stmt.setInt(4, quantity);
            stmt.executeUpdate();
            showAlert("Cart Update", quantity + " x " + produit.getNom() + " added to cart!");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Cart Error", "Failed to add product to cart.");
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
