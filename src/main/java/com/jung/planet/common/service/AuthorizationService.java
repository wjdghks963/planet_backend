package com.jung.planet.common.service;

import com.jung.planet.exception.ResourceNotFoundException;
import com.jung.planet.plant.entity.Plant;
import com.jung.planet.plant.repository.PlantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 권한 검사를 담당하는 공통 서비스
 */
@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final PlantRepository plantRepository;

    /**
     * 사용자가 특정 식물의 소유자인지 확인합니다.
     *
     * @param userId 사용자 ID
     * @param plantId 식물 ID
     * @return 소유자 여부
     * @throws ResourceNotFoundException 식물을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public boolean isOwnerOfPlant(Long userId, Long plantId) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new ResourceNotFoundException("Plant", "id", plantId));

        return plant.getUser().getId().equals(userId);
    }
}
