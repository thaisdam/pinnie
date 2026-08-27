package com.pinnie.controller;

import com.pinnie.dto.ImageUploadResponseDTO;
import com.pinnie.dto.PinCreateRequestDTO;
import com.pinnie.dto.PinResponseDTO;
import com.pinnie.dto.PinUpdateRequestDTO;
import com.pinnie.service.PinService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

import com.pinnie.repository.PinLikeRepository;

@RestController
@RequestMapping("/api/pins")
@Tag(name = "Pins", description = "Endpoints para upload, criação e gerenciamento de Pins")
public class PinController {

    private final PinService pinService;
    private final PinLikeRepository pinLikeRepository;

    public PinController(PinService pinService, PinLikeRepository pinLikeRepository) {
        this.pinService = pinService;
        this.pinLikeRepository = pinLikeRepository;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload de Imagem (Etapa 1)", description = "Faz o upload do arquivo binário e retorna um uploadId. Válido por tempo limitado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido ou muito grande"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<ImageUploadResponseDTO> uploadImage(
            @Parameter(description = "Arquivo da imagem", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(type = "string", format = "binary")))
            @RequestParam("file") MultipartFile file,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) throws Exception {
        UUID userId = UUID.fromString(userDetails.getUsername());
        ImageUploadResponseDTO response = pinService.processAndStoreImage(file.getBytes(), userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Criar Pin (Etapa 2)", description = "Associa os metadados textuais ao uploadId gerado na Etapa 1.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pin criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou uploadId não encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<PinResponseDTO> createPin(
            @Valid @RequestBody PinCreateRequestDTO request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        PinResponseDTO response = pinService.createPin(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter Pin por ID", description = "Retorna os detalhes públicos de um Pin.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pin encontrado"),
            @ApiResponse(responseCode = "404", description = "Pin não encontrado")
    })
    public ResponseEntity<PinResponseDTO> getPin(
            @Parameter(description = "ID do pin") @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        PinResponseDTO dto = pinService.getPin(id);
        dto.setLikesCount(pinLikeRepository.countByPinId(id));
        if (userDetails != null) {
            UUID currentUserId = UUID.fromString(userDetails.getUsername());
            dto.setLikedByMe(pinLikeRepository.existsByPinIdAndUserId(id, currentUserId));
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Obter Pins criados por usuário", description = "Lista os Pins criados por um determinado usuário com suporte a paginação.")
    public ResponseEntity<Page<PinResponseDTO>> getUserPins(
            @Parameter(description = "ID do usuário") @PathVariable UUID userId,
            @Parameter(hidden = true) Pageable pageable) {
        return ResponseEntity.ok(pinService.getUserPins(userId, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Pin", description = "Altera os metadados do Pin. Apenas o criador pode atualizar.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pin atualizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (não é o criador)"),
            @ApiResponse(responseCode = "404", description = "Pin não encontrado")
    })
    public ResponseEntity<PinResponseDTO> updatePin(
            @Parameter(description = "ID do pin") @PathVariable UUID id,
            @Valid @RequestBody PinUpdateRequestDTO request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        PinResponseDTO response = pinService.updatePin(id, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir Pin", description = "Remove o Pin definitivamente. Apenas o criador pode excluir.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pin excluído com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (não é o criador)"),
            @ApiResponse(responseCode = "404", description = "Pin não encontrado")
    })
    public ResponseEntity<Void> deletePin(
            @Parameter(description = "ID do pin") @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        pinService.deletePin(id, userId);
        return ResponseEntity.noContent().build();
    }
}
