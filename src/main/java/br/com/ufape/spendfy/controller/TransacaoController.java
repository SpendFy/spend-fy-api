package br.com.ufape.spendfy.controller;

import br.com.ufape.spendfy.dto.transacao.TransacaoDTO;
import br.com.ufape.spendfy.service.TransacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction management endpoints")
public class TransacaoController {
    
    private final TransacaoService transacaoService;
    
    @PostMapping
    @Operation(summary = "Create a new transaction", description = "Create a new income, expense, or transfer transaction")
    public ResponseEntity<TransacaoDTO> createTransacao(
            @Valid @RequestBody TransacaoDTO dto,
            @RequestHeader("X-User-Id") String userId) {
        TransacaoDTO transacao = transacaoService.createTransacao(dto, userId);
        return new ResponseEntity<>(transacao, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID", description = "Retrieve a transaction by its unique ID")
    public ResponseEntity<TransacaoDTO> getTransacaoById(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        TransacaoDTO transacao = transacaoService.getTransacaoById(id, userId);
        return ResponseEntity.ok(transacao);
    }
    
    @GetMapping
    @Operation(summary = "Get all transactions", description = "Retrieve all transactions for the authenticated user with pagination")
    public ResponseEntity<Page<TransacaoDTO>> getAllTransacoes(
            @RequestHeader("X-User-Id") String userId,
            Pageable pageable) {
        Page<TransacaoDTO> transacoes = transacaoService.getTransacoesByUser(userId, pageable);
        return ResponseEntity.ok(transacoes);
    }
    
    @GetMapping("/conta/{contaId}")
    @Operation(summary = "Get transactions by account", description = "Retrieve all transactions for a specific account")
    public ResponseEntity<List<TransacaoDTO>> getTransacoesByConta(
            @PathVariable String contaId,
            @RequestHeader("X-User-Id") String userId) {
        List<TransacaoDTO> transacoes = transacaoService.getTransacoesByConta(userId, contaId);
        return ResponseEntity.ok(transacoes);
    }
    
    @GetMapping("/date-range")
    @Operation(summary = "Get transactions by date range", description = "Retrieve transactions within a date range")
    public ResponseEntity<List<TransacaoDTO>> getTransacoesByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestHeader("X-User-Id") String userId) {
        List<TransacaoDTO> transacoes = transacaoService.getTransacoesByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(transacoes);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update transaction", description = "Update an existing transaction")
    public ResponseEntity<TransacaoDTO> updateTransacao(
            @PathVariable String id,
            @Valid @RequestBody TransacaoDTO dto,
            @RequestHeader("X-User-Id") String userId) {
        TransacaoDTO transacao = transacaoService.updateTransacao(id, dto, userId);
        return ResponseEntity.ok(transacao);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete transaction", description = "Delete a transaction")
    public ResponseEntity<Void> deleteTransacao(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        transacaoService.deleteTransacao(id, userId);
        return ResponseEntity.noContent().build();
    }
}