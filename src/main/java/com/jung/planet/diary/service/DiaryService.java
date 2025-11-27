package com.jung.planet.diary.service;

import com.jung.planet.diary.dto.DiaryDetailDTO;
import com.jung.planet.diary.dto.request.DiaryFormDTO;
import com.jung.planet.diary.entity.Diary;
import com.jung.planet.diary.repository.DiaryRepository;
import com.jung.planet.plant.entity.Plant;
import com.jung.planet.plant.repository.PlantRepository;
import com.jung.planet.r2.CloudflareR2Uploader;
import com.jung.planet.security.UserDetail.CustomUserDetails;
import com.jung.planet.user.entity.UserRole;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.ByteBuffer;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final PlantRepository plantRepository;
    private final CloudflareR2Uploader cloudflareR2Uploader;


    @Transactional(readOnly = true)
    public DiaryDetailDTO findDiary(Long diaryId, Long userId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new EntityNotFoundException("다이어리 데이터를 찾을 수 없습니다."));

        boolean isOwner = diary.getPlant().getUser().getId().equals(userId);

        return convertToDiaryDetailDTO(diary, isOwner);
    }


    @Transactional
    public Diary addDiary(String userName, DiaryFormDTO diaryFormDTO) {
        Plant plant = plantRepository.findById(diaryFormDTO.getPlantId()).orElseThrow(() -> new EntityNotFoundException("관련된 식물을 찾을 수 없습니다."));
        Diary diary = diaryFormDTO.toEntity(plant, "encodedImg");

        diaryRepository.save(diary);

        ByteBuffer imageBuffer = ByteBuffer.wrap(Base64.getDecoder().decode(diaryFormDTO.getImgData()));

        cloudflareR2Uploader.uploadDiaryImage(userName, diary, imageBuffer);

        return diaryRepository.save(diary);
    }


    @Transactional
    public void editDiary(CustomUserDetails customUserDetails, Long diaryId, DiaryFormDTO diaryFormDTO) {
        Diary diary = diaryRepository.findByIdAndUserId(diaryId, customUserDetails.getUserId())
                .orElseThrow(() -> new AccessDeniedException("다이어리를 찾을 수 없거나 수정 권한이 없습니다. diaryId: " + diaryId));

        ByteBuffer imageBuffer = ByteBuffer.wrap(Base64.getDecoder().decode(diaryFormDTO.getImgData()));

        diary.setContent(diaryFormDTO.getContent());
        diary.setPublic(diaryFormDTO.getIsPublic());

        cloudflareR2Uploader.editDiaryImage(customUserDetails.getUsername(), diary, imageBuffer);

        diaryRepository.save(diary);
    }

    @Transactional
    public void deleteDiary(Long diaryId, CustomUserDetails customUserDetails) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new EntityNotFoundException("다이어리 데이터를 찾을 수 없습니다."));

        // 어드민이거나 일기의 소유자일 경우 삭제 허용
        boolean isAdmin = customUserDetails.getUserRole().equals(UserRole.ADMIN);
        boolean isOwner = diary.getPlant().getUser().getId().equals(customUserDetails.getUserId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("다이어리 삭제 권한이 없습니다. diaryId: " + diaryId);
        }

        diaryRepository.deleteById(diaryId);
        cloudflareR2Uploader.deleteDiary(diary, customUserDetails.getUsername());
    }


    private DiaryDetailDTO convertToDiaryDetailDTO(Diary diary, boolean isOwner) {
        DiaryDetailDTO diaryDetailDTO = new DiaryDetailDTO();
        diaryDetailDTO.setId(diary.getId());
        diaryDetailDTO.setContent(diary.getContent());
        diaryDetailDTO.setImgUrl(diary.getImgUrl());
        diaryDetailDTO.setPublic(diary.getIsPublic());
        diaryDetailDTO.setCreatedAt(diary.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        diaryDetailDTO.setMine(isOwner);

        return diaryDetailDTO;
    }


}
