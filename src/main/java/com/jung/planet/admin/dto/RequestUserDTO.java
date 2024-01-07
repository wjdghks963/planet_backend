package com.jung.planet.admin.dto;

import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class RequestUserDTO {

    private Long id;
    private String email;
    private String createdAt;
}