package com.jung.planet.report.service;

import com.jung.planet.admin.service.SlackNotificationService;
import com.jung.planet.diary.dto.DiaryDetailDTO;
import com.jung.planet.diary.entity.Diary;
import com.jung.planet.diary.repository.DiaryRepository;
import com.jung.planet.diary.service.DiaryService;
import com.jung.planet.plant.dto.PlantDetailDTO;
import com.jung.planet.plant.entity.Plant;
import com.jung.planet.plant.repository.PlantRepository;
import com.jung.planet.plant.service.PlantService;
import com.jung.planet.report.dto.ReportDTO;
import com.jung.planet.report.entity.Report;
import com.jung.planet.report.entity.ReportType;
import com.jung.planet.report.repository.ReportRepository;
import com.jung.planet.user.entity.User;
import com.jung.planet.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private PlantRepository plantRepository;

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlantService plantService;

    @Mock
    private DiaryService diaryService;

    @Mock
    private SlackNotificationService slackNotificationService;

    @InjectMocks
    private ReportService reportService;

    private Plant plant;
    private Diary diary;
    private User reporter;
    private User plantOwner;
    private Report plantReport;
    private Report diaryReport;

    @BeforeEach
    void setUp() {
        // Setup test data
        plantOwner = User.builder()
                .id(1L)
                .email("owner@example.com")
                .name("Plant Owner")
                .build();

        reporter = User.builder()
                .id(2L)
                .email("reporter@example.com")
                .name("Reporter")
                .build();

        plant = Plant.builder()
                .id(1L)
                .nickName("Test Plant")
                .imgUrl("http://example.com/plant.jpg")
                .user(plantOwner)
                .build();

        diary = Diary.builder()
                .id(1L)
                .content("Test Diary Content")
                .imgUrl("http://example.com/diary.jpg")
                .plant(plant)
                .build();

        plantReport = new Report();
        plantReport.setId(1L);
        plantReport.setReportedPlant(plant);
        plantReport.setReporterId(2L);

        diaryReport = new Report();
        diaryReport.setId(2L);
        diaryReport.setReportedDiary(diary);
        diaryReport.setReporterId(2L);
    }

    @Test
    @DisplayName("식물 신고 - 성공")
    void testReportEntity_Plant_Success() {
        // Given
        Long plantId = 1L;
        Long reporterId = 2L;

        when(plantRepository.findById(plantId)).thenReturn(Optional.of(plant));
        when(reportRepository.save(any(Report.class))).thenReturn(plantReport);
        doNothing().when(slackNotificationService).sendSlackReportNotification(anyString(), anyMap());

        // When
        reportService.reportEntity(plantId, ReportType.PLANT, reporterId);

        // Then
        verify(plantRepository).findById(plantId);
        verify(reportRepository).save(any(Report.class));
        verify(slackNotificationService).sendSlackReportNotification(eq("식물 신고"), anyMap());
    }

    @Test
    @DisplayName("식물 신고 - 식물을 찾을 수 없음")
    void testReportEntity_Plant_NotFound() {
        // Given
        Long plantId = 999L;
        Long reporterId = 2L;

        when(plantRepository.findById(plantId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class,
            () -> reportService.reportEntity(plantId, ReportType.PLANT, reporterId));

        verify(plantRepository).findById(plantId);
        verify(reportRepository, never()).save(any(Report.class));
        verify(slackNotificationService, never()).sendSlackReportNotification(anyString(), anyMap());
    }

    @Test
    @DisplayName("다이어리 신고 - 성공")
    void testReportEntity_Diary_Success() {
        // Given
        Long diaryId = 1L;
        Long reporterId = 2L;

        when(diaryRepository.findById(diaryId)).thenReturn(Optional.of(diary));
        when(reportRepository.save(any(Report.class))).thenReturn(diaryReport);
        doNothing().when(slackNotificationService).sendSlackReportNotification(anyString(), anyMap());

        // When
        reportService.reportEntity(diaryId, ReportType.DIARY, reporterId);

        // Then
        verify(diaryRepository).findById(diaryId);
        verify(reportRepository).save(any(Report.class));
        verify(slackNotificationService).sendSlackReportNotification(eq("다이어리 신고"), anyMap());
    }

    @Test
    @DisplayName("다이어리 신고 - 다이어리를 찾을 수 없음")
    void testReportEntity_Diary_NotFound() {
        // Given
        Long diaryId = 999L;
        Long reporterId = 2L;

        when(diaryRepository.findById(diaryId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class,
            () -> reportService.reportEntity(diaryId, ReportType.DIARY, reporterId));

        verify(diaryRepository).findById(diaryId);
        verify(reportRepository, never()).save(any(Report.class));
        verify(slackNotificationService, never()).sendSlackReportNotification(anyString(), anyMap());
    }

    @Test
    @DisplayName("모든 다이어리 신고 조회 - fetch join으로 N+1 문제 해결")
    void testGetAllDiaryReports_WithFetchJoin() {
        // Given
        List<Report> reports = Arrays.asList(diaryReport);
        DiaryDetailDTO diaryDetailDTO = new DiaryDetailDTO();

        when(reportRepository.findAllDiaryReportsWithUser()).thenReturn(reports);
        when(diaryService.findDiary(anyLong(), anyLong())).thenReturn(diaryDetailDTO);
        when(userRepository.findById(2L)).thenReturn(Optional.of(reporter));

        // When
        List<ReportDTO> result = reportService.getAllDiaryReports();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reportRepository).findAllDiaryReportsWithUser(); // fetch join 쿼리 사용 확인
        verify(diaryService).findDiary(anyLong(), anyLong());
        verify(userRepository).findById(2L);
    }

    @Test
    @DisplayName("모든 식물 신고 조회 - fetch join으로 N+1 문제 해결")
    void testGetAllPlantReports_WithFetchJoin() {
        // Given
        List<Report> reports = Arrays.asList(plantReport);
        PlantDetailDTO plantDetailDTO = new PlantDetailDTO();

        when(reportRepository.findAllPlantReportsWithUser()).thenReturn(reports);
        when(plantService.getPlantDetailsByPlantId(anyLong(), anyLong())).thenReturn(plantDetailDTO);
        when(userRepository.findById(2L)).thenReturn(Optional.of(reporter));

        // When
        List<ReportDTO> result = reportService.getAllPlantReports();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reportRepository).findAllPlantReportsWithUser(); // fetch join 쿼리 사용 확인
        verify(plantService).getPlantDetailsByPlantId(anyLong(), anyLong());
        verify(userRepository).findById(2L);
    }

    @Test
    @DisplayName("다이어리 신고 조회 - 신고자를 찾을 수 없음")
    void testGetAllDiaryReports_ReporterNotFound() {
        // Given
        List<Report> reports = Arrays.asList(diaryReport);

        when(reportRepository.findAllDiaryReportsWithUser()).thenReturn(reports);
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> reportService.getAllDiaryReports());

        verify(reportRepository).findAllDiaryReportsWithUser();
        verify(userRepository).findById(2L);
    }

    @Test
    @DisplayName("식물 신고 조회 - 신고자를 찾을 수 없음")
    void testGetAllPlantReports_ReporterNotFound() {
        // Given
        List<Report> reports = Arrays.asList(plantReport);

        when(reportRepository.findAllPlantReportsWithUser()).thenReturn(reports);
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> reportService.getAllPlantReports());

        verify(reportRepository).findAllPlantReportsWithUser();
        verify(userRepository).findById(2L);
    }
}
