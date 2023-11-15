package com.jung.planet.plant.controller;

import com.jung.planet.plant.dto.PlantDetailDTO;
import com.jung.planet.plant.dto.PlantFormDTO;
import com.jung.planet.plant.dto.PlantSummaryDTO;
import com.jung.planet.plant.entity.Plant;
import com.jung.planet.plant.service.PlantService;
import com.jung.planet.security.UserDetail.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/plants")
public class PlantController {

    private static final Logger logger = LoggerFactory.getLogger(PlantController.class);

    private final PlantService plantService;

    @GetMapping
    public ResponseEntity<?> getPlants(@RequestParam(defaultValue = "0") int page) {
        List<PlantSummaryDTO> plants = plantService.getPlantsByRecent(page);
        return ResponseEntity.ok(plants);
    }


    @PostMapping("/add")
    public ResponseEntity<?> addPlant(@AuthenticationPrincipal CustomUserDetails user, @RequestBody PlantFormDTO plantFormDTO) {
        logger.info("Request to add plant: {}", plantFormDTO);
        logger.info("Request token: {}", user.toString());
        plantFormDTO.setUserId(user.getUserId());
        Plant newPlant = plantService.addPlant(plantFormDTO);
        logger.info("Plant added: {}", newPlant);

        return ResponseEntity.ok(newPlant);
    }

    @PostMapping("/edit/{id}")
    public ResponseEntity<?> editPlant(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody PlantFormDTO plantFormDTO, @PathVariable("id") Long plantId) {
        Long userId = customUserDetails.getUserId();
        // 식물 소유권 확인
        if (!plantService.isOwnerOfPlant(userId, plantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not own this plant");
        }


        try {
            plantFormDTO.setUserId(userId);
            Plant newPlant = plantService.editPlant(plantFormDTO, plantId);
            logger.info("Plant edited: {}", newPlant);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            logger.error("Error editing plant: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurred while editing the plant.");
        }
    }


    @GetMapping("/my")
    public ResponseEntity<List<PlantSummaryDTO>> getPlants(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long userId = customUserDetails.getUserId();
        List<PlantSummaryDTO> plants = plantService.getPlantsByUserId(userId);
        return ResponseEntity.ok(plants);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlantDetailDTO> getPlant(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable("id") Long plantId) {
        Long userId = customUserDetails != null ? customUserDetails.getUserId() : -1;
        PlantDetailDTO plant = plantService.getPlantDetailsByPlantId(userId, plantId);
        return ResponseEntity.ok(plant);
    }


    @DeleteMapping("/remove/{id}")
    public ResponseEntity<?> deletePlant(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable("id") Long plantId) {
        Long userId = customUserDetails.getUserId();

        if (!plantService.isOwnerOfPlant(userId, plantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not own this plant");
        }


        try {
            plantService.removePlant(plantId);
            logger.info("Plant deleted");
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            logger.error("Error delete plant: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurred while editing the plant.");
        }

    }

}
