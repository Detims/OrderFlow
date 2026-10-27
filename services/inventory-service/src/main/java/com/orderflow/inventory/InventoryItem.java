package com.orderflow.inventory;
import jakarta.persistence.*;
@Entity @Table(name="inventory")
public class InventoryItem {
    @Id @Column(length=80) private String sku;
    @Column(nullable=false) private int available;
    protected InventoryItem() {}
    public InventoryItem(String sku, int available) { this.sku=sku; this.available=available; }
    public String getSku(){return sku;} public int getAvailable(){return available;}
}
