package com.coldchain.simulator;

public class InventoryItem {
    public String itemId;
    public String storeId;
    public String itemName;
    public boolean requiresRefrigeration;
    public int quantityLbs;
    public int daysUntilExpiry; // ⬅️ NEW FIELD

      
    public InventoryItem() {

    }

    public InventoryItem(String itemId, String storeId, String itemName, boolean requiresRefrigeration, int quantityLbs, int daysUntilExpiry) {
        this.itemId = itemId;
        this.storeId = storeId;
        this.itemName = itemName;
        this.requiresRefrigeration = requiresRefrigeration;
        this.quantityLbs = quantityLbs;
        this.daysUntilExpiry = daysUntilExpiry;
    }

    
    public String getItemName() { return itemName; }
    public boolean isRequiresRefrigeration() { return requiresRefrigeration; }
    public int getQuantityLbs() { return quantityLbs; }
    public int getDaysUntilExpiry() { return daysUntilExpiry; }
}