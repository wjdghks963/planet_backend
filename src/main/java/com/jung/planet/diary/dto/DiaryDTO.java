package com.jung.planet.diary.dto;

import com.jung.planet.diary.entity.Diary;
import com.jung.planet.plant.entity.Plant;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class DiaryDTO {

    private Long plantId;
    private String title;
    private String imgUrl;
    private String content;
    private Boolean isPublic;


    public Diary toEntity(Plant plant) {
        return Diary.builder().plant(plant).title(title).content(content).imgUrl(imgUrl).isPublic(isPublic).build();
    }

}
