package com.jung.planet.plant.mapper;

import com.jung.planet.diary.entity.Diary;
import com.jung.planet.plant.dto.DiaryDetailDTO;
import com.jung.planet.plant.dto.PlantDetailDTO;
import com.jung.planet.plant.dto.PlantSummaryDTO;
import com.jung.planet.plant.entity.Plant;
import com.jung.planet.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class PlantMapperTest {

    private PlantMapper plantMapper;
    private User user;
    private Plant plant;
    private Diary diary1;
    private Diary diary2;

    @BeforeEach
    void setUp() {
        plantMapper = new PlantMapper();

        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .build();

        plant = Plant.builder()
                .id(1L)
                .nickName("Test Plant")
                .scientificName("Plantae Testus")
                .imgUrl("http://example.com/plant.jpg")
                .heartCount(10)
                .createdAt(LocalDateTime.now().minusDays(5))
                .user(user)
                .build();

        diary1 = Diary.builder()
                .id(1L)
                .content("First diary entry")
                .imgUrl("http://example.com/diary1.jpg")
                .isPublic(true)
                .createdAt(LocalDateTime.now().minusDays(2))
                .plant(plant)
                .build();

        diary2 = Diary.builder()
                .id(2L)
                .content("Second diary entry")
                .imgUrl("http://example.com/diary2.jpg")
                .isPublic(false)
                .createdAt(LocalDateTime.now().minusDays(1))
                .plant(plant)
                .build();
    }

    @Test
    @DisplayName("Plant를 PlantSummaryDTO로 변환 - 성공")
    void testToSummaryDto_Success() {
        // When
        PlantSummaryDTO result = plantMapper.toSummaryDto(plant);

        // Then
        assertNotNull(result);
        assertEquals(plant.getId(), result.getId());
        assertEquals(plant.getNickName(), result.getNickName());
        assertEquals(plant.getHeartCount(), result.getHeartCount());
        assertEquals(plant.getImgUrl(), result.getImgUrl());
        assertEquals(5, result.getPeriod()); // 5일 전 생성
    }

    @Test
    @DisplayName("Plant를 PlantDetailDTO로 변환 - 좋아요 누르지 않음")
    void testToDetailDto_NotHearted() {
        // Given
        Long userId = 2L; // 다른 사용자
        boolean isHearted = false;

        // When
        PlantDetailDTO result = plantMapper.toDetailDto(userId, plant, isHearted);

        // Then
        assertNotNull(result);
        assertEquals(plant.getId(), result.getId());
        assertEquals(plant.getNickName(), result.getNickName());
        assertEquals(plant.getScientificName(), result.getScientificName());
        assertEquals(plant.getImgUrl(), result.getImgUrl());
        assertEquals(plant.getHeartCount(), result.getHeartCount());
        assertEquals(5, result.getPeriod());
        assertFalse(result.isHearted());
        assertFalse(result.isMine()); // userId가 다르므로
    }

    @Test
    @DisplayName("Plant를 PlantDetailDTO로 변환 - 좋아요 누름")
    void testToDetailDto_Hearted() {
        // Given
        Long userId = 2L;
        boolean isHearted = true;

        // When
        PlantDetailDTO result = plantMapper.toDetailDto(userId, plant, isHearted);

        // Then
        assertNotNull(result);
        assertTrue(result.isHearted());
        assertFalse(result.isMine());
    }

    @Test
    @DisplayName("Plant를 PlantDetailDTO로 변환 - 자신의 식물")
    void testToDetailDto_OwnPlant() {
        // Given
        Long userId = 1L; // plant owner와 동일
        boolean isHearted = false;

        // When
        PlantDetailDTO result = plantMapper.toDetailDto(userId, plant, isHearted);

        // Then
        assertNotNull(result);
        assertTrue(result.isMine()); // userId가 owner와 동일
    }

    @Test
    @DisplayName("Diary를 DiaryDetailDTO로 변환 - 성공")
    void testToDiaryDetailDto_Success() {
        // Given
        Long userId = 1L;

        // When
        DiaryDetailDTO result = plantMapper.toDiaryDetailDto(diary1, userId);

        // Then
        assertNotNull(result);
        assertEquals(diary1.getId(), result.getId());
        assertEquals(diary1.getContent(), result.getContent());
        assertEquals(diary1.getImgUrl(), result.getImgUrl());
        assertTrue(result.isPublic());
        assertTrue(result.isMine()); // userId가 plant owner와 동일
    }

    @Test
    @DisplayName("Diary를 DiaryDetailDTO로 변환 - 다른 사용자의 다이어리")
    void testToDiaryDetailDto_OtherUserDiary() {
        // Given
        Long userId = 2L; // 다른 사용자

        // When
        DiaryDetailDTO result = plantMapper.toDiaryDetailDto(diary1, userId);

        // Then
        assertNotNull(result);
        assertEquals(diary1.getId(), result.getId());
        assertFalse(result.isMine()); // userId가 plant owner와 다름
    }

    @Test
    @DisplayName("여러 Plant를 PlantSummaryDTO로 변환")
    void testToSummaryDto_MultiplePlants() {
        // Given
        Plant plant1 = Plant.builder()
                .id(1L)
                .nickName("Plant 1")
                .heartCount(5)
                .imgUrl("http://example.com/plant1.jpg")
                .createdAt(LocalDateTime.now().minusDays(10))
                .user(user)
                .build();

        Plant plant2 = Plant.builder()
                .id(2L)
                .nickName("Plant 2")
                .heartCount(3)
                .imgUrl("http://example.com/plant2.jpg")
                .createdAt(LocalDateTime.now().minusDays(20))
                .user(user)
                .build();

        // When
        PlantSummaryDTO result1 = plantMapper.toSummaryDto(plant1);
        PlantSummaryDTO result2 = plantMapper.toSummaryDto(plant2);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);

        assertEquals("Plant 1", result1.getNickName());
        assertEquals(5, result1.getHeartCount());
        assertEquals(10, result1.getPeriod());

        assertEquals("Plant 2", result2.getNickName());
        assertEquals(3, result2.getHeartCount());
        assertEquals(20, result2.getPeriod());

        // 각각 독립적인 DTO 객체인지 확인
        assertNotSame(result1, result2);
    }
}
