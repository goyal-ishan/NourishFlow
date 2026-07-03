package com.coldchain.simulator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MLPredictionClient {

    private static final Logger logger = LoggerFactory.getLogger(MLPredictionClient.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${ml.engine.url:http://localhost:8000}")
    private String ML_ENGINE_URL;
    
    // ============================================
    // FEATURE 1: Predictive Expiry Forecasting
    // ============================================
    
    /**
     * Calls Python ML service to predict exact expiry hours for a food item
     * 
     * @param itemType Name of the food item (e.g., "Fresh Paneer")
     * @param temp Current storage temperature in Celsius
     * @param humidity Current humidity percentage
     * @param daysUntilExpiry Days remaining from inventory
     * @return Predicted hours remaining before spoilage
     */
    public Map<String, Object> getPredictedExpiry(String itemType, double temp, double humidity, int daysUntilExpiry) {
        String url = ML_ENGINE_URL + "/predict-expiry";
        
        try {
            Map<String, Object> request = new HashMap<>();
            Map<String, Object> item = new HashMap<>();
            
            item.put("item_type", itemType);
            item.put("weight_lbs", 10.0);
            item.put("storage_temp_c", temp);
            item.put("humidity_percent", humidity);
            item.put("days_until_expiry", daysUntilExpiry);
            
            request.put("item", item);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("✅ ML Expiry Prediction Success: " + itemType);
                return response.getBody();
            }
        } catch (RestClientException e) {
            logger.error("❌ ML Service Error (Expiry Prediction): " + e.getMessage());
        }
        
        // Fallback: Default prediction if ML service unavailable
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("predicted_hours_remaining", daysUntilExpiry * 24.0);
        fallback.put("critical_risk", daysUntilExpiry <= 2);
        fallback.put("risk_level", daysUntilExpiry <= 2 ? "CRITICAL" : "MEDIUM");
        return fallback;
    }
    
    // ============================================
    // FEATURE 2: Demand Optimization & Routing
    // ============================================
    
    /**
     * Calls Python ML service to find optimal NGO for food redistribution
     * 
     * @param itemType Name of the food item
     * @param temp Storage temperature
     * @param humidity Humidity percentage
     * @param daysUntilExpiry Days remaining
     * @param ngoList List of nearby NGOs with their details
     * @return Best matching NGO with confidence score
     */
    public Map<String, Object> getOptimalRouting(
            String itemType, 
            double temp, 
            double humidity, 
            int daysUntilExpiry,
            List<Map<String, Object>> ngoList) {
        
        String url = ML_ENGINE_URL + "/optimize-routing";
        
        try {
            Map<String, Object> request = new HashMap<>();
            Map<String, Object> item = new HashMap<>();
            
            item.put("item_type", itemType);
            item.put("weight_lbs", 10.0);
            item.put("storage_temp_c", temp);
            item.put("humidity_percent", humidity);
            item.put("days_until_expiry", daysUntilExpiry);
            
            request.put("item", item);
            request.put("ngos", ngoList);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("✅ ML Routing Optimization Success: " + itemType);
                return response.getBody();
            }
        } catch (RestClientException e) {
            logger.error("❌ ML Service Error (Routing Optimization): " + e.getMessage());
        }
        
        // Fallback: Random selection if ML service unavailable
        Map<String, Object> fallback = new HashMap<>();
        if (!ngoList.isEmpty()) {
            Map<String, Object> randomNgo = ngoList.get(0);
            fallback.put("recommended_ngo_id", randomNgo.get("ngo_id"));
            fallback.put("recommended_ngo_name", randomNgo.get("name"));
            fallback.put("confidence_score", 0.5);
            fallback.put("reasoning", "ML service unavailable, using default selection");
        }
        return fallback;
    }
    
    /**
     * Health check to verify ML service is running
     */
    public boolean isMLServiceHealthy() {
        try {
            String url = ML_ENGINE_URL + "/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            logger.warn("⚠️ ML Service Health Check Failed: " + e.getMessage());
            return false;
        }
    }
}