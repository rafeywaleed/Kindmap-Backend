package com.exotech.kindmap.service;

import com.exotech.kindmap.dto.GridDTO;
import com.exotech.kindmap.dto.PinDTO;
import com.exotech.kindmap.dto.UserDTO;
import com.exotech.kindmap.model.Grid;
import com.exotech.kindmap.model.Pin;
import com.exotech.kindmap.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DTOServices {

    public PinDTO convertToPinDTO(Pin pin) {
        PinDTO dto = new PinDTO();
        dto.setPinId(pin.getPinId());
        dto.setGridId(pin.getGrid().getGridId());
        dto.setCreatedAt(pin.getCreatedAt());
        dto.setDetails(pin.getDetails());
        dto.setNote(pin.getNote());
        dto.setLatitude(pin.getLatitude());
        dto.setLongitude(pin.getLongitude());
        dto.setImageBase64(pin.getImageBase64());
        dto.setTimer(pin.getTimer());
        dto.setCreatedBy(pin.getCreatedBy());
        return dto;
    }

    public UserDTO convertToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setAvatarIndex(user.getAvatarIndex());
        dto.setHelped(user.getHelped());
        dto.setJoinedDate(user.getJoinedDate());

        List<String> gridIds = user.getSubscribedGridIds()
                .stream()
                .map(Grid::getGridId)
                .toList();

        dto.setSubscribedGridIds(gridIds);
        return dto;
    }


    public GridDTO convertToGridDTO(Grid grid) {
        GridDTO dto = new GridDTO();
        dto.setGridId(grid.getGridId());

        List<PinDTO> pinDTOs = grid.getPins()
                .stream()
                .map(this::convertToPinDTO) // assuming method is in same class
                .toList();

        List<String> userIds = grid.getUsers()
                .stream()
                .map(User::getUserId)
                .toList();

        dto.setPins(pinDTOs);
        dto.setUsers(userIds);
        return dto;
    }


}
