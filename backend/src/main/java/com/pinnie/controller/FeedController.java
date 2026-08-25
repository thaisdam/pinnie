package com.pinnie.controller;

import com.pinnie.dto.PinResponseDTO;
import com.pinnie.service.FeedService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/feed")
@Tag(name = "Feed", description = "Endpoints públicos de Feed (Scroll Infinito)")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping
    @Operation(summary = "Obter feed de pins público", description = "Retorna um Slice paginado ordenado por data de criação descrescente. Limite máximo é 50 itens por página.")
    public ResponseEntity<Slice<PinResponseDTO>> getFeed(
            @Parameter(description = "Página atual (inicia em 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Quantidade de itens (limite 50, padrão 20)") @RequestParam(defaultValue = "20") int size) {
        
        int safePage = Math.max(0, page);
        int safeSize = (size < 1 || size > 50) ? 20 : size;
        
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        return ResponseEntity.ok(feedService.getFeed(pageable));
    }
}
