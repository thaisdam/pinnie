package com.pinnie.repository;

import com.pinnie.model.Pin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PinRepository extends JpaRepository<Pin, UUID> {
    Page<Pin> findByUserId(UUID userId, Pageable pageable);
}
