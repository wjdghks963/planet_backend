package com.jung.planet.plant.service;

import com.jung.planet.plant.dto.PlantDTO;
import com.jung.planet.plant.dto.PlantSummaryDTO;
import com.jung.planet.plant.entity.Plant;
import com.jung.planet.plant.repository.PlantRepository;
import com.jung.planet.user.entity.User;
import com.jung.planet.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<PlantSummaryDTO> getPlantsByUserId(Long userId) {
        List<Plant> plants = plantRepository.findByUserId(userId);
        return plants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private PlantSummaryDTO convertToDto(Plant plant) {
        PlantSummaryDTO dto = new PlantSummaryDTO();
        dto.setNickName(plant.getNickName());
        dto.setScientificName(plant.getScientificName());
        dto.setHeartCount(plant.getHeartCount());
        dto.setImgUrl(plant.getImgUrl());
        dto.setPeriod(calculatePeriod(plant.getCreatedAt()));
        return dto;
    }

    private int calculatePeriod(LocalDateTime createdAt) {
        return (int) ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
    }


}
