package com.pinnie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição para criação de comentário")
public class CommentRequestDTO {

    @Schema(description = "Texto do comentário", example = "Que receita maravilhosa!", maxLength = 500, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Text cannot be blank")
    @Size(max = 500, message = "Text must not exceed 500 characters")
    private String text;

    public CommentRequestDTO() {}

    public CommentRequestDTO(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
