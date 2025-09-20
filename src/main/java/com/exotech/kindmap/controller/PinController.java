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
@RequestMapping("/pin")

public class PinController {

    @Autowired
    private PinService pinService;

    @GetMapping("/all")
    public ResponseEntity<List<PinDTO>> getAllPins(){
        return new ResponseEntity<>(pinService.getAllPins(), HttpStatus.OK);
    }

    @GetMapping("/{pinId}")
    public ResponseEntity<PinDTO> getPin(@PathVariable String pinId){
        return pinService.getPin(pinId);

    }

    @PostMapping("/add")
    public ResponseEntity<PinDTO> addPin(@RequestBody Pin pin){
        pinService.addPin(pin);
        return pinService.getPin(pin.getPinId());
    }

    @DeleteMapping("/delete/{pinId}")
    public ResponseEntity<Boolean> removePin(@PathVariable String pinId){
        pinService.removePin(pinId);

        return pinService.searchPin(pinId) == null
                ? new ResponseEntity<>(Boolean.TRUE, HttpStatus.OK)
                : new ResponseEntity<>(Boolean.FALSE, HttpStatus.NO_CONTENT);
    }
}
