package com.pinnie.dto;

import java.util.UUID;

public class ImageUploadResponseDTO {

    private UUID uploadId;
    private int width;
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
