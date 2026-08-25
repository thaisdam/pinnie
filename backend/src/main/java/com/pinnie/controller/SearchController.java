package com.pinnie.controller;

import com.pinnie.dto.PinResponseDTO;
import com.pinnie.service.SearchService;
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
@RequestMapping("/api/search")
@Tag(name = "Search", description = "Endpoints públicos para busca textual de Pins")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    @Operation(summary = "Buscar pins", description = "Busca pins por título ou descrição utilizando índices trigrama do PostgreSQL. Retorna Slice paginado.")
    public ResponseEntity<Slice<PinResponseDTO>> search(
            @Parameter(description = "Termo de busca") @RequestParam(defaultValue = "") String q,
            @Parameter(description = "Página atual (inicia em 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Quantidade de itens (limite 50)") @RequestParam(defaultValue = "20") int size) {
        
        String normalizedQ = q != null ? q.trim() : "";
        
        if (normalizedQ.length() < 2 || normalizedQ.length() > 100) {
            return ResponseEntity.badRequest().build();
        }
        
        int safePage = Math.max(0, page);
        int safeSize = (size < 1 || size > 50) ? 20 : size;
        
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        return ResponseEntity.ok(searchService.searchPins(normalizedQ, pageable));
    }
}
