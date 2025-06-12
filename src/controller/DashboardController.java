package controller;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Client;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DashboardController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(DashboardController.class.getName());

    @FXML private BorderPane dashboardRoot;
    @FXML private StackPane contentArea;
    @FXML private Label welcomeText;
    @FXML private Label userTypeText;
    @FXML private Label currentDateLabel;
    @FXML private HBox clientLinks;
    @FXML private HBox adminLinks;
    @FXML private HBox deliveryLinks;
    @FXML private Button homeBtnClient, productsBtnClient, cartBtnClient, ordersBtnClient, profileBtnClient;
    @FXML private Button homeBtnAdmin, productManagerBtn, orderManagerBtn, clientManagerBtn, deliveryManagerBtn, reportsBtn;
    @FXML private Button homeBtnDelivery, availableOrdersBtn, myDeliveriesBtn, earningsBtn;
    @FXML private Button logoutBtn, notificationsBtn, settingsBtn;

    private Client currentUser;
    private int userType;
    private String currentPage;
    private final Map<String, Parent> pageCache = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        updateDateDisplay();
        setupButtonAnimations();
        currentPage = "login.fxml";
    }

    private void updateDateDisplay() {
        if (currentDateLabel != null) {
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
            currentDateLabel.setText(today.format(formatter));
        } else {
            LOGGER.warning("currentDateLabel is null — check fx:id in FXML.");
        }
    }

    private void setupButtonAnimations() {
        Button[] buttons = {
                homeBtnClient, productsBtnClient, cartBtnClient, ordersBtnClient, profileBtnClient,
                homeBtnAdmin, productManagerBtn, orderManagerBtn, clientManagerBtn, deliveryManagerBtn, reportsBtn, logoutBtn
        };
        for (Button button : buttons) {
            if (button != null) setupButtonHoverEffect(button);
        }
    }

    private void setupButtonHoverEffect(Button button) {
        button.setOnMouseEntered(e -> {
            TranslateTransition tt = new TranslateTransition(Duration.millis(100), button);
            tt.setByX(5);
            tt.play();
        });

        button.setOnMouseExited(e -> {
            TranslateTransition tt = new TranslateTransition(Duration.millis(100), button);
            tt.setByX(-5);
            tt.play();
        });
    }

    public void setUser(Client client) {
        this.currentUser = client;
        this.userType = client.getUserType();

        if (welcomeText != null) welcomeText.setText("Welcome, " + client.getNom());
        if (userTypeText != null) {
            String userTypeString = switch (userType) {
                case 1 -> "Client";
                case 2 -> "Administrator";
                case 3 -> "Delivery Guy";
                default -> "User";
            };
            userTypeText.setText(userTypeString);
        }

        // Show/hide navigation based on user type
        if (clientLinks != null) {
            clientLinks.setVisible(userType == 1);
            clientLinks.setManaged(userType == 1);
        }
        if (adminLinks != null) {
            adminLinks.setVisible(userType == 2);
            adminLinks.setManaged(userType == 2);
        }
        if (deliveryLinks != null) {
            deliveryLinks.setVisible(userType == 3);
            deliveryLinks.setManaged(userType == 3);
        }

        resetActiveButtons();
        switch (userType) {
            case 1 -> {
                if (homeBtnClient != null) {
                    homeBtnClient.getStyleClass().add("active");
                    loadPage("client_dashboard.fxml");
                }
            }
            case 2 -> {
                if (homeBtnAdmin != null) {
                    homeBtnAdmin.getStyleClass().add("active");
                    loadPage("admin_dashboard.fxml");
                }
            }
            case 3 -> {
                if (homeBtnDelivery != null) {
                    homeBtnDelivery.getStyleClass().add("active");
                    loadPage("delivery_dashboard.fxml");
                }
            }
        }
    }

    private void resetActiveButtons() {
        Button[] buttons = {
                homeBtnClient, productsBtnClient, cartBtnClient, ordersBtnClient, profileBtnClient,
                homeBtnAdmin, productManagerBtn, orderManagerBtn, clientManagerBtn, deliveryManagerBtn, reportsBtn,
                homeBtnDelivery, availableOrdersBtn, myDeliveriesBtn, earningsBtn
        };
        for (Button b : buttons) {
            if (b != null) b.getStyleClass().remove("active");
        }
    }

    public void loadPage(String fxmlFile) {
        try {
            URL resource = getClass().getClassLoader().getResource("view/" + fxmlFile);
            if (resource == null) throw new IOException("FXML not found: view/" + fxmlFile);

            FXMLLoader loader = new FXMLLoader(resource);
            Parent page = loader.load();

            Object controller = loader.getController();
            if (controller instanceof UserAware userAware) userAware.setUser(currentUser);
            if (controller instanceof DashboardAware dashAware) dashAware.setDashboardController(this);

            pageCache.put(fxmlFile, page);
            currentPage = fxmlFile;

            contentArea.getChildren().clear();
            contentArea.getChildren().add(page);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), page);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load page: " + fxmlFile, e);
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to load page: " + fxmlFile,
                    "Please ensure the file exists in /resources/view.");
        }
    }

    public void refreshCurrentPage() {
        pageCache.remove(currentPage);
        loadPage(currentPage);
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Client navigation
    @FXML private void handleHomeClick(ActionEvent e) { switchTo("client_dashboard.fxml", homeBtnClient, homeBtnAdmin, null); }
    @FXML private void handleProductsClick(ActionEvent e) { switchTo("products.fxml", productsBtnClient, null, null); }
    @FXML private void handleCartClick(ActionEvent e) { switchTo("cart.fxml", cartBtnClient, null, null); }
    @FXML private void handleOrdersClick(ActionEvent e) { switchTo("orders.fxml", ordersBtnClient, null, null); }
    @FXML private void handleProfileClick(ActionEvent e) { switchTo("profile.fxml", profileBtnClient, null, null); }

    // Admin navigation
    @FXML private void handleProductManagerClick(ActionEvent e) { switchTo("product_manager.fxml", productManagerBtn, null, null); }
    @FXML private void handleOrderManagerClick(ActionEvent e) { switchTo("orders.fxml", orderManagerBtn, null, null); }
    @FXML private void handleClientManagerClick(ActionEvent e) { switchTo("client_manager.fxml", clientManagerBtn, null, null); }
    @FXML private void handleDeliveryManagerClick(ActionEvent e) { switchTo("delivery_manager.fxml", deliveryManagerBtn, null, null); }
    @FXML private void handleReportsClick(ActionEvent e) { switchTo("reports.fxml", reportsBtn, null, null); }

    // Delivery guy navigation
    @FXML private void handleDeliveryHomeClick(ActionEvent e) { switchTo("delivery_dashboard.fxml", null, null, homeBtnDelivery); }
    @FXML private void handleAvailableOrdersClick(ActionEvent e) { switchTo("available_orders.fxml", null, null, availableOrdersBtn); }
    @FXML private void handleMyDeliveriesClick(ActionEvent e) { switchTo("my_deliveries.fxml", null, null, myDeliveriesBtn); }
    @FXML private void handleEarningsClick(ActionEvent e) { switchTo("earnings.fxml", null, null, earningsBtn); }

    private void switchTo(String page, Button clientBtn, Button adminBtn, Button deliveryBtn) {
        resetActiveButtons();
        if (userType == 1 && clientBtn != null) clientBtn.getStyleClass().add("active");
        else if (userType == 2 && adminBtn != null) adminBtn.getStyleClass().add("active");
        else if (userType == 3 && deliveryBtn != null) deliveryBtn.getStyleClass().add("active");
        loadPage(page);
    }

    @FXML private void handleSettings(ActionEvent e) { loadPage("settings.fxml"); }

    @FXML private void handleLogout(ActionEvent e) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("Any unsaved changes will be lost.");
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) performLogout(e);
        });
    }

    private void performLogout(ActionEvent e) {
        try {
            currentUser = null;
            userType = 0;
            pageCache.clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent loginRoot = loader.load();
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
            stage.setTitle("KURESEL - Login");
            stage.show();
            FadeTransition ft = new FadeTransition(Duration.millis(500), loginRoot);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load login screen", ex);
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to logout", "Failed to load login screen.");
        }
    }

    public interface UserAware {
        void setUser(Client user);
    }

    public interface DashboardAware {
        void setDashboardController(DashboardController controller);
    }
}
