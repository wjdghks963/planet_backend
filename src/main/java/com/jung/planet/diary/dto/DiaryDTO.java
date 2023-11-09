package com.jung.planet.diary.dto;

import com.jung.planet.diary.entity.Diary;
import com.jung.planet.plant.entity.Plant;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class DiaryDTO {

    @NotNull(message = "plant를 찾을 수 없습니다.")
    private Long plantId;

    @NotNull(message = "제목은 꼭 필요합니다.")
    private String title;

    @NotNull(message = "이미지는 꼭 필요합니다.")
    private String imgUrl;

    @NotNull(message = "내용은 꼭 채워야 합니다.")
    private String content;

    private Boolean isPublic;


    public Diary toEntity(Plant plant) {
        return Diary.builder().plant(plant).title(title).content(content).imgUrl(imgUrl).isPublic(isPublic).build();
    }

}
