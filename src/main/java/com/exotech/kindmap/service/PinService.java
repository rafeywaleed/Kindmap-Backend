package com.exotech.kindmap.service;

import com.exotech.kindmap.dto.PinDTO;
import com.exotech.kindmap.model.Grid;
import com.exotech.kindmap.model.Pin;
import com.exotech.kindmap.repository.GridRepo;
import com.exotech.kindmap.repository.PinRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PinService {

    @Autowired
    private PinRepo pinRepo;

    @Autowired
    private GridRepo gridRepo;

    @Autowired
    private DTOServices dtoServices;


    public List<PinDTO> getAllPins() {
        return pinRepo
                .findAll()
                .stream()
                .map(pin -> dtoServices.convertToPinDTO(pin))
                .toList();
    }

    public ResponseEntity<PinDTO> getPin(String pinId) {
        Pin pin =  searchPin(pinId);

        return pin == null
                ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                : new ResponseEntity<>(dtoServices.convertToPinDTO(pin), HttpStatus.OK);
    }

    public Pin searchPin(String pinId){
//        Pin pin = pinRepo.findById(pinId).orElse(null);
//        return pin != null
//                ? dtoServices.convertToPinDTO(pin)
//                : new PinDTO();
        return pinRepo.findById(pinId).orElse(null);
    }

    public void addPin(PinDTO pinDTO) {
        if(pinDTO.getDetails() == null || pinDTO.getDetails().isEmpty())
            pinDTO.setDetails("(none)");
        if(pinDTO.getNote() == null || pinDTO.getNote().isEmpty())
            pinDTO.setNote("(none)");

//        String gridId = pin.getGrid().getGridId();
        String gridId = pinDTO.getGridId();
        Grid grid = gridRepo.findById(gridId)
                        .orElseGet(() ->{
                            Grid newGrid = new Grid();
                            newGrid.setGridId(gridId);
                            return gridRepo.save(newGrid);
                        });
        Pin pin = new Pin();
        pin.setPinId(pinDTO.getPinId());
        pin.setGrid(grid);
        pin.setNote(pinDTO.getNote());
        pin.setDetails(pinDTO.getDetails());
        pin.setLatitude(pinDTO.getLatitude());
        pin.setLongitude(pinDTO.getLongitude());
        pin.setTimer(pinDTO.getTimer());
        pin.setCreatedAt(pinDTO.getCreatedAt());
        pin.setImageBase64(pinDTO.getImageBase64());
        pin.setCreatedBy(pinDTO.getCreatedBy());

        System.out.println("Image base64 length: " + pin.getImageBase64().length());

        Pin savedPin = pinRepo.save(pin);

        grid.getPins().add(savedPin);
        gridRepo.save(grid);
    }

    public void removePin(String pinId) {
        pinRepo.deleteById(pinId);
    }

}
