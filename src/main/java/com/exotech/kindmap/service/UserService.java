package com.exotech.kindmap.service;


import com.exotech.kindmap.dto.UserDTO;
import com.exotech.kindmap.model.Grid;
import com.exotech.kindmap.model.User;
import com.exotech.kindmap.repository.GridRepo;
import com.exotech.kindmap.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public UserDTO addUser(User user) {
        if(user.getAvatarIndex()==0) user.setAvatarIndex(1);
        User savedUser = userRepo.save(user);
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
    public List<String> unsubscribeFromGrid(String userId, String gridId) {
        User user = userRepo.findByIdWithSubscriptions(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Grid grid = gridRepo.findByIdWithUsers(gridId)
                .orElseThrow(() -> new RuntimeException("Grid not found"));

        user.getSubscribedGridIds().remove(grid);
        grid.getUsers().remove(user);

        return user
                .getSubscribedGridIds()
                .stream()
                .map(Grid::getGridId)
                .toList();
    }

    private List<String> stringListofGridId(User user) {
        return user.getSubscribedGridIds()
                .stream()
                .map(grid -> grid.getGridId())
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
        }

        return dtoServices.convertToUserDTO(user);
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
    public Optional<String> getToken(String userId) {
        return userRepo.findById(userId)
                .map(User::getToken)
                .filter(token -> token != null && !token.isEmpty());
    }

    @Transactional
    public String updateToken(String userId, String token) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setToken(token);
        return token;
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
