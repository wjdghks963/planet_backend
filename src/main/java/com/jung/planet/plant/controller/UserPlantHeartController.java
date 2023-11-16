package com.jung.planet.plant.controller;


import com.jung.planet.plant.service.UserPlantHeartService;
import com.jung.planet.security.UserDetail.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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


    @PostMapping("/heart/{id}")
    public ResponseEntity<?> togglePlantHeart(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable("id") Long plantId) {
        boolean hearted = userPlantHeartService.togglePlantHeart(customUserDetails.getUserId(), plantId);
        return ResponseEntity.ok(Map.of("hearted", hearted));
    }
}
