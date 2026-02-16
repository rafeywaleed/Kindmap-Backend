package com.exotech.kindmap.controller;

import com.exotech.kindmap.dto.UserDTO;
import com.exotech.kindmap.model.User;
import com.exotech.kindmap.service.DTOServices;
import com.exotech.kindmap.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private DTOServices dtoServices;

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUser(@PathVariable String userId) {
        return userService.getUser(userId)
                .map(dtoServices::convertToUserDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }


    @PostMapping("/add")
    public ResponseEntity<UserDTO> addUser(@RequestBody UserDTO user) {
        if (userService.getUser(user.getUserId()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        UserDTO created = userService.addUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{userId}/changename")
    public ResponseEntity<UserDTO> changeName(
            @PathVariable String userId,
            @RequestParam String newName) {
        try {
            UserDTO updated = userService.changeName(userId, newName);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{userId}/helped")
    public ResponseEntity<Integer> getUserHelped(@PathVariable String userId) {
        try {
            return ResponseEntity.ok(userService.getUserHelped(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{userId}/helped/increment")
    public ResponseEntity<Integer> incrHelped(@PathVariable String userId) {
        try {
            return ResponseEntity.ok(userService.incHelped(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

//    @PutMapping("/{userId}")
//    public ResponseEntity<UserDTO> modifyUser(
//            @RequestBody UserDTO user,
//            @PathVariable String userId) {
//
//        if (!user.getUserId().equals(userId)) {
//            return ResponseEntity.badRequest().build();
//        }
//
//        if (userService.getUser(userId).isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        UserDTO updated = userService.addUser(user);
//        return ResponseEntity.ok(updated);
//    }

    @GetMapping("/{userId}/subscriptions")
    public ResponseEntity<List<String>> getSubscribedGrids(@PathVariable String userId) {
        try {
            return ResponseEntity.ok(userService.getSubscribedTopics(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{userId}/subscriptions/{gridId}")
    public ResponseEntity<List<String>> unsubscribeFromGrid(
            @PathVariable String userId,
            @PathVariable String gridId) {
        try {
            return ResponseEntity.ok(userService.unsubscribeFromGrid(userId, gridId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping("/{userId}/subscriptions/{gridId}")
    public ResponseEntity<UserDTO> subscribeToGrid(
            @PathVariable String userId,
            @PathVariable String gridId) {
        try {
            UserDTO user = userService.subscribeUserToGrid(userId, gridId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{userId}/avatar")
    public ResponseEntity<Integer> getAvatarIndex(@PathVariable String userId) {
        try {
            return ResponseEntity.ok(userService.getAvatarIndex(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @PutMapping("/{userId}/avatar")
    public ResponseEntity<Integer> changeAvatarIndex(
            @PathVariable String userId,
            @RequestParam Integer avatarIndex) {
        try {
            return ResponseEntity.ok(
                    userService.changeAvatarIndex(userId, avatarIndex)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/{userId}/token")
    public ResponseEntity<String> getToken(@PathVariable String userId) {
        return userService.getToken(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping("/{userId}/token")
    public ResponseEntity<String> updateToken(
            @PathVariable String userId,
            @RequestParam String token) {
        try {
            return ResponseEntity.ok(userService.updateToken(userId, token));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
