package br.com.ufape.spendfy.controller;

import br.com.ufape.spendfy.dto.orcamento.OrcamentoDTO;
import br.com.ufape.spendfy.service.OrcamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
@Tag(name = "Budgets", description = "Budget management endpoints")
public class OrcamentoController {
    
    private final OrcamentoService orcamentoService;
    
    @PostMapping
    @Operation(summary = "Create a new budget", description = "Create a new spending budget for a category")
    public ResponseEntity<OrcamentoDTO> createOrcamento(
            @Valid @RequestBody OrcamentoDTO dto,
            @RequestHeader("X-User-Id") String userId) {
        OrcamentoDTO orcamento = orcamentoService.createOrcamento(dto, userId);
        return new ResponseEntity<>(orcamento, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get budget by ID", description = "Retrieve a budget by its unique ID")
    public ResponseEntity<OrcamentoDTO> getOrcamentoById(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        OrcamentoDTO orcamento = orcamentoService.getOrcamentoById(id, userId);
        return ResponseEntity.ok(orcamento);
    }
    
    @GetMapping
    @Operation(summary = "Get all budgets", description = "Retrieve all budgets for the authenticated user")
    public ResponseEntity<List<OrcamentoDTO>> getAllOrcamentos(@RequestHeader("X-User-Id") String userId) {
        List<OrcamentoDTO> orcamentos = orcamentoService.getAllOrcamentosByUser(userId);
        return ResponseEntity.ok(orcamentos);
    }
    
    @GetMapping("/categoria/{categoriaId}")
    @Operation(summary = "Get budgets by category", description = "Retrieve all budgets for a specific category")
    public ResponseEntity<List<OrcamentoDTO>> getOrcamentosByCategoria(
            @PathVariable String categoriaId,
            @RequestHeader("X-User-Id") String userId) {
        List<OrcamentoDTO> orcamentos = orcamentoService.getOrcamentosByCategoria(userId, categoriaId);
        return ResponseEntity.ok(orcamentos);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update budget", description = "Update an existing budget")
    public ResponseEntity<OrcamentoDTO> updateOrcamento(
            @PathVariable String id,
            @Valid @RequestBody OrcamentoDTO dto,
            @RequestHeader("X-User-Id") String userId) {
        OrcamentoDTO orcamento = orcamentoService.updateOrcamento(id, dto, userId);
        return ResponseEntity.ok(orcamento);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete budget", description = "Delete a budget")
    public ResponseEntity<Void> deleteOrcamento(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        orcamentoService.deleteOrcamento(id, userId);
        return ResponseEntity.noContent().build();
    }
}