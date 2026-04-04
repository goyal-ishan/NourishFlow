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
    private final String[] foodItems = {"Fresh Milk", "Organic Spinach", "Greek Yogurt", "Ground Beef", "Strawberries"};

    // This runs every 5 seconds automatically!
    @Scheduled(fixedRate = 5000)
    public void produceRandomFood() {
        String name = foodItems[random.nextInt(foodItems.length)];
        
        // Create a random food item
        InventoryItem item = new InventoryItem(
            UUID.randomUUID().toString().substring(0, 8),
            "Walmart-Store-101",
            name,
            random.nextBoolean(), // Randomly needs fridge or not
            random.nextInt(50) + 1, // 1 to 50 lbs
            random.nextInt(10) + 1  // 1 to 10 days until expiry
        );

        System.out.println("🚀 PRODUCING: Sending " + item.itemName + " to Kafka...");
        
        // Send it to the topic your consumer is listening to
        kafkaTemplate.send("food-inventory-stream", item);
    }
}