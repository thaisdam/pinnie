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

import java.util.UUID;

@RestController
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @PostMapping
    public ResponseEntity<BoardResponseDTO> createBoard(
            @AuthenticationPrincipal UserDetails currentUser,
            @Valid @RequestBody BoardCreateRequestDTO request) {
        UUID requesterId = UUID.fromString(currentUser.getUsername());
        BoardResponseDTO response = boardService.createBoard(requesterId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardResponseDTO> getBoard(
            @AuthenticationPrincipal UserDetails currentUser,
            @PathVariable UUID id) {
        UUID requesterId = currentUser != null ? UUID.fromString(currentUser.getUsername()) : null;
        BoardResponseDTO response = boardService.getBoardById(id, requesterId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BoardResponseDTO> updateBoard(
            @AuthenticationPrincipal UserDetails currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody BoardUpdateRequestDTO request) {
        UUID requesterId = UUID.fromString(currentUser.getUsername());
        BoardResponseDTO response = boardService.updateBoard(id, requesterId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(
            @AuthenticationPrincipal UserDetails currentUser,
            @PathVariable UUID id) {
        UUID requesterId = UUID.fromString(currentUser.getUsername());
        boardService.deleteBoard(id, requesterId);
        return ResponseEntity.noContent().build();
    }
}
