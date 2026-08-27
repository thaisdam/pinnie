package com.pinnie.controller;

import com.pinnie.service.UserFollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/{userId}/follow")
@Tag(name = "User Follows", description = "Endpoints para seguir e deixar de seguir usuários")
public class UserFollowController {

    private final UserFollowService userFollowService;

    public UserFollowController(UserFollowService userFollowService) {
        this.userFollowService = userFollowService;
    }

    @PostMapping
    @Operation(summary = "Seguir um usuário")
    public ResponseEntity<Void> followUser(@PathVariable UUID userId, @AuthenticationPrincipal UserDetails userDetails) {
        UUID followerId = UUID.fromString(userDetails.getUsername());
        userFollowService.followUser(followerId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    @Operation(summary = "Deixar de seguir um usuário")
    public ResponseEntity<Void> unfollowUser(@PathVariable UUID userId, @AuthenticationPrincipal UserDetails userDetails) {
        UUID followerId = UUID.fromString(userDetails.getUsername());
        userFollowService.unfollowUser(followerId, userId);
        return ResponseEntity.noContent().build();
    }
}
