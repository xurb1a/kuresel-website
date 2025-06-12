package model;

import javafx.beans.property.*;

import java.time.LocalDate;

public class RecentOrder {
    private final IntegerProperty orderId;
    private final ObjectProperty<LocalDate> orderDate;
    private final StringProperty orderStatus;

    public RecentOrder(int orderId, LocalDate orderDate, String orderStatus) {
        this.orderId = new SimpleIntegerProperty(orderId);
        this.orderDate = new SimpleObjectProperty<>(orderDate);
        this.orderStatus = new SimpleStringProperty(orderStatus);
    }

    public IntegerProperty orderIdProperty() {
        return orderId;
    }

    public ObjectProperty<LocalDate> orderDateProperty() {
        return orderDate;
    }

    public StringProperty orderStatusProperty() {
        return orderStatus;
    }
}
