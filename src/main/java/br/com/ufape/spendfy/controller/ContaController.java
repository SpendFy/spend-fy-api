package br.com.ufape.spendfy.controller;

import br.com.ufape.spendfy.dto.conta.ContaDTO;
import br.com.ufape.spendfy.service.ContaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Account management endpoints")
public class ContaController {
    
    private final ContaService contaService;
    
    @PostMapping
    @Operation(summary = "Create a new account", description = "Create a new bank account or financial account")
    public ResponseEntity<ContaDTO> createConta(
            @Valid @RequestBody ContaDTO dto,
            @RequestHeader("X-User-Id") String userId) {
        ContaDTO conta = contaService.createConta(dto, userId);
        return new ResponseEntity<>(conta, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID", description = "Retrieve an account by its unique ID")
    public ResponseEntity<ContaDTO> getContaById(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        ContaDTO conta = contaService.getContaById(id, userId);
        return ResponseEntity.ok(conta);
    }
    
    @GetMapping
    @Operation(summary = "Get all accounts", description = "Retrieve all accounts for the authenticated user")
    public ResponseEntity<List<ContaDTO>> getAllContas(@RequestHeader("X-User-Id") String userId) {
        List<ContaDTO> contas = contaService.getAllContasByUser(userId);
        return ResponseEntity.ok(contas);
    }
    
    @GetMapping("/active")
    @Operation(summary = "Get active accounts", description = "Retrieve only active accounts for the authenticated user")
    public ResponseEntity<List<ContaDTO>> getActiveContas(@RequestHeader("X-User-Id") String userId) {
        List<ContaDTO> contas = contaService.getActiveContasByUser(userId);
        return ResponseEntity.ok(contas);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update account", description = "Update an existing account")
    public ResponseEntity<ContaDTO> updateConta(
            @PathVariable String id,
            @Valid @RequestBody ContaDTO dto,
            @RequestHeader("X-User-Id") String userId) {
        ContaDTO conta = contaService.updateConta(id, dto, userId);
        return ResponseEntity.ok(conta);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete account", description = "Deactivate an account")
    public ResponseEntity<Void> deleteConta(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        contaService.deleteConta(id, userId);
        return ResponseEntity.noContent().build();
    }
}