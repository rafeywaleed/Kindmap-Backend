package com.exotech.kindmap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GridDTO {

    private String gridId;
    private List<PinDTO> pins = new ArrayList<>();
    private List<String>  users = new ArrayList<>();
}