package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.Client;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// Controller for the user profile view
public class ProfileController implements DashboardController.UserAware {

    // --- FXML Components ---
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button editButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button changePasswordButton;
    @FXML private Label passwordErrorLabel;

    // --- State ---
    private Client currentUser;
    private boolean isEditing = false;

    // --- Initialization and User Setting ---

    // Method from UserAware interface to receive the logged-in user
    @Override
    public void setUser(Client user) {
        this.currentUser = user;
        if (currentUser != null) {
            populateProfileData();
            setEditingState(false); // Start in non-editing mode
        } else {
            // Handle case where user is null (e.g., error state)
            clearProfileData();
            disableAllControls();
            showAlert(Alert.AlertType.ERROR, "Error", "No user data available to display profile.");
        }
    }

    // Initialize method (called after FXML loading)
    @FXML
    public void initialize() {
        // Initial UI state is set in setUser after currentUser is available
        passwordErrorLabel.setVisible(false);
        passwordErrorLabel.getStyleClass().add("error-label"); // Ensure style is applied
    }

    // --- UI Update Methods ---

    // Populate form fields with current user data
    private void populateProfileData() {
        nameField.setText(currentUser.getNom());
        emailField.setText(currentUser.getEmail());
        phoneField.setText(currentUser.getTelephone() != null ? currentUser.getTelephone() : "");
        addressField.setText(currentUser.getAdresse() != null ? currentUser.getAdresse() : "");

        // Clear password fields and error label
        clearPasswordFields();
    }

    // Clear all profile data fields
    private void clearProfileData() {
        nameField.clear();
        emailField.clear();
        phoneField.clear();
        addressField.clear();
        clearPasswordFields();
    }

    // Clear only password-related fields and error label
    private void clearPasswordFields() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        passwordErrorLabel.setText("");
        passwordErrorLabel.setVisible(false);
    }

    // Set the editing state for profile information fields
    private void setEditingState(boolean editing) {
        isEditing = editing;

        // Toggle editability of fields
        nameField.setEditable(editing);
        emailField.setEditable(editing);
        phoneField.setEditable(editing);
        addressField.setEditable(editing);

        // Toggle visibility/managed state of buttons
        editButton.setVisible(!editing);
        editButton.setManaged(!editing);
        saveButton.setVisible(editing);
        saveButton.setManaged(editing);
        cancelButton.setVisible(editing);
        cancelButton.setManaged(editing);

        // Disable password change while editing profile info
        changePasswordButton.setDisable(editing);
        currentPasswordField.setDisable(editing);
        newPasswordField.setDisable(editing);
        confirmPasswordField.setDisable(editing);
    }

    // Disable all controls if no user is logged in
    private void disableAllControls() {
        nameField.setEditable(false);
        emailField.setEditable(false);
        phoneField.setEditable(false);
        addressField.setEditable(false);
        currentPasswordField.setDisable(true);
        newPasswordField.setDisable(true);
        confirmPasswordField.setDisable(true);
        editButton.setDisable(true);
        saveButton.setDisable(true);
        cancelButton.setDisable(true);
        changePasswordButton.setDisable(true);
    }

    // --- FXML Action Handlers ---

    // Handle "Edit Profile" button click
    @FXML
    private void handleEditProfile() {
        setEditingState(true);
    }

    // Handle "Save Changes" button click for profile info
    @FXML
    private void handleSaveChanges() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot save changes, no user logged in.");
            return;
        }

        // Get data from fields
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();

        // --- Validation ---
        if (name.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Name and Email cannot be empty.");
            return;
        }
        // Basic email format check
        if (!email.matches("^[\\w.-]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a valid email address.");
            return;
        }

        // --- Database Update ---
        // Field order in UPDATE: nom, email, telephone, adresse, id
        String sql = "UPDATE clients SET nom = ?, email = ?, telephone = ?, adresse = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Set parameters in order matching SQL
            // Parameter order: nom, email, telephone, adresse, id
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone.isEmpty() ? null : phone); // Use null for empty optional fields
            ps.setString(4, address.isEmpty() ? null : address);
            ps.setInt(5, currentUser.getId());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                // Update the currentUser object in memory to reflect changes
                currentUser.setNom(name);
                currentUser.setEmail(email);
                currentUser.setTelephone(phone.isEmpty() ? null : phone);
                currentUser.setAdresse(address.isEmpty() ? null : address);

                showAlert(Alert.AlertType.INFORMATION, "Success", "Profile information updated successfully.");
                setEditingState(false); // Exit editing mode
            } else {
                // This might happen if the user ID doesn't exist (unlikely if logged in)
                showAlert(Alert.AlertType.ERROR, "Update Failed", "Failed to update profile information. User record might not exist.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Check for duplicate email error (MySQL error code 1062)
            if (e.getErrorCode() == 1062) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not update profile. The email address " + email + " is already in use.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not update profile: " + e.getMessage());
            }
        }
    }

    // Handle "Cancel" button click during profile edit
    @FXML
    private void handleCancelEdit() {
        populateProfileData(); // Revert changes by reloading original data
        setEditingState(false); // Exit editing mode
    }

    // Handle "Change Password" button click
    @FXML
    private void handleChangePassword() {
        passwordErrorLabel.setVisible(false); // Clear previous errors
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot change password, no user logged in.");
            return;
        }

        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // --- Validation ---
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showPasswordError("All password fields are required.");
            return;
        }

        // Verify current password against the stored password (case-sensitive)
        if (!currentUser.getPassword().equals(currentPassword)) {
            showPasswordError("Incorrect current password.");
            return;
        }

        // Check if new password and confirmation match
        if (!newPassword.equals(confirmPassword)) {
            showPasswordError("New passwords do not match.");
            return;
        }

        // Check if new password is the same as the old one
        if (newPassword.equals(currentPassword)) {
            showPasswordError("New password must be different from the current password.");
            return;
        }

        // Optional: Add password complexity rules here (length, characters, etc.)
        if (newPassword.length() < 6) {
            showPasswordError("New password must be at least 6 characters long.");
            return;
        }

        // --- Database Update ---
        // Field order in UPDATE: motdepasse, id
        String sql = "UPDATE clients SET motdepasse = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Parameter order: motdepasse, id
            ps.setString(1, newPassword);
            ps.setInt(2, currentUser.getId());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                // Update the password in the currentUser object in memory
                currentUser.setPassword(newPassword);

                clearPasswordFields(); // Clear fields on success
                showAlert(Alert.AlertType.INFORMATION, "Success", "Password updated successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Update Failed", "Failed to update password. User record might not exist.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not update password: " + e.getMessage());
        }
    }

    // --- Helper Methods ---

    // Show password-specific errors
    private void showPasswordError(String message) {
        passwordErrorLabel.setText(message);
        passwordErrorLabel.setVisible(true);
    }

    // Helper method to show general alerts
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

