package com.jung.planet.diary.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jung.planet.diary.dto.DiaryDTO;
import com.jung.planet.diary.entity.Diary;
import com.jung.planet.diary.repository.DiaryRepository;
import com.jung.planet.plant.entity.Plant;
import com.jung.planet.plant.repository.PlantRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class DiaryServiceTest {

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private PlantRepository plantRepository;

    @InjectMocks
    private DiaryService diaryService;

    private DiaryDTO diaryDTO;
    private Plant plant;
    private Diary diary;


    @BeforeEach
    void setUp() {
        // 가정된 Plant 객체를 생성합니다.
        plant = new Plant(); // 실제로는 빌더 패턴이나 목 객체를 사용하여 생성합니다.

        // DiaryDTO 객체를 생성합니다.
        diaryDTO = new DiaryDTO();
        diaryDTO.setPlantId(1L); // 예시 ID
        diaryDTO.setTitle("My Green Buddy Diary");
        diaryDTO.setContent("Today, my plant looks more lively than ever!");
        diaryDTO.setImgUrl("plant.jpg");
        diaryDTO.setIsPublic(true);

        // Diary 객체를 빌더 패턴으로 생성합니다.
        diary = Diary.builder()
                .plant(plant)
                .title(diaryDTO.getTitle())
                .content(diaryDTO.getContent())
                .imgUrl(diaryDTO.getImgUrl())
                .isPublic(diaryDTO.getIsPublic())
                .build();
    }


    // ADD
    @Test
    void whenAddDiary_withValidPlantId_thenDiaryShouldBeSaved() {
        // PlantRepository가 특정 ID로 Plant를 찾을 때, 가정된 Plant 객체를 반환하도록 설정합니다.
        when(plantRepository.findById(diaryDTO.getPlantId())).thenReturn(Optional.of(plant));

        // DiaryRepository의 save 메소드가 호출될 때, 가정된 Diary 객체를 반환하도록 설정합니다.
        when(diaryRepository.save(any(Diary.class))).thenReturn(diary);

        // 실제 서비스 메소드를 호출합니다.
        Diary savedDiary = diaryService.addDiary(diaryDTO);

        // 검증: 생성된 Diary 객체가 null이 아닌지 확인합니다.
        assertNotNull(savedDiary);

        // 검증: 반환된 Diary 객체가 예상된 Diary 객체와 동일한지 확인합니다.
        assertEquals(diary.getTitle(), savedDiary.getTitle());
        assertEquals(diary.getContent(), savedDiary.getContent());
        assertEquals(diary.getImgUrl(), savedDiary.getImgUrl());
        assertEquals(diary.getIsPublic(), savedDiary.getIsPublic());

        // 검증: DiaryRepository의 save 메소드가 실제로 호출되었는지 확인합니다.
        verify(diaryRepository).save(any(Diary.class));
    }

    @Test
    void whenAddDiary_withInvalidPlantId_thenThrowException() {
        // PlantRepository가 특정 ID로 Plant를 찾을 때, 빈 Optional을 반환하도록 설정합니다.
        when(plantRepository.findById(diaryDTO.getPlantId())).thenReturn(Optional.empty());

        // 예외가 발생하는지 확인합니다.
        assertThrows(RuntimeException.class, () -> diaryService.addDiary(diaryDTO));
    }


    // FIND
    @Test
    void whenFindDiaryById_thenDiaryShouldBeFound() {
        // DiaryRepository가 특정 ID로 Diary를 찾을 때, 미리 정의된 Diary 객체를 반환하도록 설정
        when(diaryRepository.findById(diary.getId())).thenReturn(Optional.of(diary));

        // 실제 서비스 메소드를 호출
        Diary foundDiary = diaryService.findDiary(diary.getId());

        // 검증: 반환된 Diary 객체가 null이 아닌지 확인
        assertNotNull(foundDiary);

        // 검증: 반환된 Diary 객체가 예상된 Diary 객체와 동일한지 확인
        assertEquals(diary, foundDiary);
    }


    // DELETE
    @Test
    void deleteDiary_whenDiaryExists_shouldDeleteDiary() {
        // Arrange
        Long diaryId = 1L;
        when(diaryRepository.existsById(diaryId)).thenReturn(true);

        // Act
        diaryService.deleteDiary(diaryId);

        // Assert
        verify(diaryRepository).deleteById(diaryId);
    }


    @Test
    void deleteDiary_whenDiaryDoesNotExist_shouldThrowException() {
        // Arrange
        Long diaryId = 99L;
        when(diaryRepository.existsById(diaryId)).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> diaryService.deleteDiary(diaryId));
    }

}