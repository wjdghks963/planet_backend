package com.jung.planet.plant.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jung.planet.plant.dto.request.PlantFormDTO;
import com.jung.planet.plant.entity.Plant;
import com.jung.planet.plant.repository.PlantRepository;
import com.jung.planet.r2.CloudflareR2Uploader;
import com.jung.planet.user.entity.User;
import com.jung.planet.user.entity.UserRole;
import com.jung.planet.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PlantServiceTest {

    @Mock
    private PlantRepository plantRepository;

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private CloudflareR2Uploader cloudflareR2Uploader;

    @InjectMocks
    private PlantService plantService;

    private PlantFormDTO plantFormDTO;

    @BeforeEach
    void setUp() {
        // 테스트용 PlantFormDTO 생성
        plantFormDTO = new PlantFormDTO();
        plantFormDTO.setUserId(1L);
        plantFormDTO.setNickName("Green Plant");
        plantFormDTO.setScientificName("Plantae Greenus");
        plantFormDTO.setImgData("base64EncodedImageData");
    }
    
    @Test
    void testAddPlant_Success() {
        // Given
        User user = mock(User.class);
        Plant plant = mock(Plant.class);
        
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(user));
        when(plantRepository.save(any(Plant.class))).thenReturn(plant);
        doNothing().when(cloudflareR2Uploader).uploadPlantImage(any(User.class), any(Plant.class), any(ByteBuffer.class));
        
        when(plant.getNickName()).thenReturn(plantFormDTO.getNickName());
        when(plant.getScientificName()).thenReturn(plantFormDTO.getScientificName());
        
        // When
        Plant result = plantService.addPlant(plantFormDTO);
        
        // Then
        assertNotNull(result);
        assertEquals(plantFormDTO.getNickName(), result.getNickName());
        assertEquals(plantFormDTO.getScientificName(), result.getScientificName());
        
        verify(userRepository).findById(eq(1L));
        verify(plantRepository, times(2)).save(any(Plant.class));
        verify(cloudflareR2Uploader).uploadPlantImage(eq(user), any(Plant.class), any(ByteBuffer.class));
    }
    
    @Test
    void testAddPlant_UserNotFound() {
        // Given
        when(userRepository.findById(eq(1L))).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> plantService.addPlant(plantFormDTO));
        
        verify(userRepository).findById(eq(1L));
        verify(plantRepository, never()).save(any(Plant.class));
        verify(cloudflareR2Uploader, never()).uploadPlantImage(any(User.class), any(Plant.class), any(ByteBuffer.class));
    }
}
