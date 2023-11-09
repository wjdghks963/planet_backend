package com.jung.planet.diary.service;

import com.jung.planet.diary.dto.DiaryDTO;
import com.jung.planet.diary.entity.Diary;
import com.jung.planet.diary.repository.DiaryRepository;
import com.jung.planet.plant.entity.Plant;
import com.jung.planet.plant.repository.PlantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final PlantRepository plantRepository;


    @Transactional
    public Diary addDiary(DiaryDTO diaryDTO) {
        Plant plant = plantRepository.findById(diaryDTO.getPlantId()).orElseThrow(() -> new RuntimeException("Plant Not Found"));
        Diary diary = diaryDTO.toEntity(plant);
        return diaryRepository.save(diary);
    }


}
