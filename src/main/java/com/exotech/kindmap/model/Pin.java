package com.exotech.kindmap.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pins")
public class Pin {

    @Id
    private String pinId;

    @ManyToOne
    @JoinColumn(name = "grid_id")
    private Grid grid;

    // convert the date to ISO || in Flutter: String createdAt = DateTime.now().toIso8601String();
    private LocalDateTime createdAt;
    private String createdBy;
    private String details;
    private String note;
    private double latitude;
    private double longitude;
    private int timer;

    //    @Lob
    @Column(columnDefinition = "TEXT")
    private String imageBase64;

}
