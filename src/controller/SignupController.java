package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.Client;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class SignupController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private Label errorLabel;
    @FXML private ImageView storeImage;
    @FXML
    public void initialize() {
        Image gif = new Image(getClass().getResource("/images/kuresel.gif").toExternalForm());
        storeImage.setImage(gif);
    }
    @FXML
    private void handleSignUp() {
        String nom = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();

        if (nom.isEmpty() || email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Name, email and password are required.");
            errorLabel.setVisible(true);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO clients (nom, email, motdepasse, adresse, telephone, type) VALUES (?, ?, ?, ?, ?, 1)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nom);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.setString(4, address);
            stmt.setString(5, phone);
            stmt.executeUpdate();

            // Create the new client object
            Client newUser = new Client(0, nom, email, password, address, phone, 1);

            // Load the main dashboard frame
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dashboard.fxml"));
            Parent root = loader.load();
            DashboardController controller = loader.getController();

            // Inject the new user → dashboard will detect user type and load client_dashboard.fxml inside
            controller.setUser(newUser);

            // Show dashboard scene
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("KURESEL - Dashboard");
            stage.show();

        } catch (Exception e) {
            errorLabel.setText("Database error: " + e.getMessage());
            errorLabel.setVisible(true);
            e.printStackTrace();
        }
    }

    @FXML
    private void goToLogin() {
        try {
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/view/login.fxml")));
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
