package com.jung.planet.plant.dto;

import com.jung.planet.diary.dto.DiaryDetailDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class PlantDetailDTO {

    private Long plantId;
    private String nickName;
    private String scientificName;
    private String imgUrl;
    private int period;
    private int heartCount;
    private List<DiaryDetailDTO> diaries;
    private boolean isMine;

}
