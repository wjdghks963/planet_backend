package com.jung.planet.report.controller;

import com.jung.planet.report.dto.ReportDTO;
import com.jung.planet.report.entity.ReportType;
import com.jung.planet.report.service.ReportService;
import com.jung.planet.security.UserDetail.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Report", description = "신고 관련 API")
@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @Operation(summary = "식물 신고", description = "특정 식물을 신고합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "신고 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "404", description = "식물을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/plant/{plantId}")
    public ResponseEntity<?> reportPlant(
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @Parameter(description = "식물 ID") @PathVariable Long plantId) {
        reportService.reportEntity(plantId, ReportType.PLANT, customUserDetails.getUserId());
        return ResponseEntity.ok("Plant reported successfully");
    }

    @Operation(summary = "다이어리 신고", description = "특정 다이어리를 신고합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "신고 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "404", description = "다이어리를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/diary/{diaryId}")
    public ResponseEntity<?> reportDiary(
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @Parameter(description = "다이어리 ID") @PathVariable Long diaryId) {
        reportService.reportEntity(diaryId, ReportType.DIARY, customUserDetails.getUserId());
        return ResponseEntity.ok("Diary reported successfully");
    }

    @Operation(summary = "신고 목록 조회", description = "신고된 식물 또는 다이어리 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ReportDTO.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping
    public ResponseEntity<?> reportedDiary(
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @Parameter(description = "신고 유형 (plant/diary)") @RequestParam String type) {
        if (type.equals("plant")) {
            List<ReportDTO> allPlantReports = reportService.getAllPlantReports();
            return ResponseEntity.ok(Map.of("reportedPlants", allPlantReports));
        } else {
            List<ReportDTO> allDiaryReports = reportService.getAllDiaryReports();
            return ResponseEntity.ok(Map.of("reportedDiaries", allDiaryReports));
        }
    }
}

