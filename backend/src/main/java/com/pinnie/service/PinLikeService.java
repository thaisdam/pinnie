package com.pinnie.service;

import com.pinnie.exception.DuplicateResourceException;
import com.pinnie.exception.ResourceNotFoundException;
import com.pinnie.model.Pin;
import com.pinnie.model.PinLike;
import com.pinnie.model.User;
import com.pinnie.repository.PinLikeRepository;
import com.pinnie.repository.PinRepository;
import com.pinnie.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PinLikeService {

    private final PinLikeRepository pinLikeRepository;
    private final PinRepository pinRepository;
    private final UserRepository userRepository;

    public PinLikeService(PinLikeRepository pinLikeRepository, PinRepository pinRepository, UserRepository userRepository) {
        this.pinLikeRepository = pinLikeRepository;
        this.pinRepository = pinRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void likePin(UUID pinId, UUID userId) {
        if (pinLikeRepository.existsByPinIdAndUserId(pinId, userId)) {
            throw new DuplicateResourceException("O usuário já curtiu este Pin.");
        }

        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new ResourceNotFoundException("Pin não encontrado"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        PinLike like = new PinLike(pin, user);
        pinLikeRepository.save(like);
    }

    @Transactional
    public void unlikePin(UUID pinId, UUID userId) {
        if (pinLikeRepository.existsByPinIdAndUserId(pinId, userId)) {
            pinLikeRepository.deleteByPinIdAndUserId(pinId, userId);
        }
    }
}
