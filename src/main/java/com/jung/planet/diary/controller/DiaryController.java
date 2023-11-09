package com.jung.planet.diary.controller;

import com.jung.planet.diary.dto.DiaryDTO;
import com.jung.planet.diary.entity.Diary;
import com.jung.planet.diary.service.DiaryService;
import com.jung.planet.plant.controller.PlantController;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diary")
public class DiaryController {

    private static final Logger logger = LoggerFactory.getLogger(PlantController.class);

    private final DiaryService diaryService;

    @PostMapping("/add")
    public ResponseEntity<Diary> addDiary(@RequestBody DiaryDTO diaryDTO) {
        logger.info("Request to add diary: {}", diaryDTO);
        Diary diary = diaryService.addDiary(diaryDTO);
        logger.info("Diary added: {}", diary);

        return ResponseEntity.ok(diary);
    }

}
