package com.pinnie.dto;

import com.pinnie.model.Pin;

import java.time.Instant;
import java.util.UUID;

public class PinResponseDTO {

    private UUID id;
    private String title;
    private String description;
    private String link;
    private String altText;
    private UUID userId;
    private String imageUrl;
    private int imageWidth;
    private int imageHeight;
    private Instant createdAt;
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
