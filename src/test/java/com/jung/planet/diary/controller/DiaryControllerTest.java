package com.jung.planet.diary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jung.planet.diary.dto.DiaryDetailDTO;
import com.jung.planet.diary.dto.request.DiaryFormDTO;
import com.jung.planet.diary.entity.Diary;
import com.jung.planet.diary.service.DiaryService;
import com.jung.planet.plant.entity.Plant;
import com.jung.planet.security.UserDetail.CustomUserDetails;
import com.jung.planet.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DiaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DiaryService diaryService;

    private CustomUserDetails mockUserDetails;
    private Diary mockDiary;
    private DiaryDetailDTO mockDiaryDetail;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 정보 설정
        mockUserDetails = new CustomUserDetails(1L, "test@example.com",  UserRole.NORMAL,null);
        
        // 테스트용 Plant 객체 생성
        Plant mockPlant = Plant.builder()
            .nickName("테스트 식물")
            .scientificName("테스트 학명")
            .imgUrl("http://example.com/plant.jpg")
            .build();
            
        // 테스트 다이어리 설정 - builder 패턴 사용
        mockDiary = Diary.builder()
            .plant(mockPlant)
            .content("테스트 내용")
            .isPublic(true)
            .imgUrl("http://example.com/image.jpg")
            .createdAt(LocalDateTime.now())
            .build();
            
        // 테스트용으로 ID 설정을 위한 리플렉션 사용
        try {
            java.lang.reflect.Field idField = Diary.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(mockDiary, 1L);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 테스트 다이어리 상세 DTO 설정
        mockDiaryDetail = new DiaryDetailDTO();
        mockDiaryDetail.setId(1L);
        mockDiaryDetail.setContent("테스트 내용");
        mockDiaryDetail.setPublic(true);
        mockDiaryDetail.setImgUrl("http://example.com/image.jpg");
        mockDiaryDetail.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        mockDiaryDetail.setMine(true);
        
        // 서비스 메소드 모킹
        when(diaryService.findDiary(anyLong(), anyLong())).thenReturn(mockDiaryDetail);
        when(diaryService.addDiary(anyString(), any(DiaryFormDTO.class))).thenReturn(mockDiary);
        doNothing().when(diaryService).editDiary(any(CustomUserDetails.class), anyLong(), any(DiaryFormDTO.class));
        doNothing().when(diaryService).deleteDiary(anyLong(), any(CustomUserDetails.class));
    }

    @Test
    @DisplayName("다이어리 상세 조회")
    @WithMockUser
    void findDiary() throws Exception {
        mockMvc.perform(get("/diary/1")
                .with(SecurityMockMvcRequestPostProcessors.user(mockUserDetails)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").value("테스트 내용"))
            .andExpect(jsonPath("$.data.imgUrl").value("http://example.com/image.jpg"));
    }

    @Test
    @DisplayName("다이어리 작성")
    @WithMockUser
    void addDiary() throws Exception {
        DiaryFormDTO diaryFormDTO = new DiaryFormDTO();
        diaryFormDTO.setPlantId(1L);
        diaryFormDTO.setContent("새 내용");
        diaryFormDTO.setImgData("base64EncodedImageData");
        diaryFormDTO.setIsPublic(true);
        diaryFormDTO.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        
        mockMvc.perform(post("/diary/add")
                .with(SecurityMockMvcRequestPostProcessors.user(mockUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(diaryFormDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.diaryId").value(1L));
    }

    @Test
    @DisplayName("다이어리 수정")
    @WithMockUser
    void editDiary() throws Exception {
        DiaryFormDTO diaryFormDTO = new DiaryFormDTO();
        diaryFormDTO.setPlantId(1L);
        diaryFormDTO.setContent("수정된 내용");
        diaryFormDTO.setImgData("base64EncodedImageData");
        diaryFormDTO.setIsPublic(true);
        diaryFormDTO.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        
        mockMvc.perform(post("/diary/edit/1")
                .with(SecurityMockMvcRequestPostProcessors.user(mockUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(diaryFormDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.diaryId").value(1L));
    }

    @Test
    @DisplayName("다이어리 삭제")
    @WithMockUser
    void removeDiary() throws Exception {
        mockMvc.perform(delete("/diary/remove/1")
                .with(SecurityMockMvcRequestPostProcessors.user(mockUserDetails)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.diaryId").value(1L));
    }
} 