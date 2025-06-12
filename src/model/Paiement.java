package model;

import java.time.LocalDate;

public class Paiement {
    private int id;
    private Commande commande;
    private double montant;
    private LocalDate datePaiement;
    private String methode;
    private String statut;

    public Paiement(int id, Commande commande, double montant, LocalDate datePaiement, String methode, String statut) {
        this.id = id;
        this.commande = commande;
        this.montant = montant;
        this.datePaiement = datePaiement;
        this.methode = methode;
        this.statut = statut;
    }

    public Paiement(int id, String methode, double montant) {
        this.id = id;
        this.methode = methode;
        this.montant = montant;
        this.datePaiement = LocalDate.now(); // or a fixed mock date
        this.statut = "En attente"; // or any default
        this.commande = null; // since it's just for display
    }


    public int getId() { return id; }
    public Commande getCommande() { return commande; }
    public double getMontant() { return montant; }
    public LocalDate getDatePaiement() { return datePaiement; }
    public String getMethode() { return methode; }
    public String getStatut() { return statut; }

    public void setId(int id) { this.id = id; }
    public void setCommande(Commande commande) { this.commande = commande; }
    public void setMontant(double montant) { this.montant = montant; }
    public void setDatePaiement(LocalDate datePaiement) { this.datePaiement = datePaiement; }
    public void setMethode(String methode) { this.methode = methode; }
    public void setStatut(String statut) { this.statut = statut; }

    @Override
    public String toString() {
        return "Paiement{" +
                "id=" + id +
                ", commandeId=" + (commande != null ? commande.getId() : "null") +
                ", montant=" + montant +
                ", datePaiement=" + datePaiement +
                ", methode='" + methode + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }
}
