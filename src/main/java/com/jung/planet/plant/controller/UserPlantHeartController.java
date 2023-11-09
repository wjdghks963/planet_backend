package com.jung.planet.plant.controller;


import com.jung.planet.plant.service.UserPlantHeartService;
import com.jung.planet.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/plants")
@RequiredArgsConstructor
public class UserPlantHeartController {

    private final UserPlantHeartService userPlantHeartService;


    @PostMapping("/{plantId}/heart")
    public ResponseEntity<?> togglePlantHeart(@PathVariable("plantId") Long plantId, Long userId) {
        boolean hearted = userPlantHeartService.togglePlantHeart(userId, plantId);
        return ResponseEntity.ok(Map.of("hearted", hearted));
    }
}
