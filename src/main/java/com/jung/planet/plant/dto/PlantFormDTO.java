package com.jung.planet.plant.dto;

import com.jung.planet.plant.entity.Plant;
import com.jung.planet.user.entity.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlantFormDTO {
    private Long userId;
    private String nickName;
    private String scientificName;
    private String img;


    public Plant toEntity(User user, String imgUrl) {
        return Plant.builder().user(user).nickName(nickName).scientificName(scientificName).imgUrl(imgUrl).build();
    }
}
