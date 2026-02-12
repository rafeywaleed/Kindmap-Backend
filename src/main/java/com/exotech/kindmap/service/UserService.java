package com.exotech.kindmap.service;


import com.exotech.kindmap.dto.UserDTO;
import com.exotech.kindmap.model.Grid;
import com.exotech.kindmap.model.User;
import com.exotech.kindmap.repository.GridRepo;
import com.exotech.kindmap.repository.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private GridRepo gridRepo;

    @Autowired
    private DTOServices dtoServices;

    @Autowired
    private FCMService fcmService;

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public Optional<User> getUser(String userId) {
        return userRepo.findByIdWithSubscriptions(userId);
    }

    public List<UserDTO> getAllUsers() {
        return userRepo
                .findAllWithSubscriptions()
                .stream()
                .map(user -> dtoServices.convertToUserDTO(user))
                .toList();
    }

    @Transactional
    public UserDTO addUser(UserDTO userDTO) {
        if(userDTO.getAvatarIndex()==0) userDTO.setAvatarIndex(1);
        User user = new User();
        user.setUserId(userDTO.getUserId());
        user.setName(userDTO.getName());
        user.setAvatarIndex(userDTO.getAvatarIndex());
        user.setToken(userDTO.getToken());
        user.setEmail(userDTO.getEmail());
        user.setHelped(userDTO.getHelped());
        user.setJoinedDate(userDTO.getJoinedDate());
        User savedUser = userRepo.save(user);

        String token = savedUser.getToken();
        if (token != null && !token.isEmpty()) {
            boolean success = fcmService.subscribeToTopic(token, "allUsers");
            if (success) {
                log.info("✅ New user {} auto-subscribed to allUsers topic", user.getUserId());
            } else {
                log.warn("⚠️ Failed to auto-subscribe user {} to allUsers topic", user.getUserId());
            }
        }

        return dtoServices.convertToUserDTO(savedUser);
    }

    @Transactional
    public UserDTO changeName(String userId, String newName) {
        User user = userRepo.findByIdWithSubscriptions(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setName(newName);
        return dtoServices.convertToUserDTO(user);
    }

    public List<String> getSubscribedTopics(String userId) {
        User user = userRepo.findByIdWithSubscriptions(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getSubscribedGridIds()
                .stream()
                .map(Grid :: getGridId)
                .toList();
    }

    @Transactional
    public UserDTO subscribeUserToGrid(String userId, String gridId) {
        User user = userRepo.findByIdWithSubscriptions(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Grid grid = gridRepo.findByIdWithUsers(gridId)
                .orElseThrow(() -> new RuntimeException("Grid not found"));

        if (!user.getSubscribedGridIds().contains(grid)) {
            user.getSubscribedGridIds().add(grid);
            grid.getUsers().add(user);

            String token = user.getToken();
            if (token != null && !token.isEmpty()) {
                boolean success = fcmService.subscribeToTopic(token, gridId);
                if (!success) {
                    log.warn("⚠️ Firebase subscription failed for user {} to grid {}", userId, gridId);
                }
            } else {
                log.info("User {} has no FCM token - skipping Firebase subscription", userId);
            }

            userRepo.save(user);
        }

        return dtoServices.convertToUserDTO(user);
    }

    @Transactional
    public List<String> unsubscribeFromGrid(String userId, String gridId) {
        User user = userRepo.findByIdWithSubscriptions(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Grid grid = gridRepo.findByIdWithUsers(gridId)
                .orElseThrow(() -> new RuntimeException("Grid not found"));

        boolean removed = user.getSubscribedGridIds().remove(grid);
        if (removed) {
            grid.getUsers().remove(user);

            String token = user.getToken();
            if (token != null && !token.isEmpty()) {
                boolean success = fcmService.unsubscribeFromTopic(token, gridId);
                if (!success) {
                    log.warn("⚠️ Firebase unsubscription failed for user {} from grid {}", userId, gridId);
                }
            }

            userRepo.save(user);
        }

        return user
                .getSubscribedGridIds()
                .stream()
                .map(Grid::getGridId)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<String> getToken(String userId) {
        return userRepo.findById(userId)
                .map(User::getToken)
                .filter(token -> token != null && !token.isEmpty());
    }

    @Transactional
    public String updateToken(String userId, String newToken) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String oldToken = user.getToken();

        user.setToken(newToken);

        if (oldToken != null && !oldToken.isEmpty() && !oldToken.equals(newToken)) {
            log.info("🔄 Cleaning up old token for user {}: {} -> {}",
                    userId, maskToken(oldToken), maskToken(newToken));

            boolean unsubscribedGlobal = fcmService.unsubscribeFromTopic(oldToken, "allUsers");
            if (unsubscribedGlobal) {
                log.info("✅ Unsubscribed old token from allUsers topic");
            }

            List<String> subscribedGrids = user.getSubscribedGridIds()
                    .stream()
                    .map(Grid::getGridId)
                    .toList();

            int unsubscribedCount = 0;
            for (String gridId : subscribedGrids) {
                boolean success = fcmService.unsubscribeFromTopic(oldToken, gridId);
                if (success) unsubscribedCount++;
            }

            log.info("✅ Unsubscribed old token from {}/{} grid topics",
                    unsubscribedCount, subscribedGrids.size());
        }

        boolean allUsersSuccess = fcmService.subscribeToTopic(newToken, "allUsers");
        if (allUsersSuccess) {
            log.info("✅ User {} subscribed new token to allUsers topic", userId);
        }

        List<String> subscribedGrids = user.getSubscribedGridIds()
                .stream()
                .map(Grid::getGridId)
                .toList();

        int successCount = 0;
        for (String gridId : subscribedGrids) {
            boolean success = fcmService.subscribeToTopic(newToken, gridId);
            if (success) successCount++;
        }

        log.info("✅ User {} updated token. Subscribed new token to {}/{} grids",
                userId, successCount, subscribedGrids.size());

        userRepo.save(user);
        return newToken;
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 8) return "***";
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }

    private List<String> stringListofGridId(User user) {
        return user.getSubscribedGridIds()
                .stream()
                .map(grid -> grid.getGridId())
                .toList();
    }

    @Transactional
    public int getAvatarIndex(String userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getAvatarIndex() == 0) {
            user.setAvatarIndex(1);
        }
        return user.getAvatarIndex();
    }

    @Transactional
    public int changeAvatarIndex(String userId, int newAvatarIndex) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setAvatarIndex(newAvatarIndex);
        return newAvatarIndex;
    }

    @Transactional(readOnly = true)
    public int getUserHelped(String userId) {
        return userRepo.findById(userId)
                .map(User::getHelped)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public int incHelped(String userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setHelped(user.getHelped() + 1);
        return user.getHelped();
    }

    @Transactional
    public int changeHelped(String userId, int newNumber) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setHelped(newNumber);
        return newNumber;
    }
}
