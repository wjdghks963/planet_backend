package com.jung.planet.diary.dto.request;

import com.jung.planet.diary.entity.Diary;
import com.jung.planet.plant.entity.Plant;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class DiaryFormDTO {

    private Long plantId;

    @NotNull(message = "이미지는 꼭 필요합니다.")
    private String imgData;

    @NotNull(message = "내용은 꼭 채워야 합니다.")
    private String content;

    private Boolean isPublic;

    public Diary toEntity(Plant plant, String imgUrl) {
        return Diary.builder().plant(plant).content(content).imgUrl(imgUrl).isPublic(isPublic).build();
    }
}
