package com.exotech.kindmap.dto;

import com.exotech.kindmap.model.Grid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PinDTO {

    private String pinId;
    private String gridId;
    private LocalDateTime createdAt;
    private String details;
    private String note;
    private double latitude;
    private double longitude;
    private String imageBase64;
    private int timer;
    private String createdBy;
}
