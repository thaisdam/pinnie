package com.pinnie.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição de autenticação")
public record LoginRequestDTO(
        @Schema(description = "E-mail do usuário", example = "maria@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail com formato inválido")
        String email,

        @Schema(description = "Senha do usuário", example = "senha123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Senha é obrigatória")
        String password
) {}
