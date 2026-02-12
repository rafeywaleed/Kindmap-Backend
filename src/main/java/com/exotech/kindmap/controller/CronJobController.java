package com.exotech.kindmap.controller;

import com.exotech.kindmap.config.SchedulerConfig;
import com.exotech.kindmap.dto.GridDTO;
import com.exotech.kindmap.dto.PinDTO;
import com.exotech.kindmap.dto.UserDTO;
import com.exotech.kindmap.model.Grid;
import com.exotech.kindmap.model.Pin;
import com.exotech.kindmap.model.User;
import com.exotech.kindmap.repository.GridRepo;
import com.exotech.kindmap.repository.PinRepo;
import com.exotech.kindmap.repository.UserRepo;
import com.exotech.kindmap.service.DTOServices;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

//package com.exotech.kindmap.controller;
//
//import com.exotech.kindmap.dto.GridDTO;
//import com.exotech.kindmap.dto.PinDTO;
//import com.exotech.kindmap.dto.UserDTO;
//import com.exotech.kindmap.model.Grid;
//import com.exotech.kindmap.model.Pin;
//import com.exotech.kindmap.model.User;
//import com.exotech.kindmap.repository.GridRepo;
//import com.exotech.kindmap.repository.PinRepo;
//import com.exotech.kindmap.repository.UserRepo;
//import com.exotech.kindmap.service.DTOServices;
//import lombok.Data;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//        import java.time.LocalDate;
//import java.util.*;
//        import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/admin/api")
//public class AdminDataController {
//
//    @Autowired
//    private UserRepo userRepo;
//
//    @Autowired
//    private GridRepo gridRepo;
//
//    @Autowired
//    private PinRepo pinRepo;
//
//    @Autowired
//    private DTOServices dtoServices;
//
//    // ==================== DASHBOARD STATS ====================
//
//    @GetMapping("/stats")
//    public ResponseEntity<Map<String, Object>> getDashboardStats() {
//        Map<String, Object> stats = new HashMap<>();
//
//        // Basic counts
//        stats.put("totalUsers", userRepo.count());
//        stats.put("totalGrids", gridRepo.count());
//        stats.put("totalPins", pinRepo.count());
//
//        // Additional stats
//        stats.put("usersWithTokens", userRepo.countUsersWithTokens());
//        stats.put("gridsWithPins", gridRepo.countGridsWithPins());
//        stats.put("gridsWithUsers", gridRepo.countGridsWithUsers());
//
//        // Recent activity
//        stats.put("recentUsers", userRepo.findRecentUsers(PageRequest.of(0, 5))
//                .stream().map(dtoServices::convertToUserDTO).collect(Collectors.toList()));
//        stats.put("recentPins", pinRepo.findRecentPins(PageRequest.of(0, 5))
//                .stream().map(dtoServices::convertToPinDTO).collect(Collectors.toList()));
//
//        return ResponseEntity.ok(stats);
//    }
//
//    // ==================== USER MANAGEMENT ====================
//
//    @GetMapping("/users")
//    public ResponseEntity<Map<String, Object>> getAllUsers(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @RequestParam(defaultValue = "joinedDate") String sort,
//            @RequestParam(defaultValue = "desc") String direction,
//            @RequestParam(required = false) String search) {
//
//        Pageable pageable = PageRequest.of(page, size,
//                Sort.by(Sort.Direction.fromString(direction), sort));
//
//        Page<User> userPage;
//        if (search != null && !search.isEmpty()) {
//            userPage = userRepo.searchUsers(search, pageable);
//        } else {
//            userPage = userRepo.findAllWithSubscriptions(pageable);
//        }
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("content", userPage.getContent().stream()
//                .map(user -> {
//                    UserDTO dto = dtoServices.convertToUserDTO(user);
//                    // Add additional admin info
//                    Map<String, Object> userMap = new HashMap<>();
//                    userMap.put("user", dto);
//                    userMap.put("subscriptionCount", user.getSubscribedGridIds().size());
//                    userMap.put("hasToken", user.getToken() != null && !user.getToken().isEmpty());
//                    return userMap;
//                })
//                .collect(Collectors.toList()));
//        response.put("totalElements", userPage.getTotalElements());
//        response.put("totalPages", userPage.getTotalPages());
//        response.put("currentPage", userPage.getNumber());
//
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/users/{userId}")
//    public ResponseEntity<UserDetailDTO> getUserDetails(@PathVariable String userId) {
//        return userRepo.findByIdWithSubscriptions(userId)
//                .map(user -> {
//                    UserDetailDTO detailDTO = new UserDetailDTO();
//                    detailDTO.setUser(dtoServices.convertToUserDTO(user));
//
//                    // Get user's pins
//                    List<Pin> userPins = pinRepo.findByCreatedBy(userId);
//                    detailDTO.setPins(userPins.stream()
//                            .map(dtoServices::convertToPinDTO)
//                            .collect(Collectors.toList()));
//
//                    // Get subscription details
//                    detailDTO.setSubscribedGrids(user.getSubscribedGridIds().stream()
//                            .map(grid -> {
//                                Map<String, Object> gridInfo = new HashMap<>();
//                                gridInfo.put("gridId", grid.getGridId());
//                                gridInfo.put("pinCount", grid.getPins().size());
//                                gridInfo.put("userCount", grid.getUsers().size());
//                                return gridInfo;
//                            })
//                            .collect(Collectors.toList()));
//
//                    return ResponseEntity.ok(detailDTO);
//                })
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    // ==================== GRID MANAGEMENT ====================
//
//    @GetMapping("/grids")
//    public ResponseEntity<Map<String, Object>> getAllGrids(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @RequestParam(defaultValue = "gridId") String sort,
//            @RequestParam(defaultValue = "asc") String direction,
//            @RequestParam(required = false) String search) {
//
//        Pageable pageable = PageRequest.of(page, size,
//                Sort.by(Sort.Direction.fromString(direction), sort));
//
//        Page<Grid> gridPage;
//        if (search != null && !search.isEmpty()) {
//            gridPage = gridRepo.searchGrids(search, pageable);
//        } else {
//            gridPage = gridRepo.findAllWithPinsAndUsers(pageable);
//        }
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("content", gridPage.getContent().stream()
//                .map(grid -> {
//                    Map<String, Object> gridMap = new HashMap<>();
//                    gridMap.put("gridId", grid.getGridId());
//                    gridMap.put("pinCount", grid.getPins().size());
//                    gridMap.put("userCount", grid.getUsers().size());
//                    gridMap.put("recentPin", grid.getPins().stream()
//                            .max(Comparator.comparing(Pin::getCreatedAt))
//                            .map(Pin::getCreatedAt)
//                            .orElse(null));
//                    return gridMap;
//                })
//                .collect(Collectors.toList()));
//        response.put("totalElements", gridPage.getTotalElements());
//        response.put("totalPages", gridPage.getTotalPages());
//        response.put("currentPage", gridPage.getNumber());
//
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/grids/{gridId}")
//    public ResponseEntity<GridDetailDTO> getGridDetails(@PathVariable String gridId) {
//        return gridRepo.findByIdWithPinsAndUsers(gridId)
//                .map(grid -> {
//                    GridDetailDTO detailDTO = new GridDetailDTO();
//                    detailDTO.setGrid(dtoServices.convertToGridDTO(grid));
//
//                    // Additional grid statistics
//                    detailDTO.setPinCount(grid.getPins().size());
//                    detailDTO.setUserCount(grid.getUsers().size());
//
//                    // Pin timeline
//                    detailDTO.setPinTimeline(grid.getPins().stream()
//                            .collect(Collectors.groupingBy(
//                                    pin -> pin.getCreatedAt().toLocalDate(),
//                                    Collectors.counting()
//                            )));
//
//                    // Most active users in this grid
//                    Map<String, Long> userActivity = grid.getPins().stream()
//                            .filter(pin -> pin.getCreatedBy() != null)
//                            .collect(Collectors.groupingBy(
//                                    Pin::getCreatedBy,
//                                    Collectors.counting()
//                            ));
//                    detailDTO.setMostActiveUsers(userActivity);
//
//                    return ResponseEntity.ok(detailDTO);
//                })
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    // ==================== PIN MANAGEMENT ====================
//
//    @GetMapping("/pins")
//    public ResponseEntity<Map<String, Object>> getAllPins(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @RequestParam(defaultValue = "createdAt") String sort,
//            @RequestParam(defaultValue = "desc") String direction,
//            @RequestParam(required = false) String search) {
//
//        Pageable pageable = PageRequest.of(page, size,
//                Sort.by(Sort.Direction.fromString(direction), sort));
//
//        Page<Pin> pinPage;
//        if (search != null && !search.isEmpty()) {
//            pinPage = pinRepo.searchPins(search, pageable);
//        } else {
//            pinPage = pinRepo.findAllWithGrid(pageable);
//        }
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("content", pinPage.getContent().stream()
//                .map(pin -> {
//                    Map<String, Object> pinMap = new HashMap<>();
//                    pinMap.put("pin", dtoServices.convertToPinDTO(pin));
//                    pinMap.put("hasImage", pin.getImageBase64() != null && !pin.getImageBase64().isEmpty());
//                    pinMap.put("imageLength", pin.getImageBase64() != null ? pin.getImageBase64().length() : 0);
//                    return pinMap;
//                })
//                .collect(Collectors.toList()));
//        response.put("totalElements", pinPage.getTotalElements());
//        response.put("totalPages", pinPage.getTotalPages());
//        response.put("currentPage", pinPage.getNumber());
//
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/pins/{pinId}")
//    public ResponseEntity<PinDetailDTO> getPinDetails(@PathVariable String pinId) {
//        return pinRepo.findByIdWithGrid(pinId)
//                .map(pin -> {
//                    PinDetailDTO detailDTO = new PinDetailDTO();
//                    detailDTO.setPin(dtoServices.convertToPinDTO(pin));
//
//                    // Get user who created this pin
//                    if (pin.getCreatedBy() != null) {
//                        userRepo.findById(pin.getCreatedBy())
//                                .ifPresent(user -> detailDTO.setCreatedByUser(
//                                        dtoServices.convertToUserDTO(user)));
//                    }
//
//                    // Grid information
//                    if (pin.getGrid() != null) {
//                        detailDTO.setGridId(pin.getGrid().getGridId());
//                        detailDTO.setGridPinCount(pin.getGrid().getPins().size());
//                    }
//
//                    return ResponseEntity.ok(detailDTO);
//                })
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    // ==================== RELATIONSHIPS ====================
//
//    @GetMapping("/relationships")
//    public ResponseEntity<Map<String, Object>> getAllRelationships() {
//        Map<String, Object> relationships = new HashMap<>();
//
//        // User-Grid subscriptions
//        List<Map<String, String>> subscriptions = new ArrayList<>();
//        userRepo.findAllWithSubscriptions(Pageable.unpaged()).forEach(user -> {
//            for (Grid grid : user.getSubscribedGridIds()) {
//                Map<String, String> sub = new HashMap<>();
//                sub.put("userId", user.getUserId());
//                sub.put("userName", user.getName());
//                sub.put("gridId", grid.getGridId());
//                subscriptions.add(sub);
//            }
//        });
//        relationships.put("subscriptions", subscriptions);
//
//        // Grid-Pin relationships
//        List<Map<String, Object>> gridPins = new ArrayList<>();
//        gridRepo.findAllWithPinsAndUsers(Pageable.unpaged()).forEach(grid -> {
//            for (Pin pin : grid.getPins()) {
//                Map<String, Object> gp = new HashMap<>();
//                gp.put("gridId", grid.getGridId());
//                gp.put("pinId", pin.getPinId());
//                gp.put("createdBy", pin.getCreatedBy());
//                gp.put("createdAt", pin.getCreatedAt());
//                gridPins.add(gp);
//            }
//        });
//        relationships.put("gridPins", gridPins);
//
//        // User-Pin creation relationships
//        List<Map<String, Object>> userPins = new ArrayList<>();
//        pinRepo.findAllWithGrid(Pageable.unpaged()).forEach(pin -> {
//            if (pin.getCreatedBy() != null) {
//                Map<String, Object> up = new HashMap<>();
//                up.put("userId", pin.getCreatedBy());
//                up.put("pinId", pin.getPinId());
//                up.put("gridId", pin.getGrid().getGridId());
//                up.put("createdAt", pin.getCreatedAt());
//                userPins.add(up);
//            }
//        });
//        relationships.put("userPins", userPins);
//
//        return ResponseEntity.ok(relationships);
//    }
//
//    // ==================== DATABASE STATS ====================
//
//    @GetMapping("/db-stats")
//    public ResponseEntity<Map<String, Object>> getDatabaseStats() {
//        Map<String, Object> stats = new HashMap<>();
//
//        // Table sizes
//        stats.put("userTableSize", userRepo.getTableSize());
//        stats.put("gridTableSize", gridRepo.getTableSize());
//        stats.put("pinTableSize", pinRepo.getTableSize());
//        stats.put("subscriptionTableSize", userRepo.getSubscriptionTableSize());
//
//        // Null/empty values
//        stats.put("usersWithoutToken", userRepo.countUsersWithoutToken());
//        stats.put("pinsWithoutImage", pinRepo.countPinsWithoutImage());
//        stats.put("pinsWithoutDetails", pinRepo.countPinsWithoutDetails());
//        stats.put("gridsWithoutPins", gridRepo.countGridsWithoutPins());
//        stats.put("gridsWithoutUsers", gridRepo.countGridsWithoutUsers());
//
//        // Time-based stats
//        stats.put("pinsLast24h", pinRepo.countPinsLast24Hours());
//        stats.put("pinsLast7d", pinRepo.countPinsLast7Days());
//        stats.put("usersJoinedLast24h", userRepo.countUsersJoinedLast24Hours());
//        stats.put("usersJoinedLast7d", userRepo.countUsersJoinedLast7Days());
//
//        return ResponseEntity.ok(stats);
//    }
//
//    // ==================== ADMIN ACTIONS ====================
//
//    @DeleteMapping("/users/{userId}")
//    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
//        if (userRepo.existsById(userId)) {
//            userRepo.deleteById(userId);
//            return ResponseEntity.ok().build();
//        }
//        return ResponseEntity.notFound().build();
//    }
//
//    @DeleteMapping("/pins/{pinId}")
//    public ResponseEntity<Void> deletePin(@PathVariable String pinId) {
//        if (pinRepo.existsById(pinId)) {
//            pinRepo.deleteById(pinId);
//            return ResponseEntity.ok().build();
//        }
//        return ResponseEntity.notFound().build();
//    }
//
//    @DeleteMapping("/grids/{gridId}")
//    public ResponseEntity<Void> deleteGrid(@PathVariable String gridId) {
//        if (gridRepo.existsById(gridId)) {
//            gridRepo.deleteById(gridId);
//            return ResponseEntity.ok().build();
//        }
//        return ResponseEntity.notFound().build();
//    }
//
//    @PostMapping("/users/{userId}/token/refresh")
//    public ResponseEntity<String> refreshUserToken(@PathVariable String userId) {
//        // This would typically trigger a token refresh notification to the user
//        return ResponseEntity.ok("Token refresh requested for user: " + userId);
//    }
//}
//
//@Data
//class UserDetailDTO {
//    private UserDTO user;
//    private List<PinDTO> pins;
//    private List<Map<String, Object>> subscribedGrids;
//}
//
//@Data
//class GridDetailDTO {
//    private GridDTO grid;
//    private int pinCount;
//    private int userCount;
//    private Map<LocalDate, Long> pinTimeline;
//    private Map<String, Long> mostActiveUsers;
//}
//
//@Data
//class PinDetailDTO {
//    private PinDTO pin;
//    private UserDTO createdByUser;
//    private String gridId;
//    private int gridPinCount;
//}