package com.pinnie.dto;

import com.pinnie.model.Report;
import com.pinnie.model.ReportStatus;
import com.pinnie.model.TargetType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Dados de uma denúncia")
public record ReportResponseDTO(
        UUID id,
        UserResponseDTO reporter,
        TargetType targetType,
        UUID targetId,
        String reason,
        ReportStatus status,
        Instant createdAt,
        Instant resolvedAt,
        UserResponseDTO resolvedBy
) {
    public static ReportResponseDTO fromEntity(Report report) {
        return new ReportResponseDTO(
                report.getId(),
                UserResponseDTO.fromEntity(report.getReporter()),
                report.getTargetType(),
                report.getTargetId(),
                report.getReason(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getResolvedAt(),
                report.getResolvedBy() != null ? UserResponseDTO.fromEntity(report.getResolvedBy()) : null
        );
    }
}
