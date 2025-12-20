package br.com.ufape.spendfy.controller;

import br.com.ufape.spendfy.dto.transacao.TransacaoRequest;
import br.com.ufape.spendfy.dto.transacao.TransacaoResponse;
import br.com.ufape.spendfy.service.TransacaoService;
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
@RequestMapping("/api/transacoes")
@RequiredArgsConstructor
@Tag(name = "Transações", description = "Endpoints para gerenciamento de transações")
@SecurityRequirement(name = "bearerAuth")
public class TransacaoController {

    private final TransacaoService transacaoService;

    @PostMapping
    @Operation(summary = "Criar transação", description = "Cria uma nova transação para o usuário autenticado")
    public ResponseEntity<TransacaoResponse> criar(@Valid @RequestBody TransacaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transacaoService.criar(request));
    }

    @GetMapping
    @Operation(summary = "Listar transações", description = "Lista todas as transações do usuário autenticado")
    public ResponseEntity<List<TransacaoResponse>> listarTodas() {
        return ResponseEntity.ok(transacaoService.listarTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar transação por ID", description = "Busca uma transação específica pelo ID")
    public ResponseEntity<TransacaoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(transacaoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar transação", description = "Atualiza uma transação existente")
    public ResponseEntity<TransacaoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody TransacaoRequest request
    ) {
        return ResponseEntity.ok(transacaoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar transação", description = "Deleta uma transação existente")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        transacaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
