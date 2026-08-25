package com.pinnie.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição para criação final de um Pin (Etapa 2 do Upload)")
public class PinCreateRequestDTO {

    @Schema(description = "Título do Pin", example = "Receita de Bolo", maxLength = 100)
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    @Schema(description = "Descrição detalhada do Pin", example = "Bolo de chocolate super fofinho feito no liquidificador.")
    private String description;

    @Schema(description = "Link de destino (opcional)", example = "https://meublog.com/bolo")
    @Pattern(regexp = "^(https?://.*)?$", message = "Link must be a valid HTTP/HTTPS URL or empty")
    @Size(max = 2048, message = "Link must not exceed 2048 characters")
    private String link;

    @Schema(description = "Texto alternativo para acessibilidade", example = "Bolo de chocolate com cobertura derretida", maxLength = 255)
    @Size(max = 255, message = "Alt text must not exceed 255 characters")
    private String altText;

    @Schema(description = "ID do upload retornado na Etapa 1. Obrigatório para vincular a imagem ao Pin.", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Upload ID is required")
    private UUID uploadId;

    public PinCreateRequestDTO() {}

    public PinCreateRequestDTO(String title, String description, String link, String altText, UUID uploadId) {
        this.title = title;
        this.description = description;
        this.link = link;
        this.altText = altText;
        this.uploadId = uploadId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public UUID getUploadId() {
        return uploadId;
    }

    public void setUploadId(UUID uploadId) {
        this.uploadId = uploadId;
    }
}
