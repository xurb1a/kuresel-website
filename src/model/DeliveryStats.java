package model;

import java.time.LocalDateTime;

public class DeliveryStats {
    private int id;
    private int deliveryGuyId;
    private int totalDeliveries;
    private double totalEarnings;
    private int successfulDeliveries;
    private LocalDateTime lastUpdated;

    public DeliveryStats(int id, int deliveryGuyId, int totalDeliveries, double totalEarnings,
                         int successfulDeliveries, LocalDateTime lastUpdated) {
        this.id = id;
        this.deliveryGuyId = deliveryGuyId;
        this.totalDeliveries = totalDeliveries;
        this.totalEarnings = totalEarnings;
        this.successfulDeliveries = successfulDeliveries;
        this.lastUpdated = lastUpdated;
    }

    // Getters
    public int getId() { return id; }
    public int getDeliveryGuyId() { return deliveryGuyId; }
    public int getTotalDeliveries() { return totalDeliveries; }
    public double getTotalEarnings() { return totalEarnings; }
    public int getSuccessfulDeliveries() { return successfulDeliveries; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setDeliveryGuyId(int deliveryGuyId) { this.deliveryGuyId = deliveryGuyId; }
    public void setTotalDeliveries(int totalDeliveries) { this.totalDeliveries = totalDeliveries; }
    public void setTotalEarnings(double totalEarnings) { this.totalEarnings = totalEarnings; }
    public void setSuccessfulDeliveries(int successfulDeliveries) { this.successfulDeliveries = successfulDeliveries; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    @Override
    public String toString() {
        return "DeliveryStats{" +
                "id=" + id +
                ", deliveryGuyId=" + deliveryGuyId +
                ", totalDeliveries=" + totalDeliveries +
                ", totalEarnings=" + totalEarnings +
                ", successfulDeliveries=" + successfulDeliveries +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
