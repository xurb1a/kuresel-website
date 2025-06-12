package controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import model.Commande;
import model.Livraison;
import util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;

public class DeliveryManagerController implements DashboardController.UserAware {

    @FXML private TextField searchField;
    @FXML private TableView<Livraison> deliveriesTable;
    @FXML private TableColumn<Livraison, Integer> idCol;
    @FXML private TableColumn<Livraison, String> clientCol;
    @FXML private TableColumn<Livraison, String> addressCol;
    @FXML private TableColumn<Livraison, LocalDate> dateCol;
    @FXML private TableColumn<Livraison, String> statusCol;
    @FXML private TableColumn<Livraison, Void> actionCol;

    private final ObservableList<Livraison> allDeliveries = FXCollections.observableArrayList();
    private final FilteredList<Livraison> filteredDeliveries = new FilteredList<>(allDeliveries);

    private model.Client currentUser;

    @Override
    public void setUser(model.Client user) {
        this.currentUser = user;
        loadDeliveries();
        setupSearch();
        addActionButtons();
    }

    private void loadDeliveries() {
        allDeliveries.clear();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                    SELECT l.id, l.commande_id, l.adresse_livraison, l.date_livraison, l.statut, cl.nom AS client 
                    FROM livraison l 
                    JOIN commandes c ON l.commande_id = c.id 
                    JOIN clients cl ON c.client_id = cl.id
                    """;

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Commande cmd = new Commande(
                        rs.getInt("commande_id"),
                        null,
                        null,
                        rs.getDate("date_livraison").toLocalDate(),
                        "",
                        null
                );

                Livraison l = new Livraison(
                        rs.getInt("id"),
                        cmd,
                        rs.getString("adresse_livraison"),
                        rs.getString("statut"),
                        rs.getDate("date_livraison").toLocalDate()
                );

                l.setClientNom(rs.getString("client"));
                allDeliveries.add(l);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        idCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
        clientCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getClientNom()));
        addressCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAdresseLivraison()));
        dateCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getDateLivraison()));
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatut()));

        statusCol.setCellFactory(ComboBoxTableCell.forTableColumn(
                new StringConverter<>() {
                    @Override public String toString(String object) { return object; }
                    @Override public String fromString(String string) { return string; }
                },
                "En attente", "En cours", "Livrée", "Annulée"
        ));

        statusCol.setOnEditCommit(event -> {
            Livraison livraison = event.getRowValue();
            livraison.setStatut(event.getNewValue());
            updateStatusInDatabase(livraison.getId(), event.getNewValue());
        });

        deliveriesTable.setEditable(true);
        deliveriesTable.setItems(filteredDeliveries);
        deliveriesTable.setPlaceholder(new Label("🚚 Aucune livraison disponible"));
    }

    private void updateStatusInDatabase(int livraisonId, String newStatus) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE livraison SET statut = ? WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, newStatus);
            ps.setInt(2, livraisonId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateDateInDatabase(int livraisonId, LocalDate newDate) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE livraison SET date_livraison = ? WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1, Date.valueOf(newDate));
            ps.setInt(2, livraisonId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            String lower = newText.toLowerCase().trim();
            filteredDeliveries.setPredicate(l ->
                    String.valueOf(l.getId()).contains(lower)
                            || l.getClientNom().toLowerCase().contains(lower)
                            || l.getAdresseLivraison().toLowerCase().contains(lower)
                            || l.getStatut().toLowerCase().contains(lower)
            );
        });
    }

    private void addActionButtons() {
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button cancelBtn = new Button("🛑");
            private final DatePicker datePicker = new DatePicker();
            private final HBox box = new HBox(6, cancelBtn, datePicker);

            {
                cancelBtn.getStyleClass().add("remove-button");
                datePicker.getStyleClass().add("combo-box");

                cancelBtn.setOnAction(e -> {
                    Livraison livraison = getTableView().getItems().get(getIndex());
                    livraison.setStatut("Annulée");
                    updateStatusInDatabase(livraison.getId(), "Annulée");
                    getTableView().refresh();
                });

                datePicker.setOnAction(e -> {
                    Livraison livraison = getTableView().getItems().get(getIndex());
                    LocalDate selectedDate = datePicker.getValue();
                    livraison.setDateLivraison(selectedDate);
                    updateDateInDatabase(livraison.getId(), selectedDate);
                    getTableView().refresh();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }
}
