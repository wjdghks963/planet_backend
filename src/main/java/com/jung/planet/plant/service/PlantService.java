package com.jung.planet.plant.service;

import com.jung.planet.diary.dto.DiaryDetailDTO;
import com.jung.planet.diary.entity.Diary;
import com.jung.planet.exception.PermissionDeniedException;
import com.jung.planet.exception.ResourceNotFoundException;
import com.jung.planet.exception.ExternalServiceException;
import com.jung.planet.plant.dto.PlantDetailDTO;
import com.jung.planet.plant.dto.PlantSummaryDTO;
import com.jung.planet.plant.dto.request.PlantFormDTO;
import com.jung.planet.plant.entity.Plant;
import com.jung.planet.plant.entity.UserPlantHeart;
import com.jung.planet.plant.repository.PlantRepository;
import com.jung.planet.plant.repository.UserPlantHeartRepository;
import com.jung.planet.r2.CloudflareR2Uploader;
import com.jung.planet.user.entity.User;
import com.jung.planet.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlantService {
    private final PlantRepository plantRepository;
    private final UserRepository userRepository;
    private final UserPlantHeartRepository userPlantHeartRepository;
    private final CloudflareR2Uploader cloudflareR2Uploader;

    @Transactional
    public Plant addPlant(PlantFormDTO plantFormDTO) {
        User user = userRepository.findById(plantFormDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", plantFormDTO.getUserId()));

        Plant plant = plantFormDTO.toEntity(user, "encodedImg");
        plantRepository.save(plant);

        try {
            ByteBuffer imageBuffer = ByteBuffer.wrap(Base64.getDecoder().decode(plantFormDTO.getImgData()));
            cloudflareR2Uploader.uploadPlantImage(user, plant, imageBuffer);
        } catch (Exception e) {
            throw new ExternalServiceException("이미지 업로드 서비스", "식물 이미지 업로드", e);
        }

        return plantRepository.save(plant);
    }

    @Transactional
    public Plant editPlant(PlantFormDTO plantFormDTO, Long plantId) {
        User user = userRepository.findById(plantFormDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", plantFormDTO.getUserId()));
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new ResourceNotFoundException("Plant", "id", plantId));

        // 권한 체크
        if (!plant.getUser().getId().equals(user.getId())) {
            throw new PermissionDeniedException("Plant", plantId);
        }

        try {
            ByteBuffer imageBuffer = ByteBuffer.wrap(Base64.getDecoder().decode(plantFormDTO.getImgData()));
            cloudflareR2Uploader.editPlantImage(user, plant, imageBuffer);
        } catch (Exception e) {
            throw new ExternalServiceException("이미지 업로드 서비스", "식물 이미지 수정", e);
        }

        plant.setNickName(plantFormDTO.getNickName());
        plant.setScientificName(plantFormDTO.getScientificName());

        return plantRepository.save(plant);
    }

    @Transactional
    public void removePlant(Long plantId, String userEmail) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new ResourceNotFoundException("Plant", "id", plantId));
        
        // 권한 체크 로직 추가 필요 (userEmail로 확인하는 로직)

        userPlantHeartRepository.deleteByPlantId(plantId);
        
        try {
            cloudflareR2Uploader.deletePlant(plantId, userEmail);
        } catch (Exception e) {
            throw new ExternalServiceException("이미지 삭제 서비스", "식물 이미지 삭제", e);
        }

        plantRepository.delete(plant);
    }

    @Transactional(readOnly = true)
    public List<PlantSummaryDTO> getPlantsByUserId(Long userId) {
        // 사용자 존재 여부 체크
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        
        List<Plant> plants = plantRepository.findByUserId(userId);

        return plants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PlantSummaryDTO> getPlantsByRecent(int page) {
        Pageable pageable = PageRequest.of(page, 4, Sort.by("createdAt").descending());
        Page<Plant> plantPage = plantRepository.findAllByOrderByCreatedAtDesc(pageable);
        if (plantPage.isEmpty()) {
            return Collections.emptyList();
        }
        return plantPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PlantSummaryDTO> getPlantsByPopularity(int page) {
        Pageable pageable = PageRequest.of(page, 4, Sort.by("heartCount").descending());
        Page<Plant> plantPage = plantRepository.findAll(pageable);

        if (plantPage.isEmpty()) {
            return Collections.emptyList();
        }

        return plantPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PlantSummaryDTO> getHeartedPlantsByUser(Long userId, int page) {
        Pageable pageable = PageRequest.of(page, 4);
        Page<UserPlantHeart> hearts = userPlantHeartRepository.findByUserId(userId, pageable);

        return hearts.stream()
                .map(heart -> convertToDto(heart.getPlant()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PlantSummaryDTO> getRandomPlants() {
        List<Plant> randomPlants = plantRepository.findRandomPlants();

        if (randomPlants == null || randomPlants.isEmpty()) {
            return Collections.emptyList();
        }

        return randomPlants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlantDetailDTO getPlantDetailsByPlantId(Long userId, Long plantId) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new ResourceNotFoundException("Plant", "id", plantId));
                
        // 사용자 존재 여부 체크
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        
        boolean isHearted = userPlantHeartRepository.findByUserIdAndPlantId(userId, plantId).isPresent();

        return convertToDetailDto(userId, plant, isHearted);
    }

    public int getTotalHearts(Long userId) {
        Integer totalHearts = plantRepository.sumHeartsByUserId(userId);
        return totalHearts != null ? totalHearts : 0;
    }

    private PlantSummaryDTO convertToDto(Plant plant) {
        PlantSummaryDTO dto = new PlantSummaryDTO();
        dto.setId(plant.getId());
        dto.setNickName(plant.getNickName());
        dto.setHeartCount(plant.getHeartCount());
        dto.setImgUrl(plant.getImgUrl());
        dto.setPeriod(calculatePeriod(plant.getCreatedAt()));
        return dto;
    }

    private PlantDetailDTO convertToDetailDto(Long userId, Plant plant, boolean isHearted) {
        boolean isMine = Objects.equals(plant.getUser().getId(), userId);

        PlantDetailDTO dto = new PlantDetailDTO();
        dto.setPlantId(plant.getId());
        dto.setNickName(plant.getNickName());
        dto.setScientificName(plant.getScientificName());
        dto.setImgUrl(plant.getImgUrl());
        dto.setHeartCount(plant.getHeartCount());
        dto.setPeriod(calculatePeriod(plant.getCreatedAt()));
        dto.setMine(isMine);
        dto.setHearted(isHearted);
        dto.setCreatedAt(plant.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        List<DiaryDetailDTO> diaryDTOs = plant.getDiaries().stream()
                .map(diary -> convertToDiaryDto(diary, userId))
                .collect(Collectors.toList());
        dto.setDiaries(diaryDTOs);

        return dto;
    }

    private DiaryDetailDTO convertToDiaryDto(Diary diary, Long userId) {
        DiaryDetailDTO diaryDTO = new DiaryDetailDTO();
        diaryDTO.setId(diary.getId());
        diaryDTO.setContent(diary.getContent());
        diaryDTO.setPublic(diary.getIsPublic());
        diaryDTO.setImgUrl(diary.getImgUrl());
        diaryDTO.setCreatedAt(diary.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        boolean isMine = diary.getPlant().getUser().getId().equals(userId);
        diaryDTO.setMine(isMine);

        return diaryDTO;
    }

    private int calculatePeriod(LocalDateTime createdAt) {
        return (int) ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
    }

    public boolean isOwnerOfPlant(Long userId, Long plantId) {
        // Plant 존재 여부 확인
        if (!plantRepository.existsById(plantId)) {
            throw new ResourceNotFoundException("Plant", "id", plantId);
        }
        return plantRepository.existsByIdAndUserId(plantId, userId);
    }
}
