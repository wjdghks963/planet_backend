package com.jung.planet.diary.service;

import com.jung.planet.diary.dto.DiaryDTO;
import com.jung.planet.diary.dto.DiaryDetailDTO;
import com.jung.planet.diary.dto.request.DiaryFormDTO;
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


    // TODO:: R2
    @Transactional
    public Diary addDiary(DiaryFormDTO diaryFormDTO) {
        Plant plant = plantRepository.findById(diaryFormDTO.getPlantId()).orElseThrow(() -> new EntityNotFoundException("Plant Not Found"));

        String encodedImg = diaryFormDTO.getImgData().substring(0, 20);

        //byte[] imageBytes = Base64.getDecoder().decode(encodedImg);


        Diary diary = diaryFormDTO.toEntity(plant, encodedImg);
        return diaryRepository.save(diary);
    }



    // TODO:: R2
    @Transactional
    public void editDiary(Long diaryId, DiaryFormDTO diaryFormDTO, Long userId) {
        if (diaryRepository.existsById(diaryId)) {
            Optional<Diary> diary = diaryRepository.findByIdAndUserId(diaryId, userId);

            if (diary.isPresent()) {
                String encodedImg = diaryFormDTO.getImgData().substring(0, 20);


                diary.get().setContent(diaryFormDTO.getContent());
                diary.get().setImgUrl(encodedImg);
                diary.get().setPublic(diaryFormDTO.getIsPublic());

                diaryRepository.save(diary.get());

            } else {
                // 소유자가 아닌 경우 예외 발생
                throw new AccessDeniedException("No access rights to delete diary with ID " + diaryId);
            }
        } else {
            throw new EntityNotFoundException("Diary with ID " + diaryId + " not found");
        }
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


    private DiaryDetailDTO convertToDiaryDetailDTO(Diary diary) {
        DiaryDetailDTO diaryDetailDTO = new DiaryDetailDTO();
        diaryDetailDTO.setId(diary.getId());
        diaryDetailDTO.setContent(diary.getContent());
        diaryDetailDTO.setImgUrl(diary.getImgUrl());
        diaryDetailDTO.setPublic(diary.getIsPublic());
        diaryDetailDTO.setCreatedAt(diary.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        return diaryDetailDTO;
    }


}
