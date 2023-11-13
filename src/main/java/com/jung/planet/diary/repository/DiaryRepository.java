package com.jung.planet.diary.repository;

import com.jung.planet.diary.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    @Query("SELECT d FROM Diary d JOIN d.plant p WHERE d.id = :diaryId AND p.user.id = :userId")
    Optional<Diary> findByIdAndUserId(@Param("diaryId") Long diaryId, @Param("userId") Long userId);

}
