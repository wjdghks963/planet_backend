package com.jung.planet.diary.controller;

import com.jung.planet.diary.dto.DiaryDTO;
import com.jung.planet.diary.dto.DiaryDetailDTO;
import com.jung.planet.diary.dto.request.DiaryEditDTO;
import com.jung.planet.diary.entity.Diary;
import com.jung.planet.diary.service.DiaryService;
import com.jung.planet.plant.controller.PlantController;
import com.jung.planet.security.UserDetail.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diary")
public class DiaryController {

    private static final Logger logger = LoggerFactory.getLogger(PlantController.class);

    private final DiaryService diaryService;

    @GetMapping("/{id}")
    public ResponseEntity<DiaryDetailDTO> findDiary(@PathVariable("id") Long diaryId) {
        DiaryDetailDTO diary = diaryService.findDiary(diaryId);
        logger.info("Diary: {}", diary);

        return ResponseEntity.ok(diary);
    }


    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addDiary(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody DiaryDTO diaryDTO) {
        logger.info("Request to add diary: {}", diaryDTO);
        Diary diary = diaryService.addDiary(diaryDTO);
        logger.info("Diary added: {}", diary);

        return ResponseEntity.ok(Map.of("ok", true));
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<?> removeDiary(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable("id") Long diaryId) {
            diaryService.deleteDiary(diaryId, customUserDetails.getUserId());
            return ResponseEntity.ok(Map.of("ok", true));
    }


    @PostMapping("/edit/{id}")
    public ResponseEntity<?> editDiary(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable("id") Long diaryId, @RequestBody DiaryEditDTO diaryEditDTO) {
        diaryService.editDiary(diaryId, diaryEditDTO,customUserDetails.getUserId());
        return ResponseEntity.ok(Map.of("ok", true));
    }



}
