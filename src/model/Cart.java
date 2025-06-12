package model;

public class Cart {
    private int id;
    private int produitId; // Matches DB column produit_id
    private int clientId;  // Matches DB column client_id
    private int quantite;  // Matches DB column quantite

    // Constructor matching DB columns
    public Cart(int id, int produitId, int clientId, int quantite) {
        this.id = id;
        this.produitId = produitId;
        this.clientId = clientId;
        this.quantite = quantite;
    }

    // Constructor for creating new cart items (without id)
    public Cart(int produitId, int clientId, int quantite) {
        this.produitId = produitId;
        this.clientId = clientId;
        this.quantite = quantite;
    }

    // Getters
    public int getId() { return id; }
    public int getProduitId() { return produitId; }
    public int getClientId() { return clientId; }
    public int getQuantite() { return quantite; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setProduitId(int produitId) { this.produitId = produitId; }
    public void setClientId(int clientId) { this.clientId = clientId; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    @Override
    public String toString() {
        return "Cart{" +
                "id=" + id +
                ", produitId=" + produitId +
                ", clientId=" + clientId +
                ", quantite=" + quantite +
                '}';
    }
}
