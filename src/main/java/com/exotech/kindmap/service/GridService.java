package com.exotech.kindmap.service;

import com.exotech.kindmap.dto.GridDTO;
import com.exotech.kindmap.dto.PinDTO;
import com.exotech.kindmap.dto.UserDTO;
import com.exotech.kindmap.repository.GridRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class GridService {

    @Autowired
    private GridRepo gridRepo;

    @Autowired
    private DTOServices dtoServices;

    public List<GridDTO> getAllGrids() {
        return gridRepo
                .findAllWithPins()
                .stream()
                .map(grid -> dtoServices.convertToGridDTO(grid))
                .toList();
    }

    public List<PinDTO> getPinsById(String gridId) {

        return gridRepo
                .findByIdWithPins(gridId)
                .map(grid -> grid.getPins()
                        .stream()
                        .map(pin -> dtoServices.convertToPinDTO(pin))
                        .toList())
                .orElse(new ArrayList<>());

//        Grid grid = gridRepo.findById(gridId).orElse(null);
//        if(grid==null) return new ArrayList<>();
//        return grid
//                .getPins()
//                .stream()
//                .map(pin -> dtoServices.convertToPinDTO(pin))
//                .toList();
    }

    public List<UserDTO> getUsersById(String gridId) {
        return gridRepo
                .findByIdWithUsers(gridId)
                .map(grid -> grid.getUsers()
                        .stream()
                        .map(user -> dtoServices.convertToUserDTO(user))
                        .toList())
                .orElse(new ArrayList<>());

//        Grid grid = gridRepo.findById(gridId).orElse(null);
//        if(grid==null) return new ArrayList<>();
//        return grid
//                .getUsers()
//                .stream()
//                .map(user -> dtoServices.convertToUserDTO(user))
//                .toList();
    }

    public Optional<GridDTO> getGridById(String gridId) {
        return gridRepo
                .findByIdWithPins(gridId)
                .map(grid -> dtoServices.convertToGridDTO(grid));
    }

}
