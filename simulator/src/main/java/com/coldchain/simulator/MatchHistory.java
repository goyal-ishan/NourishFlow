package com.coldchain.simulator;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "match_history")
public class MatchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originStore;
    private String itemName;
    private int quantityLbs;
    private int expiryDaysAtMatch;
    private String charityName;
    private LocalDateTime matchTime;

    // Required empty constructor
    public MatchHistory() {}

    // Constructor for saving new matches
    public MatchHistory(String originStore, String itemName, int quantityLbs, int expiryDaysAtMatch, String charityName) {
        this.originStore = originStore;
        this.itemName = itemName;
        this.quantityLbs = quantityLbs;
        this.expiryDaysAtMatch = expiryDaysAtMatch;
        this.charityName = charityName;
        this.matchTime = LocalDateTime.now();
    }

    // ==========================================
    // THE MISSING GETTERS (This exposes the JSON)
    // ==========================================
    
    public Long getId() {
        return id;
    }

    public String getOriginStore() {
        return originStore;
    }

    public String getItemName() {
        return itemName;
    }

    public int getQuantityLbs() {
        return quantityLbs;
    }

    public int getExpiryDaysAtMatch() {
        return expiryDaysAtMatch;
    }

    public String getCharityName() {
        return charityName;
    }

    public LocalDateTime getMatchTime() {
        return matchTime;
    }
}