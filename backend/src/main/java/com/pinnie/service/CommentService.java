package com.pinnie.service;

import com.pinnie.exception.ResourceNotFoundException;
import com.pinnie.model.Comment;
import com.pinnie.model.Pin;
import com.pinnie.model.User;
import com.pinnie.repository.CommentRepository;
import com.pinnie.repository.PinRepository;
import com.pinnie.repository.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PinRepository pinRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, PinRepository pinRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.pinRepository = pinRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Slice<Comment> getCommentsByPin(UUID pinId, Pageable pageable) {
        if (!pinRepository.existsById(pinId)) {
            throw new ResourceNotFoundException("Pin not found");
        }
        return commentRepository.findByPinIdOrderByCreatedAtDesc(pinId, pageable);
    }

    @Transactional
    public Comment createComment(UUID pinId, UUID userId, String text) {
        if (userId == null) {
            throw new AccessDeniedException("User must be authenticated");
        }
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be blank");
        }
        if (text.length() > 500) {
            throw new IllegalArgumentException("Text must not exceed 500 characters");
        }

        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new ResourceNotFoundException("Pin not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        Comment comment = new Comment(pin, user, text);
        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(UUID pinId, UUID commentId, UUID userId) {
        if (userId == null) {
            throw new AccessDeniedException("User must be authenticated");
        }

        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new ResourceNotFoundException("Pin not found"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getPin().getId().equals(pin.getId())) {
            throw new ResourceNotFoundException("Comment does not belong to the specified Pin");
        }

        boolean isCommentAuthor = comment.getUser().getId().equals(userId);
        boolean isPinOwner = pin.getUser().getId().equals(userId);

        if (!isCommentAuthor && !isPinOwner) {
            throw new AccessDeniedException("User does not have permission to delete this comment");
        }

        commentRepository.delete(comment);
    }
}
