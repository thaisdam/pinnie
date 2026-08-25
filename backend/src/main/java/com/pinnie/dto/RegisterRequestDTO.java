package com.pinnie.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição para criação de conta")
public record RegisterRequestDTO(
        @Schema(description = "Nome de usuário único (apenas letras, números e underlines)", example = "maria_silva", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Username é obrigatório")
        @Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$", message = "Username deve ter entre 3 e 20 caracteres e conter apenas letras, números e underlines")
        String username,

        @Schema(description = "Endereço de e-mail (usado para login)", example = "maria@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail com formato inválido")
        String email,

        @Schema(description = "Senha de acesso (mínimo 6 caracteres)", example = "senha123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        String password,

        @Schema(description = "Nome visível no perfil", example = "Maria Silva", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Nome de exibição é obrigatório")
        String displayName
) {}
