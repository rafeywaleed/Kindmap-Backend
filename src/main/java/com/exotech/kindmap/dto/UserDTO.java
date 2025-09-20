package com.exotech.kindmap.dto;

import com.exotech.kindmap.model.Grid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String userId;
    private String name;
    private String email;
    private int avatarIndex;
    private int helped;
    private LocalDateTime joinedDate;
    private String token;
    private List<String> subscribedGridIds = new ArrayList<>();
}
