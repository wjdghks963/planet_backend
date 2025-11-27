package com.jung.planet.common.service;

import com.jung.planet.exception.ResourceNotFoundException;
import com.jung.planet.plant.entity.Plant;
import com.jung.planet.plant.repository.PlantRepository;
import com.jung.planet.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private PlantRepository plantRepository;

    @InjectMocks
    private AuthorizationService authorizationService;

    private User owner;
    private User otherUser;
    private Plant plant;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .email("owner@example.com")
                .name("Owner")
                .build();

        otherUser = User.builder()
                .id(2L)
                .email("other@example.com")
                .name("Other User")
                .build();

        plant = Plant.builder()
                .id(1L)
                .nickName("Test Plant")
                .user(owner)
                .build();
    }

    @Test
    @DisplayName("식물 소유권 확인 - 소유자인 경우 true 반환")
    void testIsOwnerOfPlant_Owner_ReturnsTrue() {
        // Given
        Long userId = 1L;
        Long plantId = 1L;

        when(plantRepository.findById(plantId)).thenReturn(Optional.of(plant));

        // When
        boolean result = authorizationService.isOwnerOfPlant(userId, plantId);

        // Then
        assertTrue(result);
        verify(plantRepository).findById(plantId);
    }

    @Test
    @DisplayName("식물 소유권 확인 - 소유자가 아닌 경우 false 반환")
    void testIsOwnerOfPlant_NotOwner_ReturnsFalse() {
        // Given
        Long userId = 2L; // otherUser의 ID
        Long plantId = 1L;

        when(plantRepository.findById(plantId)).thenReturn(Optional.of(plant));

        // When
        boolean result = authorizationService.isOwnerOfPlant(userId, plantId);

        // Then
        assertFalse(result);
        verify(plantRepository).findById(plantId);
    }

    @Test
    @DisplayName("식물 소유권 확인 - 식물을 찾을 수 없음")
    void testIsOwnerOfPlant_PlantNotFound_ThrowsException() {
        // Given
        Long userId = 1L;
        Long plantId = 999L;

        when(plantRepository.findById(plantId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
            () -> authorizationService.isOwnerOfPlant(userId, plantId));

        verify(plantRepository).findById(plantId);
    }

    @Test
    @DisplayName("식물 소유권 확인 - 트랜잭션 readOnly 확인")
    void testIsOwnerOfPlant_TransactionReadOnly() {
        // Given
        Long userId = 1L;
        Long plantId = 1L;

        when(plantRepository.findById(plantId)).thenReturn(Optional.of(plant));

        // When
        authorizationService.isOwnerOfPlant(userId, plantId);

        // Then
        // @Transactional(readOnly = true)가 적용되어 있으므로
        // 데이터 변경 없이 조회만 수행되는지 확인
        verify(plantRepository).findById(plantId);
        verify(plantRepository, never()).save(any(Plant.class));
    }

    @Test
    @DisplayName("식물 소유권 확인 - null userId")
    void testIsOwnerOfPlant_NullUserId_ReturnsFalse() {
        // Given
        Long userId = null;
        Long plantId = 1L;

        when(plantRepository.findById(plantId)).thenReturn(Optional.of(plant));

        // When
        boolean result = authorizationService.isOwnerOfPlant(userId, plantId);

        // Then
        assertFalse(result);
        verify(plantRepository).findById(plantId);
    }

    @Test
    @DisplayName("식물 소유권 확인 - 중앙화된 권한 검사 로직 테스트")
    void testIsOwnerOfPlant_CentralizedAuthorizationLogic() {
        // Given
        Long userId = 1L;
        Long plantId = 1L;

        when(plantRepository.findById(plantId)).thenReturn(Optional.of(plant));

        // When
        boolean result1 = authorizationService.isOwnerOfPlant(userId, plantId);
        boolean result2 = authorizationService.isOwnerOfPlant(2L, plantId);

        // Then
        assertTrue(result1);
        assertFalse(result2);

        // 중앙화된 로직이므로 같은 방식으로 동작하는지 확인
        verify(plantRepository, times(2)).findById(plantId);
    }
}
