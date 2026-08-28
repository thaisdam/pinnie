package com.pinnie.service;

import com.pinnie.dto.ReportRequestDTO;
import com.pinnie.model.Report;
import com.pinnie.model.ReportStatus;
import com.pinnie.model.TargetType;
import com.pinnie.model.User;
import com.pinnie.repository.PinRepository;
import com.pinnie.repository.ReportRepository;
import com.pinnie.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PinRepository pinRepository;

    @InjectMocks
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createReport_SelfReportUser_ThrowsException() {
        UUID reporterId = UUID.randomUUID();
        ReportRequestDTO request = new ReportRequestDTO(TargetType.USER, reporterId, "Spam");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
            reportService.createReport(reporterId, request)
        );
        assertEquals("Você não pode denunciar a si mesmo.", ex.getMessage());
    }

    @Test
    void createReport_DuplicatePending_ThrowsException() {
        UUID reporterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        ReportRequestDTO request = new ReportRequestDTO(TargetType.PIN, targetId, "Inapropriado");

        when(pinRepository.existsById(targetId)).thenReturn(true);
        when(reportRepository.existsByReporterIdAndTargetTypeAndTargetIdAndStatus(
                reporterId, TargetType.PIN, targetId, ReportStatus.PENDING)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> 
            reportService.createReport(reporterId, request)
        );
        assertEquals("Você já possui uma denúncia pendente para este conteúdo.", ex.getMessage());
    }

    @Test
    void createReport_Valid_Success() {
        UUID reporterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        ReportRequestDTO request = new ReportRequestDTO(TargetType.PIN, targetId, "Inapropriado");
        
        User reporter = new User();
        
        when(pinRepository.existsById(targetId)).thenReturn(true);
        when(reportRepository.existsByReporterIdAndTargetTypeAndTargetIdAndStatus(
                reporterId, TargetType.PIN, targetId, ReportStatus.PENDING)).thenReturn(false);
        when(userRepository.findById(reporterId)).thenReturn(Optional.of(reporter));
        
        Report mockSaved = new Report(reporter, TargetType.PIN, targetId, "Inapropriado");
        when(reportRepository.save(any(Report.class))).thenReturn(mockSaved);

        Report result = reportService.createReport(reporterId, request);
        
        assertNotNull(result);
        assertEquals("Inapropriado", result.getReason());
        verify(reportRepository, times(1)).save(any(Report.class));
    }
}
