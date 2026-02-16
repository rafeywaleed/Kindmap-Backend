package com.exotech.kindmap.service;

import com.exotech.kindmap.dto.PinDTO;
import com.exotech.kindmap.model.Grid;
import com.exotech.kindmap.model.Pin;
import com.exotech.kindmap.repository.GridRepo;
import com.exotech.kindmap.repository.PinRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PinService {

    @Autowired
    private PinRepo pinRepo;

    @Autowired
    private GridRepo gridRepo;

    @Autowired
    private DTOServices dtoServices;

    @Autowired
    private FCMService fcmService;

    @Autowired
    private PinDeletionScheduler pinDeletionScheduler;

    private static final Logger log = LoggerFactory.getLogger(PinService.class);


    public List<PinDTO> getAllPins() {
        return pinRepo
                .findAllWithGrid()
                .stream()
                .map(pin -> dtoServices.convertToPinDTO(pin))
                .toList();
    }

//    public ResponseEntity<PinDTO> getPin(String pinId) {
//        Pin pin =  searchPin(pinId);
//
//        return pin == null
//                ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
//                : new ResponseEntity<>(dtoServices.convertToPinDTO(pin), HttpStatus.OK);
//    }

    public Optional<PinDTO> getPin(String pinId) {
        return pinRepo
                .findByIdWithGrid(pinId)
                .map(pin -> dtoServices.convertToPinDTO(pin));
    }

    public Optional<Pin> findPinById(String pinId) {
        return pinRepo.findByIdWithGrid(pinId);
    }

//    public Pin searchPin(String pinId){
////        Pin pin = pinRepo.findById(pinId).orElse(null);
////        return pin != null
////                ? dtoServices.convertToPinDTO(pin)
////                : new PinDTO();
//        return pinRepo.findById(pinId).orElse(null);
//    }

    @Transactional
    public PinDTO addPin(PinDTO pinDTO) {
        if (pinRepo.existsById(pinDTO.getPinId())) {
            log.warn("⚠️ Pin with ID {} already exists! Cannot create duplicate.", pinDTO.getPinId());
            throw new RuntimeException("Pin with ID " + pinDTO.getPinId() + " already exists");
        }

        if(pinDTO.getDetails() == null || pinDTO.getDetails().isEmpty())
            pinDTO.setDetails("(none)");
        if(pinDTO.getNote() == null || pinDTO.getNote().isEmpty())
            pinDTO.setNote("(none)");

        String gridId = pinDTO.getGridId();
        Grid grid = gridRepo.findByIdWithPins(gridId)
                .orElseGet(() -> {
                    Grid newGrid = new Grid();
                    newGrid.setGridId(gridId);
                    newGrid.setPins(new ArrayList<>());
                    newGrid.setUsers(new ArrayList<>());
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

//        System.out.println("Image base64 length: " + pin.getImageBase64().length());

        Pin savedPin = pinRepo.save(pin);
        if (grid.getPins() == null) {
            grid.setPins(new ArrayList<>());
        }
        grid.getPins().add(savedPin);
        gridRepo.save(grid);

        pinDeletionScheduler.schedulePinDeletion(savedPin.getPinId(), savedPin.getCreatedAt(),savedPin.getTimer());

        try {
            fcmService.sendNewPinNotification(pinDTO);
            log.info("✅ Notification sent for new pin {} in grid {}",
                    pinDTO.getPinId(), pinDTO.getGridId());
        } catch (Exception e) {
            log.error("❌ Failed to send notification for pin {}: {}",
                    pinDTO.getPinId(), e.getMessage());
        }
        return dtoServices.convertToPinDTO(savedPin);
    }

    @Transactional
    public void removePin(String pinId) {
        pinRepo.deleteById(pinId);
    }

}
