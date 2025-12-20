package br.com.ufape.spendfy.controller;

import br.com.ufape.spendfy.dto.conta.ContaRequest;
import br.com.ufape.spendfy.dto.conta.ContaResponse;
import br.com.ufape.spendfy.service.ContaService;
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
@RequestMapping("/api/contas")
@RequiredArgsConstructor
@Tag(name = "Contas", description = "Endpoints para gerenciamento de contas")
@SecurityRequirement(name = "bearerAuth")
public class ContaController {

    private final ContaService contaService;

    @PostMapping
    @Operation(summary = "Criar conta", description = "Cria uma nova conta para o usuário autenticado")
    public ResponseEntity<ContaResponse> criar(@Valid @RequestBody ContaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contaService.criar(request));
    }

    @GetMapping
    @Operation(summary = "Listar contas", description = "Lista todas as contas do usuário autenticado")
    public ResponseEntity<List<ContaResponse>> listarTodas() {
        return ResponseEntity.ok(contaService.listarTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar conta por ID", description = "Busca uma conta específica pelo ID")
    public ResponseEntity<ContaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(contaService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar conta", description = "Atualiza uma conta existente")
    public ResponseEntity<ContaResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ContaRequest request
    ) {
        return ResponseEntity.ok(contaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar conta", description = "Deleta uma conta existente")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        contaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
