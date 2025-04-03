package com.jung.planet.admin.dto.response;

import com.jung.planet.admin.dto.PremiumUserDTO;
import com.jung.planet.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminResponseDTO {
    private User user;
    private String message;
    private List<PremiumUserDTO> premiumUsers;
} 