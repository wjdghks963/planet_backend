package com.jung.planet.diary.service;

import com.jung.planet.diary.dto.DiaryDTO;
import com.jung.planet.diary.dto.DiaryDetailDTO;
import com.jung.planet.diary.dto.request.DiaryEditDTO;
import com.jung.planet.diary.entity.Diary;
import com.jung.planet.diary.repository.DiaryRepository;
import com.jung.planet.plant.entity.Plant;
import com.jung.planet.plant.repository.PlantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final PlantRepository plantRepository;

    @Transactional(readOnly = true)
    public DiaryDetailDTO findDiary(Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId).orElseThrow(() -> new EntityNotFoundException("Diary with ID " + diaryId + " not found"));
        return convertToDiaryDetailDTO(diary);
    }


    @Transactional
    public Diary addDiary(DiaryDTO diaryDTO) {
        Plant plant = plantRepository.findById(diaryDTO.getPlantId()).orElseThrow(() -> new EntityNotFoundException("Plant Not Found"));
        Diary diary = diaryDTO.toEntity(plant);
        return diaryRepository.save(diary);
    }

    @Transactional
    public void deleteDiary(Long diaryId, Long userId) {
        if (diaryRepository.existsById(diaryId)) {
            Optional<Diary> diary = diaryRepository.findByIdAndUserId(diaryId, userId);

            if (diary.isPresent()) {
                diaryRepository.deleteById(diary.get().getId());
            } else {
                // 소유자가 아닌 경우 예외 발생
                throw new AccessDeniedException("No access rights to delete diary with ID " + diaryId);
            }
        } else {
            throw new EntityNotFoundException("Diary with ID " + diaryId + " not found");
        }
    }


    @Transactional
    public void editDiary(Long diaryId, DiaryEditDTO diaryDTO, Long userId) {
        if (diaryRepository.existsById(diaryId)) {
            Optional<Diary> diary = diaryRepository.findByIdAndUserId(diaryId, userId);

            if (diary.isPresent()) {
                diary.get().setTitle(diaryDTO.getTitle());
                diary.get().setContent(diaryDTO.getContent());
                diary.get().setImgUrl(diaryDTO.getImgUrl());
                diary.get().setPublic(diaryDTO.getIsPublic());

                diaryRepository.save(diary.get());

            } else {
                // 소유자가 아닌 경우 예외 발생
                throw new AccessDeniedException("No access rights to delete diary with ID " + diaryId);
            }
        } else {
            throw new EntityNotFoundException("Diary with ID " + diaryId + " not found");
        }
    }

    private DiaryDetailDTO convertToDiaryDetailDTO(Diary diary) {
        DiaryDetailDTO diaryDetailDTO = new DiaryDetailDTO();
        diaryDetailDTO.setId(diary.getId());
        diaryDetailDTO.setTitle(diary.getTitle());
        diaryDetailDTO.setContent(diary.getContent());
        diaryDetailDTO.setImgUrl(diary.getImgUrl());
        diaryDetailDTO.setPublic(diary.getIsPublic());
        diaryDetailDTO.setCreatedAt(diary.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        return diaryDetailDTO;
    }


}
