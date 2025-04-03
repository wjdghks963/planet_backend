package com.jung.planet.plant.dto.response;

import com.jung.planet.plant.dto.PlantSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantResponseDTO {
    private Long plantId;
    private boolean success;
    private List<PlantSummaryDTO> plants;
    
    // 단일 식물 응답을 위한 팩토리 메서드
    public static PlantResponseDTO forSinglePlant(Long plantId) {
        return PlantResponseDTO.builder()
                .plantId(plantId)
                .success(true)
                .build();
    }
    
    // 식물 목록 응답을 위한 팩토리 메서드
    public static PlantResponseDTO forPlantList(List<PlantSummaryDTO> plants) {
        return PlantResponseDTO.builder()
                .plants(plants)
                .success(true)
                .build();
    }
} 