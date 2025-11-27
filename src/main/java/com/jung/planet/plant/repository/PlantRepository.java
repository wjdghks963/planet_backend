package com.jung.planet.plant.repository;

import com.jung.planet.plant.entity.Plant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantRepository extends JpaRepository<Plant, Long> {
    List<Plant> findByUserId(Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);

    Page<Plant> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 랜덤으로 5개의 식물을 조회합니다.
     * 데이터베이스 독립적인 방식으로 구현하기 위해 애플리케이션 레벨에서 랜덤 처리를 권장하지만,
     * 성능을 위해 네이티브 쿼리를 유지합니다.
     */
    @Query(value = "SELECT * FROM plant ORDER BY RAND() LIMIT 5", nativeQuery = true)
    List<Plant> findTop5ByRandom();

    /**
     * 사용자가 소유한 모든 식물의 하트 개수 합계를 계산합니다.
     */
    @Query("SELECT SUM(p.heartCount) FROM Plant p WHERE p.user.id = :userId")
    Integer sumHeartCountByUserId(@Param("userId") Long userId);
}
