package com.coldchain.simulator;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Entity
@Table(name = "match_history", 
       uniqueConstraints = @UniqueConstraint(columnNames = "item_id"))
public class MatchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)  
    private String itemId;
    
    private String originStore;
    private String itemName;
    private int quantityLbs;
    private int expiryDaysAtMatch;
    private String charityName;
    
    // 🛠️ FIX: Formats LocalDateTime to ISO-8601 string so Jackson can serialize it cleanly to JSON
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime matchTime;
    
    // Geographical Coordinates for Map Routing
    private Double sourceLat; // Supermarket Latitude
    private Double sourceLng; // Supermarket Longitude
    private Double destLat;   // Charity/Storage Latitude
    private Double destLng;   // Charity/Storage Longitude

    // Required empty constructor for JPA (Database)
    public MatchHistory() {}

    // Constructor including itemId
    public MatchHistory(String itemId, String originStore, String itemName, int quantityLbs, 
                        int expiryDaysAtMatch, String charityName, 
                        Double sourceLat, Double sourceLng, Double destLat, Double destLng) {
        this.itemId = itemId;
        this.originStore = originStore;
        this.itemName = itemName;
        this.quantityLbs = quantityLbs;
        this.expiryDaysAtMatch = expiryDaysAtMatch;
        this.charityName = charityName;
        this.matchTime = LocalDateTime.now(); // Automatically tags the exact time
        
        // Save the map locations
        this.sourceLat = sourceLat;
        this.sourceLng = sourceLng;
        this.destLat = destLat;
        this.destLng = destLng;
    }

    // ==========================================
    // GETTERS (Expose data to JSON/React)
    // ==========================================
    
    public Long getId() { return id; }
    public String getItemId() { return itemId; }
    public String getOriginStore() { return originStore; }
    public String getItemName() { return itemName; }
    public int getQuantityLbs() { return quantityLbs; }
    public int getExpiryDaysAtMatch() { return expiryDaysAtMatch; }
    public String getCharityName() { return charityName; }
    public LocalDateTime getMatchTime() { return matchTime; }
    
    // Coordinate Getters
    public Double getSourceLat() { return sourceLat; }
    public Double getSourceLng() { return sourceLng; }
    public Double getDestLat() { return destLat; }
    public Double getDestLng() { return destLng; }

    // ==========================================
    // SETTERS
    // ==========================================

    public void setItemId(String itemId) { this.itemId = itemId; }
    public void setOriginStore(String originStore) { this.originStore = originStore; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setQuantityLbs(int quantityLbs) { this.quantityLbs = quantityLbs; }
    public void setExpiryDaysAtMatch(int expiryDaysAtMatch) { this.expiryDaysAtMatch = expiryDaysAtMatch; }
    public void setCharityName(String charityName) { this.charityName = charityName; }
    public void setMatchTime(LocalDateTime matchTime) { this.matchTime = matchTime; }
    
    // Coordinate Setters
    public void setSourceLat(Double sourceLat) { this.sourceLat = sourceLat; }
    public void setSourceLng(Double sourceLng) { this.sourceLng = sourceLng; }
    public void setDestLat(Double destLat) { this.destLat = destLat; }
    public void setDestLng(Double destLng) { this.destLng = destLng; }
}