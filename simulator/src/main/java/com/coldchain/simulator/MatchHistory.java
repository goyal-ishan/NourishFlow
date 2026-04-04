package com.coldchain.simulator;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "match_history")
public class MatchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originStore;      // Added: "Walmart-Store-101"
    private String itemName;         // "Strawberries"
    private int quantityLbs;         // 18
    private int expiryDaysAtMatch;   // Added: 3
    private String charityName;      // "City Orphanage"
    private LocalDateTime matchTime;

    public MatchHistory() {}

    // Updated Constructor to catch all the data from your log
    public MatchHistory(String originStore, String itemName, int quantityLbs, int expiryDaysAtMatch, String charityName) {
        this.originStore = originStore;
        this.itemName = itemName;
        this.quantityLbs = quantityLbs;
        this.expiryDaysAtMatch = expiryDaysAtMatch;
        this.charityName = charityName;
        this.matchTime = LocalDateTime.now();
    }

    // Getters and Setters for all fields...
}