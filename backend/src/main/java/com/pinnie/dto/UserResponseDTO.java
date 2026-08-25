package com.pinnie.dto;

import com.pinnie.model.User;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados públicos do Usuário")
public record UserResponseDTO(
        @Schema(description = "ID único do usuário")
        UUID id,
        @Schema(description = "Username único", example = "maria_silva")
        String username,
        @Schema(description = "E-mail do usuário", example = "maria@example.com")
        String email,
        @Schema(description = "Nome de exibição", example = "Maria Silva")
        String displayName,
        @Schema(description = "Biografia do usuário", example = "Amante de receitas e tecnologia.")
        String bio,
        @Schema(description = "URL do avatar do usuário")
        String avatarUrl,
        @Schema(description = "Indica se a conta está ativada")
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
