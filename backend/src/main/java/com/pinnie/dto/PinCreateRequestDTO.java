package com.pinnie.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class PinCreateRequestDTO {

    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    private String description;

    @Pattern(regexp = "^(https?://.*)?$", message = "Link must be a valid HTTP/HTTPS URL or empty")
    @Size(max = 2048, message = "Link must not exceed 2048 characters")
    private String link;

    @Size(max = 255, message = "Alt text must not exceed 255 characters")
    private String altText;

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
