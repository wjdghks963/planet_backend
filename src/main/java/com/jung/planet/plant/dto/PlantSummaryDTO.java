package com.jung.planet.plant.dto;

import lombok.Getter;
import lombok.Setter;



@Builder
@Getter
public class PlantSummaryDTO {
    private Long id;
    private String nickName;
    private String imgUrl;
    private int period;
    private int heartCount;
}
