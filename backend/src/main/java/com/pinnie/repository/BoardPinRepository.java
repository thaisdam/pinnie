package com.pinnie.repository;

import com.pinnie.model.BoardPin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BoardPinRepository extends JpaRepository<BoardPin, UUID> {
    
    boolean existsByBoardIdAndPinId(UUID boardId, UUID pinId);
    
    Optional<BoardPin> findByBoardIdAndPinId(UUID boardId, UUID pinId);
    
    Page<BoardPin> findByBoardId(UUID boardId, Pageable pageable);
}
