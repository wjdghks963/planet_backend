package com.jung.planet.plant.service;

import com.jung.planet.common.service.AuthorizationService;
import com.jung.planet.exception.UnauthorizedActionException;
import com.jung.planet.plant.entity.Plant;
import com.jung.planet.plant.entity.UserPlantHeart;
import com.jung.planet.plant.entity.UserPlantHeartId;
import com.jung.planet.plant.repository.PlantRepository;
import com.jung.planet.plant.repository.UserPlantHeartRepository;
import com.jung.planet.user.entity.User;
import com.jung.planet.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPlantHeartServiceTest {

    @Mock
    private UserPlantHeartRepository userPlantHeartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlantRepository plantRepository;

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private UserPlantHeartService userPlantHeartService;

    private User user;
    private User plantOwner;
    private Plant plant;
    private UserPlantHeartId userPlantHeartId;
    private UserPlantHeart userPlantHeart;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("user@example.com")
                .name("Test User")
                .build();

        plantOwner = User.builder()
                .id(2L)
                .email("owner@example.com")
                .name("Plant Owner")
                .build();

        plant = Plant.builder()
                .id(1L)
                .nickName("Test Plant")
                .heartCount(5)
                .user(plantOwner)
                .build();

        userPlantHeartId = new UserPlantHeartId(user.getId(), plant.getId());
        userPlantHeart = new UserPlantHeart(user, plant);
    }

    @Test
    @DisplayName("좋아요 토글 - 좋아요 추가 성공")
    void testTogglePlantHeart_AddHeart_Success() {
        // Given
        Long userId = 1L;
        Long plantId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(plantRepository.findById(plantId)).thenReturn(Optional.of(plant));
        when(authorizationService.isOwnerOfPlant(userId, plantId)).thenReturn(false);
        when(userPlantHeartRepository.findById(userPlantHeartId)).thenReturn(Optional.empty());
        when(userPlantHeartRepository.save(any(UserPlantHeart.class))).thenReturn(userPlantHeart);
        when(plantRepository.save(any(Plant.class))).thenReturn(plant);

        // When
        boolean result = userPlantHeartService.togglePlantHeart(userId, plantId);

        // Then
        assertTrue(result); // 좋아요가 추가되었으므로 true
        verify(authorizationService).isOwnerOfPlant(userId, plantId);
        verify(userPlantHeartRepository).save(any(UserPlantHeart.class));
        verify(plantRepository).save(plant);
    }

    @Test
    @DisplayName("좋아요 토글 - 좋아요 제거 성공")
    void testTogglePlantHeart_RemoveHeart_Success() {
        // Given
        Long userId = 1L;
        Long plantId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(plantRepository.findById(plantId)).thenReturn(Optional.of(plant));
        when(authorizationService.isOwnerOfPlant(userId, plantId)).thenReturn(false);
        when(userPlantHeartRepository.findById(userPlantHeartId)).thenReturn(Optional.of(userPlantHeart));
        doNothing().when(userPlantHeartRepository).delete(userPlantHeart);
        when(plantRepository.save(any(Plant.class))).thenReturn(plant);

        // When
        boolean result = userPlantHeartService.togglePlantHeart(userId, plantId);

        // Then
        assertFalse(result); // 좋아요가 제거되었으므로 false
        verify(authorizationService).isOwnerOfPlant(userId, plantId);
        verify(userPlantHeartRepository).delete(userPlantHeart);
        verify(plantRepository).save(plant);
    }

    @Test
    @DisplayName("좋아요 토글 - 자신의 식물에 좋아요 시도 (실패)")
    void testTogglePlantHeart_OwnPlant_ThrowsException() {
        // Given
        Long userId = 2L; // plantOwner의 ID
        Long plantId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(plantOwner));
        when(plantRepository.findById(plantId)).thenReturn(Optional.of(plant));
        when(authorizationService.isOwnerOfPlant(userId, plantId)).thenReturn(true);

        // When & Then
        assertThrows(UnauthorizedActionException.class,
            () -> userPlantHeartService.togglePlantHeart(userId, plantId));

        verify(authorizationService).isOwnerOfPlant(userId, plantId);
        verify(userPlantHeartRepository, never()).save(any(UserPlantHeart.class));
        verify(userPlantHeartRepository, never()).delete(any(UserPlantHeart.class));
        verify(plantRepository, never()).save(any(Plant.class));
    }

    @Test
    @DisplayName("좋아요 토글 - 사용자를 찾을 수 없음")
    void testTogglePlantHeart_UserNotFound() {
        // Given
        Long userId = 999L;
        Long plantId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class,
            () -> userPlantHeartService.togglePlantHeart(userId, plantId));

        verify(userRepository).findById(userId);
        verify(plantRepository, never()).findById(anyLong());
        verify(authorizationService, never()).isOwnerOfPlant(anyLong(), anyLong());
    }

    @Test
    @DisplayName("좋아요 토글 - 식물을 찾을 수 없음")
    void testTogglePlantHeart_PlantNotFound() {
        // Given
        Long userId = 1L;
        Long plantId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(plantRepository.findById(plantId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class,
            () -> userPlantHeartService.togglePlantHeart(userId, plantId));

        verify(userRepository).findById(userId);
        verify(plantRepository).findById(plantId);
        verify(authorizationService, never()).isOwnerOfPlant(anyLong(), anyLong());
    }

    @Test
    @DisplayName("좋아요 토글 - AuthorizationService 통합 확인")
    void testTogglePlantHeart_UsesAuthorizationService() {
        // Given
        Long userId = 1L;
        Long plantId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(plantRepository.findById(plantId)).thenReturn(Optional.of(plant));
        when(authorizationService.isOwnerOfPlant(userId, plantId)).thenReturn(false);
        when(userPlantHeartRepository.findById(userPlantHeartId)).thenReturn(Optional.empty());
        when(userPlantHeartRepository.save(any(UserPlantHeart.class))).thenReturn(userPlantHeart);
        when(plantRepository.save(any(Plant.class))).thenReturn(plant);

        // When
        userPlantHeartService.togglePlantHeart(userId, plantId);

        // Then
        // AuthorizationService가 호출되었는지 확인 (중복 권한 검사 로직 제거 확인)
        verify(authorizationService, times(1)).isOwnerOfPlant(userId, plantId);
    }
}
