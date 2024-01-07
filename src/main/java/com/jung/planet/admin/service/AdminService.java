package com.jung.planet.admin.service;

import com.jung.planet.admin.dto.PremiumUserDTO;
import com.jung.planet.admin.dto.RequestUserDTO;
import com.jung.planet.user.entity.GradeUpRequest;
import com.jung.planet.user.entity.Subscription;
import com.jung.planet.user.entity.SubscriptionType;
import com.jung.planet.user.entity.User;
import com.jung.planet.user.repository.GradeUpRequestRepository;
import com.jung.planet.user.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class AdminService {

    private final SubscriptionRepository subscriptionRepository;
    private final GradeUpRequestRepository gradeUpRequestRepository;

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


    @Transactional(readOnly = true)
    public List<RequestUserDTO> getAllGradeUpUsers() {
        List<GradeUpRequest> premiumSubscriptions = gradeUpRequestRepository.findAll();
        return premiumSubscriptions.stream()
                .map(this::convertUserToRequestUserDTO)
                .collect(Collectors.toList());
    }

    private RequestUserDTO convertUserToRequestUserDTO(GradeUpRequest gradeUpRequest) {
        RequestUserDTO requestUserDTO = new RequestUserDTO();
        requestUserDTO.setId(gradeUpRequest.getId());
        requestUserDTO.setEmail(gradeUpRequest.getUser().getEmail());
        requestUserDTO.setCreatedAt(gradeUpRequest.getRequestTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        return requestUserDTO;
    }
}
