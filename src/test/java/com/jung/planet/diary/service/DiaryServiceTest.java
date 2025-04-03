package com.jung.planet.diary.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.jung.planet.diary.dto.DiaryDetailDTO;
import com.jung.planet.diary.dto.request.DiaryFormDTO;
import com.jung.planet.diary.entity.Diary;
import com.jung.planet.diary.repository.DiaryRepository;
import com.jung.planet.plant.entity.Plant;
import com.jung.planet.plant.repository.PlantRepository;
import com.jung.planet.r2.CloudflareR2Uploader;
import com.jung.planet.security.UserDetail.CustomUserDetails;
import com.jung.planet.user.entity.User;
import com.jung.planet.user.entity.UserRole;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class DiaryServiceTest {

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private PlantRepository plantRepository;
    
    @Mock
    private CloudflareR2Uploader cloudflareR2Uploader;

    @InjectMocks
    private DiaryService diaryService;

    private DiaryFormDTO diaryFormDTO;
    private Plant plant;
    private Diary diary;
    private User user;
    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        // 테스트용 DiaryFormDTO 생성
        diaryFormDTO = new DiaryFormDTO();
        diaryFormDTO.setPlantId(1L);
        diaryFormDTO.setContent("Today, my plant looks more lively than ever!");
        diaryFormDTO.setImgData("base64EncodedImageData");
        diaryFormDTO.setIsPublic(true);
        diaryFormDTO.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }

    // ADD
    @Test
    void testAddDiary_Success() {
        // Given
        String userName = "test@example.com";
        plant = mock(Plant.class);
        diary = mock(Diary.class);
        
        when(plantRepository.findById(eq(1L))).thenReturn(Optional.of(plant));
        when(diaryRepository.save(any(Diary.class))).thenReturn(diary);
        doNothing().when(cloudflareR2Uploader).uploadDiaryImage(anyString(), any(Diary.class), any(ByteBuffer.class));
        
        when(diary.getContent()).thenReturn(diaryFormDTO.getContent());
        
        // When
        Diary result = diaryService.addDiary(userName, diaryFormDTO);
        
        // Then
        assertNotNull(result);
        assertEquals(diaryFormDTO.getContent(), result.getContent());
        verify(plantRepository).findById(eq(1L));
        verify(diaryRepository, times(2)).save(any(Diary.class));
        verify(cloudflareR2Uploader).uploadDiaryImage(eq(userName), any(Diary.class), any(ByteBuffer.class));
    }

    @Test
    void testAddDiary_PlantNotFound() {
        // Given
        String userName = "test@example.com";
        when(plantRepository.findById(eq(1L))).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> diaryService.addDiary(userName, diaryFormDTO));
        verify(plantRepository).findById(eq(1L));
        verify(diaryRepository, never()).save(any(Diary.class));
    }

    // FIND
    @Test
    void testFindDiary_Success() {
        // Given
        Long diaryId = 1L;
        Long userId = 1L;
        diary = mock(Diary.class);
        plant = mock(Plant.class);
        user = mock(User.class);
        
        when(diaryRepository.findById(eq(diaryId))).thenReturn(Optional.of(diary));
        when(diary.getPlant()).thenReturn(plant);
        when(plant.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(diary.getId()).thenReturn(diaryId);
        when(diary.getContent()).thenReturn(diaryFormDTO.getContent());
        when(diary.getImgUrl()).thenReturn("http://example.com/image.jpg");
        when(diary.getIsPublic()).thenReturn(true);
        when(diary.getCreatedAt()).thenReturn(LocalDateTime.now());
        
        // When
        DiaryDetailDTO result = diaryService.findDiary(diaryId, userId);
        
        // Then
        assertNotNull(result);
        assertEquals(diaryId, result.getId());
        assertEquals(diary.getContent(), result.getContent());
        assertEquals(diary.getImgUrl(), result.getImgUrl());
        assertEquals(diary.getIsPublic(), result.isPublic());
        assertTrue(result.isMine()); // 같은 사용자 ID이므로 true
    }
    
    @Test
    void testFindDiary_DiaryNotFound() {
        // Given
        Long diaryId = 99L;
        Long userId = 1L;
        when(diaryRepository.findById(eq(diaryId))).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> diaryService.findDiary(diaryId, userId));
    }

    // DELETE
    @Test
    void testDeleteDiary_AsOwner_Success() {
        // Given
        Long diaryId = 1L;
        diary = mock(Diary.class);
        plant = mock(Plant.class);
        user = mock(User.class);
        customUserDetails = mock(CustomUserDetails.class);
        
        when(diaryRepository.findById(eq(diaryId))).thenReturn(Optional.of(diary));
        when(diary.getPlant()).thenReturn(plant);
        when(plant.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(1L);
        when(customUserDetails.getUserId()).thenReturn(1L);
        when(customUserDetails.getUsername()).thenReturn("test@example.com");
        when(customUserDetails.getUserRole()).thenReturn(UserRole.NORMAL);
        doNothing().when(diaryRepository).deleteById(eq(diaryId));
        doNothing().when(cloudflareR2Uploader).deleteDiary(eq(diary), eq("test@example.com"));
        
        // When
        diaryService.deleteDiary(diaryId, customUserDetails);
        
        // Then
        verify(diaryRepository).deleteById(eq(diaryId));
        verify(cloudflareR2Uploader).deleteDiary(eq(diary), eq("test@example.com"));
    }
    
    @Test
    void testDeleteDiary_DiaryNotFound() {
        // Given
        Long diaryId = 99L;
        customUserDetails = mock(CustomUserDetails.class);
        when(diaryRepository.findById(eq(diaryId))).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> diaryService.deleteDiary(diaryId, customUserDetails));
        verify(diaryRepository, never()).deleteById(anyLong());
    }
}