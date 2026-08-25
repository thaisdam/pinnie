package com.pinnie.dto;

import com.pinnie.model.Pin;

import java.time.Instant;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados públicos de um Pin")
public class PinResponseDTO {

    @Schema(description = "ID do Pin", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;
    @Schema(description = "Título do Pin", example = "Receita de Bolo")
    private String title;
    @Schema(description = "Descrição detalhada")
    private String description;
    @Schema(description = "URL externa")
    private String link;
    @Schema(description = "Texto alternativo para acessibilidade")
    private String altText;
    @Schema(description = "ID do usuário que publicou")
    private UUID userId;
    @Schema(description = "URL relativa da imagem", example = "/uploads/123-uuid.jpg")
    private String imageUrl;
    @Schema(description = "Largura da imagem em pixels", example = "1080")
    private int imageWidth;
    @Schema(description = "Altura da imagem em pixels", example = "1920")
    private int imageHeight;
    @Schema(description = "Data de criação")
    private Instant createdAt;
    @Schema(description = "Data de última atualização")
    private Instant updatedAt;

    public PinResponseDTO() {}

    public PinResponseDTO(Pin pin, String imageUrl) {
        this.id = pin.getId();
        this.title = pin.getTitle();
        this.description = pin.getDescription();
        this.link = pin.getLink();
        this.altText = pin.getAltText();
        this.userId = pin.getUser() != null ? pin.getUser().getId() : null;
        this.imageUrl = imageUrl;
        this.imageWidth = pin.getMediaWidth();
        this.imageHeight = pin.getMediaHeight();
        this.createdAt = pin.getCreatedAt();
        this.updatedAt = pin.getUpdatedAt();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
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
