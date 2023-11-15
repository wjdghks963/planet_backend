package com.jung.planet.plant.service;


import com.jung.planet.plant.entity.Plant;
import com.jung.planet.plant.entity.UserPlantHeart;
import com.jung.planet.plant.entity.UserPlantHeartId;
import com.jung.planet.plant.repository.PlantRepository;
import com.jung.planet.plant.repository.UserPlantHeartRepository;
import com.jung.planet.user.entity.User;
import com.jung.planet.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserPlantHeartService {

    private final UserPlantHeartRepository userPlantHeartRepository;
    private final UserRepository userRepository;
    private final PlantRepository plantRepository;


    @Transactional
    public boolean togglePlantHeart(Long userId, Long plantId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new EntityNotFoundException("Plant not found"));

        UserPlantHeartId userPlantHeartId = new UserPlantHeartId(userId, plantId);
        Optional<UserPlantHeart> userPlantHeart = userPlantHeartRepository.findById(userPlantHeartId);

        if (userPlantHeart.isPresent()) {
            // 좋아요가 이미 존재하면 삭제하고 하트 카운트 감소
            userPlantHeartRepository.delete(userPlantHeart.get());
            plant.removeHeart();
        } else {
            // 좋아요가 존재하지 않으면 추가하고 하트 카운트 증가
            UserPlantHeart newUserPlantHeart = new UserPlantHeart(user, plant);
            userPlantHeartRepository.save(newUserPlantHeart);
            plant.addHeart();
        }

        // 변경된 plant 객체를 저장
        plantRepository.save(plant);

        return userPlantHeart.isEmpty();
    }




}
