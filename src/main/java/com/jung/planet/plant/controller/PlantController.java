package com.jung.planet.plant.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jung.planet.plant.dto.PlantDTO;
import com.jung.planet.plant.entity.Plant;
import com.jung.planet.plant.service.PlantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/plants")
public class PlantController {

    private static final Logger logger = LoggerFactory.getLogger(PlantController.class);


    private final PlantService plantService;

    @PostMapping("/add")
    public ResponseEntity<Plant> addPlant(@RequestBody PlantDTO plantDTO) {
        logger.info("Request to add plant: {}", plantDTO);
        Plant newPlant = plantService.addPlant(plantDTO);
        logger.info("Plant added: {}", newPlant);

        return ResponseEntity.ok(newPlant);
    }
}
