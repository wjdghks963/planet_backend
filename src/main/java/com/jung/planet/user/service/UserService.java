package com.jung.planet.user.service;

import com.jung.planet.admin.service.SlackNotificationService;
import com.jung.planet.exception.UnauthorizedActionException;
import com.jung.planet.r2.CloudflareR2Uploader;
import com.jung.planet.security.JwtTokenProvider;
import com.jung.planet.security.UserDetail.CustomUserDetails;
import com.jung.planet.user.dto.UserDTO;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final CloudflareR2Uploader cloudflareR2Uploader;
    private final SlackNotificationService slackNotificationService;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);


    @Transactional
    public User adminUser(UserDTO userDTO) {
        Optional<User> user = userRepository.findByEmail(userDTO.getEmail());

        if (user.isEmpty()) {
            Subscription subscription = Subscription.builder()
                    .type(SubscriptionType.PREMIUM)
                    .maxPlants(6)
                    .aiServiceAccess(true)
                    .build();

            User newUser = User.builder()
                    .email(userDTO.getEmail())
                    .name(userDTO.getName())
                    .subscription(subscription)
                    .build();

            subscription.setUser(newUser);

            newUser.setRole(UserRole.ADMIN);

            String refreshToken = jwtTokenProvider.createRefreshToken(newUser.getId(), newUser.getEmail(), newUser.getRole());
            newUser.setRefreshToken(refreshToken);

            userRepository.save(newUser);


            Map<String, String> infoData = new HashMap<>();
            infoData.put("Email", newUser.getEmail());

            slackNotificationService.sendSlackOkNotification("어드민 유저 생성 ",infoData);

            return newUser;
        } else {
            User existingUser = user.get();
            String refreshToken = jwtTokenProvider.createRefreshToken(existingUser.getId(), existingUser.getEmail(), existingUser.getRole());
            existingUser.setRefreshToken(refreshToken);
            userRepository.save(existingUser);

            return existingUser;
        }
    }

    @Transactional
    public User processUser(UserDTO userDTO) {
        Optional<User> user = userRepository.findByEmail(userDTO.getEmail());

        // 사용자가 존재하지 않으면 새로운 사용자를 생성
        if (user.isEmpty()) {
            Subscription subscription = Subscription.builder()
                    .type(SubscriptionType.BASIC)
                    .maxPlants(3)
                    .aiServiceAccess(false)
                    .build();

            User newUser = User.builder()
                    .email(userDTO.getEmail())
                    .name(userDTO.getName())
                    .subscription(subscription)
                    .build();

            subscription.setUser(newUser);
            newUser.setRole(UserRole.NORMAL);


            String refreshToken = jwtTokenProvider.createRefreshToken(newUser.getId(), newUser.getEmail(), newUser.getRole());
            newUser.setRefreshToken(refreshToken);

            userRepository.save(newUser);
            return newUser;
        } else {
            User existingUser = user.get();
            String refreshToken = jwtTokenProvider.createRefreshToken(existingUser.getId(), existingUser.getEmail(), existingUser.getRole());
            existingUser.setRefreshToken(refreshToken);
            userRepository.save(existingUser);

            return existingUser;
        }
    }


    public void deleteUser(CustomUserDetails customUserDetails) {
        User userToDelete = userRepository.findById(customUserDetails.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        if (customUserDetails.getUserRole().equals(UserRole.ADMIN) || userToDelete != null) {
            userRepository.deleteById(customUserDetails.getUserId());
            cloudflareR2Uploader.deleteUser(userToDelete.getEmail());
        } else {
            throw new UnauthorizedActionException("삭제 권한이 없습니다.");
        }
    }

    @Transactional
    public User upgradeUserSubscription(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));

        Subscription subscription = user.getSubscription();
        subscription.setType(SubscriptionType.PREMIUM);
        subscription.setMaxPlants(6);
        subscription.setAiServiceAccess(true);
        subscription.startSubscription();


        user.setSubscription(subscription);

        userRepository.save(user);

        Map<String, String> infoData = new HashMap<>();
        infoData.put("mail", user.getEmail());
        infoData.put("start date", subscription.getStartDate().toString());
        infoData.put("end date", subscription.getEndDate().toString());

        slackNotificationService.sendSlackOkNotification("유저 구독 업그레이드",infoData);
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


}
