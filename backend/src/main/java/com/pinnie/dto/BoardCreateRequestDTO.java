package com.pinnie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição para criação de uma Pasta")
public class BoardCreateRequestDTO {

    @Schema(description = "Nome da pasta", example = "Receitas", maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Name cannot be blank")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Schema(description = "Descrição opcional da pasta", example = "Minhas receitas favoritas")
    private String description;

    @Schema(description = "Define se a pasta é privada (apenas o dono pode ver) ou pública", example = "false")
    private boolean isPrivate;

    public BoardCreateRequestDTO() {}

    public BoardCreateRequestDTO(String name, String description, boolean isPrivate) {
        this.name = name;
        this.description = description;
        this.isPrivate = isPrivate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }
}
