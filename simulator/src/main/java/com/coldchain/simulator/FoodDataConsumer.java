package com.coldchain.simulator;

import org.springframework.beans.factory.annotation.Autowired; // NEW IMPORT
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class FoodDataConsumer {

    private final Random random = new Random();
    
    // --- NEW: Inject the database saver ---
    @Autowired
    private MatchHistoryRepository matchHistoryRepository;
    
    // A mock list of local charity centers
    private final String[] charityCenters = {
        "Downtown Community Kitchen", 
        "Hope Food Bank", 
        "City Orphanage", 
        "Green Valley Shelter",
        "Salvation Army Center"
    };

    @KafkaListener(topics = "food-inventory-stream", groupId = "cold-storage-matchers")
    public void consumeFoodData(InventoryItem item) {
        
        System.out.println("\n📥 NEW INVENTORY ALERT TRIGGERED");
        System.out.println("   🏪 Origin: " + item.storeId);
        System.out.println("   📦 Item: " + item.getItemName());
        System.out.println("   ⚖️ Quantity: " + item.getQuantityLbs() + " lbs");
        System.out.println("   ⏳ Expires in: " + item.getDaysUntilExpiry() + " days");
        System.out.println("   ❄️ Needs Refrigeration: " + (item.isRequiresRefrigeration() ? "YES" : "NO"));
        
        // Only trigger the charity matching logic if the item is expiring soon (e.g., 5 days or less)
        if (item.getDaysUntilExpiry() <= 5) {
            System.out.println("   ⚠️ HIGH RISK: Item expiring soon. Initiating Charity Search...");
            matchWithCharity(item);
        } else {
            System.out.println("   ✅ SAFE: Item has plenty of shelf life. No immediate action required.");
        }
        System.out.println("--------------------------------------------------");
    }

    // Helper method to simulate finding a nearby charity
    private void matchWithCharity(InventoryItem item) {
        // 1. Pick a random charity from our list
        String nearbyCharity = charityCenters[random.nextInt(charityCenters.length)];
        
        // 2. Randomly determine if this specific charity has an industrial fridge available
        boolean charityHasFridge = random.nextBoolean(); 
        
        System.out.println("   🔍 SEARCHING: Found nearby center -> " + nearbyCharity);
        System.out.println("   ❄️ Center Accepts Refrigerated Goods: " + (charityHasFridge ? "YES" : "NO"));

        // The Match Logic
        if (item.isRequiresRefrigeration() && !charityHasFridge) {
            System.out.println("   ❌ MATCH FAILED: " + nearbyCharity + " cannot safely store " + item.getItemName() + ".");
            // important to resume the search
            System.out.println("   🔄 ACTION: Rerouting search to find a facility with cold storage...");
        } else {
            System.out.println("   🤝 MATCH SUCCESS: Dispatching " + item.getQuantityLbs() + " lbs of " + item.getItemName() + " to " + nearbyCharity + "!");
            
            // --- NEW: Save the successful match to PostgreSQL ---
            MatchHistory newMatch = new MatchHistory(
                item.storeId,               // Origin Store
                item.getItemName(),         // Item Name
                item.getQuantityLbs(),      // Quantity
                item.getDaysUntilExpiry(),  // Expiry Days
                nearbyCharity               // Charity Name
            );
            
            matchHistoryRepository.save(newMatch);
            System.out.println("   💾 SAVED: Match successfully recorded in database!");
        }
    }
}