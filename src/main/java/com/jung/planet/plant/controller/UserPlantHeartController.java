package com.jung.planet.plant.controller;

import com.jung.planet.common.dto.ApiResponseDTO;
import com.jung.planet.plant.dto.response.HeartResponseDTO;
import com.jung.planet.plant.service.UserPlantHeartService;
import com.jung.planet.security.UserDetail.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/plants")
@RequiredArgsConstructor
public class UserPlantHeartController {

    private final UserPlantHeartService userPlantHeartService;

    @PostMapping("/heart/{id}")
    public ApiResponseDTO<HeartResponseDTO> togglePlantHeart(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable("id") Long plantId) {
        boolean hearted = userPlantHeartService.togglePlantHeart(customUserDetails.getUserId(), plantId);
        
        HeartResponseDTO responseDTO = HeartResponseDTO.builder()
                .plantId(plantId)
                .hearted(hearted)
                .build();
                
        return ApiResponseDTO.success(responseDTO, hearted ? "식물에 좋아요를 표시했습니다." : "식물에 좋아요를 취소했습니다.");
    }
}
