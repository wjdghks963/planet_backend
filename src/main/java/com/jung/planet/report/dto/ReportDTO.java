package com.jung.planet.report.dto;


import com.jung.planet.diary.dto.DiaryDetailDTO;
import com.jung.planet.plant.dto.PlantDetailDTO;
import com.jung.planet.plant.dto.PlantSummaryDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportDTO {
    private Long reportId;

    private String reporterId; // 신고한 사용자의 ID
    private String entityOwnerId; // 신고된 엔티티(다이어리 또는 식물)의 소유자 ID


    private PlantDetailDTO plantDetails; // 식물에 대한 상세 정보
    private DiaryDetailDTO diaryDetails; // 다이어리에 대한 상세 정보


    @Builder
    public ReportDTO(Long reportId, String reporterId, String entityOwnerId, PlantDetailDTO plantDetails, DiaryDetailDTO diaryDetails) {
        this.reportId = reportId;
        this.reporterId = reporterId;
        this.entityOwnerId = entityOwnerId;
        this.plantDetails = plantDetails;
        this.diaryDetails = diaryDetails;
    }
}
