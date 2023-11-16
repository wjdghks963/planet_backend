package com.jung.planet.plant.repository;

import com.jung.planet.plant.entity.Plant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantRepository extends JpaRepository<Plant, Long> {
    List<Plant> findByUserId(Long userId);

    boolean existsByIdAndUserId(Long plantId, Long userId);

    Page<Plant> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query(value = "SELECT * FROM plant ORDER BY RAND() LIMIT 5", nativeQuery = true)
    List<Plant> findRandomPlants();

}
