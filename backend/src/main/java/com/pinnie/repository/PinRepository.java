package com.pinnie.repository;

import com.pinnie.model.Pin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PinRepository extends JpaRepository<Pin, UUID> {
    Page<Pin> findByUserId(UUID userId, Pageable pageable);
    Slice<Pin> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Slice<Pin> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByCreatedAtDesc(String title, String description, Pageable pageable);
    Optional<Pin> findByIdAndUserId(UUID id, UUID userId);
}
