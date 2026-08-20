package com.pinnie.controller;

import com.pinnie.dto.UserResponseDTO;
import com.pinnie.dto.BoardResponseDTO;
import com.pinnie.model.User;
import com.pinnie.repository.UserRepository;
import com.pinnie.service.BoardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final BoardService boardService;

    public UserController(UserRepository userRepository, BoardService boardService) {
        this.userRepository = userRepository;
        this.boardService = boardService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return ResponseEntity.ok(UserResponseDTO.fromEntity(user));
    }

    @GetMapping("/{userId}/boards")
    public ResponseEntity<Page<BoardResponseDTO>> getUserBoards(
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserDetails currentUser,
            Pageable pageable) {
        UUID requesterId = currentUser != null ? UUID.fromString(currentUser.getUsername()) : null;
        Page<BoardResponseDTO> boards = boardService.getUserBoards(userId, requesterId, pageable);
        return ResponseEntity.ok(boards);
    }
}
