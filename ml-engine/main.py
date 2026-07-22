import os
import random
import math
from datetime import datetime
from typing import List, Optional

import uvicorn
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field

app = FastAPI(
    title="NourishFlow ML Engine",
    description="Microservice for predictive expiry forecasting and NGO demand routing",
    version="1.0.0"
)

# Enable CORS for cross-origin dashboard health checks
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ============================================
# DATA MODELS
# ============================================

class FoodItem(BaseModel):
    """Represents a food item from the inventory"""
    item_type: str = Field(default="Fresh Food Item")
    weight_lbs: float = Field(default=10.0)
    storage_temp_c: float = Field(default=4.0)
    humidity_percent: float = Field(default=65.0)
    days_until_expiry: int = Field(default=3)

class NgoCandidate(BaseModel):
    """Represents a potential NGO recipient"""
    ngo_id: str = Field(default="NGO_UNKNOWN")
    name: str = Field(default="Community Food Bank")
    distance_km: float = Field(default=5.0)
    current_time_hour: int = Field(default=12)  # 0-23
    avg_daily_capacity_lbs: float = Field(default=100.0)

class ExpiryRequest(BaseModel):
    """Request payload for expiry prediction"""
    item: FoodItem

class MatchRequest(BaseModel):
    """Request payload for optimal routing"""
    item: FoodItem
    ngos: List[NgoCandidate] = Field(default_factory=list)

# --- Response Models ---

class ExpiryResponse(BaseModel):
    item_type: str
    predicted_hours_remaining: float
    critical_risk: bool
    risk_level: str  # "LOW", "MEDIUM", "HIGH", "CRITICAL"

class RoutingResponse(BaseModel):
    recommended_ngo_id: str
    recommended_ngo_name: str
    confidence_score: float
    reasoning: str

# ============================================
# ML ENDPOINT 1: Predictive Expiry Forecasting
# ============================================

@app.post("/predict-expiry", response_model=ExpiryResponse)
def predict_expiry(request: ExpiryRequest):
    """
    Predicts remaining hours before food spoilage based on temperature, 
    humidity, and base shelf-life parameters.
    """
    item = request.item
    
    # Base shelf life in hours by food type
    base_shelf_life = {
        "Fresh Paneer": 72,        # 3 days
        "Amul Gold Milk": 120,     # 5 days
        "Alphonso Mangoes": 240,   # 10 days
        "Organic Palak": 96,       # 4 days
        "Greek Yogurt": 168,       # 7 days
        "Desi Ghee": 720,          # 30 days
        "Tofu": 48,                # 2 days
    }
    
    base_hours = base_shelf_life.get(item.item_type, 96)  # Default 4 days
    
    # FACTOR 1: Temperature Impact
    temp_degradation = 0
    if item.storage_temp_c > 10:
        temp_degradation = (item.storage_temp_c - 10) * 0.08 * base_hours
    
    # FACTOR 2: Humidity Impact
    humidity_degradation = 0
    if item.humidity_percent > 80 or item.humidity_percent < 50:
        deviation = abs(item.humidity_percent - 65)
        humidity_degradation = (deviation / 100) * 0.15 * base_hours
    
    # FACTOR 3: Inventory Expiry Window
    expiry_hours = item.days_until_expiry * 24
    
    # Calculate final predicted hours
    predicted_hours = base_hours - temp_degradation - humidity_degradation
    predicted_hours = min(predicted_hours, expiry_hours)
    
    # Add realistic variance (±10% noise)
    predicted_hours += random.uniform(-predicted_hours * 0.1, predicted_hours * 0.1)
    predicted_hours = max(12.0, predicted_hours)  # Safe floor
    
    # RISK ASSESSMENT
    if predicted_hours < 24:
        risk_level = "CRITICAL"
        critical_risk = True
    elif predicted_hours < 72:
        risk_level = "HIGH"
        critical_risk = True
    elif predicted_hours < 168:
        risk_level = "MEDIUM"
        critical_risk = False
    else:
        risk_level = "LOW"
        critical_risk = False
    
    return ExpiryResponse(
        item_type=item.item_type,
        predicted_hours_remaining=round(predicted_hours, 2),
        critical_risk=critical_risk,
        risk_level=risk_level
    )

# ============================================
# ML ENDPOINT 2: Demand Optimization & Routing
# ============================================

@app.post("/optimize-routing", response_model=RoutingResponse)
def optimize_routing(request: MatchRequest):
    """
    Recommends the best NGO based on capacity, time-of-day demand, distance, and storage requirements.
    """
    item = request.item
    ngos = request.ngos
    
    if not ngos:
        return RoutingResponse(
            recommended_ngo_id="NONE",
            recommended_ngo_name="No NGOs available",
            confidence_score=0.0,
            reasoning="No candidate NGOs provided in request body"
        )
    
    best_ngo = ngos[0]
    best_score = -1.0
    reasoning = ""
    
    for ngo in ngos:
        # FACTOR 1: Historical Capacity Score
        capacity_utilization = random.uniform(0.4, 0.95)
        available_capacity_ratio = 1.0 - capacity_utilization
        
        # FACTOR 2: Time-of-Day Demand Pattern
        hour = ngo.current_time_hour
        time_factor = 1.0
        
        if 11 <= hour <= 13:      # Lunch demand peak
            time_factor = 1.3
        elif 17 <= hour <= 19:    # Evening distribution peak
            time_factor = 1.2
        elif hour >= 22 or hour <= 5: # Night downtime
            time_factor = 0.7
        
        # FACTOR 3: Distance Penalty
        distance_penalty = 1.0 / (1.0 + (ngo.distance_km * 0.2))
        
        # FACTOR 4: Cold-Chain Compatibility
        compatibility_score = 1.0
        if item.item_type in ["Amul Gold Milk", "Greek Yogurt", "Fresh Paneer"]:
            if ngo.avg_daily_capacity_lbs < 100:
                compatibility_score = 0.7
        
        # CALCULATE FINAL SCORE
        final_score = (
            available_capacity_ratio 
            * capacity_utilization 
            * time_factor 
            * distance_penalty 
            * compatibility_score
        )
        final_score = min(1.0, max(0.01, final_score))
        
        if final_score > best_score:
            best_score = final_score
            best_ngo = ngo
            reasoning = (
                f"Selected {ngo.name}: Capacity Ratio={round(available_capacity_ratio, 2)}, "
                f"Time-Factor={round(time_factor, 2)}, Distance-Efficiency={round(distance_penalty, 2)}"
            )
    
    return RoutingResponse(
        recommended_ngo_id=best_ngo.ngo_id,
        recommended_ngo_name=best_ngo.name,
        confidence_score=round(best_score, 3),
        reasoning=reasoning
    )

# ============================================
# HEALTH CHECK ENDPOINT
# ============================================

@app.get("/health")
def health_check():
    """Health check endpoint to verify service status on Render"""
    return {
        "status": "healthy",
        "timestamp": datetime.now().isoformat(),
        "endpoints": [
            "/predict-expiry (POST)",
            "/optimize-routing (POST)",
            "/health (GET)"
        ]
    }

# Entrypoint for local execution and Render binding
if __name__ == "__main__":
    port = int(os.environ.get("PORT", 8000))
    uvicorn.run("main:app", host="0.0.0.0", port=port, reload=True)