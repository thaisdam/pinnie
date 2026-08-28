package com.pinnie.dto;

import com.pinnie.model.TargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Dados para criar uma denúncia")
public record ReportRequestDTO(
        @NotNull(message = "O tipo de alvo é obrigatório")
        @Schema(description = "Tipo do alvo (PIN ou USER)")
        TargetType targetType,

        @NotNull(message = "O ID do alvo é obrigatório")
        @Schema(description = "ID do Pin ou Usuário sendo denunciado")
        UUID targetId,

        @NotBlank(message = "O motivo da denúncia não pode estar em branco")
        @Size(max = 500, message = "O motivo deve ter no máximo 500 caracteres")
        @Schema(description = "Motivo da denúncia detalhado pelo usuário")
        String reason
) {}
