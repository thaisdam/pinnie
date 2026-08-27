package com.pinnie.service;

import com.pinnie.exception.DuplicateResourceException;
import com.pinnie.exception.ResourceNotFoundException;
import com.pinnie.model.User;
import com.pinnie.model.UserFollow;
import com.pinnie.repository.UserFollowRepository;
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

class UserFollowServiceTest {

    @Mock
    private UserFollowRepository userFollowRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserFollowService userFollowService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void followUser_Success() {
        UUID followerId = UUID.randomUUID();
        UUID followingId = UUID.randomUUID();

        when(userFollowRepository.existsByFollowerIdAndFollowingId(followerId, followingId)).thenReturn(false);
        when(userRepository.findById(followerId)).thenReturn(Optional.of(new User()));
        when(userRepository.findById(followingId)).thenReturn(Optional.of(new User()));

        userFollowService.followUser(followerId, followingId);

        verify(userFollowRepository, times(1)).save(any(UserFollow.class));
    }

    @Test
    void followUser_SelfFollow_ThrowsException() {
        UUID sameId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> userFollowService.followUser(sameId, sameId));
    }

    @Test
    void followUser_AlreadyFollowing_ThrowsException() {
        UUID followerId = UUID.randomUUID();
        UUID followingId = UUID.randomUUID();

        when(userFollowRepository.existsByFollowerIdAndFollowingId(followerId, followingId)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userFollowService.followUser(followerId, followingId));
    }

    @Test
    void followUser_UserNotFound_ThrowsException() {
        UUID followerId = UUID.randomUUID();
        UUID followingId = UUID.randomUUID();

        when(userFollowRepository.existsByFollowerIdAndFollowingId(followerId, followingId)).thenReturn(false);
        when(userRepository.findById(followerId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userFollowService.followUser(followerId, followingId));
    }

    @Test
    void unfollowUser_Success() {
        UUID followerId = UUID.randomUUID();
        UUID followingId = UUID.randomUUID();

        when(userFollowRepository.existsByFollowerIdAndFollowingId(followerId, followingId)).thenReturn(true);

        userFollowService.unfollowUser(followerId, followingId);

        verify(userFollowRepository, times(1)).deleteByFollowerIdAndFollowingId(followerId, followingId);
    }
}
