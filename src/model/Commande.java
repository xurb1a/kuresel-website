package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Commande {
    private int id;
    private Client client;
    private List<LigneCommande> lignes;
    private LocalDate date;
    private String statutLivraison;
    private Paiement paiement;
    private String deliveryCity;
    private double deliveryFee;
    private LocalDate estimatedDeliveryDate;

    public Commande(int id, Client client, List<LigneCommande> lignes, LocalDate date, String statutLivraison, Paiement paiement) {
        this.id = id;
        this.client = client;
        this.lignes = lignes != null ? lignes : new ArrayList<>();
        this.date = date;
        this.statutLivraison = statutLivraison;
        this.paiement = paiement;
        this.deliveryFee = 0.0;
    }

    // Getters
    public int getId() { return id; }
    public Client getClient() { return client; }
    public List<LigneCommande> getLignes() { return lignes; }
    public LocalDate getDate() { return date; }
    public String getStatutLivraison() { return statutLivraison; }
    public Paiement getPaiement() { return paiement; }
    public String getDeliveryCity() { return deliveryCity; }
    public double getDeliveryFee() { return deliveryFee; }
    public LocalDate getEstimatedDeliveryDate() { return estimatedDeliveryDate; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setClient(Client client) { this.client = client; }
    public void setLignes(List<LigneCommande> lignes) { this.lignes = lignes; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setStatutLivraison(String statutLivraison) { this.statutLivraison = statutLivraison; }
    public void setPaiement(Paiement paiement) { this.paiement = paiement; }
    public void setDeliveryCity(String deliveryCity) { this.deliveryCity = deliveryCity; }
    public void setDeliveryFee(double deliveryFee) { this.deliveryFee = deliveryFee; }
    public void setEstimatedDeliveryDate(LocalDate estimatedDeliveryDate) { this.estimatedDeliveryDate = estimatedDeliveryDate; }

    // Methods
    public void addLigne(LigneCommande ligne) {
        if (lignes == null) {
            lignes = new ArrayList<>();
        }
        lignes.add(ligne);
    }

    public double getTotal() {
        double total = 0;
        if (lignes != null) {
            for (LigneCommande ligne : lignes) {
                total += ligne.getSousTotal();
            }
        }
        return total;
    }

    public double getTotalWithDelivery() {
        return getTotal() + deliveryFee;
    }

    @Override
    public String toString() {
        return "Commande{" +
                "id=" + id +
                ", client=" + client +
                ", date=" + date +
                ", statutLivraison='" + statutLivraison + '\''  +
                ", deliveryCity='" + deliveryCity + '\'' +
                ", deliveryFee=" + deliveryFee +
                '}';
    }
}
