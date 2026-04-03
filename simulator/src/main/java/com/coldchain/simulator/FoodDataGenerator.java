package com.coldchain.simulator;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
public class FoodDataGenerator {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Random random = new Random();
    
    // The name of the Kafka queue (topic) we are sending data to
    private static final String TOPIC = "food-inventory-stream";

    public FoodDataGenerator(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // Run this function automatically every 2 seconds (2000 ms)
    @Scheduled(fixedRate = 2000)
    public void generateAndSend() {
        String[] foods = {"Milk", "Apples", "Raw Chicken", "Canned Soup", "Yogurt"};
        String randomFood = foods[random.nextInt(foods.length)];
        
        // If the food is Milk, Chicken, or Yogurt, flag it as needing a fridge!
        boolean needsFridge = randomFood.equals("Milk") || randomFood.equals("Raw Chicken") || randomFood.equals("Yogurt");
        
        InventoryItem item = new InventoryItem(
                UUID.randomUUID().toString(),
                "STORE_" + (random.nextInt(10) + 1), // Random store from 1 to 10
                randomFood,
                needsFridge,
                random.nextInt(45) + 5 // Random weight between 5 and 50 lbs
        );

        // Send the item to Kafka
        kafkaTemplate.send(TOPIC, item);
        System.out.println("🚨 SUPERMARKET SCANNED: " + item.quantityLbs + " lbs of " + item.itemName + " -> Sent to Kafka!");
    }
}