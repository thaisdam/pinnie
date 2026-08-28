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

import com.pinnie.repository.UserFollowRepository;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Endpoints para gerenciamento de perfil e dados do usuário")
public class UserController {

    private final UserRepository userRepository;
    private final BoardService boardService;
    private final UserFollowRepository userFollowRepository;
    private final com.pinnie.service.UserService userService;

    public UserController(UserRepository userRepository, BoardService boardService, UserFollowRepository userFollowRepository, com.pinnie.service.UserService userService) {
        this.userRepository = userRepository;
        this.boardService = boardService;
        this.userFollowRepository = userFollowRepository;
        this.userService = userService;
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
        
        long followersCount = userFollowRepository.countByFollowingId(userId);
        long followingCount = userFollowRepository.countByFollowerId(userId);
        
        return ResponseEntity.ok(new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getBio(),
                user.getAvatarUrl(),
                user.isEnabled(),
                user.getRole(),
                followersCount,
                followingCount,
                false
        ));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Obter perfil de um usuário", description = "Retorna os dados públicos de um usuário e verifica se o usuário autenticado o segue.")
    public ResponseEntity<UserResponseDTO> getUserProfile(
            @Parameter(description = "ID do usuário") @PathVariable UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
            
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        long followersCount = userFollowRepository.countByFollowingId(userId);
        long followingCount = userFollowRepository.countByFollowerId(userId);
        boolean followedByMe = false;
        
        if (userDetails != null) {
            UUID currentUserId = UUID.fromString(userDetails.getUsername());
            followedByMe = userFollowRepository.existsByFollowerIdAndFollowingId(currentUserId, userId);
        }
        
        return ResponseEntity.ok(new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getBio(),
                user.getAvatarUrl(),
                user.isEnabled(),
                user.getRole(),
                followersCount,
                followingCount,
                followedByMe
        ));
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

    @org.springframework.web.bind.annotation.PutMapping("/me/profile")
    @Operation(summary = "Atualizar perfil", description = "Atualiza nome e bio do usuário logado.")
    public ResponseEntity<UserResponseDTO> updateProfile(
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.pinnie.dto.UserProfileUpdateRequestDTO request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        User updatedUser = userService.updateProfile(userId, request);
        return ResponseEntity.ok(UserResponseDTO.fromEntity(updatedUser));
    }

    @org.springframework.web.bind.annotation.PutMapping("/me/password")
    @Operation(summary = "Atualizar senha", description = "Atualiza a senha do usuário logado.")
    public ResponseEntity<Void> updatePassword(
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.pinnie.dto.UserPasswordUpdateRequestDTO request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        userService.updatePassword(userId, request);
        return ResponseEntity.noContent().build();
    }

    @org.springframework.web.bind.annotation.PostMapping(value = "/me/avatar", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Atualizar foto de perfil", description = "Faz o upload de uma imagem e define como avatar do usuário.")
    public ResponseEntity<UserResponseDTO> updateAvatar(
            @org.springframework.web.bind.annotation.RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        User updatedUser = userService.updateAvatar(userId, file);
        return ResponseEntity.ok(UserResponseDTO.fromEntity(updatedUser));
    }
}
