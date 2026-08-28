package com.pinnie.service;

import com.pinnie.dto.ReportRequestDTO;
import com.pinnie.model.Report;
import com.pinnie.model.ReportStatus;
import com.pinnie.model.TargetType;
import com.pinnie.model.User;
import com.pinnie.repository.PinRepository;
import com.pinnie.repository.ReportRepository;
import com.pinnie.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PinRepository pinRepository;

    public ReportService(ReportRepository reportRepository, UserRepository userRepository, PinRepository pinRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.pinRepository = pinRepository;
    }

    @Transactional
    public Report createReport(UUID reporterId, ReportRequestDTO request) {
        if (request.targetType() == TargetType.USER && reporterId.equals(request.targetId())) {
            throw new IllegalArgumentException("Você não pode denunciar a si mesmo.");
        }

        if (request.targetType() == TargetType.USER) {
            if (!userRepository.existsById(request.targetId())) {
                throw new IllegalArgumentException("Usuário alvo não encontrado.");
            }
        } else if (request.targetType() == TargetType.PIN) {
            if (!pinRepository.existsById(request.targetId())) {
                throw new IllegalArgumentException("Pin alvo não encontrado.");
            }
        }

        boolean alreadyPending = reportRepository.existsByReporterIdAndTargetTypeAndTargetIdAndStatus(
                reporterId, request.targetType(), request.targetId(), ReportStatus.PENDING
        );

        if (alreadyPending) {
            throw new IllegalStateException("Você já possui uma denúncia pendente para este conteúdo.");
        }

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new RuntimeException("Reporter not found"));

        Report report = new Report(reporter, request.targetType(), request.targetId(), request.reason());
        return reportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<com.pinnie.dto.ReportResponseDTO> getPendingReports(org.springframework.data.domain.Pageable pageable) {
        return reportRepository.findByStatus(ReportStatus.PENDING, pageable).map(com.pinnie.dto.ReportResponseDTO::fromEntity);
    }

    @Transactional
    public com.pinnie.dto.ReportResponseDTO resolveReport(UUID reportId, ReportStatus status, UUID adminId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        User admin = userRepository.getReferenceById(adminId);
        
        report.setStatus(status);
        report.setResolvedBy(admin);
        report.setResolvedAt(java.time.Instant.now());
        
        return com.pinnie.dto.ReportResponseDTO.fromEntity(reportRepository.save(report));
    }
}
