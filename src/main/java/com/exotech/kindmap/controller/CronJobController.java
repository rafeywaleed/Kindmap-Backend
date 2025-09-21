package com.exotech.kindmap.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CronJobController {

    @GetMapping("/kindmap")
    public ResponseEntity<String> cron(){
        return new ResponseEntity<>("Welcome to Kindmap", HttpStatus.OK);
    }
}
