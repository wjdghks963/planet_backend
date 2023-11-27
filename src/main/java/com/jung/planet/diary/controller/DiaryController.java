package com.jung.planet.diary.controller;

import com.jung.planet.diary.dto.DiaryDetailDTO;
import com.jung.planet.diary.dto.request.DiaryFormDTO;
import com.jung.planet.diary.entity.Diary;
import com.jung.planet.diary.service.DiaryService;
import com.jung.planet.plant.controller.PlantController;
import com.jung.planet.security.UserDetail.CustomUserDetails;
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

    private static final Logger logger = LoggerFactory.getLogger(DiaryController.class);

    private final DiaryService diaryService;

    @GetMapping("/{id}")
    public ResponseEntity<DiaryDetailDTO> findDiary(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable("id") Long diaryId) {
        DiaryDetailDTO diary = diaryService.findDiary(diaryId, customUserDetails.getUserId());

        return ResponseEntity.ok(diary);
    }


    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addDiary(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody DiaryFormDTO diaryFormDTO) {
        logger.info("Request to add diary: {}", diaryFormDTO.toString());
        Diary diary = diaryService.addDiary(customUserDetails.getUsername(), diaryFormDTO);
        logger.info("Diary added: {}", diary);

        return ResponseEntity.ok(Map.of("ok", true));
    }


    @PostMapping("/edit/{id}")
    public ResponseEntity<?> editDiary(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable("id") Long diaryId, @RequestBody DiaryFormDTO diaryEditDTO) {
        diaryService.editDiary(customUserDetails, diaryId, diaryEditDTO);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<?> removeDiary(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable("id") Long diaryId) {
        diaryService.deleteDiary(diaryId, customUserDetails);
        return ResponseEntity.ok(Map.of("ok", true));
    }


}
