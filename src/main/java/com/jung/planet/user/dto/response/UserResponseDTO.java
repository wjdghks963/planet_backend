package com.jung.planet.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private String name;
    private long period;
    private int receivedHearts;
    private int givenHearts;
    private int maxPlants;
    private boolean aiServiceAccess;
    
    public static UserResponseDTO fromMap(Map<String, Object> map) {
        return UserResponseDTO.builder()
                .name((String) map.get("name"))
                .period((long) map.get("period"))
                .receivedHearts((int) map.get("receivedHearts"))
                .givenHearts((int) map.get("givenHearts"))
                .maxPlants((int) map.get("maxPlants"))
                .aiServiceAccess((boolean) map.get("aiServiceAccess"))
                .build();
    }
} 