package com.exotech.kindmap.controller;

import com.exotech.kindmap.dto.UserDTO;
import com.exotech.kindmap.model.Grid;
import com.exotech.kindmap.model.User;
import com.exotech.kindmap.service.DTOServices;
import com.exotech.kindmap.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private DTOServices dtoServices;

    @GetMapping("/{userId}/details")
    public ResponseEntity<User> getUser(@PathVariable String userId){
        User user = userService.getUser(userId);

        return (user == null)
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(user);
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<UserDTO>> getAllUsers(){
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<UserDTO> addUser(@RequestBody User user){
        User u = userService.getUser(user.getUserId());
        if(u!=null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        userService.addUser(user);
        UserDTO userDTO = dtoServices.convertToUserDTO(user);
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

    @PutMapping("/{userId}/changename")
    public ResponseEntity<?> changeName(
            @PathVariable String userId,
            @RequestParam String newName){
        return userService.changeName(userId, newName);
    }

    @GetMapping("/{userId}/helped")
    public ResponseEntity<?> getUserHelped(
            @PathVariable String userId
    ){
        return userService.getUserHelped(userId);
    }

    @PutMapping("/{userId}/inchelped")
    public ResponseEntity<?> incrHelped(
            @PathVariable String userId
    ){
        return userService.incHelped(userId);
    }

    @PutMapping("/{userId}/changehelped")
    public ResponseEntity<?> changeHelped(
            @PathVariable String userId,
            @RequestParam int newNumber
    ){
        return userService.changeHelped(userId, newNumber);
    }

    @PutMapping("/{userId}/modify")
    public ResponseEntity<?> modify(
            @RequestBody User user,
            @PathVariable String userId){
        if(!user.getUserId().equals(userId)) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        User u = userService.getUser(user.getUserId());
        if(u==null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        userService.addUser(user);
        UserDTO userDTO = dtoServices.convertToUserDTO(user);
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

    @GetMapping("/{userId}/grids")
    public ResponseEntity<List<String>> allTopics(@PathVariable String userId){
        return userService.getSubscribedTopics(userId);
    }

    @GetMapping("/{userId}/unsubscribe")
    public ResponseEntity<List<String>> unsubscribeTopics(
            @PathVariable String userId,
            @RequestParam String gridId){
        return userService.unsubscribeTopic(userId, gridId);
    }

    @PostMapping("/{userId}/subscribe")
    public ResponseEntity<List<String>> subscribeUser(
            @PathVariable String userId,
            @RequestParam String gridId ) {
        return userService.subscribeUserToGrid(userId, gridId);
    }

    @PutMapping("/{userId}/avatar")
    public ResponseEntity<Integer> changeAvatarIndex(
            @PathVariable String userId,
            @RequestParam int newAvatarIndex){
        return userService.changeAvatarIndex(userId, newAvatarIndex);
    }

    @GetMapping("/{userId}/avatar")
    public ResponseEntity<Integer> getAvatarIndex(@PathVariable String userId){
        int avatar = userService.getAvatarIndex(userId);
        if(avatar==0) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(avatar, HttpStatus.OK);
    }

    @GetMapping("/{userId}/token")
    public ResponseEntity<String> getToken(@PathVariable String userId){
        return userService.getToken(userId);
    }

    @PutMapping("/{userId}/token")
    public ResponseEntity<String> updateToken(
            @PathVariable String userId,
            @RequestParam String token){
        return userService.updateToken(userId, token);
    }

}
