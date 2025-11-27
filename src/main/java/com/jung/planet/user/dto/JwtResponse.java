package com.jung.planet.user.dto;

import com.jung.planet.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class JwtResponse {
    private String access_token;
    private String refresh_token;
    private User user;
}

