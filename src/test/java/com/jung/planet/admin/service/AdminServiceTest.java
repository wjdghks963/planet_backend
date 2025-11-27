package com.jung.planet.admin.service;

import com.jung.planet.admin.dto.PremiumUserDTO;
import com.jung.planet.admin.mapper.AdminMapper;
import com.jung.planet.user.entity.Subscription;
import com.jung.planet.user.entity.SubscriptionType;
import com.jung.planet.user.entity.User;
import com.jung.planet.user.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private AdminMapper adminMapper;

    @InjectMocks
    private AdminService adminService;

    private User premiumUser1;
    private User premiumUser2;
    private Subscription subscription1;
    private Subscription subscription2;
    private PremiumUserDTO premiumUserDTO1;
    private PremiumUserDTO premiumUserDTO2;

    @BeforeEach
    void setUp() {
        premiumUser1 = User.builder()
                .id(1L)
                .email("premium1@example.com")
                .name("Premium User 1")
                .build();

        premiumUser2 = User.builder()
                .id(2L)
                .email("premium2@example.com")
                .name("Premium User 2")
                .build();

        subscription1 = Subscription.builder()
                .id(1L)
                .user(premiumUser1)
                .type(SubscriptionType.PREMIUM)
                .startDate(LocalDateTime.now().minusMonths(1))
                .endDate(LocalDateTime.now().plusMonths(11))
                .build();

        subscription2 = Subscription.builder()
                .id(2L)
                .user(premiumUser2)
                .type(SubscriptionType.PREMIUM)
                .startDate(LocalDateTime.now().minusMonths(2))
                .endDate(LocalDateTime.now().plusMonths(10))
                .build();

        premiumUserDTO1 = new PremiumUserDTO();
        premiumUserDTO1.setEmail("premium1@example.com");
        premiumUserDTO1.setName("Premium User 1");
        premiumUserDTO1.setSubscription(subscription1);

        premiumUserDTO2 = new PremiumUserDTO();
        premiumUserDTO2.setEmail("premium2@example.com");
        premiumUserDTO2.setName("Premium User 2");
        premiumUserDTO2.setSubscription(subscription2);
    }

    @Test
    @DisplayName("모든 프리미엄 사용자 조회 - 성공")
    void testGetAllPremiumUsers_Success() {
        // Given
        List<Subscription> subscriptions = Arrays.asList(subscription1, subscription2);

        when(subscriptionRepository.findByType(SubscriptionType.PREMIUM)).thenReturn(subscriptions);
        when(adminMapper.toPremiumUserDto(premiumUser1)).thenReturn(premiumUserDTO1);
        when(adminMapper.toPremiumUserDto(premiumUser2)).thenReturn(premiumUserDTO2);

        // When
        List<PremiumUserDTO> result = adminService.getAllPremiumUsers();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("premium1@example.com", result.get(0).getEmail());
        assertEquals("premium2@example.com", result.get(1).getEmail());

        verify(subscriptionRepository).findByType(SubscriptionType.PREMIUM);
        verify(adminMapper).toPremiumUserDto(premiumUser1);
        verify(adminMapper).toPremiumUserDto(premiumUser2);
    }

    @Test
    @DisplayName("모든 프리미엄 사용자 조회 - 프리미엄 사용자 없음")
    void testGetAllPremiumUsers_EmptyList() {
        // Given
        when(subscriptionRepository.findByType(SubscriptionType.PREMIUM)).thenReturn(Collections.emptyList());

        // When
        List<PremiumUserDTO> result = adminService.getAllPremiumUsers();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(subscriptionRepository).findByType(SubscriptionType.PREMIUM);
        verify(adminMapper, never()).toPremiumUserDto(any(User.class));
    }

    @Test
    @DisplayName("모든 프리미엄 사용자 조회 - AdminMapper 사용 확인")
    void testGetAllPremiumUsers_UsesAdminMapper() {
        // Given
        List<Subscription> subscriptions = Arrays.asList(subscription1);

        when(subscriptionRepository.findByType(SubscriptionType.PREMIUM)).thenReturn(subscriptions);
        when(adminMapper.toPremiumUserDto(premiumUser1)).thenReturn(premiumUserDTO1);

        // When
        List<PremiumUserDTO> result = adminService.getAllPremiumUsers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        // AdminMapper가 정확히 한 번 호출되었는지 확인 (DTO 변환 로직이 Mapper로 분리된 것 확인)
        verify(adminMapper, times(1)).toPremiumUserDto(premiumUser1);
    }

    @Test
    @DisplayName("모든 프리미엄 사용자 조회 - 트랜잭션 readOnly 확인")
    void testGetAllPremiumUsers_TransactionReadOnly() {
        // Given
        List<Subscription> subscriptions = Arrays.asList(subscription1, subscription2);

        when(subscriptionRepository.findByType(SubscriptionType.PREMIUM)).thenReturn(subscriptions);
        when(adminMapper.toPremiumUserDto(premiumUser1)).thenReturn(premiumUserDTO1);
        when(adminMapper.toPremiumUserDto(premiumUser2)).thenReturn(premiumUserDTO2);

        // When
        adminService.getAllPremiumUsers();

        // Then
        // @Transactional(readOnly = true)가 적용되어 있으므로
        // 데이터 변경 없이 조회만 수행되는지 확인
        verify(subscriptionRepository).findByType(SubscriptionType.PREMIUM);
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    @DisplayName("모든 프리미엄 사용자 조회 - Stream 처리 확인")
    void testGetAllPremiumUsers_StreamProcessing() {
        // Given
        List<Subscription> subscriptions = Arrays.asList(subscription1, subscription2);

        when(subscriptionRepository.findByType(SubscriptionType.PREMIUM)).thenReturn(subscriptions);
        when(adminMapper.toPremiumUserDto(premiumUser1)).thenReturn(premiumUserDTO1);
        when(adminMapper.toPremiumUserDto(premiumUser2)).thenReturn(premiumUserDTO2);

        // When
        List<PremiumUserDTO> result = adminService.getAllPremiumUsers();

        // Then
        // Stream 처리가 올바르게 되었는지 확인
        assertNotNull(result);
        assertEquals(2, result.size());

        // 모든 subscription에서 user를 가져오고 mapper를 통해 변환되었는지 확인
        verify(adminMapper, times(2)).toPremiumUserDto(any(User.class));
    }

    @Test
    @DisplayName("모든 프리미엄 사용자 조회 - 단일 사용자")
    void testGetAllPremiumUsers_SingleUser() {
        // Given
        List<Subscription> subscriptions = Arrays.asList(subscription1);

        when(subscriptionRepository.findByType(SubscriptionType.PREMIUM)).thenReturn(subscriptions);
        when(adminMapper.toPremiumUserDto(premiumUser1)).thenReturn(premiumUserDTO1);

        // When
        List<PremiumUserDTO> result = adminService.getAllPremiumUsers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("premium1@example.com", result.get(0).getEmail());
        assertEquals("Premium User 1", result.get(0).getName());
        assertEquals(subscription1, result.get(0).getSubscription());

        verify(subscriptionRepository).findByType(SubscriptionType.PREMIUM);
        verify(adminMapper).toPremiumUserDto(premiumUser1);
    }
}
