package com.jung.planet.plant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jung.planet.common.dto.ApiResponseDTO;
import com.jung.planet.plant.dto.PlantDetailDTO;
import com.jung.planet.plant.dto.PlantSummaryDTO;
import com.jung.planet.plant.dto.request.PlantFormDTO;
import com.jung.planet.plant.entity.Plant;
import com.jung.planet.plant.service.PlantService;
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
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PlantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlantService plantService;

    private CustomUserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 정보 설정
        mockUserDetails = new CustomUserDetails(1L, "test@example.com", UserRole.NORMAL, null);
        
        // 테스트 데이터 설정 - builder 패턴 사용
        Plant mockPlant = Plant.builder()
            .nickName("테스트 식물")
            .scientificName("테스트 학명")
            .imgUrl("http://example.com/image.jpg")
            .build();
        
        // 테스트용으로 ID 설정을 위한 리플렉션 사용 (실제 엔티티에 setter 없을 때)
        try {
            java.lang.reflect.Field idField = Plant.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(mockPlant, 1L);
            
            java.lang.reflect.Field createdAtField = Plant.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(mockPlant, LocalDateTime.now());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Mock 응답 설정 - setter 사용
        List<PlantSummaryDTO> mockPlants = new ArrayList<>();
        
        PlantSummaryDTO plant1 = new PlantSummaryDTO();
        plant1.setId(1L);
        plant1.setNickName("테스트 식물1");
        plant1.setImgUrl("http://example.com/image1.jpg");
        plant1.setPeriod(30);
        plant1.setHeartCount(10);
        
        PlantSummaryDTO plant2 = new PlantSummaryDTO();
        plant2.setId(2L);
        plant2.setNickName("테스트 식물2");
        plant2.setImgUrl("http://example.com/image2.jpg");
        plant2.setPeriod(60);
        plant2.setHeartCount(20);
        
        mockPlants.add(plant1);
        mockPlants.add(plant2);
        
        // PlantDetailDTO도 setter 사용
        PlantDetailDTO mockPlantDetail = new PlantDetailDTO();
        mockPlantDetail.setPlantId(1L);
        mockPlantDetail.setNickName("테스트 식물");
        mockPlantDetail.setScientificName("테스트 학명");
        mockPlantDetail.setImgUrl("http://example.com/image.jpg");
        mockPlantDetail.setHeartCount(5);
        mockPlantDetail.setPeriod(30);
        mockPlantDetail.setMine(false);
        mockPlantDetail.setHearted(false);
        mockPlantDetail.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        mockPlantDetail.setDiaries(Collections.emptyList());
            
        // 서비스 메소드 모킹
        when(plantService.getPlantsByRecent(0)).thenReturn(mockPlants);
        when(plantService.getPlantsByPopularity(0)).thenReturn(mockPlants);
        when(plantService.getHeartedPlantsByUser(anyLong(), eq(0))).thenReturn(mockPlants);
        when(plantService.getPlantDetailsByPlantId(anyLong(), anyLong())).thenReturn(mockPlantDetail);
        when(plantService.addPlant(any(PlantFormDTO.class))).thenReturn(mockPlant);
        when(plantService.isOwnerOfPlant(eq(1L), anyLong())).thenReturn(true);
        when(plantService.editPlant(any(PlantFormDTO.class), anyLong())).thenReturn(mockPlant);
        when(plantService.getPlantsByUserId(anyLong())).thenReturn(mockPlants);
        when(plantService.getRandomPlants()).thenReturn(mockPlants);
    }

    @Test
    @DisplayName("식물 목록 조회 - 최신순")
    @WithMockUser
    void getPlantsRecent() throws Exception {
        mockMvc.perform(get("/plants")
                .param("type", "recent")
                .param("page", "0")
                .with(SecurityMockMvcRequestPostProcessors.user(mockUserDetails)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.plants[0].nickName").value("테스트 식물1"))
            .andExpect(jsonPath("$.data.plants[1].nickName").value("테스트 식물2"));
    }

    @Test
    @DisplayName("식물 목록 조회 - 인기순")
    @WithMockUser
    void getPlantsPopular() throws Exception {
        mockMvc.perform(get("/plants")
                .param("type", "popular")
                .param("page", "0")
                .with(SecurityMockMvcRequestPostProcessors.user(mockUserDetails)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.plants[0].nickName").value("테스트 식물1"));
    }

    @Test
    @DisplayName("식물 상세 조회")
    @WithMockUser
    void getPlant() throws Exception {
        mockMvc.perform(get("/plants/1")
                .with(SecurityMockMvcRequestPostProcessors.user(mockUserDetails)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.nickName").value("테스트 식물"))
            .andExpect(jsonPath("$.data.scientificName").value("테스트 학명"));
    }

    @Test
    @DisplayName("식물 추가")
    @WithMockUser
    void addPlant() throws Exception {
        PlantFormDTO plantFormDTO = new PlantFormDTO();
        plantFormDTO.setUserId(1L);
        plantFormDTO.setNickName("새 식물");
        plantFormDTO.setScientificName("새 식물 학명");
        plantFormDTO.setImgData("base64EncodedImageData");

        mockMvc.perform(post("/plants/add")
                .with(SecurityMockMvcRequestPostProcessors.user(mockUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(plantFormDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.plantId").value(1L));
    }

    @Test
    @DisplayName("식물 수정")
    @WithMockUser
    void editPlant() throws Exception {
        PlantFormDTO plantFormDTO = new PlantFormDTO();
        plantFormDTO.setUserId(1L);
        plantFormDTO.setNickName("수정된 식물");
        plantFormDTO.setScientificName("수정된 학명");
        plantFormDTO.setImgData("base64EncodedImageData");

        mockMvc.perform(post("/plants/edit/1")
                .with(SecurityMockMvcRequestPostProcessors.user(mockUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(plantFormDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.plantId").value(1L));
    }

    @Test
    @DisplayName("내 식물 목록 조회")
    @WithMockUser
    void getMyPlants() throws Exception {
        mockMvc.perform(get("/plants/my")
                .with(SecurityMockMvcRequestPostProcessors.user(mockUserDetails)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.plants[0].nickName").value("테스트 식물1"));
    }

    @Test
    @DisplayName("랜덤 식물 목록 조회")
    @WithMockUser
    void getRandomPlants() throws Exception {
        mockMvc.perform(get("/plants/random"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.plants[0].nickName").value("테스트 식물1"));
    }

    @Test
    @DisplayName("식물 삭제")
    @WithMockUser
    void deletePlant() throws Exception {
        mockMvc.perform(delete("/plants/remove/1")
                .with(SecurityMockMvcRequestPostProcessors.user(mockUserDetails)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.plantId").value(1L));
    }
} 