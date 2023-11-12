package com.jung.planet.user.dto;

import com.jung.planet.user.entity.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtResponse {
    private String access_token;
    private User user;

    public JwtResponse(String token, User user) {
        this.access_token = token;
        this.user = user;
    }
}

