package model;

public class LigneCommande {
    private Produit produit;
    private int quantite;
    private int id;

    public LigneCommande(int id, Produit produit, int quantite) {
        this.produit = produit;
        this.quantite = quantite;
        this.id = id;
    }

    public Produit getProduit() { return produit; }
    public int getQuantite() { return quantite; }
    public int getId() { return id;}

    public void setProduit(Produit produit) { this.produit = produit; }
    public void setQuantite(int quantite) { this.quantite = quantite; }
    public void setId(int id) {this.id = id; }

    public double getSousTotal() {
        if (produit != null) {
            return produit.getPrix() * quantite;
        }
        return 0.0;
    }

    public double getPrixUnitaire() {
        return produit != null ? produit.getPrix() : 0.0;
    }
    @Override
    public String toString() {
        return "LigneCommande{" +
                "produit=" + (produit != null ? produit.getNom() : "null") +
                ", quantite=" + quantite +
                '}';
    }
}
