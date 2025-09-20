package com.exotech.kindmap.controller;

import com.exotech.kindmap.dto.GridDTO;
import com.exotech.kindmap.dto.PinDTO;
import com.exotech.kindmap.dto.UserDTO;
import com.exotech.kindmap.model.Grid;
import com.exotech.kindmap.model.Pin;
import com.exotech.kindmap.model.User;
import com.exotech.kindmap.service.GridService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/grid")
public class GridController {

    @Autowired
    private GridService gridService;

    @GetMapping("/all")
    public ResponseEntity<List<GridDTO>> getAllGrids(){
        return new ResponseEntity<>(gridService.getAllGrids(), HttpStatus.OK);
    }

    @GetMapping("/{gridId}/pins")
    public ResponseEntity<List<PinDTO>> getPinsByGridId(@PathVariable String gridId){
        return new ResponseEntity<>(gridService.getPinsById(gridId), HttpStatus.OK);
    }

    @GetMapping("/{gridId}/users")
    public ResponseEntity<List<UserDTO>> getUsersByGridId(@PathVariable String gridId){
        return new ResponseEntity<>(gridService.getUsersById(gridId), HttpStatus.OK);
    }

}

