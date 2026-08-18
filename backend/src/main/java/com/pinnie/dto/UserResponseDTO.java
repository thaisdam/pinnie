package com.pinnie.dto;

import com.pinnie.model.User;
import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String username,
        String email,
        String displayName,
        String bio,
        String avatarUrl,
        boolean enabled
) {
    public static UserResponseDTO fromEntity(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getBio(),
                user.getAvatarUrl(),
                user.isEnabled()
        );
    }
}
