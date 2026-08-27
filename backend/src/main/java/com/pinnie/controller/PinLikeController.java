package com.pinnie.controller;

import com.pinnie.service.PinLikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@RestController
@RequestMapping("/api/pins/{pinId}/like")
@Tag(name = "Pin Likes", description = "Endpoints para curtir e descurtir Pins")
public class PinLikeController {

    private final PinLikeService pinLikeService;

    public PinLikeController(PinLikeService pinLikeService) {
        this.pinLikeService = pinLikeService;
    }

    @PostMapping
    @Operation(summary = "Curtir um Pin")
    public ResponseEntity<Void> likePin(@PathVariable UUID pinId, @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        pinLikeService.likePin(pinId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    @Operation(summary = "Remover curtida de um Pin")
    public ResponseEntity<Void> unlikePin(@PathVariable UUID pinId, @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        pinLikeService.unlikePin(pinId, userId);
        return ResponseEntity.noContent().build();
    }
}
