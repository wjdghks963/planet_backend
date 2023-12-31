package com.jung.planet.plant.entity;

import com.jung.planet.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Entity
@NoArgsConstructor
@Table(name = "user_plant_hearts")
public class UserPlantHeart {
    @EmbeddedId
    private UserPlantHeartId id;

    @ManyToOne(cascade = CascadeType.ALL)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(cascade = CascadeType.ALL)
    @MapsId("plantId")
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;


    @Builder
    public UserPlantHeart(User user, Plant plant) {
        this.id = new UserPlantHeartId(user.getId(), plant.getId());
        this.user = user;
        this.plant = plant;
    }


}
