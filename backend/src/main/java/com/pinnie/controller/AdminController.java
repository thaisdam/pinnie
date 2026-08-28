package com.pinnie.controller;

import com.pinnie.dto.ReportResponseDTO;
import com.pinnie.model.ReportStatus;
import com.pinnie.service.PinService;
import com.pinnie.service.ReportService;
import com.pinnie.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Endpoints de moderação restritos a administradores")
public class AdminController {

    private final ReportService reportService;
    private final PinService pinService;
    private final UserService userService;

    public AdminController(ReportService reportService, PinService pinService, UserService userService) {
        this.reportService = reportService;
        this.pinService = pinService;
        this.userService = userService;
    }

    @GetMapping("/reports")
    @Operation(summary = "Listar denúncias pendentes", description = "Retorna uma página de denúncias aguardando resolução.")
    public ResponseEntity<Page<ReportResponseDTO>> getPendingReports(@Parameter(hidden = true) Pageable pageable) {
        return ResponseEntity.ok(reportService.getPendingReports(pageable));
    }

    @PostMapping("/reports/{id}/resolve")
    @Operation(summary = "Resolver denúncia", description = "Marca uma denúncia como RESOLVED ou DISMISSED e registra o admin responsável.")
    public ResponseEntity<ReportResponseDTO> resolveReport(
            @PathVariable UUID id,
            @RequestParam ReportStatus status,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails admin) {
        UUID adminId = UUID.fromString(admin.getUsername());
        return ResponseEntity.ok(reportService.resolveReport(id, status, adminId));
    }

    @PostMapping("/users/{id}/block")
    @Operation(summary = "Bloquear usuário", description = "Altera o status enabled do usuário para false, barrando novos logins e invalidando acessos.")
    public ResponseEntity<Void> blockUser(@PathVariable UUID id) {
        userService.blockUser(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/pins/{id}")
    @Operation(summary = "Excluir Pin forçadamente", description = "Remove um Pin e todas as suas associações e arquivos por decisão de moderação.")
    public ResponseEntity<Void> deletePin(@PathVariable UUID id) {
        pinService.deletePinAsAdmin(id);
        return ResponseEntity.noContent().build();
    }
}
