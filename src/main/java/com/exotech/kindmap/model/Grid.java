package com.exotech.kindmap.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor

@Table(name = "grids")
public class Grid {

    @Id
    private String gridId;

    @OneToMany(mappedBy = "grid", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pin> pins = new ArrayList<>();

    @ManyToMany(mappedBy = "subscribedGridIds")
    private List<User>  users = new ArrayList<>();

}



