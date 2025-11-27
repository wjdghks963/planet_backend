package com.jung.planet.admin.mapper;

import com.jung.planet.admin.dto.PremiumUserDTO;
import com.jung.planet.user.entity.User;
import org.springframework.stereotype.Component;

/**
 * Admin 관련 엔티티와 DTO 간 변환을 담당하는 Mapper 클래스
 */
@Component
public class AdminMapper {

    /**
     * User 엔티티를 PremiumUserDTO로 변환합니다.
     *
     * @param user User 엔티티
     * @return PremiumUserDTO
     */
    public PremiumUserDTO toPremiumUserDto(User user) {
        PremiumUserDTO premiumUserDTO = new PremiumUserDTO();
        premiumUserDTO.setEmail(user.getEmail());
        premiumUserDTO.setName(user.getName());
        premiumUserDTO.setSubscription(user.getSubscription());
        return premiumUserDTO;
    }
}
