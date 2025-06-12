package model;

import java.time.LocalDate;

public class Livraison {
    private int id;
    private Commande commande;
    private String adresseLivraison;
    private String statut;
    private LocalDate dateLivraison;
    private String clientNom;
    private LocalDate estimatedDeliveryDate;
    private LocalDate actualDeliveryDate;
    private int deliveryGuyId;

    public Livraison(int id, Commande commande, String adresseLivraison, String statut, LocalDate dateLivraison) {
        this.id = id;
        this.commande = commande;
        this.adresseLivraison = adresseLivraison;
        this.statut = statut;
        this.dateLivraison = dateLivraison;
    }

    // Getters
    public int getId() { return id; }
    public Commande getCommande() { return commande; }
    public String getAdresseLivraison() { return adresseLivraison; }
    public String getStatut() { return statut; }
    public LocalDate getDateLivraison() { return dateLivraison; }
    public String getClientNom() { return clientNom; }
    public LocalDate getEstimatedDeliveryDate() { return estimatedDeliveryDate; }
    public LocalDate getActualDeliveryDate() { return actualDeliveryDate; }
    public int getDeliveryGuyId() { return deliveryGuyId; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setCommande(Commande commande) { this.commande = commande; }
    public void setAdresseLivraison(String adresseLivraison) { this.adresseLivraison = adresseLivraison; }
    public void setStatut(String statut) { this.statut = statut; }
    public void setDateLivraison(LocalDate dateLivraison) { this.dateLivraison = dateLivraison; }
    public void setClientNom(String clientNom) { this.clientNom = clientNom; }
    public void setEstimatedDeliveryDate(LocalDate estimatedDeliveryDate) { this.estimatedDeliveryDate = estimatedDeliveryDate; }
    public void setActualDeliveryDate(LocalDate actualDeliveryDate) { this.actualDeliveryDate = actualDeliveryDate; }
    public void setDeliveryGuyId(int deliveryGuyId) { this.deliveryGuyId = deliveryGuyId; }

    @Override
    public String toString() {
        return "Livraison{" +
                "id=" + id +
                ", commande=" + commande +
                ", adresseLivraison='" + adresseLivraison + '\'' +
                ", statut='" + statut + '\'' +
                ", dateLivraison=" + dateLivraison +
                ", clientNom='" + clientNom + '\'' +
                '}';
    }
}
