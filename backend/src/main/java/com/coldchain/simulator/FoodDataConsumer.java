package com.coldchain.simulator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;

@Service
public class FoodDataConsumer {

    private static final Logger logger = LoggerFactory.getLogger(FoodDataConsumer.class);
    
    private final Random random = new Random();
    
    @Autowired
    private MatchHistoryRepository matchHistoryRepository;
    
    @Autowired
    private MLPredictionClient mlClient;
    
    private final Set<String> processedItemIds = new HashSet<>();
    
    private final String[] charityCenters = {
        "Feeding India (Zomato)", 
        "Akshaya Patra Foundation", 
        "Robin Hood Army - Local Chapter", 
        "Goonj - Rahat Food Center",
        "Roti Bank Mumbai",
        "No Food Waste NGO",
        "India FoodBanking Network"
    };

    //Base city coordinates (Centered on Prayagraj / Allahabad)
    private final double BASE_LAT = 25.4358;
    private final double BASE_LNG = 81.8463;

    @KafkaListener(
        topics = "food-inventory-stream", 
        groupId = "cold-storage-matchers",
        concurrency = "1"
    )
    public void consumeFoodData(InventoryItem item) {
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🇮🇳 REAL-TIME LOGISTICS UPDATE: Incoming Stock");
        System.out.println("=".repeat(60));
        System.out.println("📍 Origin Store: " + item.storeId);
        System.out.println("📦 Item: " + item.getItemName());
        System.out.println("🆔 Item ID: " + item.itemId);
        System.out.println("⚖️ Weight: " + item.getQuantityLbs() + " lbs");
        System.out.println("⏳ Inventory Window: " + item.getDaysUntilExpiry() + " days");
        System.out.println("❄️ Refrigeration Required: " + (item.isRequiresRefrigeration() ? "YES" : "NO"));
        
        // Check duplicate 
        if (isDuplicate(item.itemId)) {
            System.out.println("   ⚠️ DUPLICATE DETECTED: Item ID " + item.itemId + " already processed!");
            System.out.println("   ⏭️ SKIPPING: Ignoring duplicate message\n");
            return;
        }
        
       
        processedItemIds.add(item.itemId);
        System.out.println("   ✅ NEW MESSAGE: Processing item " + item.itemId);
        
        double storageTemp = item.isRequiresRefrigeration() ? 4.0 : 20.0;
        double humidity = 65.0;
        
        Map<String, Object> expiryPrediction = mlClient.getPredictedExpiry(
            item.getItemName(), 
            storageTemp, 
            humidity, 
            item.getDaysUntilExpiry()
        );
        
        double predictedHours = (double) expiryPrediction.get("predicted_hours_remaining");
        boolean criticalRisk = (boolean) expiryPrediction.get("critical_risk");
        String riskLevel = (String) expiryPrediction.get("risk_level");
        
        System.out.println("\n🤖 ML EXPIRY PREDICTION:");
        System.out.println("   ⏱️ Predicted Hours Remaining: " + predictedHours);
        System.out.println("   🚨 Risk Level: " + riskLevel);
        System.out.println("   ⚠️ Critical Risk: " + (criticalRisk ? "YES ❌" : "NO ✅"));
        
        
        if (criticalRisk || item.getDaysUntilExpiry() <= 2) {
            System.out.println("\n📢 ACTION: Triggering food redistribution...");
            matchWithCharityML(item, storageTemp, humidity);
        } else {
            System.out.println("\n✅ STATUS: Shelf life sufficient. Retaining in store.");
        }
        System.out.println("=".repeat(60) + "\n");
    }

    private boolean isDuplicate(String itemId) {
        return processedItemIds.contains(itemId);
    }

    /**
     * Enhanced matching with ML optimization for NGO selection
     */
    private void matchWithCharityML(InventoryItem item, double temp, double humidity) {
        
       
        List<Map<String, Object>> ngoList = createNGOCandidates();
        
        
        Map<String, Object> routingResult = mlClient.getOptimalRouting(
            item.getItemName(),
            temp,
            humidity,
            item.getDaysUntilExpiry(),
            ngoList
        );
        
        String bestNgoId = (String) routingResult.get("recommended_ngo_id");
        String bestNgoName = (String) routingResult.get("recommended_ngo_name");
        double confidence = (double) routingResult.get("confidence_score");
        String reasoning = (String) routingResult.get("reasoning");
        
        System.out.println("🤖 ML ROUTING OPTIMIZATION:");
        System.out.println("   🏢 Recommended NGO: " + bestNgoName);
        System.out.println("   📊 Confidence Score: " + confidence);
        System.out.println("   💭 Reasoning: " + reasoning);
        
        // Simulate if NGO has cold storage
        boolean charityHasFridge = random.nextBoolean();
        System.out.println("   ❄️ NGO Cold Storage Status: " + (charityHasFridge ? "ACTIVE ✅" : "NONE ❌"));

        // The Match Decision
        if (item.isRequiresRefrigeration() && !charityHasFridge) {
            System.out.println("   ❌ MATCH FAILED: " + bestNgoName + " cannot handle " + item.getItemName() + " (Refrigeration Required)");
            System.out.println("   🔄 REROUTING: Searching for alternative...");
            
        } else {
            System.out.println("   ✅ MATCH SUCCESSFUL: Routing " + item.getItemName() + " to " + bestNgoName + "!");
            
            double sourceLat = BASE_LAT + (random.nextDouble() - 0.5) * 0.10;
            double sourceLng = BASE_LNG + (random.nextDouble() - 0.5) * 0.10;
            double destLat = BASE_LAT + (random.nextDouble() - 0.5) * 0.10;
            double destLng = BASE_LNG + (random.nextDouble() - 0.5) * 0.10;
            
            // Save to database
            try {
                MatchHistory newMatch = new MatchHistory(
                    item.itemId,
                    item.storeId,
                    item.getItemName(),
                    item.getQuantityLbs(),
                    item.getDaysUntilExpiry(),
                    bestNgoName,
                    sourceLat,
                    sourceLng,
                    destLat,
                    destLng
                );
                
                matchHistoryRepository.save(newMatch);
                System.out.println("   💾 DATABASE: Match record saved successfully");
                System.out.println("   📍 Route: (" + String.format("%.4f", sourceLat) + ", " + String.format("%.4f", sourceLng) + ") → (" + String.format("%.4f", destLat) + ", " + String.format("%.4f", destLng) + ")");
            } catch (Exception e) {
                System.out.println("   ❌ ERROR: Failed to save to database: " + e.getMessage());
            }
        }
    }

    /**
     * Creates a list of nearby NGO candidates for ML optimization
     */
    private List<Map<String, Object>> createNGOCandidates() {
        List<Map<String, Object>> ngos = new ArrayList<>();
        
        for (int i = 0; i < charityCenters.length; i++) {
            Map<String, Object> ngo = new HashMap<>();
            ngo.put("ngo_id", "NGO_" + i);
            ngo.put("name", charityCenters[i]);
            ngo.put("distance_km", random.nextDouble() * 10 + 1);  // 1-11 km
            ngo.put("current_time_hour", LocalDateTime.now().getHour());
            ngo.put("avg_daily_capacity_lbs", random.nextDouble() * 500 + 100);  // 100-600 lbs
            ngos.add(ngo);
        }
        
        return ngos;
    }
}