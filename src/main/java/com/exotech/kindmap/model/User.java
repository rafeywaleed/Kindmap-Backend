package com.exotech.kindmap.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Component
@Table(name = "users")
public class User{

    @Id
    private String userId;

    private String name;
    private String email;
    private int avatarIndex;
    private int helped;
    private LocalDateTime joinedDate;
    private String token;

    @ManyToMany
    @JoinTable(
            name = "user_grid_subscriptions",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "grid_id")
    )
    private List<Grid> subscribedGridIds = new ArrayList<>();

}