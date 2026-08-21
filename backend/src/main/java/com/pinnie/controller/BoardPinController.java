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

import java.util.UUID;

@RestController
@RequestMapping("/api/boards")
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
    public ResponseEntity<Void> savePinToBoard(
            @PathVariable UUID boardId,
            @PathVariable UUID pinId) {
        UUID userId = getAuthenticatedUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boardPinService.savePinToBoard(boardId, pinId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{boardId}/pins/{pinId}")
    public ResponseEntity<Void> removePinFromBoard(
            @PathVariable UUID boardId,
            @PathVariable UUID pinId) {
        UUID userId = getAuthenticatedUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boardPinService.removePinFromBoard(boardId, pinId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{boardId}/pins")
    public ResponseEntity<Page<BoardPinResponseDTO>> getPinsFromBoard(
            @PathVariable UUID boardId,
            Pageable pageable) {
        UUID userId = getAuthenticatedUserId();
        return ResponseEntity.ok(boardPinService.getPinsFromBoard(boardId, userId, pageable));
    }

    @GetMapping("/{boardId}/pins/{pinId}")
    public ResponseEntity<BoardPinResponseDTO> checkPinInBoard(
            @PathVariable UUID boardId,
            @PathVariable UUID pinId) {
        UUID userId = getAuthenticatedUserId();
        return ResponseEntity.ok(boardPinService.checkPinInBoard(boardId, pinId, userId));
    }
}
