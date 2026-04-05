package com.coldchain.simulator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class FoodDataConsumer {

    private final Random random = new Random();
    
    @Autowired
    private MatchHistoryRepository matchHistoryRepository;
    
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

    @KafkaListener(topics = "food-inventory-stream", groupId = "cold-storage-matchers")
    public void consumeFoodData(InventoryItem item) {
        
        System.out.println("\n🇮🇳 LOGISTICS UPDATE: Incoming Stock from " + item.storeId);
        System.out.println("   📦 Item: " + item.getItemName());
        System.out.println("   ⚖️ Weight: " + item.getQuantityLbs() + " lbs");
        System.out.println("   ⏳ Expiry Window: " + item.getDaysUntilExpiry() + " days");
        System.out.println("   ❄️ Cold Storage Req: " + (item.isRequiresRefrigeration() ? "YES" : "NO"));
        
        // Match logic: Items with 5 days or less are redirected to NGOs
        if (item.getDaysUntilExpiry() <= 5) {
            System.out.println("   ⚠️ ACTION: Redirecting surplus to prevent waste...");
            matchWithCharity(item);
        } else {
            System.out.println("   ✅ STATUS: Shelf life sufficient. Retaining in store.");
        }
        System.out.println("--------------------------------------------------");
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
            
            // 💾 Save the match to PostgreSQL
            MatchHistory newMatch = new MatchHistory(
                item.storeId,               // e.g., Reliance Fresh
                item.getItemName(),         // e.g., Paneer
                item.getQuantityLbs(), 
                item.getDaysUntilExpiry(), 
                nearbyCharity               // e.g., Akshaya Patra
            );
            
            matchHistoryRepository.save(newMatch);
            System.out.println("   💾 DATABASE: Record updated for localized distribution.");
        }
    }
}