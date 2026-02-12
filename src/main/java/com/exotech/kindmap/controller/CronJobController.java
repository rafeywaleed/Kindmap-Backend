package com.exotech.kindmap.controller;

import com.exotech.kindmap.config.SchedulerConfig;
import com.exotech.kindmap.repository.GridRepo;
import com.exotech.kindmap.repository.PinRepo;
import com.exotech.kindmap.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class CronJobController {

    @Autowired
    private GridRepo gridRepo;

    @Autowired
    private PinRepo pinRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private SchedulerConfig schedulerConfig;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/keep-alive")
    public ResponseEntity<Map<String, Object>> keepAlive() {
        try {
            long userCount = userRepo.count();
            long gridCount = gridRepo.count();
            long pinCount = pinRepo.count();

            Map<String, Object> stats = new HashMap<>();
            stats.put("users", userCount);
            stats.put("grids", gridCount);
            stats.put("pins", pinCount);
            stats.put("timestamp", LocalDateTime.now().toString());
            stats.put("status", "healthy");

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of(
                    "status", "unhealthy",
                    "error", e.getMessage()
            ));
        }
    }

    @Scheduled(fixedRate = 720000, initialDelay = 120000)
    public void selfPing() {
        try {
            String url = schedulerConfig.getFullBaseUrl() + "/keep-alive";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            System.out.println("✅ Self-ping: " + response.getBody());
            System.out.println("   URL: " + url);
        } catch (Exception e) {
            System.err.println("❌ Self-ping failed: " + e.getMessage());
            System.err.println("   URL attempted: " + schedulerConfig.getFullBaseUrl() + "/keep-alive");
        }
    }

    @GetMapping("/kindmap")
    public ResponseEntity<String> hello(){
        return ResponseEntity.ok("Welcome to Kindmap");
    }
}

