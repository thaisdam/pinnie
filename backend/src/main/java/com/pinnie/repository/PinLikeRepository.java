package com.pinnie.repository;

import com.pinnie.model.PinLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PinLikeRepository extends JpaRepository<PinLike, UUID> {
    
    long countByPinId(UUID pinId);
    
    boolean existsByPinIdAndUserId(UUID pinId, UUID userId);
    
    void deleteByPinIdAndUserId(UUID pinId, UUID userId);
}
