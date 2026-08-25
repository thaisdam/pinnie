package com.pinnie.controller;

import com.pinnie.dto.BoardCreateRequestDTO;
import com.pinnie.dto.BoardResponseDTO;
import com.pinnie.dto.BoardUpdateRequestDTO;
import com.pinnie.service.BoardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.UUID;

@RestController
@RequestMapping("/api/boards")
@Tag(name = "Boards", description = "Endpoints para gerenciamento de pastas")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @PostMapping
    @Operation(summary = "Criar nova pasta", description = "Cria uma nova pasta associada ao usuário autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pasta criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<BoardResponseDTO> createBoard(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser,
            @Valid @RequestBody BoardCreateRequestDTO request) {
        UUID requesterId = UUID.fromString(currentUser.getUsername());
        BoardResponseDTO response = boardService.createBoard(requesterId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar pasta", description = "Retorna os dados da pasta. Retorna 404 se a pasta for privada e o solicitante não for o dono.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pasta retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pasta não encontrada ou acesso negado (privada)")
    })
    public ResponseEntity<BoardResponseDTO> getBoard(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser,
            @Parameter(description = "ID da pasta") @PathVariable UUID id) {
        UUID requesterId = currentUser != null ? UUID.fromString(currentUser.getUsername()) : null;
        BoardResponseDTO response = boardService.getBoardById(id, requesterId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar pasta", description = "Atualiza os dados de uma pasta. Apenas o dono pode atualizar.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pasta atualizada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para alterar (não é o dono)"),
            @ApiResponse(responseCode = "404", description = "Pasta não encontrada")
    })
    public ResponseEntity<BoardResponseDTO> updateBoard(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser,
            @Parameter(description = "ID da pasta") @PathVariable UUID id,
            @Valid @RequestBody BoardUpdateRequestDTO request) {
        UUID requesterId = UUID.fromString(currentUser.getUsername());
        BoardResponseDTO response = boardService.updateBoard(id, requesterId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar pasta", description = "Exclui a pasta indicada. Apenas o dono pode excluir.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pasta excluída com sucesso"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para alterar (não é o dono)")
    })
    public ResponseEntity<Void> deleteBoard(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser,
            @Parameter(description = "ID da pasta") @PathVariable UUID id) {
        UUID requesterId = UUID.fromString(currentUser.getUsername());
        boardService.deleteBoard(id, requesterId);
        return ResponseEntity.noContent().build();
    }
}
