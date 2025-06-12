package model;

public class City {
    private String name;
    private double deliveryFee;
    private int deliveryDays;
    private String region;

    public City(String name, double deliveryFee, int deliveryDays, String region) {
        this.name = name;
        this.deliveryFee = deliveryFee;
        this.deliveryDays = deliveryDays;
        this.region = region;
    }

    // Getters
    public String getName() { return name; }
    public double getDeliveryFee() { return deliveryFee; }
    public int getDeliveryDays() { return deliveryDays; }
    public String getRegion() { return region; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setDeliveryFee(double deliveryFee) { this.deliveryFee = deliveryFee; }
    public void setDeliveryDays(int deliveryDays) { this.deliveryDays = deliveryDays; }
    public void setRegion(String region) { this.region = region; }

    @Override
    public String toString() {
        return name + " (" + deliveryFee + " DH - " + deliveryDays + " days)";
    }
}
