package model;

public class Produit {
    private int id;
    private String nom;
    private double prix;
    private int stock;
    private String imageUrl;
    private String description;

    public Produit(int id, String nom, double prix, int stock, String imageUrl, String description) {
        this.id = id;
        this.nom = nom;
        this.prix = prix;
        this.stock = stock;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getDescription(){ return description; }
    public double getPrix() { return prix; }
    public int getStock() { return stock; }
    public String getImageUrl() { return imageUrl; }

    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setDescription(String description){ this.description = description;}
    public void setPrix(double prix) { this.prix = prix; }
    public void setStock(int stock) { this.stock = stock; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @Override
    public String toString() {
        return "Produit{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", Description=" + description +
                ", prix=" + prix +
                ", stock=" + stock +
                ", imageUrl=" + imageUrl +
                '}';
    }

}
