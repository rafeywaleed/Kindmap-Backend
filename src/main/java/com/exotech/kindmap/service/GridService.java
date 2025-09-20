package com.exotech.kindmap.service;

import com.exotech.kindmap.dto.GridDTO;
import com.exotech.kindmap.dto.PinDTO;
import com.exotech.kindmap.dto.UserDTO;
import com.exotech.kindmap.model.Grid;
import com.exotech.kindmap.model.Pin;
import com.exotech.kindmap.model.User;
import com.exotech.kindmap.repository.GridRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GridService {

    @Autowired
    private GridRepo gridRepo;

    @Autowired
    private DTOServices dtoServices;

    public List<GridDTO> getAllGrids() {
        return gridRepo
                .findAll()
                .stream()
                .map(grid -> dtoServices.convertToGridDTO(grid))
                .toList();
    }

    public List<PinDTO> getPinsById(String gridId) {
        Grid grid = gridRepo.findById(gridId).orElse(null);
        if(grid==null) return new ArrayList<>();
        return grid
                .getPins()
                .stream()
                .map(pin -> dtoServices.convertToPinDTO(pin))
                .toList();
    }

    public List<UserDTO> getUsersById(String gridId) {
        Grid grid = gridRepo.findById(gridId).orElse(null);
        if(grid==null) return new ArrayList<>();
        return grid
                .getUsers()
                .stream()
                .map(user -> dtoServices.convertToUserDTO(user))
                .toList();
    }

}
