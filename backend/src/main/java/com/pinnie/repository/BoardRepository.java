package com.pinnie.repository;

import com.pinnie.model.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BoardRepository extends JpaRepository<Board, UUID> {
    Page<Board> findByUserId(UUID userId, Pageable pageable);
    Page<Board> findByUserIdAndIsPrivateFalse(UUID userId, Pageable pageable);
}
