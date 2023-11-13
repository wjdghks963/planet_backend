package com.jung.planet.user.dto;

import com.jung.planet.user.entity.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtResponse {
    private String access_token;
    private String refresh_token;
    private User user;

    public JwtResponse(String access_token,String refresh_token, User user) {
        this.access_token = access_token;
        this.refresh_token = refresh_token;
        this.user = user;
    }
}

