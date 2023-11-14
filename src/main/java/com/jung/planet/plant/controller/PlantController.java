package com.jung.planet.plant.controller;

import com.jung.planet.plant.dto.PlantDetailDTO;
import com.jung.planet.plant.dto.PlantSummaryDTO;
import com.jung.planet.security.UserDetail.CustomUserDetails;
import com.jung.planet.user.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jung.planet.plant.dto.PlantDTO;
import com.jung.planet.plant.entity.Plant;
import com.jung.planet.plant.service.PlantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/plants")
public class PlantController {

    private static final Logger logger = LoggerFactory.getLogger(PlantController.class);

    private final PlantService plantService;

    @PostMapping("/add")
    public ResponseEntity<Plant> addPlant(@AuthenticationPrincipal CustomUserDetails user, @RequestBody PlantDTO plantDTO) {
        logger.info("Request to add plant: {}", plantDTO);
        logger.info("Request token: {}", user.toString());
        plantDTO.setUserId(user.getUserId());
        Plant newPlant = plantService.addPlant(plantDTO);
        logger.info("Plant added: {}", newPlant);

        return ResponseEntity.ok(newPlant);
    }


    @GetMapping
    public ResponseEntity<List<PlantSummaryDTO>> getPlants(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long userId = customUserDetails.getUserId();
        List<PlantSummaryDTO> plants = plantService.getPlantsByUserId(userId);
        return ResponseEntity.ok(plants);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlantDetailDTO> getPlant(@PathVariable("id") Long plantId) {
        PlantDetailDTO plant = plantService.getPlantDetailsByPlantId(plantId);
        return ResponseEntity.ok(plant);
    }

}
