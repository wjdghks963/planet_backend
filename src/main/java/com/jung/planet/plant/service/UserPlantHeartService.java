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

        UserPlantHeartId userPlantHeartId = UserPlantHeartId.builder().userId(userId).plantId(plantId).build();

        Optional<UserPlantHeart> userPlantHeart = userPlantHeartRepository.findById(userPlantHeartId);

        if (userPlantHeart.isPresent()) {
            // 좋아요가 이미 존재하면 삭제
            userPlantHeartRepository.delete(userPlantHeart.get());
            return false; // 좋아요 취소를 나타내는 false 반환
        } else {
            // 좋아요가 존재하지 않으면 추가
            UserPlantHeart newUserPlantHeart = new UserPlantHeart(user, plant);
            userPlantHeartRepository.save(newUserPlantHeart);
            return true; // 좋아요 설정을 나타내는 true 반환
        }
    }

}
