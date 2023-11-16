package com.jung.planet.diary.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class DiaryDetailDTO {
    private Long id;
    private String content;
    private boolean isPublic;
    private String imgUrl;
    private String createdAt;
    private boolean isMine;

}
