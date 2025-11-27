package com.jung.planet.report.repository;

import com.jung.planet.report.entity.Report;
import com.jung.planet.report.entity.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByReportedPlantIsNotNull();
    List<Report> findByReportedDiaryIsNotNull();

    /**
     * N+1 문제 해결을 위해 fetch join을 사용하여 Plant와 User를 함께 조회합니다.
     */
    @Query("SELECT r FROM Report r " +
            "JOIN FETCH r.reportedPlant p " +
            "JOIN FETCH p.user " +
            "WHERE r.reportedPlant IS NOT NULL")
    List<Report> findPlantReportsWithUserFetchJoin();

    /**
     * N+1 문제 해결을 위해 fetch join을 사용하여 Diary, Plant, User를 함께 조회합니다.
     */
    @Query("SELECT r FROM Report r " +
            "JOIN FETCH r.reportedDiary d " +
            "JOIN FETCH d.plant p " +
            "JOIN FETCH p.user " +
            "WHERE r.reportedDiary IS NOT NULL")
    List<Report> findDiaryReportsWithUserFetchJoin();
}

