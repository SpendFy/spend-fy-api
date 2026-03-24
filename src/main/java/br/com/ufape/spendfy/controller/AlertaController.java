package br.com.ufape.spendfy.controller;

import br.com.ufape.spendfy.dto.alerta.AlertaResponse;
import br.com.ufape.spendfy.service.AlertaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
@Tag(name = "Alertas", description = "Notificações automáticas sobre saúde financeira")
@SecurityRequirement(name = "bearerAuth")
public class AlertaController {

    private final AlertaService alertaService;

    @GetMapping
    @Operation(summary = "Listar alertas não lidos",
               description = "Retorna todos os alertas pendentes: orçamentos estourados, saldo baixo, etc.")
    public ResponseEntity<List<AlertaResponse>> listarNaoLidos() {
        return ResponseEntity.ok(alertaService.listarNaoLidos());
    }

    @PatchMapping("/{id}/lido")
    @Operation(summary = "Marcar alerta como lido")
    public ResponseEntity<AlertaResponse> marcarComoLido(@PathVariable Long id) {
        return ResponseEntity.ok(alertaService.marcarComoLido(id));
    }
}
