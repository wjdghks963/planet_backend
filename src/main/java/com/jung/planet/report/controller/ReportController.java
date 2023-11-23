package com.jung.planet.report.controller;


import com.jung.planet.report.dto.ReportDTO;
import com.jung.planet.report.entity.ReportType;
import com.jung.planet.report.service.ReportService;
import com.jung.planet.security.UserDetail.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PostMapping("/plant/{plantId}")
    public ResponseEntity<?> reportPlant(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long plantId) {
        reportService.reportEntity(plantId, ReportType.PLANT, customUserDetails.getUserId());
        return ResponseEntity.ok("Plant reported successfully");
    }


    @PostMapping("/diary/{diaryId}")
    public ResponseEntity<?> reportDiary(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long diaryId) {
        reportService.reportEntity(diaryId, ReportType.DIARY, customUserDetails.getUserId());
        return ResponseEntity.ok("Diary reported successfully");
    }


    @GetMapping
    public ResponseEntity<?> reportedDiary(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestParam String type) {
        if (type.equals("plant")) {
            List<ReportDTO> allPlantReports = reportService.getAllPlantReports();
            return ResponseEntity.ok(Map.of("reportedPlants", allPlantReports));
        } else {
            List<ReportDTO> allDiaryReports = reportService.getAllDiaryReports();
            return ResponseEntity.ok(Map.of("reportedDiaries", allDiaryReports));
        }


    }


}

