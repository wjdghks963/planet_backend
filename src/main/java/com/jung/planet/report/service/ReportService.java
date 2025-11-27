package com.jung.planet.report.service;

import com.jung.planet.admin.service.SlackNotificationService;
import com.jung.planet.diary.dto.DiaryDetailDTO;
import com.jung.planet.diary.entity.Diary;
import com.jung.planet.diary.repository.DiaryRepository;
import com.jung.planet.diary.service.DiaryService;
import com.jung.planet.plant.dto.PlantDetailDTO;
import com.jung.planet.plant.entity.Plant;
import com.jung.planet.plant.repository.PlantRepository;
import com.jung.planet.plant.service.PlantService;
import com.jung.planet.report.dto.ReportDTO;
import com.jung.planet.report.entity.Report;
import com.jung.planet.report.entity.ReportType;
import com.jung.planet.report.repository.ReportRepository;
import com.jung.planet.user.entity.User;
import com.jung.planet.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final PlantRepository plantRepository;
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;

    private final PlantService plantService;
    private final DiaryService diaryService;
    private final SlackNotificationService slackNotificationService;

    @Transactional
    public void reportEntity(Long entityId, ReportType entityType, Long reporterId) {
        Report report;

        if (entityType.equals(ReportType.PLANT)) {
            Plant plant = plantRepository.findById(entityId)
                    .orElseThrow(() -> new EntityNotFoundException("식물을 찾을 수 없습니다."));

            report = Report.builder()
                    .reportedPlant(plant)
                    .reporterId(reporterId)
                    .build();

            Map<String, String> infoData = new HashMap<>();
            infoData.put("PLANT ID", plant.getId().toString());
            infoData.put("PLANT CONTENT", plant.getNickName());
            infoData.put("PLANT IMG_URL", plant.getImgUrl());
            slackNotificationService.sendSlackReportNotification("식물 신고", infoData);

        } else if (entityType.equals(ReportType.DIARY)) {
            Diary diary = diaryRepository.findById(entityId)
                    .orElseThrow(() -> new EntityNotFoundException("일지를 찾을 수 없습니다."));

            report = Report.builder()
                    .reportedDiary(diary)
                    .reporterId(reporterId)
                    .build();

            Map<String, String> infoData = new HashMap<>();
            infoData.put("DIARY ID", diary.getId().toString());
            infoData.put("DIARY CONTENT", diary.getContent());
            infoData.put("DIARY IMG_URL", diary.getImgUrl());
            slackNotificationService.sendSlackReportNotification("다이어리 신고", infoData);
        } else {
            throw new IllegalArgumentException("지원하지 않는 신고 유형입니다.");
        }

        reportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public List<ReportDTO> getAllDiaryReports() {
        return reportRepository.findAllDiaryReportsWithUser().stream()
                .map(this::convertToDiaryReportDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReportDTO> getAllPlantReports() {
        return reportRepository.findAllPlantReportsWithUser().stream()
                .map(this::convertToPlantReportDTO)
                .collect(Collectors.toList());
    }


    private ReportDTO convertToDiaryReportDTO(Report report) {
        Diary diary = report.getReportedDiary();
        DiaryDetailDTO diaryDetailDTO = diaryService.findDiary(diary.getId(), 0L);
        User reporter = userRepository.findById(report.getReporterId())
                .orElseThrow(() -> new EntityNotFoundException("유저 정보 없음"));
        User diaryOwner = diary.getPlant().getUser();

        return ReportDTO.builder()
                .reportId(report.getId())
                .reporterId(reporter.getEmail())
                .entityOwnerId(diaryOwner.getEmail())
                .diaryDetails(diaryDetailDTO)
                .build();
    }

    private ReportDTO convertToPlantReportDTO(Report report) {
        Plant plant = report.getReportedPlant();
        PlantDetailDTO plantDetailDTO = plantService.getPlantDetailsByPlantId(0L, plant.getId());
        User reporter = userRepository.findById(report.getReporterId())
                .orElseThrow(() -> new EntityNotFoundException("유저 정보 없음"));
        User plantOwner = plant.getUser();

        return ReportDTO.builder()
                .reportId(report.getId())
                .reporterId(reporter.getEmail())
                .entityOwnerId(plantOwner.getEmail())
                .plantDetails(plantDetailDTO)
                .build();
    }


}