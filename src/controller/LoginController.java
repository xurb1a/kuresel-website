package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.Client;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginController {
    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private ImageView storeImage;

    @FXML
    private void goToSignUp() {
        try {
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/view/signup.fxml")));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error navigating to signup page", e);
            showError("Navigation error: " + e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        try {
            Image gif = new Image(getClass().getResource("/images/kuresel.gif").toExternalForm());
            storeImage.setImage(gif);
            errorLabel.setVisible(false);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error loading store image", e);
        }
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT * FROM clients WHERE email = ? AND motdepasse = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, password);

            rs = stmt.executeQuery();

            if (rs.next()) {
                Client client = new Client(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("email"),
                        rs.getString("adresse"),
                        rs.getString("telephone"),
                        rs.getString("motdepasse"),
                        rs.getInt("type")
                );

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Dashboard.fxml"));
                Scene dashboardScene = new Scene(loader.load());

                DashboardController controller = loader.getController();
                controller.setUser(client); // will internally load either client_dashboard.fxml or admin_dashboard.fxml

                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.setScene(dashboardScene);
                stage.setTitle("KURESEL Dashboard");
                stage.show();

            } else {
                showError("Invalid email or password.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during login", e);
            showError("Error connecting to database.");
        } finally {
            DatabaseConnection.closeResources(rs, stmt);
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
