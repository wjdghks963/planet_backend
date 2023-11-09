package com.jung.planet.plant.repository;

import com.jung.planet.plant.entity.UserPlantHeart;
import com.jung.planet.plant.entity.UserPlantHeartId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPlantHeartRepository extends JpaRepository<UserPlantHeart, UserPlantHeartId> {
}
