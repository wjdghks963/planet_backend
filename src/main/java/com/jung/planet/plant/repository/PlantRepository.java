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

    boolean existsByIdAndUserId(Long plantId, Long userId);

    Page<Plant> findAllByOrderByCreatedAtDesc(Pageable pageable);


    @Query(value = "SELECT * FROM plant ORDER BY RAND() LIMIT 5", nativeQuery = true)
    List<Plant> findRandomPlants();


    // 사용자 ID를 기반으로 하트의 총합을 계산하는 쿼리
    @Query("SELECT SUM(p.heartCount) FROM Plant p WHERE p.user.id = :userId")
    Integer sumHeartsByUserId(@Param("userId") Long userId);
}
