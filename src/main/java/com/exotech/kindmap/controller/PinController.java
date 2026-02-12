package com.exotech.kindmap.controller;

import com.exotech.kindmap.dto.PinDTO;
import com.exotech.kindmap.model.Pin;
import com.exotech.kindmap.service.PinService;
import org.hibernate.engine.spi.Resolution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pins")

public class PinController {

    @Autowired
    private PinService pinService;

    @GetMapping("/all")
    public ResponseEntity<List<PinDTO>> getAllPins(){
        return ResponseEntity.ok(pinService.getAllPins());
    }

    @GetMapping("/{pinId}")
    public ResponseEntity<PinDTO> getPin(@PathVariable String pinId){
        return pinService.getPin(pinId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());

    }

    @PostMapping("/add")
    public ResponseEntity<PinDTO> addPin(@RequestBody PinDTO pin){
        PinDTO created = pinService.addPin(pin);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/delete/{pinId}")
    public ResponseEntity<Boolean> removePin(@PathVariable String pinId){
        pinService.removePin(pinId);
        return ResponseEntity.noContent().build();
    }
}
