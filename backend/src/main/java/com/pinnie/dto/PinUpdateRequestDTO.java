package com.pinnie.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PinUpdateRequestDTO {

    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    private String description;

    @Pattern(regexp = "^(https?://.*)?$", message = "Link must be a valid HTTP/HTTPS URL or empty")
    @Size(max = 2048, message = "Link must not exceed 2048 characters")
    private String link;

    @Size(max = 255, message = "Alt text must not exceed 255 characters")
    private String altText;

    public PinUpdateRequestDTO() {}

    public PinUpdateRequestDTO(String title, String description, String link, String altText) {
        this.title = title;
        this.description = description;
        this.link = link;
        this.altText = altText;
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
}
