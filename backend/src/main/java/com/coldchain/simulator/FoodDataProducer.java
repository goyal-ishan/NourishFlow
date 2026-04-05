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

    // 🇮🇳 Indian Retailers
    private final String[] indianStores = {
        "Reliance Fresh - Mumbai", 
        "Big Bazaar - Delhi", 
        "Nature's Basket - Bangalore", 
        "Star Bazaar - Pune",
        "Zomato Hyperpure - Hyderabad",
        "blinkit Dark Store - Gurgaon"
    };

    // 🥦 Indian Grocery Items
    private final String[] foodItems = {
        "Fresh Paneer", "Amul Gold Milk", "Alphonso Mangoes", 
        "Organic Palak", "Greek Yogurt", "Desi Ghee", "Tofu"
    };

    @Scheduled(fixedRate = 5000)
    public void produceRandomFood() {
        // Pick a random store and a random item
        String storeName = indianStores[random.nextInt(indianStores.length)];
        String itemName = foodItems[random.nextInt(foodItems.length)];
        
        // Logic: Dairy/Meat usually needs refrigeration, Mangoes/Veg might vary
        boolean needsFridge = itemName.contains("Paneer") || itemName.contains("Milk") || itemName.contains("Yogurt");

        InventoryItem item = new InventoryItem(
            UUID.randomUUID().toString().substring(0, 8),
            storeName, // No longer "Walmart-Store-101"!
            itemName,
            needsFridge, 
            random.nextInt(50) + 5, // 5 to 55 lbs
            random.nextInt(7) + 1   // 1 to 7 days until expiry (High risk)
        );

        System.out.println("🚀 PRODUCING: [" + item.storeId + "] is shipping " + item.itemName);
        
        kafkaTemplate.send("food-inventory-stream", item);
    }
}