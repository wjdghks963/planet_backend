package com.jung.planet.user.controller;

import com.jung.planet.security.JwtTokenProvider;
import com.jung.planet.user.dto.JwtResponse;
import com.jung.planet.user.dto.UserDTO;
import com.jung.planet.user.entity.User;
import com.jung.planet.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Slf4j
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;


    @PostMapping("/login")
    public ResponseEntity<?> getCurrentUser(@RequestBody UserDTO userDTO) {
        User user = userService.processUser(userDTO);
        String access_token = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());

        return ResponseEntity.ok(new JwtResponse(access_token, user.getRefreshToken(), user));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshTokens(@RequestParam String refreshToken) {
        String email = jwtTokenProvider.decodeJwt(refreshToken).getSubject();
        Optional<User> user = userService.findByEmail(email);

        if (user.isPresent() && user.get().getRefreshToken().equals(refreshToken) && jwtTokenProvider.validateToken(refreshToken)) {
            String newAccessToken = jwtTokenProvider.createAccessToken(user.get().getId(), user.get().getEmail());
            String newRefreshToken = jwtTokenProvider.createRefreshToken(user.get().getId(), user.get().getEmail());

            // Update user's refresh token
            userService.updateRefreshToken(user.get().getId(), newRefreshToken);

            Map<String, String> tokens = new HashMap<>();
            tokens.put("access_token", newAccessToken);
            tokens.put("refresh_token", newRefreshToken);
            return ResponseEntity.ok(tokens);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid Refresh Token");
        }
    }

}
