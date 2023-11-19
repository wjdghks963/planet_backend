package com.jung.planet.plant.controller;

import com.jung.planet.plant.dto.PlantDetailDTO;
import com.jung.planet.plant.dto.request.PlantFormDTO;
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
    public ResponseEntity<?> getPlants(@RequestParam(defaultValue = "recent") String type,
                                       @RequestParam(defaultValue = "0") int page) {
        List<PlantSummaryDTO> plants;
        if ("popular".equals(type)) {
            plants = plantService.getPlantsByPopularity(page);
        } else {
            plants = plantService.getPlantsByRecent(page);
        }
        return ResponseEntity.ok(plants);
    }


    @PostMapping("/add")
    public ResponseEntity<?> addPlant(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody PlantFormDTO plantFormDTO) {
        logger.info("Request to add plant: {}", plantFormDTO);

        plantFormDTO.setUserId(customUserDetails.getUserId());
        Plant newPlant = plantService.addPlant(plantFormDTO);
        logger.info("Plant added: {}", newPlant);

        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/edit/{id}")
    public ResponseEntity<?> editPlant(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody PlantFormDTO plantFormDTO, @PathVariable("id") Long plantId) {
        Long userId = customUserDetails.getUserId();
        logger.debug("userId :: {}", userId);
        // 식물 소유권 확인
        if (!plantService.isOwnerOfPlant(userId, plantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("식물의 주인이 아닙니다.");
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


    @GetMapping("/random")
    public ResponseEntity<List<PlantSummaryDTO>> getRandomPlants() {
        List<PlantSummaryDTO> randomPlants = plantService.getRandomPlants();
        return ResponseEntity.ok(randomPlants);
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
        String userEmail = customUserDetails.getUsername();

        if (!plantService.isOwnerOfPlant(userId, plantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("식물에 대한 권한이 없습니다.");
        }


        try {
            plantService.removePlant(plantId, userEmail);
            logger.info("Plant deleted");
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            logger.error("Error delete plant: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("삭제 중 에러가 발생했습니다.");
        }

    }

}
