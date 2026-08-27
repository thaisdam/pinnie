package com.pinnie.dto;

import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequestDTO(
        @Size(max = 50, message = "O nome de exibição não pode ultrapassar 50 caracteres")
        String displayName,
        
        @Size(max = 255, message = "A bio não pode ultrapassar 255 caracteres")
        String bio
) {}
