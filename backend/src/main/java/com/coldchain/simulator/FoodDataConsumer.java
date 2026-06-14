package com.coldchain.simulator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;

@Service
public class FoodDataConsumer {

    private final Random random = new Random();
    
    @Autowired
    private MatchHistoryRepository matchHistoryRepository;
    
    // ✅ IN-MEMORY CACHE FOR DUPLICATE DETECTION
    private final Set<String> processedItemIds = new HashSet<>();
    
    // 🇮🇳 Updated: Renowned Indian Food Banks & NGOs
    private final String[] charityCenters = {
        "Feeding India (Zomato)", 
        "Akshaya Patra Foundation", 
        "Robin Hood Army - Local Chapter", 
        "Goonj - Rahat Food Center",
        "Roti Bank Mumbai",
        "No Food Waste NGO",
        "India FoodBanking Network"
    };

    // 🗺️ Map Configurations: Base city coordinates (Centered on Prayagraj / Allahabad)
    private final double BASE_LAT = 25.4358;
    private final double BASE_LNG = 81.8463;

    // ✅ PARTITION KEY AS TOPIC (Ensures ordering by store)
    @KafkaListener(
        topics = "food-inventory-stream", 
        groupId = "cold-storage-matchers",
        concurrency = "1"  // ← ONE consumer at a time to ensure ordering
    )
    public void consumeFoodData(InventoryItem item) {
        
        System.out.println("\n🇮🇳 LOGISTICS UPDATE: Incoming Stock from " + item.storeId);
        System.out.println("   📦 Item: " + item.getItemName());
        System.out.println("   🆔 Item ID: " + item.itemId);
        System.out.println("   ⚖️ Weight: " + item.getQuantityLbs() + " lbs");
        System.out.println("   ⏳ Expiry Window: " + item.getDaysUntilExpiry() + " days");
        System.out.println("   ❄️ Cold Storage Req: " + (item.isRequiresRefrigeration() ? "YES" : "NO"));
        
        // ✅ DUPLICATE CHECK
        if (isDuplicate(item.itemId)) {
            System.out.println("   ⚠️ DUPLICATE DETECTED: Item ID " + item.itemId + " already processed!");
            System.out.println("   ⏭️ SKIPPING: Ignoring duplicate message\n");
            return;  // Skip duplicate
        }
        
        // Mark as processed
        processedItemIds.add(item.itemId);
        System.out.println("   ✅ NEW MESSAGE: Processing item " + item.itemId);
        
        // Match logic: Items with 5 days or less are redirected to NGOs
        if (item.getDaysUntilExpiry() <= 5) {
            System.out.println("   ⚠️ ACTION: Redirecting surplus to prevent waste...");
            matchWithCharity(item);
        } else {
            System.out.println("   ✅ STATUS: Shelf life sufficient. Retaining in store.");
        }
        System.out.println("--------------------------------------------------");
    }

    // ✅ DUPLICATE DETECTION METHOD
    private boolean isDuplicate(String itemId) {
        return processedItemIds.contains(itemId);
    }

    private void matchWithCharity(InventoryItem item) {
        // 1. Pick a random Indian Charity
        String nearbyCharity = charityCenters[random.nextInt(charityCenters.length)];
        
        // 2. Simulate if they have cold storage (Industrial Fridges)
        boolean charityHasFridge = random.nextBoolean(); 
        
        System.out.println("   🔍 SCANNING: Nearest NGO found -> " + nearbyCharity);
        System.out.println("   ❄️ NGO Cold Storage Status: " + (charityHasFridge ? "ACTIVE" : "NONE"));

        // The Match Logic
        if (item.isRequiresRefrigeration() && !charityHasFridge) {
            System.out.println("   ❌ MATCH FAILED: " + nearbyCharity + " cannot store " + item.getItemName() + " (Fridge Required).");
            System.out.println("   🔄 REROUTING: Searching for another local NGO...");
        } else {
            System.out.println("   🤝 MATCH SUCCESS: Routing " + item.getItemName() + " to " + nearbyCharity + "!");
            
            // ==============================================================
            // 🗺️ GENERATE SIMULATED GEOSPATIAL COORDINATES
            // ==============================================================
            double sourceLat = BASE_LAT + (random.nextDouble() - 0.5) * 0.10;
            double sourceLng = BASE_LNG + (random.nextDouble() - 0.5) * 0.10;
            
            double destLat = BASE_LAT + (random.nextDouble() - 0.5) * 0.10;
            double destLng = BASE_LNG + (random.nextDouble() - 0.5) * 0.10;
            
            // ✅ WITH ERROR HANDLING FOR DATABASE SAVE
            try {
                MatchHistory newMatch = new MatchHistory(
                    item.itemId,                // ← UNIQUE ITEM ID
                    item.storeId,               
                    item.getItemName(),         
                    item.getQuantityLbs(), 
                    item.getDaysUntilExpiry(), 
                    nearbyCharity,              
                    sourceLat,                  
                    sourceLng,                  
                    destLat,                    
                    destLng                     
                );
                
                matchHistoryRepository.save(newMatch);
                System.out.println("   💾 DATABASE: Record saved successfully with coordinates.");
            } catch (Exception e) {
                System.out.println("   ❌ ERROR: Failed to save to database: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
