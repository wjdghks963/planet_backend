package com.jung.planet.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "week_popular")
public class WeekPopular {
    @Id
    @Column(name = "week_start_date", nullable = false)
    private LocalDateTime weekStartDate;

    @ManyToOne
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    @Column(name = "heart_count", nullable = false)
    private Integer heartCount;

    @Column(nullable = false)
    private Integer ranking;

}

