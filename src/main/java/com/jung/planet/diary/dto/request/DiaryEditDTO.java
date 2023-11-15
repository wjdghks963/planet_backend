package com.jung.planet.diary.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class DiaryEditDTO {

    @NotNull(message = "제목은 꼭 필요합니다.")
    private String title;

    @NotNull(message = "이미지는 꼭 필요합니다.")
    private String imgUrl;

    @NotNull(message = "내용은 꼭 채워야 합니다.")
    private String content;

    private Boolean isPublic;


}
