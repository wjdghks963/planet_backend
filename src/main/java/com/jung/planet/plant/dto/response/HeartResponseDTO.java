package com.jung.planet.plant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeartResponseDTO {
    private Long plantId;
    private boolean hearted;
} 