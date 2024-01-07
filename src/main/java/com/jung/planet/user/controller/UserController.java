package com.jung.planet.user.controller;

import com.jung.planet.plant.repository.UserPlantHeartRepository;
import com.jung.planet.plant.service.PlantService;
import com.jung.planet.security.JwtTokenProvider;
import com.jung.planet.security.UserDetail.CustomUserDetails;
import com.jung.planet.user.dto.JwtResponse;
import com.jung.planet.user.dto.UserDTO;
import com.jung.planet.user.entity.User;
import com.jung.planet.user.repository.UserRepository;
import com.jung.planet.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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


    @PostMapping("/login")
    public ResponseEntity<?> getCurrentUser(@RequestBody UserDTO userDTO) {
        logger.debug("USER LOGIN :: {} ", userDTO);
        User user = userService.processUser(userDTO);
        String access_token = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole());

        return ResponseEntity.ok(new JwtResponse(access_token, user.getRefreshToken(), user));
    }

    @DeleteMapping("/remove")
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        userService.deleteUser(customUserDetails);
        return ResponseEntity.ok(Map.of("ok", true));
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refreshTokens(@RequestParam String refreshToken) {
        String email = jwtTokenProvider.decodeJwt(refreshToken).getSubject();
        Optional<User> user = userService.findByEmail(email);

        if (user.isPresent() && user.get().getRefreshToken().equals(refreshToken) && jwtTokenProvider.validateToken(refreshToken)) {
            String newAccessToken = jwtTokenProvider.createAccessToken(user.get().getId(), user.get().getEmail(), user.get().getRole());
            String newRefreshToken = jwtTokenProvider.createRefreshToken(user.get().getId(), user.get().getEmail(), user.get().getRole());

            userService.updateRefreshToken(user.get().getId(), newRefreshToken);

            Map<String, String> tokens = new HashMap<>();
            tokens.put("access_token", newAccessToken);
            tokens.put("refresh_token", newRefreshToken);
            return ResponseEntity.ok(tokens);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid Refresh Token");
        }
    }


    @GetMapping("/my-info")
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long userId = customUserDetails.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("해당하는 유저를 찾을 수 없습니다."));

        int totalHeartsGivenByUser = userPlantHeartRepository.countByUserId(userId); // 사용자가 누른 하트 개수
        int totalHearts = plantService.getTotalHearts(userId); // 사용자의 식물이 받은 총 하트 수
        long daysSinceCreated = ChronoUnit.DAYS.between(user.getCreatedAt(), LocalDateTime.now()); // 가입 후 경과 일수
        int maxPlants = user.getSubscription().getMaxPlants(); // 사용자가 가질 수 있는 최대 식물 수
        boolean aiServiceAccess = user.getSubscription().isAiServiceAccess(); // 사용자가 ai 서비스 이용할 수 있는지 확인


        Map<String, Object> userInfo = Map.of(
                "name", user.getName(),
                "period", daysSinceCreated,
                "receivedHearts", totalHearts,
                "givenHearts", totalHeartsGivenByUser,
                "maxPlants", maxPlants,
                "aiServiceAccess", aiServiceAccess
        );

        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("/request/grade-up")
    public ResponseEntity<?> requestGradeUp(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long userId = customUserDetails.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("해당하는 유저를 찾을 수 없습니다."));

        boolean isOk = userService.requestGradeUp(user);
        if (isOk) {
            return ResponseEntity.ok(Map.of("message", "신청 완료"));
        } else {
            return ResponseEntity.ok(Map.of("message", "이미 프리미엄 유저입니다."));

        }
    }
}
