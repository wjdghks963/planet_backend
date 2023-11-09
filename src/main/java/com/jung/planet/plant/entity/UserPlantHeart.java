package com.jung.planet.plant.entity;

import com.jung.planet.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.NoArgsConstructor;


@Entity
@NoArgsConstructor
@Table(name = "user_plant_hearts")
public class UserPlantHeart {
    @EmbeddedId
    private UserPlantHeartId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @MapsId("plantId")
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;


    @Builder
    public UserPlantHeart(User user, Plant plant) {
        this.user = user;
        this.plant = plant;
    }


}
