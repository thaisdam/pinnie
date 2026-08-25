package com.pinnie.controller;

import com.pinnie.dto.CommentRequestDTO;
import com.pinnie.dto.CommentResponseDTO;
import com.pinnie.model.Comment;
import com.pinnie.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/pins/{pinId}/comments")
@Tag(name = "Comments", description = "Endpoints de Comentários nos Pins")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    @Operation(summary = "Listar Comentários", description = "Retorna um Slice paginado de comentários do Pin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comentários listados com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pin não encontrado", content = @Content)
    })
    public ResponseEntity<Slice<CommentResponseDTO>> getComments(
            @PathVariable UUID pinId,
            @Parameter(description = "Número da página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        if (size > 50) size = 50;
        if (size < 1) size = 20;
        if (page < 0) page = 0;

        Slice<Comment> comments = commentService.getCommentsByPin(pinId, PageRequest.of(page, size));
        Slice<CommentResponseDTO> dtoSlice = comments.map(CommentResponseDTO::new);
        return ResponseEntity.ok(dtoSlice);
    }

    @PostMapping
    @Operation(summary = "Criar Comentário", description = "Adiciona um comentário a um Pin (Requer Autenticação e CSRF)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comentário criado", 
                content = @Content(schema = @Schema(implementation = CommentResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Texto em branco ou muito longo", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pin não encontrado", content = @Content)
    })
    public ResponseEntity<CommentResponseDTO> createComment(
            @PathVariable UUID pinId,
            @Valid @RequestBody CommentRequestDTO dto,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        Comment comment = commentService.createComment(pinId, userId, dto.getText());
        return ResponseEntity.status(HttpStatus.CREATED).body(new CommentResponseDTO(comment));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Excluir Comentário", description = "Exclui um comentário do Pin (Requer ser o autor do comentário ou dono do Pin)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Comentário excluído com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para excluir", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pin ou comentário não encontrado", content = @Content)
    })
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID pinId,
            @PathVariable UUID commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        commentService.deleteComment(pinId, commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
