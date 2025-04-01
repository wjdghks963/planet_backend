package com.jung.planet.diary.controller;

import com.jung.planet.diary.dto.DiaryDetailDTO;
import com.jung.planet.diary.dto.request.DiaryFormDTO;
import com.jung.planet.diary.entity.Diary;
import com.jung.planet.diary.service.DiaryService;
import com.jung.planet.plant.controller.PlantController;
import com.jung.planet.security.UserDetail.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Diary", description = "다이어리 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/diary")
public class DiaryController {

    private static final Logger logger = LoggerFactory.getLogger(DiaryController.class);

    private final DiaryService diaryService;

    @Operation(summary = "다이어리 상세 조회", description = "ID로 특정 다이어리의 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = DiaryDetailDTO.class))),
        @ApiResponse(responseCode = "404", description = "다이어리를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DiaryDetailDTO> findDiary(
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @Parameter(description = "다이어리 ID") @PathVariable("id") Long diaryId) {
        DiaryDetailDTO diary = diaryService.findDiary(diaryId, customUserDetails.getUserId());
        return ResponseEntity.ok(diary);
    }

    @Operation(summary = "다이어리 작성", description = "새로운 다이어리를 작성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "작성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addDiary(
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @RequestBody DiaryFormDTO diaryFormDTO) {
        logger.info("Request to add diary: {}", diaryFormDTO.toString());
        Diary diary = diaryService.addDiary(customUserDetails.getUsername(), diaryFormDTO);
        logger.info("Diary added: {}", diary);

        return ResponseEntity.ok(Map.of("ok", true));
    }

    @Operation(summary = "다이어리 수정", description = "기존 다이어리를 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/edit/{id}")
    public ResponseEntity<?> editDiary(
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @Parameter(description = "다이어리 ID") @PathVariable("id") Long diaryId,
        @RequestBody DiaryFormDTO diaryEditDTO) {
        diaryService.editDiary(customUserDetails, diaryId, diaryEditDTO);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @Operation(summary = "다이어리 삭제", description = "기존 다이어리를 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "다이어리를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @DeleteMapping("/remove/{id}")
    public ResponseEntity<?> removeDiary(
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @Parameter(description = "다이어리 ID") @PathVariable("id") Long diaryId) {
        diaryService.deleteDiary(diaryId, customUserDetails);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
