package com.jung.planet.plant.dto;

import lombok.Getter;
import lombok.Setter;



@Setter
@Getter
public class PlantSummaryDTO {
    private String nickName;
    private String imgUrl;
    private int period;
    private int heartCount;
}
