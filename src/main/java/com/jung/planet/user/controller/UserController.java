package com.jung.planet.user.controller;

import com.jung.planet.common.dto.ApiResponseDTO;
import com.jung.planet.plant.repository.UserPlantHeartRepository;
import com.jung.planet.plant.service.PlantService;
import com.jung.planet.security.JwtTokenProvider;
import com.jung.planet.security.UserDetail.CustomUserDetails;
import com.jung.planet.user.dto.JwtResponse;
import com.jung.planet.user.dto.UserDTO;
import com.jung.planet.user.dto.response.TokenResponseDTO;
import com.jung.planet.user.dto.response.UserResponseDTO;
import com.jung.planet.user.entity.User;
import com.jung.planet.user.repository.UserRepository;
import com.jung.planet.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Tag(name = "User", description = "사용자 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final PlantService plantService;
    private final UserPlantHeartRepository userPlantHeartRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "사용자 로그인", description = "사용자 계정으로 로그인합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = JwtResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/login")
    public ApiResponseDTO<JwtResponse> getCurrentUser(@RequestBody UserDTO userDTO) {
        logger.debug("USER LOGIN :: {} ", userDTO);
        User user = userService.processUser(userDTO);
        String access_token = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole());

        JwtResponse response = new JwtResponse(access_token, user.getRefreshToken(), user);
        return ApiResponseDTO.success(response, "로그인이 성공적으로 완료되었습니다.");
    }

    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자의 계정을 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "탈퇴 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @DeleteMapping("/remove")
    public ApiResponseDTO<Void> deleteUser(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        userService.deleteUser(customUserDetails);
        return ApiResponseDTO.success("사용자 계정이 성공적으로 삭제되었습니다.");
    }

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
        @ApiResponse(responseCode = "403", description = "유효하지 않은 리프레시 토큰"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/refresh")
    public ApiResponseDTO<TokenResponseDTO> refreshTokens(@RequestParam String refreshToken) {
        String email = jwtTokenProvider.decodeJwt(refreshToken).getSubject();
        Optional<User> user = userService.findByEmail(email);

        if (user.isPresent() && user.get().getRefreshToken().equals(refreshToken) && jwtTokenProvider.validateToken(refreshToken)) {
            String newAccessToken = jwtTokenProvider.createAccessToken(user.get().getId(), user.get().getEmail(), user.get().getRole());
            String newRefreshToken = jwtTokenProvider.createRefreshToken(user.get().getId(), user.get().getEmail(), user.get().getRole());

            userService.updateRefreshToken(user.get().getId(), newRefreshToken);

            TokenResponseDTO tokenResponse = TokenResponseDTO.builder()
                    .access_token(newAccessToken)
                    .refresh_token(newRefreshToken)
                    .build();
                    
            return ApiResponseDTO.success(tokenResponse, "토큰이 성공적으로 갱신되었습니다.");
        } else {
            return ApiResponseDTO.error("유효하지 않은 리프레시 토큰입니다.", TokenResponseDTO.builder().build());
        }
    }

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/my-info")
    public ApiResponseDTO<UserResponseDTO> getMyInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long userId = customUserDetails.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("해당하는 유저를 찾을 수 없습니다."));

        int totalHeartsGivenByUser = userPlantHeartRepository.countByUserId(userId);
        int totalHearts = plantService.getTotalHearts(userId);
        long daysSinceCreated = ChronoUnit.DAYS.between(user.getCreatedAt(), LocalDateTime.now());
        int maxPlants = user.getSubscription().getMaxPlants();
        boolean aiServiceAccess = user.getSubscription().isAiServiceAccess();

        UserResponseDTO userResponse = UserResponseDTO.builder()
                .name(user.getName())
                .period(daysSinceCreated)
                .receivedHearts(totalHearts)
                .givenHearts(totalHeartsGivenByUser)
                .maxPlants(maxPlants)
                .aiServiceAccess(aiServiceAccess)
                .build();

        return ApiResponseDTO.success(userResponse);
    }
}
