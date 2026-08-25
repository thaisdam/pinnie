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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Endpoints para gerenciamento de perfil e dados do usuário")
public class UserController {

    private final UserRepository userRepository;
    private final BoardService boardService;

    public UserController(UserRepository userRepository, BoardService boardService) {
        this.userRepository = userRepository;
        this.boardService = boardService;
    }

    @GetMapping("/me")
    @Operation(summary = "Obter usuário logado", description = "Retorna os dados do usuário a partir do JWT autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<UserResponseDTO> getCurrentUser(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return ResponseEntity.ok(UserResponseDTO.fromEntity(user));
    }

    @GetMapping("/{userId}/boards")
    @Operation(summary = "Obter pastas de um usuário", description = "Lista as pastas do usuário indicado. Se o usuário buscando for o próprio dono, lista todas. Se for um terceiro, lista apenas pastas não-privadas.")
    public ResponseEntity<Page<BoardResponseDTO>> getUserBoards(
            @Parameter(description = "ID do usuário") @PathVariable UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser,
            @Parameter(hidden = true) Pageable pageable) {
        UUID requesterId = currentUser != null ? UUID.fromString(currentUser.getUsername()) : null;
        Page<BoardResponseDTO> boards = boardService.getUserBoards(userId, requesterId, pageable);
        return ResponseEntity.ok(boards);
    }
}
