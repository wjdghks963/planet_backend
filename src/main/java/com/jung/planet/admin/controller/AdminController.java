package com.jung.planet.admin.controller;

import com.jung.planet.admin.dto.PremiumUserDTO;
import com.jung.planet.admin.dto.response.AdminResponseDTO;
import com.jung.planet.admin.service.AdminService;
import com.jung.planet.common.dto.ApiResponseDTO;
import com.jung.planet.security.JwtTokenProvider;
import com.jung.planet.security.UserDetail.CustomUserDetails;
import com.jung.planet.user.dto.JwtResponse;
import com.jung.planet.user.dto.UserDTO;
import com.jung.planet.user.entity.User;
import com.jung.planet.user.entity.UserRole;
import com.jung.planet.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin", description = "관리자 관련 API")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AdminService adminService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "관리자 로그인", description = "관리자 계정으로 로그인합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = JwtResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/login")
    public ApiResponseDTO<JwtResponse> getAdminUser(@RequestBody UserDTO userDTO) {
        User user = userService.adminUser(userDTO);
        String access_token = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole());

        JwtResponse response = new JwtResponse(access_token, user.getRefreshToken(), user);
        return ApiResponseDTO.success(response, "관리자 로그인이 성공적으로 완료되었습니다.");
    }

    @Operation(summary = "구독 업그레이드", description = "사용자의 구독을 프리미엄으로 업그레이드합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "업그레이드 성공",
            content = @Content(schema = @Schema(implementation = User.class))),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/upgrade")
    public ApiResponseDTO<AdminResponseDTO> upgradeSubscription(
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @RequestBody UserDTO userDTO) {
        if (customUserDetails.getUserRole().equals(UserRole.ADMIN)) {
            User user = userService.upgradeUserSubscription(userDTO.getEmail());
            
            AdminResponseDTO responseDTO = AdminResponseDTO.builder()
                    .user(user)
                    .message("사용자 구독이 성공적으로 업그레이드되었습니다.")
                    .build();
                    
            return ApiResponseDTO.success(responseDTO);
        } else {
            throw new AccessDeniedException("권한이 없습니다.");
        }
    }

    @Operation(summary = "프리미엄 사용자 목록 조회", description = "모든 프리미엄 사용자의 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = PremiumUserDTO.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/subscriptions")
    public ApiResponseDTO<AdminResponseDTO> getAllPremiumUsers() {
        List<PremiumUserDTO> premiumUsers = adminService.getAllPremiumUsers();
        
        AdminResponseDTO responseDTO = AdminResponseDTO.builder()
                .premiumUsers(premiumUsers)
                .message("프리미엄 사용자 목록을 성공적으로 조회했습니다.")
                .build();
                
        return ApiResponseDTO.success(responseDTO);
    }
}
