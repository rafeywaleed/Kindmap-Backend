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

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private GridRepo gridRepo;

    @Autowired
    private DTOServices dtoServices;

    public User getUser(String userId) {
        return userRepo.findById(userId).orElse(null);
    }

    public List<UserDTO> getAllUsers() {

        return userRepo
                .findAll()
                .stream()
                .map(user -> dtoServices.convertToUserDTO(user))
                .toList();
    }

    public void addUser(User user) {
        if(user.getAvatarIndex()==0) user.setAvatarIndex(1);
        userRepo.save(user);
    }

    public ResponseEntity<?> changeName(String userId, String newName) {
        User user = getUser(userId);
        if(user==null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        user.setName(newName);
        userRepo.save(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<List<String>> getSubscribedTopics(String userId) {
        User user = getUser(userId);
        if(user==null || user.getSubscribedGridIds().isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(stringListofGridId(user), HttpStatus.OK);
    }

    public ResponseEntity<List<String>> unsubscribeTopic(String userId, String gridId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not Found"));

        Grid grid = gridRepo.findById(gridId)
                .orElseThrow(() -> new RuntimeException("Grid not Found"));

        user.getSubscribedGridIds().remove(grid);
        grid.getUsers().remove(user);

        userRepo.save(user);
        gridRepo.save(grid);

        return new ResponseEntity<>(stringListofGridId(user), HttpStatus.OK);
    }

    private List<String> stringListofGridId(User user) {
        return user.getSubscribedGridIds()
                .stream()
                .map(grid -> grid.getGridId())
                .toList();
    }

    public ResponseEntity<List<String>> subscribeUserToGrid(String userId, String gridId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not Found"));

        Grid grid = gridRepo.findById(gridId)
                .orElseThrow(() -> new RuntimeException("Grid not Found"));

        user.getSubscribedGridIds().add(grid);
        grid.getUsers().add(user);

        userRepo.save(user);
        gridRepo.save(grid);

        return new ResponseEntity<>(stringListofGridId(user), HttpStatus.OK);
    }

    public int getAvatarIndex(String userId) {
        User user = userRepo.findById(userId).orElse(null);
        if(user == null) return 0;
        if(user.getAvatarIndex() == 0) user.setAvatarIndex(1);
        userRepo.save(user);
        return user.getAvatarIndex();
    }

    public ResponseEntity<Integer> changeAvatarIndex(String userId, int newAvatarIndex) {
        User user = userRepo.findById(userId).orElse(null);
        if(user==null || user.getAvatarIndex()==0) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        user.setAvatarIndex(newAvatarIndex);
        userRepo.save(user);
        return new ResponseEntity<>(user.getAvatarIndex(), HttpStatus.OK);
    }

    public ResponseEntity<String> getToken(String userId) {
        User user = userRepo.findById(userId).orElse(null);
        if(user==null || user.getToken().isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);;
        return new ResponseEntity<>(user.getToken(), HttpStatus.OK);
    }

    public ResponseEntity<String> updateToken(String userId, String token) {
        User user = userRepo.findById(userId).orElse(null);
        if(user==null || user.getToken().isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);;
        user.setToken(token);
        userRepo.save(user);
        return new ResponseEntity<>(user.getToken(), HttpStatus.OK);
    }

    public ResponseEntity<Integer> getUserHelped(String userId) {
        User user = userRepo.findById(userId).orElse(null);
        if(user == null) return new ResponseEntity<>(0,HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(user.getHelped(), HttpStatus.OK);
    }

    public ResponseEntity<Integer> incHelped(String userId) {
        User user = userRepo.findById(userId).orElse(null);
        if(user == null) return new ResponseEntity<>(0,HttpStatus.NOT_FOUND);
        user.setHelped(user.getHelped()+1);
        return new ResponseEntity<>(user.getHelped(), HttpStatus.OK);
    }

    public ResponseEntity<Integer> changeHelped(String userId, int newNumber) {
        User user = userRepo.findById(userId).orElse(null);
        if(user == null) return new ResponseEntity<>(0,HttpStatus.NOT_FOUND);
        user.setHelped(newNumber);
        return new ResponseEntity<>(user.getHelped(), HttpStatus.OK);
    }
}
