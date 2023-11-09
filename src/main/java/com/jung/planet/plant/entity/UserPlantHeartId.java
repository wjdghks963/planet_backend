package com.jung.planet.plant.entity;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@NoArgsConstructor
public class UserPlantHeartId implements Serializable {
    private Long userId;
    private Long plantId;

    @Builder
    public UserPlantHeartId(Long userId, Long plantId) {
        this.userId = userId;
        this.plantId = plantId;
    }
}