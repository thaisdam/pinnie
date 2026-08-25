package com.pinnie.controller;

import com.pinnie.dto.BoardPinResponseDTO;
import com.pinnie.service.BoardPinService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.UUID;

@RestController
@RequestMapping("/api/boards")
@Tag(name = "Board Pins", description = "Endpoints para salvar e gerenciar pins dentro de pastas")
public class BoardPinController {

    private final BoardPinService boardPinService;

    public BoardPinController(BoardPinService boardPinService) {
        this.boardPinService = boardPinService;
    }

    private UUID getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return null; // Visitors
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return UUID.fromString(((UserDetails) principal).getUsername());
        }
        return null;
    }

    @PostMapping("/{boardId}/pins/{pinId}")
    @Operation(summary = "Salvar Pin na Pasta", description = "Associa um Pin existente a uma Pasta. Retorna 409 se já estiver associado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pin salvo na pasta com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (não é o dono da pasta)"),
            @ApiResponse(responseCode = "404", description = "Pasta ou Pin não encontrado"),
            @ApiResponse(responseCode = "409", description = "Pin já está salvo nesta pasta")
    })
    public ResponseEntity<Void> savePinToBoard(
            @Parameter(description = "ID da pasta") @PathVariable UUID boardId,
            @Parameter(description = "ID do pin") @PathVariable UUID pinId) {
        UUID userId = getAuthenticatedUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boardPinService.savePinToBoard(boardId, pinId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{boardId}/pins/{pinId}")
    @Operation(summary = "Remover Pin da Pasta", description = "Remove a associação de um Pin com uma Pasta. Apenas o dono da pasta pode fazer isso.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pin removido da pasta com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (não é o dono da pasta)"),
            @ApiResponse(responseCode = "404", description = "Pasta, Pin ou Associação não encontrada")
    })
    public ResponseEntity<Void> removePinFromBoard(
            @Parameter(description = "ID da pasta") @PathVariable UUID boardId,
            @Parameter(description = "ID do pin") @PathVariable UUID pinId) {
        UUID userId = getAuthenticatedUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boardPinService.removePinFromBoard(boardId, pinId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{boardId}/pins")
    @Operation(summary = "Listar Pins da Pasta", description = "Retorna os Pins salvos em uma Pasta paginados com Page.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pins listados com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pasta não encontrada ou privada")
    })
    public ResponseEntity<Page<BoardPinResponseDTO>> getPinsFromBoard(
            @Parameter(description = "ID da pasta") @PathVariable UUID boardId,
            @Parameter(hidden = true) Pageable pageable) {
        UUID userId = getAuthenticatedUserId();
        return ResponseEntity.ok(boardPinService.getPinsFromBoard(boardId, userId, pageable));
    }

    @GetMapping("/{boardId}/pins/{pinId}")
    @Operation(summary = "Verificar Pin na Pasta", description = "Verifica se um Pin específico está salvo na Pasta especificada.")
    public ResponseEntity<BoardPinResponseDTO> checkPinInBoard(
            @Parameter(description = "ID da pasta") @PathVariable UUID boardId,
            @Parameter(description = "ID do pin") @PathVariable UUID pinId) {
        UUID userId = getAuthenticatedUserId();
        return ResponseEntity.ok(boardPinService.checkPinInBoard(boardId, pinId, userId));
    }
}
