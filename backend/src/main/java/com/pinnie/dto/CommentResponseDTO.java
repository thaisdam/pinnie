package com.pinnie.dto;

import com.pinnie.model.Comment;
import com.pinnie.model.User;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Dados públicos de um comentário")
public class CommentResponseDTO {

    @Schema(description = "ID do comentário")
    private UUID id;
    
    @Schema(description = "Texto do comentário")
    private String text;
    
    @Schema(description = "Data de criação")
    private Instant createdAt;
    
    @Schema(description = "ID do autor")
    private UUID authorId;
    
    @Schema(description = "Username do autor")
    private String authorUsername;
    
    @Schema(description = "Nome de exibição do autor")
    private String authorDisplayName;
    
    @Schema(description = "Avatar do autor")
    private String authorAvatarUrl;

    public CommentResponseDTO() {}

    public CommentResponseDTO(Comment comment) {
        this.id = comment.getId();
        this.text = comment.getText();
        this.createdAt = comment.getCreatedAt();
        
        User author = comment.getUser();
        if (author != null) {
            this.authorId = author.getId();
            this.authorUsername = author.getUsername();
            this.authorDisplayName = author.getDisplayName();
            this.authorAvatarUrl = author.getAvatarUrl();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public void setAuthorId(UUID authorId) {
        this.authorId = authorId;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public String getAuthorDisplayName() {
        return authorDisplayName;
    }

    public void setAuthorDisplayName(String authorDisplayName) {
        this.authorDisplayName = authorDisplayName;
    }

    public String getAuthorAvatarUrl() {
        return authorAvatarUrl;
    }

    public void setAuthorAvatarUrl(String authorAvatarUrl) {
        this.authorAvatarUrl = authorAvatarUrl;
    }
}
