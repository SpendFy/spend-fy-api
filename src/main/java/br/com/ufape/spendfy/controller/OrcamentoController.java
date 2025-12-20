package br.com.ufape.spendfy.controller;

import br.com.ufape.spendfy.dto.orcamento.OrcamentoRequest;
import br.com.ufape.spendfy.dto.orcamento.OrcamentoResponse;
import br.com.ufape.spendfy.service.OrcamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orcamentos")
@RequiredArgsConstructor
@Tag(name = "Orçamentos", description = "Endpoints para gerenciamento de orçamentos")
@SecurityRequirement(name = "bearerAuth")
public class OrcamentoController {

    private final OrcamentoService orcamentoService;

    @PostMapping
    @Operation(summary = "Criar orçamento", description = "Cria um novo orçamento para o usuário autenticado")
    public ResponseEntity<OrcamentoResponse> criar(@Valid @RequestBody OrcamentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orcamentoService.criar(request));
    }

    @GetMapping
    @Operation(summary = "Listar orçamentos", description = "Lista todos os orçamentos do usuário autenticado")
    public ResponseEntity<List<OrcamentoResponse>> listarTodos() {
        return ResponseEntity.ok(orcamentoService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar orçamento por ID", description = "Busca um orçamento específico pelo ID")
    public ResponseEntity<OrcamentoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(orcamentoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar orçamento", description = "Atualiza um orçamento existente")
    public ResponseEntity<OrcamentoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody OrcamentoRequest request
    ) {
        return ResponseEntity.ok(orcamentoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar orçamento", description = "Deleta um orçamento existente")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        orcamentoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
