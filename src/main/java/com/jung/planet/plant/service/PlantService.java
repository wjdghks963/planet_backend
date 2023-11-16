package com.jung.planet.plant.service;

import com.jung.planet.diary.dto.DiaryDetailDTO;
import com.jung.planet.diary.entity.Diary;
import com.jung.planet.plant.dto.PlantDetailDTO;
import com.jung.planet.plant.dto.request.PlantFormDTO;
import com.jung.planet.plant.dto.PlantSummaryDTO;
import com.jung.planet.plant.entity.Plant;
import com.jung.planet.plant.repository.PlantRepository;
import com.jung.planet.user.entity.User;
import com.jung.planet.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlantService {
    private final PlantRepository plantRepository;
    private final UserRepository userRepository;


    // TODO:: R2로 이미지 전송하는 로직 추가
    @Transactional
    public Plant addPlant(PlantFormDTO plantFormDTO) {
        User user = userRepository.findById(plantFormDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String encodedImg = plantFormDTO.getImgData().substring(0, 20);

        //byte[] imageBytes = Base64.getDecoder().decode(encodedImg);

        Plant plant = plantFormDTO.toEntity(user, encodedImg);

        return plantRepository.save(plant);
    }

    @Transactional
    public Plant editPlant(PlantFormDTO plantFormDTO, Long plantId) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new RuntimeException("Plant not found"));

        String encodedImg = plantFormDTO.getImgData().substring(0, 20);

        //byte[] imageBytes = Base64.getDecoder().decode(encodedImg);
        plant.setNickName(plantFormDTO.getNickName());
        plant.setScientificName(plantFormDTO.getScientificName());
        plant.setImgUrl(encodedImg);

        return plantRepository.save(plant);
    }

    @Transactional(readOnly = true)
    public List<PlantSummaryDTO> getPlantsByUserId(Long userId) {
        List<Plant> plants = plantRepository.findByUserId(userId);
        return plants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<PlantSummaryDTO> getPlantsByRecent(int page) {
        Pageable pageable = PageRequest.of(page, 4, Sort.by("createdAt").descending());
        Page<Plant> plantPage = plantRepository.findAllByOrderByCreatedAtDesc(pageable);

        return plantPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PlantSummaryDTO> getRandomPlants() {
        List<Plant> randomPlants = plantRepository.findRandomPlants();
        return randomPlants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public PlantDetailDTO getPlantDetailsByPlantId(Long userId, Long plantId) {
        Plant plant = plantRepository.findById(plantId).orElseThrow(/* 예외 처리 */);
        return convertToDetailDto(userId, plant);
    }


    @Transactional
    public void removePlant(Long plantId) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new EntityNotFoundException("Plant not found with id: " + plantId));
        plantRepository.delete(plant);
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

    private PlantDetailDTO convertToDetailDto(Long userId, Plant plant) {
        boolean isMine = Objects.equals(plant.getUser().getId(), userId);

        PlantDetailDTO dto = new PlantDetailDTO();

        dto.setPlantId(plant.getId());
        dto.setNickName(plant.getNickName());
        dto.setScientificName(plant.getScientificName());
        dto.setImgUrl(plant.getImgUrl());
        dto.setHeartCount(plant.getHeartCount());
        dto.setPeriod(calculatePeriod(plant.getCreatedAt()));
        dto.setMine(isMine);

        List<DiaryDetailDTO> diaryDTOs = plant.getDiaries().stream()
                .map(this::convertToDiaryDto)
                .collect(Collectors.toList());
        dto.setDiaries(diaryDTOs);
        return dto;
    }

    private DiaryDetailDTO convertToDiaryDto(Diary diary) {
        DiaryDetailDTO diaryDTO = new DiaryDetailDTO();
        diaryDTO.setId(diary.getId());
        diaryDTO.setContent(diary.getContent());
        diaryDTO.setPublic(diary.getIsPublic());
        diaryDTO.setImgUrl(diary.getImgUrl());
        diaryDTO.setCreatedAt(diary.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        return diaryDTO;
    }

    private int calculatePeriod(LocalDateTime createdAt) {
        return (int) ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
    }


    public boolean isOwnerOfPlant(Long userId, Long plantId) {
        return plantRepository.existsByIdAndUserId(plantId, userId);
    }

}
