package com.pinnie.service;

import com.pinnie.exception.DuplicateResourceException;
import com.pinnie.exception.ResourceNotFoundException;
import com.pinnie.model.Pin;
import com.pinnie.model.PinLike;
import com.pinnie.model.User;
import com.pinnie.repository.PinLikeRepository;
import com.pinnie.repository.PinRepository;
import com.pinnie.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PinLikeServiceTest {

    @Mock
    private PinLikeRepository pinLikeRepository;

    @Mock
    private PinRepository pinRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PinLikeService pinLikeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void likePin_Success() {
        UUID pinId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(pinLikeRepository.existsByPinIdAndUserId(pinId, userId)).thenReturn(false);
        when(pinRepository.findById(pinId)).thenReturn(Optional.of(new Pin()));
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));

        pinLikeService.likePin(pinId, userId);

        verify(pinLikeRepository, times(1)).save(any(PinLike.class));
    }

    @Test
    void likePin_AlreadyLiked() {
        UUID pinId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(pinLikeRepository.existsByPinIdAndUserId(pinId, userId)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> pinLikeService.likePin(pinId, userId));
    }

    @Test
    void likePin_PinNotFound() {
        UUID pinId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(pinLikeRepository.existsByPinIdAndUserId(pinId, userId)).thenReturn(false);
        when(pinRepository.findById(pinId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pinLikeService.likePin(pinId, userId));
    }

    @Test
    void unlikePin_Success() {
        UUID pinId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(pinLikeRepository.existsByPinIdAndUserId(pinId, userId)).thenReturn(true);

        pinLikeService.unlikePin(pinId, userId);

        verify(pinLikeRepository, times(1)).deleteByPinIdAndUserId(pinId, userId);
    }
}
