package com.coldchain.simulator;

public class InventoryItem {
    public String itemId;
    public String storeId;
    public String itemName;
    public boolean requiresRefrigeration;
    public int quantityLbs;
    public InventoryItem(String itemId, String storeId, String itemName, boolean requiresRefrigeration, int quantityLbs) {
        this.itemId = itemId;
        this.storeId = storeId;
        this.itemName = itemName;
        this.requiresRefrigeration = requiresRefrigeration;
        this.quantityLbs = quantityLbs;
    }
}
