package com.jung.planet.admin.mapper;

import com.jung.planet.admin.dto.PremiumUserDTO;
import com.jung.planet.user.entity.Subscription;
import com.jung.planet.user.entity.SubscriptionType;
import com.jung.planet.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AdminMapperTest {

    private AdminMapper adminMapper;
    private User user;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        adminMapper = new AdminMapper();

        subscription = Subscription.builder()
                .id(1L)
                .type(SubscriptionType.PREMIUM)
                .startDate(LocalDateTime.now().minusMonths(1))
                .endDate(LocalDateTime.now().plusMonths(11))
                .build();

        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .subscription(subscription)
                .build();
    }

    @Test
    @DisplayName("User를 PremiumUserDTO로 변환 - 성공")
    void testToPremiumUserDto_Success() {
        // When
        PremiumUserDTO result = adminMapper.toPremiumUserDto(user);

        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test User", result.getName());
        assertEquals(subscription, result.getSubscription());
    }

    @Test
    @DisplayName("User를 PremiumUserDTO로 변환 - 모든 필드 매핑 확인")
    void testToPremiumUserDto_AllFieldsMapped() {
        // When
        PremiumUserDTO result = adminMapper.toPremiumUserDto(user);

        // Then
        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getSubscription(), result.getSubscription());
    }

    @Test
    @DisplayName("User를 PremiumUserDTO로 변환 - subscription이 null인 경우")
    void testToPremiumUserDto_NullSubscription() {
        // Given
        User userWithoutSubscription = User.builder()
                .id(2L)
                .email("nosubscription@example.com")
                .name("No Subscription User")
                .subscription(null)
                .build();

        // When
        PremiumUserDTO result = adminMapper.toPremiumUserDto(userWithoutSubscription);

        // Then
        assertNotNull(result);
        assertEquals("nosubscription@example.com", result.getEmail());
        assertEquals("No Subscription User", result.getName());
        assertNull(result.getSubscription());
    }

    @Test
    @DisplayName("User를 PremiumUserDTO로 변환 - 여러 사용자 변환")
    void testToPremiumUserDto_MultipleUsers() {
        // Given
        User user1 = User.builder()
                .id(1L)
                .email("user1@example.com")
                .name("User 1")
                .subscription(subscription)
                .build();

        User user2 = User.builder()
                .id(2L)
                .email("user2@example.com")
                .name("User 2")
                .subscription(subscription)
                .build();

        // When
        PremiumUserDTO result1 = adminMapper.toPremiumUserDto(user1);
        PremiumUserDTO result2 = adminMapper.toPremiumUserDto(user2);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);

        assertEquals("user1@example.com", result1.getEmail());
        assertEquals("User 1", result1.getName());

        assertEquals("user2@example.com", result2.getEmail());
        assertEquals("User 2", result2.getName());

        // 각각 독립적인 DTO 객체인지 확인
        assertNotSame(result1, result2);
    }
}
