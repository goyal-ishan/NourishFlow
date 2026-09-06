package com.coldchain.simulator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.Random;
import java.util.UUID;

@Service
public class FoodDataProducer {

    @Autowired
    private KafkaTemplate<String, InventoryItem> kafkaTemplate;

    private final Random random = new Random();

    private final String[] localStores = {
        "Reliance Fresh - Civil Lines", 
        "Spencer's Retail - Katra", 
        "Blinkit Dark Store - Jhalwa (Near IIIT)", 
        "Vishal Mega Mart - Chowk",
        "Zomato Hyperpure - Naini",
        "Nature's Basket - Ashok Nagar"
    };
    
    private final String[] foodItems = {
        "Fresh Paneer", "Amul Gold Milk", "Alphonso Mangoes", 
        "Organic Palak", "Greek Yogurt", "Desi Ghee", "Tofu"
    };
    
    @Scheduled(fixedRate = 5000)
    public void produceRandomFood() {
       
        String storeName = localStores[random.nextInt(localStores.length)];
        String itemName = foodItems[random.nextInt(foodItems.length)];
        
       
        boolean needsFridge = itemName.contains("Paneer") || itemName.contains("Milk") || itemName.contains("Yogurt");

        String uniqueItemId = UUID.randomUUID().toString();
        
        InventoryItem item = new InventoryItem(
            uniqueItemId,  //Unique ID for duplicate detection
            storeName, 
            itemName,
            needsFridge, 
            random.nextInt(50) + 5, // 5 to 55 lbs
            random.nextInt(7) + 1   // 1 to 7 days until expiry (High risk)
        );

        System.out.println(" 🚚 PRODUCING: [" + item.storeId + "] is shipping " + item.itemName);
        System.out.println("   📦 UNIQUE ID: " + uniqueItemId);
        
        
        // All messages from same store go to same partition
        kafkaTemplate.send("food-inventory-stream", storeName, item);
        System.out.println("   🔑 PARTITION KEY: " + storeName + " → Messages from same store will be ordered\n");
    }
}
