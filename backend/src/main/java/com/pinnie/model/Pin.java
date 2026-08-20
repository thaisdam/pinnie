package com.pinnie.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pins")
@EntityListeners(AuditingEntityListener.class)
public class Pin {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 2048)
    private String link;

    @Column(name = "alt_text", length = 255)
    private String altText;

    @Column(name = "media_stored_filename", nullable = false, length = 255)
    private String mediaStoredFilename;

    @Column(name = "media_content_type", nullable = false, length = 50)
    private String mediaContentType;

    @Column(name = "media_size", nullable = false)
    private long mediaSize;

    @Column(name = "media_width", nullable = false)
    private int mediaWidth;

    @Column(name = "media_height", nullable = false)
    private int mediaHeight;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Pin() {}

    public Pin(User user, String title, String description, String link, String altText,
               String mediaStoredFilename, String mediaContentType, long mediaSize,
               int mediaWidth, int mediaHeight) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.link = link;
        this.altText = altText;
        this.mediaStoredFilename = mediaStoredFilename;
        this.mediaContentType = mediaContentType;
        this.mediaSize = mediaSize;
        this.mediaWidth = mediaWidth;
        this.mediaHeight = mediaHeight;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public String getMediaStoredFilename() {
        return mediaStoredFilename;
    }

    public void setMediaStoredFilename(String mediaStoredFilename) {
        this.mediaStoredFilename = mediaStoredFilename;
    }

    public String getMediaContentType() {
        return mediaContentType;
    }

    public void setMediaContentType(String mediaContentType) {
        this.mediaContentType = mediaContentType;
    }

    public long getMediaSize() {
        return mediaSize;
    }

    public void setMediaSize(long mediaSize) {
        this.mediaSize = mediaSize;
    }

    public int getMediaWidth() {
        return mediaWidth;
    }

    public void setMediaWidth(int mediaWidth) {
        this.mediaWidth = mediaWidth;
    }

    public int getMediaHeight() {
        return mediaHeight;
    }

    public void setMediaHeight(int mediaHeight) {
        this.mediaHeight = mediaHeight;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
