package com.jung.planet.diary.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DiaryDetailDTO {
    private Long id;
    private String title;
    private String content;
    private boolean isPublic;
    private String imgUrl;
    private LocalDateTime createdAt;
}
