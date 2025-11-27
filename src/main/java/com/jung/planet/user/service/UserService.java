package com.jung.planet.user.service;


import com.jung.planet.admin.service.SlackNotificationService;

import com.jung.planet.exception.UnauthorizedActionException;
import com.jung.planet.plant.repository.PlantRepository;
import com.jung.planet.plant.repository.UserPlantHeartRepository;
import com.jung.planet.r2.CloudflareR2Uploader;
import com.jung.planet.security.JwtTokenProvider;
import com.jung.planet.security.UserDetail.CustomUserDetails;
import com.jung.planet.user.dto.UserDTO;
import com.jung.planet.user.dto.response.TokenResponseDTO;
import com.jung.planet.user.dto.response.UserResponseDTO;
import com.jung.planet.user.entity.Subscription;
import com.jung.planet.user.entity.SubscriptionType;
import com.jung.planet.user.entity.User;
import com.jung.planet.user.entity.UserRole;
import com.jung.planet.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final CloudflareR2Uploader cloudflareR2Uploader;
    private final UserPlantHeartRepository userPlantHeartRepository;
    private final PlantRepository plantRepository;
    private final SlackNotificationService slackNotificationService;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);


    @Transactional
    public User adminUser(UserDTO userDTO) {
        Optional<User> user = userRepository.findByEmail(userDTO.getEmail());

        if (user.isEmpty()) {
            User newUser = createNewUser(userDTO, SubscriptionType.PREMIUM, UserRole.ADMIN);

            Map<String, String> infoData = new HashMap<>();
            infoData.put("Email", newUser.getEmail());
            slackNotificationService.sendSlackOkNotification("어드민 유저 생성 ", infoData);

            return newUser;
        } else {
            return updateRefreshTokenForExistingUser(user.get());
        }
    }

    @Transactional
    public User processUser(UserDTO userDTO) {
        Optional<User> user = userRepository.findByEmail(userDTO.getEmail());

        if (user.isEmpty()) {
            return createNewUser(userDTO, SubscriptionType.BASIC, UserRole.NORMAL);
        } else {
            return updateRefreshTokenForExistingUser(user.get());
        }
    }

    /**
     * 새로운 사용자를 생성합니다.
     *
     * @param userDTO 사용자 정보
     * @param subscriptionType 구독 타입
     * @param userRole 사용자 역할
     * @return 생성된 사용자
     */
    private User createNewUser(UserDTO userDTO, SubscriptionType subscriptionType, UserRole userRole) {
        Subscription subscription = Subscription.builder()
                .type(subscriptionType)
                .maxPlants(subscriptionType.getMaxPlants())
                .aiServiceAccess(subscriptionType.isAiServiceAccess())
                .build();

        User newUser = User.builder()
                .email(userDTO.getEmail())
                .name(userDTO.getName())
                .subscription(subscription)
                .build();

        subscription.setUser(newUser);
        newUser.setRole(userRole);

        String refreshToken = jwtTokenProvider.createRefreshToken(newUser.getId(), newUser.getEmail(), newUser.getRole());
        newUser.setRefreshToken(refreshToken);

        userRepository.save(newUser);
        return newUser;
    }

    /**
     * 기존 사용자의 리프레시 토큰을 갱신합니다.
     *
     * @param user 기존 사용자
     * @return 토큰이 갱신된 사용자
     */
    private User updateRefreshTokenForExistingUser(User user) {
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail(), user.getRole());
        user.setRefreshToken(refreshToken);
        userRepository.save(user);
        return user;
    }


    @Transactional
    public void deleteUser(CustomUserDetails customUserDetails) {
        User userToDelete = userRepository.findById(customUserDetails.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 사용자는 자신의 계정을 삭제할 수 있음 (Admin이 아니어도 가능)
        userPlantHeartRepository.deleteByUserId(customUserDetails.getUserId());
        userRepository.deleteById(customUserDetails.getUserId());
        cloudflareR2Uploader.deleteUser(userToDelete.getEmail());
    }

    @Transactional
    public User upgradeUserSubscription(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));

        SubscriptionType subscriptionType = SubscriptionType.PREMIUM;
        Subscription subscription = user.getSubscription();
        subscription.setType(subscriptionType);
        subscription.setMaxPlants(subscriptionType.getMaxPlants());
        subscription.setAiServiceAccess(subscriptionType.isAiServiceAccess());
        subscription.startSubscription();


        user.setSubscription(subscription);

        userRepository.save(user);

        Map<String, String> infoData = new HashMap<>();
        infoData.put("mail", user.getEmail());
        infoData.put("start date", subscription.getStartDate().toString());
        infoData.put("end date", subscription.getEndDate().toString());

        slackNotificationService.sendSlackOkNotification("유저 구독 업그레이드", infoData);
        return user;
    }


    @Transactional
    public void updateRefreshToken(Long userId, String refreshToken) {
        Optional<User> user = userRepository.findById(userId);
        user.ifPresent(u -> {
            u.setRefreshToken(refreshToken);
            userRepository.save(u);
        });
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * 리프레시 토큰을 검증하고 새로운 액세스 토큰과 리프레시 토큰을 발급합니다.
     *
     * @param refreshToken 리프레시 토큰
     * @return 새로운 토큰 정보 또는 null (유효하지 않은 경우)
     */
    @Transactional
    public TokenResponseDTO refreshUserTokens(String refreshToken) {
        String email = jwtTokenProvider.decodeJwt(refreshToken).getSubject();
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return null;
        }

        User user = userOptional.get();

        // 토큰 검증: DB에 저장된 리프레시 토큰과 일치하고, 유효한 토큰인지 확인
        if (!user.getRefreshToken().equals(refreshToken) || !jwtTokenProvider.validateToken(refreshToken)) {
            return null;
        }

        // 새로운 토큰 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail(), user.getRole());

        // 새로운 리프레시 토큰 저장
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return TokenResponseDTO.builder()
                .access_token(newAccessToken)
                .refresh_token(newRefreshToken)
                .build();
    }

    /**
     * 사용자 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자 상세 정보
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당하는 유저를 찾을 수 없습니다."));

        int totalHeartsGivenByUser = userPlantHeartRepository.countByUserId(userId);
        Integer totalHearts = plantRepository.sumHeartsByUserId(userId);
        int receivedHearts = totalHearts != null ? totalHearts : 0;

        long daysSinceCreated = ChronoUnit.DAYS.between(user.getCreatedAt(), LocalDateTime.now());
        int maxPlants = user.getSubscription().getMaxPlants();
        boolean aiServiceAccess = user.getSubscription().isAiServiceAccess();

        return UserResponseDTO.builder()
                .name(user.getName())
                .period(daysSinceCreated)
                .receivedHearts(receivedHearts)
                .givenHearts(totalHeartsGivenByUser)
                .maxPlants(maxPlants)
                .aiServiceAccess(aiServiceAccess)
                .build();
    }


}
