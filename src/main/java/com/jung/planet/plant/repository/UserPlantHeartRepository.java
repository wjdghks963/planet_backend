package com.jung.planet.plant.repository;

import com.jung.planet.plant.entity.UserPlantHeart;
import com.jung.planet.plant.entity.UserPlantHeartId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPlantHeartRepository extends JpaRepository<UserPlantHeart, UserPlantHeartId> {
    Optional<UserPlantHeart> findByUserIdAndPlantId(Long userId, Long plantId);

    // 사용자 ID를 기준으로 누른 하트의 총 개수를 반환
    int countByUserId(Long userId);

    Page<UserPlantHeart> findByUserId(Long userId, Pageable pageable);

    void deleteByUserId(Long userId);

}
