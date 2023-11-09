package com.jung.planet.diary.service;

import com.jung.planet.diary.dto.DiaryDTO;
import com.jung.planet.diary.entity.Diary;
import com.jung.planet.diary.repository.DiaryRepository;
import com.jung.planet.plant.entity.Plant;
import com.jung.planet.plant.repository.PlantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final PlantRepository plantRepository;


    @Transactional(readOnly = true)
    public Diary findDiary(Long diaryId) {
        return diaryRepository.findById(diaryId).orElseThrow(() -> new EntityNotFoundException("Diary with ID " + diaryId + " not found"));
    }


    @Transactional
    public Diary addDiary(DiaryDTO diaryDTO) {
        Plant plant = plantRepository.findById(diaryDTO.getPlantId()).orElseThrow(() -> new EntityNotFoundException("Plant Not Found"));
        Diary diary = diaryDTO.toEntity(plant);
        return diaryRepository.save(diary);
    }

    @Transactional
    public void deleteDiary(Long diaryId) {
        if (diaryRepository.existsById(diaryId)) {
            diaryRepository.deleteById(diaryId);
        } else {
            throw new EntityNotFoundException("Diary with ID " + diaryId + " not found");
        }
    }



}
