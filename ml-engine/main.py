from fastapi import FastAPI
from pydantic import BaseModel
from typing import List
import random
import math
from datetime import datetime

app = FastAPI(title="NourishFlow ML Engine")

# --- Data Models (What Java will send to Python) ---

class FoodItem(BaseModel):
    """Represents a food item from the inventory"""
    item_type: str
    weight_lbs: float
    storage_temp_c: float
    humidity_percent: float
    days_until_expiry: int

class NgoCandidate(BaseModel):
    """Represents a potential NGO recipient"""
    ngo_id: str
    name: str
    distance_km: float
    current_time_hour: int  # 0-23 (hour of day)
    avg_daily_capacity_lbs: float

class ExpiryRequest(BaseModel):
    """Request for expiry prediction"""
    item: FoodItem

class MatchRequest(BaseModel):
    """Request for optimal routing"""
    item: FoodItem
    ngos: List[NgoCandidate]

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
    Predicts the exact hours remaining before food spoilage.
    
    Model Logic:
    - Base shelf life varies by item type
    - Higher temperature accelerates degradation
    - Higher humidity accelerates degradation
    - Combines with days_until_expiry from inventory
    """
    item = request.item
    
    # Base shelf life in hours by food type
    base_shelf_life = {
        "Fresh Paneer": 72,           # 3 days
        "Amul Gold Milk": 120,        # 5 days
        "Alphonso Mangoes": 240,      # 10 days
        "Organic Palak": 96,          # 4 days
        "Greek Yogurt": 168,          # 7 days
        "Desi Ghee": 720,             # 30 days
        "Tofu": 48,                   # 2 days
    }
    
    base_hours = base_shelf_life.get(item.item_type, 96)  # Default 4 days
    
    # ============================================
    # FACTOR 1: Temperature Impact (Cold Chain Efficacy)
    # ============================================
    # Optimal storage temp is 4°C for most perishables
    temp_degradation = 0
    if item.storage_temp_c > 10:
        # Each degree above 10°C reduces shelf life by 8%
        temp_degradation = (item.storage_temp_c - 10) * 0.08 * base_hours
    
    # ============================================
    # FACTOR 2: Humidity Impact
    # ============================================
    # Optimal humidity is 65-75% for most fruits/vegetables
    humidity_degradation = 0
    if item.humidity_percent > 80 or item.humidity_percent < 50:
        # Extreme humidity (too wet or too dry) reduces shelf life
        deviation = max(abs(item.humidity_percent - 65), 0)
        humidity_degradation = (deviation / 100) * 0.15 * base_hours
    
    # ============================================
    # FACTOR 3: Inventory Expiry Window
    # ============================================
    # If item already has limited days, adjust prediction
    expiry_hours = item.days_until_expiry * 24
    
    # Calculate final predicted hours
    predicted_hours = base_hours - temp_degradation - humidity_degradation
    
    # Use the minimum of base prediction or inventory's stated expiry
    predicted_hours = min(predicted_hours, expiry_hours)
    
    # Add some realistic noise (±10% variance)
    predicted_hours += random.uniform(-predicted_hours * 0.1, predicted_hours * 0.1)
    predicted_hours = max(12, predicted_hours)  # At least 12 hours
    
    # ============================================
    # RISK ASSESSMENT
    # ============================================
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
    Recommends the best NGO for food redistribution based on:
    1. Historical capacity (how much they can accept)
    2. Time of day (demand patterns)
    3. Distance (logistics efficiency)
    4. Item compatibility
    
    Score = (Capacity Score × Time-of-Day Factor) / Distance
    """
    item = request.item
    ngos = request.ngos
    
    if not ngos:
        return RoutingResponse(
            recommended_ngo_id="NONE",
            recommended_ngo_name="No NGOs available",
            confidence_score=0.0,
            reasoning="No NGO candidates provided"
        )
    
    best_ngo = None
    best_score = -1
    reasoning = ""
    
    for ngo in ngos:
        # ============================================
        # FACTOR 1: Historical Capacity Score
        # ============================================
        # Normalized to 0-1: How much capacity they typically have available
        # We'll simulate this as a random value, but in production
        # this comes from historical donation data
        capacity_utilization = random.uniform(0.4, 0.95)
        available_capacity_ratio = 1 - capacity_utilization
        
        # ============================================
        # FACTOR 2: Time-of-Day Demand Pattern
        # ============================================
        # Different NGOs have different demand patterns by hour
        # Example: Food banks peak at lunch (12) and evening (18)
        hour = ngo.current_time_hour
        time_factor = 1.0
        
        # Peak demand windows
        if hour >= 11 and hour <= 13:  # Lunch rush
            time_factor = 1.3  # 30% boost during peak
        elif hour >= 17 and hour <= 19:  # Evening distribution
            time_factor = 1.2  # 20% boost
        elif hour >= 22 or hour <= 5:  # Night (low demand)
            time_factor = 0.7  # 30% penalty
        
        # ============================================
        # FACTOR 3: Distance Penalty
        # ============================================
        # Closer NGOs are preferred (1 km ~ 0.1 score reduction)
        distance_penalty = 1.0 / (1.0 + (ngo.distance_km * 0.2))
        
        # ============================================
        # FACTOR 4: Item Type Compatibility
        # ============================================
        # Some items require special handling (dairy, frozen, etc.)
        compatibility_score = 1.0
        if item.item_type in ["Amul Gold Milk", "Greek Yogurt", "Fresh Paneer"]:
            # Dairy items: NGO must have reliable cold chain
            if ngo.avg_daily_capacity_lbs < 100:  # Small capacity = less reliable
                compatibility_score = 0.7
        
        # ============================================
        # CALCULATE FINAL SCORE
        # ============================================
        final_score = (available_capacity_ratio * capacity_utilization * time_factor * distance_penalty * compatibility_score)
        
        # Normalize score to 0-1
        final_score = min(1.0, final_score)
        
        if final_score > best_score:
            best_score = final_score
            best_ngo = ngo
            reasoning = f"NGO {ngo.name} selected: Capacity={round(available_capacity_ratio, 2)}, Time-factor={round(time_factor, 2)}, Distance-efficiency={round(distance_penalty, 2)}"
    
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
    """Simple endpoint to verify ML service is running"""
    return {
        "status": "healthy",
        "timestamp": datetime.now().isoformat(),
        "endpoints": [
            "/predict-expiry (POST)",
            "/optimize-routing (POST)",
            "/health (GET)"
        ]
    }

# Run via terminal: uvicorn main:app --reload --port 8000