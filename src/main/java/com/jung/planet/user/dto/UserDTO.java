package com.jung.planet.user.dto;

import com.jung.planet.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserDTO {
    private final String email;
    private final String name;

    // 기본 생성자, getter 생략

    @Builder
    public UserDTO(String email, String name) {
        this.email = email;
        this.name = name;
    }

    public User toEntity() {
        return User.builder()
                .email(email)
                .name(name)
                .build();
    }
}
