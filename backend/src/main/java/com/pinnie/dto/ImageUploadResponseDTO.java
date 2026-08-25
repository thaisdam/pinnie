package com.pinnie.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Resposta da Etapa 1 do Upload, contendo o ID temporário gerado para a imagem")
public class ImageUploadResponseDTO {

    @Schema(description = "ID temporário do upload. Deve ser enviado na Etapa 2 (Criação do Pin). Expira em minutos.", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID uploadId;

    @Schema(description = "Largura da imagem processada em pixels", example = "1080")
    private int width;

    @Schema(description = "Altura da imagem processada em pixels", example = "1920")
    private int height;

    public ImageUploadResponseDTO() {}

    public ImageUploadResponseDTO(UUID uploadId, int width, int height) {
        this.uploadId = uploadId;
        this.width = width;
        this.height = height;
    }

    public UUID getUploadId() {
        return uploadId;
    }

    public void setUploadId(UUID uploadId) {
        this.uploadId = uploadId;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
