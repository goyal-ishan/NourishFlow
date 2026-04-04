package com.coldchain.simulator;

public class InventoryItem {
    public String itemId;
    public String storeId; // This will now hold our Indian Store names
    public String itemName;
    public boolean requiresRefrigeration;
    public int quantityLbs;
    public int daysUntilExpiry;

    public InventoryItem() {}

    public InventoryItem(String itemId, String storeId, String itemName, boolean requiresRefrigeration, int quantityLbs, int daysUntilExpiry) {
        this.itemId = itemId;
        this.storeId = storeId;
        this.itemName = itemName;
        this.requiresRefrigeration = requiresRefrigeration;
        this.quantityLbs = quantityLbs;
        this.daysUntilExpiry = daysUntilExpiry;
    }

    // Getters for Spring/Jackson to convert to JSON
    public String getItemName() { return itemName; }
    public boolean isRequiresRefrigeration() { return requiresRefrigeration; }
    public int getQuantityLbs() { return quantityLbs; }
    public int getDaysUntilExpiry() { return daysUntilExpiry; }
}