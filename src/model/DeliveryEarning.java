package model;

import java.time.LocalDate;

public class DeliveryEarning {
    private int id;
    private int deliveryGuyId;
    private int commandeId;
    private double amount;
    private LocalDate earnedDate;
    private String status;

    public DeliveryEarning(int id, int deliveryGuyId, int commandeId, double amount, LocalDate earnedDate, String status) {
        this.id = id;
        this.deliveryGuyId = deliveryGuyId;
        this.commandeId = commandeId;
        this.amount = amount;
        this.earnedDate = earnedDate;
        this.status = status;
    }

    // Getters
    public int getId() { return id; }
    public int getDeliveryGuyId() { return deliveryGuyId; }
    public int getCommandeId() { return commandeId; }
    public double getAmount() { return amount; }
    public LocalDate getEarnedDate() { return earnedDate; }
    public String getStatus() { return status; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setDeliveryGuyId(int deliveryGuyId) { this.deliveryGuyId = deliveryGuyId; }
    public void setCommandeId(int commandeId) { this.commandeId = commandeId; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setEarnedDate(LocalDate earnedDate) { this.earnedDate = earnedDate; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "DeliveryEarning{" +
                "id=" + id +
                ", deliveryGuyId=" + deliveryGuyId +
                ", commandeId=" + commandeId +
                ", amount=" + amount +
                ", earnedDate=" + earnedDate +
                ", status='" + status + '\'' +
                '}';
    }
}
