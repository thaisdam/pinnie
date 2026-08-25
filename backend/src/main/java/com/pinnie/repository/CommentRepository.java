package com.pinnie.repository;

import com.pinnie.model.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    Slice<Comment> findByPinIdOrderByCreatedAtDesc(UUID pinId, Pageable pageable);
}
