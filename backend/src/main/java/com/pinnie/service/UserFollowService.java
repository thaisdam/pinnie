package com.pinnie.service;

import com.pinnie.exception.DuplicateResourceException;
import com.pinnie.exception.ResourceNotFoundException;
import com.pinnie.model.User;
import com.pinnie.model.UserFollow;
import com.pinnie.repository.UserFollowRepository;
import com.pinnie.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserFollowService {

    private final UserFollowRepository userFollowRepository;
    private final UserRepository userRepository;

    public UserFollowService(UserFollowRepository userFollowRepository, UserRepository userRepository) {
        this.userFollowRepository = userFollowRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void followUser(UUID followerId, UUID followingId) {
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("Um usuário não pode seguir a si mesmo.");
        }

        if (userFollowRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new DuplicateResourceException("Você já segue este usuário.");
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário (follower) não encontrado"));
                
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário (following) não encontrado"));

        UserFollow follow = new UserFollow(follower, following);
        userFollowRepository.save(follow);
    }

    @Transactional
    public void unfollowUser(UUID followerId, UUID followingId) {
        if (userFollowRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            userFollowRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
        }
    }
}
