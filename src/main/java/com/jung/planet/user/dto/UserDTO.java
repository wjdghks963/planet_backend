package com.jung.planet.user.dto;

import com.jung.planet.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserDTO {
    private  String email;
    private  String name;
    private  String imgUrl;


    public User toEntity() {
        return User.builder()
                .email(email)
                .name(name)
                .build();
    }
}
