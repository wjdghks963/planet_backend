package com.jung.planet.model;

import jakarta.persistence.*;


@Entity
@Table(name = "user_plant_hearts")
public class UserPlantHeart {
    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

}

