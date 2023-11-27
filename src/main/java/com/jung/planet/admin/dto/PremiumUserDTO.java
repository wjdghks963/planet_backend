package com.jung.planet.admin.dto;

import com.jung.planet.user.entity.Subscription;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class PremiumUserDTO {
    private String email;
    private String name;
    private Subscription subscription;


}
