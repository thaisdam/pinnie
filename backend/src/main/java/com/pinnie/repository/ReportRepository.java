package com.pinnie.repository;

import com.pinnie.model.Report;
import com.pinnie.model.ReportStatus;
import com.pinnie.model.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    boolean existsByReporterIdAndTargetTypeAndTargetIdAndStatus(UUID reporterId, TargetType targetType, UUID targetId, ReportStatus status);
    org.springframework.data.domain.Page<Report> findByStatus(ReportStatus status, org.springframework.data.domain.Pageable pageable);
}
