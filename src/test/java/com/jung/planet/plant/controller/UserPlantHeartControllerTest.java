package com.jung.planet.plant.controller;

import com.jung.planet.plant.service.UserPlantHeartService;
import com.jung.planet.security.UserDetail.CustomUserDetails;
import com.jung.planet.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserPlantHeartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserPlantHeartService userPlantHeartService;

    private CustomUserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 정보 설정
        mockUserDetails = new CustomUserDetails(1L, "test@example.com", UserRole.NORMAL, null);
        
        // 서비스 메소드 모킹
        when(userPlantHeartService.togglePlantHeart(anyLong(), anyLong())).thenReturn(true);
    }

    @Test
    @DisplayName("식물 좋아요 토글")
    @WithMockUser
    void togglePlantHeart() throws Exception {
        mockMvc.perform(post("/plants/heart/1")
                .with(SecurityMockMvcRequestPostProcessors.user(mockUserDetails)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.plantId").value(1))
            .andExpect(jsonPath("$.data.hearted").value(true));
    }
    
    @Test
    @DisplayName("식물 좋아요 취소 토글")
    @WithMockUser
    void togglePlantHeartCancel() throws Exception {
        when(userPlantHeartService.togglePlantHeart(anyLong(), anyLong())).thenReturn(false);
        
        mockMvc.perform(post("/plants/heart/1")
                .with(SecurityMockMvcRequestPostProcessors.user(mockUserDetails)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.plantId").value(1))
            .andExpect(jsonPath("$.data.hearted").value(false));
    }
} 