package com.coldchain.simulator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;


@RestController
public class DataController {

    @Autowired
    private MatchHistoryRepository matchHistoryRepository;

    @GetMapping("/api/matches")
    public List<MatchHistory> getMatchHistory() {
        // Fetches all the saved matches from PostgreSQL and sends them as JSON
        return matchHistoryRepository.findAll();
    }
}