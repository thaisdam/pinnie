package com.pinnie.dto;

import com.pinnie.model.Board;

import java.time.Instant;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados públicos de uma Pasta (Board)")
public class BoardResponseDTO {

    @Schema(description = "ID da Pasta")
    private UUID id;
    @Schema(description = "Nome da Pasta")
    private String name;
    @Schema(description = "Descrição")
    private String description;
    @Schema(description = "Indica se a pasta é privada")
    @com.fasterxml.jackson.annotation.JsonProperty("isPrivate")
    private boolean isPrivate;
    @Schema(description = "ID do dono da pasta")
    private UUID userId;
    @Schema(description = "Data de criação")
    private Instant createdAt;
    @Schema(description = "Data de última atualização")
    private Instant updatedAt;

    public BoardResponseDTO() {}

    public BoardResponseDTO(Board board) {
        this.id = board.getId();
        this.name = board.getName();
        this.description = board.getDescription();
        this.isPrivate = board.isPrivate();
        this.userId = board.getUser() != null ? board.getUser().getId() : null;
        this.createdAt = board.getCreatedAt();
        this.updatedAt = board.getUpdatedAt();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
