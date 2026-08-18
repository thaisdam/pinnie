package com.pinnie.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank(message = "Username é obrigatório")
        @Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$", message = "Username deve ter entre 3 e 20 caracteres e conter apenas letras, números e underlines")
        String username,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail com formato inválido")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        String password,

        @NotBlank(message = "Nome de exibição é obrigatório")
        String displayName
) {}
