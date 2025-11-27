package com.jung.planet.plant.mapper;

import com.jung.planet.diary.dto.DiaryDetailDTO;
import com.jung.planet.diary.entity.Diary;
import com.jung.planet.plant.dto.PlantDetailDTO;
import com.jung.planet.plant.dto.PlantSummaryDTO;
import com.jung.planet.plant.entity.Plant;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Plant 엔티티와 DTO 간 변환을 담당하는 Mapper 클래스
 */
@Component
public class PlantMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Plant 엔티티를 PlantSummaryDTO로 변환합니다.
     *
     * @param plant Plant 엔티티
     * @return PlantSummaryDTO
     */
    public PlantSummaryDTO toSummaryDto(Plant plant) {
        PlantSummaryDTO dto = new PlantSummaryDTO();
        dto.setId(plant.getId());
        dto.setNickName(plant.getNickName());
        dto.setHeartCount(plant.getHeartCount());
        dto.setImgUrl(plant.getImgUrl());
        dto.setPeriod(calculatePeriod(plant.getCreatedAt()));
        return dto;
    }

    /**
     * Plant 엔티티를 PlantDetailDTO로 변환합니다.
     *
     * @param userId 현재 사용자 ID
     * @param plant Plant 엔티티
     * @param isHearted 좋아요 여부
     * @return PlantDetailDTO
     */
    public PlantDetailDTO toDetailDto(Long userId, Plant plant, boolean isHearted) {
        boolean isMine = Objects.equals(plant.getUser().getId(), userId);

        PlantDetailDTO dto = new PlantDetailDTO();
        dto.setPlantId(plant.getId());
        dto.setNickName(plant.getNickName());
        dto.setScientificName(plant.getScientificName());
        dto.setImgUrl(plant.getImgUrl());
        dto.setHeartCount(plant.getHeartCount());
        dto.setPeriod(calculatePeriod(plant.getCreatedAt()));
        dto.setMine(isMine);
        dto.setHearted(isHearted);
        dto.setCreatedAt(plant.getCreatedAt().format(DATE_FORMATTER));

        List<DiaryDetailDTO> diaryDTOs = plant.getDiaries().stream()
                .map(diary -> toDiaryDetailDto(diary, userId))
                .collect(Collectors.toList());
        dto.setDiaries(diaryDTOs);

        return dto;
    }

    /**
     * Diary 엔티티를 DiaryDetailDTO로 변환합니다.
     *
     * @param diary Diary 엔티티
     * @param userId 현재 사용자 ID
     * @return DiaryDetailDTO
     */
    public DiaryDetailDTO toDiaryDetailDto(Diary diary, Long userId) {
        DiaryDetailDTO diaryDTO = new DiaryDetailDTO();
        diaryDTO.setId(diary.getId());
        diaryDTO.setContent(diary.getContent());
        diaryDTO.setPublic(diary.getIsPublic());
        diaryDTO.setImgUrl(diary.getImgUrl());
        diaryDTO.setCreatedAt(diary.getCreatedAt().format(DATE_FORMATTER));

        boolean isMine = diary.getPlant().getUser().getId().equals(userId);
        diaryDTO.setMine(isMine);

        return diaryDTO;
    }

    /**
     * 생성일로부터 경과한 일수를 계산합니다.
     *
     * @param createdAt 생성일시
     * @return 경과 일수
     */
    private int calculatePeriod(LocalDateTime createdAt) {
        return (int) ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
    }
}
