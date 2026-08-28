package com.pinnie.controller;

import com.pinnie.dto.ReportRequestDTO;
import com.pinnie.dto.ReportResponseDTO;
import com.pinnie.model.Report;
import com.pinnie.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Endpoints para submissão de denúncias pelos usuários")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    @Operation(summary = "Criar uma denúncia", description = "Permite a um usuário logado reportar um Pin ou outro Usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Denúncia criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, alvo não encontrado ou auto-denúncia"),
            @ApiResponse(responseCode = "409", description = "Usuário já tem denúncia pendente para este alvo"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<ReportResponseDTO> createReport(
            @Valid @RequestBody ReportRequestDTO request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID reporterId = UUID.fromString(userDetails.getUsername());
        
        try {
            Report report = reportService.createReport(reporterId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ReportResponseDTO.fromEntity(report));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
