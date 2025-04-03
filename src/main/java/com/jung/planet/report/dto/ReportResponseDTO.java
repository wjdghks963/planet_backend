package com.jung.planet.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDTO {
    private List<ReportDTO> reportedPlants;
    private List<ReportDTO> reportedDiaries;
} 