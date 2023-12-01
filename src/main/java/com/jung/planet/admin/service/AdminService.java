package com.jung.planet.admin.service;

import com.jung.planet.admin.dto.PremiumUserDTO;
import com.jung.planet.user.entity.Subscription;
import com.jung.planet.user.entity.SubscriptionType;
import com.jung.planet.user.entity.User;
import com.jung.planet.user.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class AdminService {

    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public List<PremiumUserDTO> getAllPremiumUsers() {
        List<Subscription> premiumSubscriptions = subscriptionRepository.findByType(SubscriptionType.PREMIUM);
        return premiumSubscriptions.stream()
                .map(Subscription::getUser)
                .map(this::convertUserToUserSubscriptionDTO)
                .collect(Collectors.toList());
    }

    private PremiumUserDTO convertUserToUserSubscriptionDTO(User user) {
        PremiumUserDTO premiumUserDTO = new PremiumUserDTO();
        premiumUserDTO.setEmail(user.getEmail());
        premiumUserDTO.setName(user.getName());
        premiumUserDTO.setSubscription(user.getSubscription());
        return premiumUserDTO;
    }
}
