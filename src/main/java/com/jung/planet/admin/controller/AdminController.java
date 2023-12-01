package com.jung.planet.admin.controller;


import com.jung.planet.admin.dto.PremiumUserDTO;
import com.jung.planet.admin.service.AdminService;
import com.jung.planet.security.JwtTokenProvider;
import com.jung.planet.security.UserDetail.CustomUserDetails;
import com.jung.planet.user.dto.JwtResponse;
import com.jung.planet.user.dto.UserDTO;
import com.jung.planet.user.entity.User;
import com.jung.planet.user.entity.UserRole;
import com.jung.planet.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AdminService adminService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> getAdminUser(@RequestBody UserDTO userDTO) {
        User user = userService.adminUser(userDTO);
        String access_token = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole());

        return ResponseEntity.ok(new JwtResponse(access_token, user.getRefreshToken(), user));
    }


    @PostMapping("/upgrade")
    public ResponseEntity<?> upgradeSubscription(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody UserDTO userDTO) {
        if (customUserDetails.getUserRole().equals(UserRole.ADMIN)) {
            User user = userService.upgradeUserSubscription(userDTO.getEmail());

            return ResponseEntity.ok(user);
        } else {
            throw new AccessDeniedException("권한이 없습니다.");
        }
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<?> getAllPremiumUsers() {
        List<PremiumUserDTO> premiumUsers = adminService.getAllPremiumUsers();
        return ResponseEntity.ok(premiumUsers);
    }


}
