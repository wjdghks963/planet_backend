package com.jung.planet.report.repository;

import com.jung.planet.report.entity.Report;
import com.jung.planet.report.entity.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByEntityType(ReportType entityType);

}

