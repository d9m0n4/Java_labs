package ru.vyatsu.model;

import javafx.beans.property.*;

import java.util.Date;

public class Purchase {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty supplierName = new SimpleStringProperty();
    private final StringProperty partName = new SimpleStringProperty();
    private final ObjectProperty<Date> purchaseDate = new SimpleObjectProperty<>();
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    private final DoubleProperty priceAtPurchase = new SimpleDoubleProperty();

    public Purchase(int id, String supplierName, String partName, Date purchaseDate, int quantity, double priceAtPurchase) {
        this.id.set(id);
        this.supplierName.set(supplierName);
        this.partName.set(partName);
        this.purchaseDate.set(purchaseDate);
        this.quantity.set(quantity);
        this.priceAtPurchase.set(priceAtPurchase);
    }

    public IntegerProperty idProperty() { return id; }
    public StringProperty supplierNameProperty() { return supplierName; }
    public StringProperty partNameProperty() { return partName; }
    public ObjectProperty<Date> purchaseDateProperty() { return purchaseDate; }
    public IntegerProperty quantityProperty() { return quantity; }
    public DoubleProperty priceAtPurchaseProperty() { return priceAtPurchase; }

    public String getSupplierName() { return supplierName.get(); }
    public String getPartName() { return partName.get(); }

    public int getQuantity() { return quantity.get(); }
    public double getPriceAtPurchase() { return priceAtPurchase.get(); }
}
