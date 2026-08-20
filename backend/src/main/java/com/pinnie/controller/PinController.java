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

import java.util.UUID;

@RestController
@RequestMapping("/api/pins")
public class PinController {

    private final PinService pinService;

    public PinController(PinService pinService) {
        this.pinService = pinService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponseDTO> uploadImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {
        UUID userId = UUID.fromString(userDetails.getUsername());
        ImageUploadResponseDTO response = pinService.processAndStoreImage(file.getBytes(), userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<PinResponseDTO> createPin(
            @Valid @RequestBody PinCreateRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        PinResponseDTO response = pinService.createPin(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PinResponseDTO> getPin(@PathVariable UUID id) {
        return ResponseEntity.ok(pinService.getPin(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PinResponseDTO>> getUserPins(
            @PathVariable UUID userId,
            Pageable pageable) {
        return ResponseEntity.ok(pinService.getUserPins(userId, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PinResponseDTO> updatePin(
            @PathVariable UUID id,
            @Valid @RequestBody PinUpdateRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        PinResponseDTO response = pinService.updatePin(id, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePin(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        pinService.deletePin(id, userId);
        return ResponseEntity.noContent().build();
    }
}
