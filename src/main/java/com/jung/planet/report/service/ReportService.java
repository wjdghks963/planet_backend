package com.jung.planet.report.service;

import com.jung.planet.diary.dto.DiaryDetailDTO;
import com.jung.planet.diary.entity.Diary;
import com.jung.planet.diary.repository.DiaryRepository;
import com.jung.planet.diary.service.DiaryService;
import com.jung.planet.plant.dto.PlantDetailDTO;
import com.jung.planet.plant.dto.PlantSummaryDTO;
import com.jung.planet.plant.entity.Plant;
import com.jung.planet.plant.repository.PlantRepository;
import com.jung.planet.plant.service.PlantService;
import com.jung.planet.report.dto.ReportDTO;
import com.jung.planet.report.entity.Report;
import com.jung.planet.report.entity.ReportType;
import com.jung.planet.report.repository.ReportRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final PlantRepository plantRepository;
    private final DiaryRepository diaryRepository;

    private final PlantService plantService;
    private final DiaryService diaryService;

    public void reportEntity(Long entityId, ReportType entityType, Long reporterId) {
        Report report = new Report();
        report.setEntityId(entityId);
        report.setEntityType(entityType);
        report.setReporterId(reporterId);

        // Verify the existence of the entity
        if (entityType == ReportType.PLANT) {
            plantRepository.findById(entityId)
                    .orElseThrow(() -> new EntityNotFoundException("Plant not found"));
        } else if (entityType == ReportType.DIARY) {
            diaryRepository.findById(entityId)
                    .orElseThrow(() -> new EntityNotFoundException("Diary not found"));
        }

        reportRepository.save(report);
    }


    public List<ReportDTO> getAllDiaryReports() {
        return reportRepository.findByEntityType(ReportType.DIARY).stream()
                .map(this::convertToDiaryReportDTO)
                .collect(Collectors.toList());
    }

    public List<ReportDTO> getAllPlantReports() {
        return reportRepository.findByEntityType(ReportType.PLANT).stream()
                .map(this::convertToPlantReportDTO)
                .collect(Collectors.toList());
    }


    private ReportDTO convertToDiaryReportDTO(Report report) {
        Diary diary = diaryRepository.findById(report.getEntityId()).orElseThrow(() -> new EntityNotFoundException("다이어리 데이터를 찾을 수 없습니다."));

        DiaryDetailDTO diaryDetailDTO = diaryService.findDiary(diary.getId(), 0L);

        return ReportDTO.builder()
                .reportId(report.getId())
                .reporterId(report.getReporterId())
                .entityOwnerId(diary.getPlant().getUser().getId())
                .diaryDetails(diaryDetailDTO)
                .build();
    }

    private ReportDTO convertToPlantReportDTO(Report report) {
        Plant plant = plantRepository.findById(report.getId()).orElseThrow(() -> new EntityNotFoundException("식물 데이터를 찾을 수 없습니다."));
        PlantDetailDTO plantDetailDTO = plantService.getPlantDetailsByPlantId(0L,plant.getId());

        return ReportDTO.builder()   .reportId(report.getId())
                .reporterId(report.getReporterId())
                .entityOwnerId(plant.getUser().getId())
                .plantDetails(plantDetailDTO).build();

    }


}