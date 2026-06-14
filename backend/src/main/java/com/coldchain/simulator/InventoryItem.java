package com.coldchain.simulator;

public class InventoryItem {
    
    public String itemId;           // ← Unique identifier for each message
    public String storeId;          // Store name (used as partition key)
    public String itemName;
    public boolean requiresRefrigeration;
    public int quantityLbs;
    public int daysUntilExpiry;
    public long timestamp;          // ← ADDED: For duplicate detection with time window

    public InventoryItem() {}

    public InventoryItem(String itemId, String storeId, String itemName, boolean requiresRefrigeration, int quantityLbs, int daysUntilExpiry) {
        this.itemId = itemId;
        this.storeId = storeId;
        this.itemName = itemName;
        this.requiresRefrigeration = requiresRefrigeration;
        this.quantityLbs = quantityLbs;
        this.daysUntilExpiry = daysUntilExpiry;
        this.timestamp = System.currentTimeMillis();  // ← Set current timestamp
    }

    // Getters for Spring/Jackson to convert to JSON
    public String getItemName() { return itemName; }
    public boolean isRequiresRefrigeration() { return requiresRefrigeration; }
    public int getQuantityLbs() { return quantityLbs; }
    public int getDaysUntilExpiry() { return daysUntilExpiry; }
    public String getItemId() { return itemId; }
    public String getStoreId() { return storeId; }
    public long getTimestamp() { return timestamp; }
}
