package com.jung.planet.diary.controller;

import com.jung.planet.diary.dto.DiaryDTO;
import com.jung.planet.diary.entity.Diary;
import com.jung.planet.diary.service.DiaryService;
import com.jung.planet.plant.controller.PlantController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diary")
public class DiaryController {

    private static final Logger logger = LoggerFactory.getLogger(PlantController.class);

    private final DiaryService diaryService;

    @GetMapping("/{id}")
    public ResponseEntity<Diary> findDiary(@PathVariable("id") Long diaryId) {
        Diary diary = diaryService.findDiary(diaryId);
        logger.info("Diary: {}", diary);

        return ResponseEntity.ok(diary);
    }


    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addDiary(@Valid @RequestBody DiaryDTO diaryDTO) {
        logger.info("Request to add diary: {}", diaryDTO);
        Diary diary = diaryService.addDiary(diaryDTO);
        logger.info("Diary added: {}", diary);

        return ResponseEntity.ok(Map.of("ok", true));
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<Map<String, Object>> removeDiary(@PathVariable("id") Long diaryId) {
        diaryService.deleteDiary(diaryId);
        return ResponseEntity.ok(Map.of("ok", true));
    }


}
