package com.coldchain.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MLPredictionClient {

    private static final Logger logger = LoggerFactory.getLogger(MLPredictionClient.class);

    @Value("${ml.engine.url:http://localhost:8000}")
    private String ML_ENGINE_URL;

    @Value("${ml.engine.enabled:true}")
    private boolean mlEngineEnabled;

    private RestTemplate createRestTemplateWithTimeout(int timeoutMillis) {
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(timeoutMillis);
        rf.setReadTimeout(timeoutMillis);
        return new RestTemplate(rf);
    }

    // ============================================
    // FEATURE 1: Predictive Expiry Forecasting
    // ============================================
    public Map<String, Object> getPredictedExpiry(String itemType, double temp, double humidity, int daysUntilExpiry) {
        if (!mlEngineEnabled) {
            return getExpiryFallback(daysUntilExpiry);
        }

        String url = ML_ENGINE_URL + "/predict-expiry";
        RestTemplate localTemplate = createRestTemplateWithTimeout(3000);

        try {
            Map<String, Object> request = new HashMap<>();
            Map<String, Object> item = new HashMap<>();

            item.put("item_type", itemType);
            item.put("weight_lbs", 10.0);
            item.put("storage_temp_c", temp);
            item.put("humidity_percent", humidity);
            item.put("days_until_expiry", daysUntilExpiry);

            request.put("item", item);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = localTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("✅ ML Expiry Prediction Success: {}", itemType);
                return response.getBody();
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            String body = e.getResponseBodyAsString();
            if (body != null && body.length() > 150) {
                body = body.substring(0, 150) + "... [truncated]";
            }
            logger.error("❌ ML Service HTTP Error {}: {} - {}", e.getStatusCode(), url, body);
        } catch (ResourceAccessException e) {
            logger.error("❌ ML Service Timeout / Connection Failed for {}: {}", url, e.getMessage());
        } catch (Exception e) {
            logger.error("❌ ML Prediction Error: {}", e.getMessage());
        }

        return getExpiryFallback(daysUntilExpiry);
    }

    private Map<String, Object> getExpiryFallback(int daysUntilExpiry) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("predicted_hours_remaining", daysUntilExpiry * 24.0);
        fallback.put("critical_risk", daysUntilExpiry <= 2);
        fallback.put("risk_level", daysUntilExpiry <= 2 ? "CRITICAL" : "MEDIUM");
        return fallback;
    }

    // ============================================
    // FEATURE 2: Demand Optimization & Routing
    // ============================================
    public Map<String, Object> getOptimalRouting(
            String itemType, 
            double temp, 
            double humidity, 
            int daysUntilExpiry,
            List<Map<String, Object>> ngoList) {

        if (!mlEngineEnabled) {
            return getRoutingFallback(ngoList);
        }

        String url = ML_ENGINE_URL + "/optimize-routing";
        RestTemplate localTemplate = createRestTemplateWithTimeout(3000);

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

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = localTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("✅ ML Routing Optimization Success: {}", itemType);
                return response.getBody();
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            String body = e.getResponseBodyAsString();
            if (body != null && body.length() > 150) {
                body = body.substring(0, 150) + "... [truncated]";
            }
            logger.error("❌ ML Service HTTP Error {}: {} - {}", e.getStatusCode(), url, body);
        } catch (ResourceAccessException e) {
            logger.error("❌ ML Service Timeout / Connection Failed for {}: {}", url, e.getMessage());
        } catch (Exception e) {
            logger.error("❌ ML Routing Error: {}", e.getMessage());
        }

        return getRoutingFallback(ngoList);
    }

    private Map<String, Object> getRoutingFallback(List<Map<String, Object>> ngoList) {
        Map<String, Object> fallback = new HashMap<>();
        if (ngoList != null && !ngoList.isEmpty()) {
            Map<String, Object> randomNgo = ngoList.get(0);
            fallback.put("recommended_ngo_id", randomNgo.get("ngo_id"));
            fallback.put("recommended_ngo_name", randomNgo.get("name"));
            fallback.put("confidence_score", 0.5);
            fallback.put("reasoning", "ML service unavailable, using default selection");
        }
        return fallback;
    }

    public boolean isMLServiceHealthy() {
        if (!mlEngineEnabled) return false;
        try {
            String url = ML_ENGINE_URL + "/health";
            RestTemplate localTemplate = createRestTemplateWithTimeout(2000);
            ResponseEntity<Map> response = localTemplate.getForEntity(url, Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            logger.warn("⚠️ ML Service Health Check Failed: {}", e.getMessage());
            return false;
        }
    }
}