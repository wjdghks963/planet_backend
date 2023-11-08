package com.jung.planet.plant.service;

import com.jung.planet.plant.dto.PlantDTO;
import com.jung.planet.plant.entity.Plant;
import com.jung.planet.plant.repository.PlantRepository;
import com.jung.planet.user.entity.User;
import com.jung.planet.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlantService {
    private final PlantRepository plantRepository;
    private final UserRepository userRepository;

    @Transactional
    public Plant addPlant(PlantDTO plantDTO) {
        User user = userRepository.findById(plantDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Plant plant = plantDTO.toEntity(user);
        return plantRepository.save(plant);
    }
}
