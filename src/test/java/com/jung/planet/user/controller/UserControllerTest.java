package com.jung.planet.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jung.planet.plant.repository.UserPlantHeartRepository;
import com.jung.planet.plant.service.PlantService;
import com.jung.planet.security.JwtTokenProvider;
import com.jung.planet.security.UserDetail.CustomUserDetails;
import com.jung.planet.user.dto.JwtResponse;
import com.jung.planet.user.dto.UserDTO;
import com.jung.planet.user.entity.Subscription;
import com.jung.planet.user.entity.SubscriptionType;
import com.jung.planet.user.entity.User;
import com.jung.planet.user.entity.UserRole;
import com.jung.planet.user.repository.UserRepository;
import com.jung.planet.user.service.UserService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PlantService plantService;

    @MockBean
    private UserPlantHeartRepository userPlantHeartRepository;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private CustomUserDetails mockUserDetails;
    private User mockUser;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 정보 설정
        mockUserDetails = new CustomUserDetails(1L, "test@example.com", UserRole.NORMAL, null);
        
        // 테스트 구독 정보 설정
        Subscription subscription = new Subscription();
        subscription.setType(SubscriptionType.BASIC);
        subscription.setMaxPlants(3);
        subscription.setAiServiceAccess(false);

        // 테스트 사용자 설정
        mockUser = User.builder()
            .email("test@example.com")
            .name("테스트 사용자")
            .refreshToken("test-refresh-token")
            .subscription(subscription)
            .build();
            
        // 테스트용으로 ID 및 생성일 설정을 위한 리플렉션 사용
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(mockUser, 1L);
            
            java.lang.reflect.Field createdAtField = User.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(mockUser, LocalDateTime.now().minusDays(30));
            
            java.lang.reflect.Field roleField = User.class.getDeclaredField("role");
            roleField.setAccessible(true);
            roleField.set(mockUser, UserRole.NORMAL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 서비스 메소드 모킹
        when(userService.processUser(any(UserDTO.class))).thenReturn(mockUser);
        when(userService.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(jwtTokenProvider.createAccessToken(anyLong(), anyString(), any(UserRole.class))).thenReturn("test-access-token");
        when(jwtTokenProvider.createRefreshToken(anyLong(), anyString(), any(UserRole.class))).thenReturn("new-refresh-token");
        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));
        when(plantService.getTotalHearts(anyLong())).thenReturn(5);
        when(userPlantHeartRepository.countByUserId(anyLong())).thenReturn(3);
    }

    @Test
    @DisplayName("사용자 로그인")
    void login() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("test@example.com");
        userDTO.setName("테스트 사용자");
        
        mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.access_token").exists())
            .andExpect(jsonPath("$.data.user.email").value("test@example.com"));
    }

    @Test
    @DisplayName("회원 탈퇴")
    @WithMockUser
    void deleteUser() throws Exception {
        mockMvc.perform(delete("/users/remove")
                .with(SecurityMockMvcRequestPostProcessors.user(mockUserDetails)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("토큰 갱신")
    void refreshTokens() throws Exception {
        mockMvc.perform(post("/users/refresh")
                .param("refreshToken", "test-refresh-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.access_token").value("test-access-token"))
            .andExpect(jsonPath("$.data.refresh_token").value("new-refresh-token"));
    }

    @Test
    @DisplayName("내 정보 조회")
    @WithMockUser
    void getMyInfo() throws Exception {
        mockMvc.perform(get("/users/my-info")
                .with(SecurityMockMvcRequestPostProcessors.user(mockUserDetails)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("테스트 사용자"))
            .andExpect(jsonPath("$.data.receivedHearts").value(5))
            .andExpect(jsonPath("$.data.givenHearts").value(3))
            .andExpect(jsonPath("$.data.maxPlants").value(3))
            .andExpect(jsonPath("$.data.aiServiceAccess").value(false));
    }
} 