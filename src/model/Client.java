package model;

public class Client {
    private int id;
    private String nom;
    private String email;
    private String adresse;
    private String telephone;
    private String password; // Added password field
    private int user_type;   // Added user_type field: 1 for client, 2 for admin

    // Constructor with all fields
    public Client(int id, String nom, String email, String adresse, String telephone, String password, int user_type) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.adresse = adresse;
        this.telephone = telephone;
        this.password = password;
        this.user_type = user_type;
    }

    // Constructor without id (for new clients)
    public Client(String nom, String email, String adresse, String telephone, String password, int user_type) {
        this.nom = nom;
        this.email = email;
        this.adresse = adresse;
        this.telephone = telephone;
        this.password = password;
        this.user_type = user_type;
    }

    // Constructor compatible with original code
    public Client(int id, String nom, String email, String adresse, String telephone) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.adresse = adresse;
        this.telephone = telephone;
        this.user_type = 1; // Default to client
    }

    // Getters
    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getEmail() { return email; }
    public String getAdresse() { return adresse; }
    public String getTelephone() { return telephone; }
    public String getPassword() { return password; }
    public int getUserType() { return user_type; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setEmail(String email) { this.email = email; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public void setPassword(String password) { this.password = password; }
    public void setUserType(int user_type) { this.user_type = user_type; }

    // Check if client is an admin
    public boolean isAdmin() {
        return user_type == 2;
    }

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", email='" + email + '\'' +
                ", adresse='" + adresse + '\'' +
                ", telephone='" + telephone + '\'' +
                ", user_type=" + user_type +
                '}';
    }
}