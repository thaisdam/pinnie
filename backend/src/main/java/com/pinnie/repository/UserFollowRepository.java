package com.pinnie.repository;

import com.pinnie.model.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, UUID> {

    long countByFollowerId(UUID followerId); // Quantas pessoas o usuário segue
    
    long countByFollowingId(UUID followingId); // Quantos seguidores o usuário tem
    
    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);
    
    void deleteByFollowerIdAndFollowingId(UUID followerId, UUID followingId);
}
