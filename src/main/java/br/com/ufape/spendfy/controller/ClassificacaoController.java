package br.com.ufape.spendfy.controller;

import br.com.ufape.spendfy.dto.classificacao.ClassificacaoRequest;
import br.com.ufape.spendfy.dto.classificacao.ClassificacaoResponse;
import br.com.ufape.spendfy.service.ClassificacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transacoes/classificar")
@RequiredArgsConstructor
@Tag(name = "Transações", description = "Endpoints para gerenciamento de transações")
@SecurityRequirement(name = "bearerAuth")
public class ClassificacaoController {

    private final ClassificacaoService classificacaoService;

    @PostMapping
    @Operation(summary = "Classificar transação automaticamente",
               description = "Usa IA para sugerir a categoria mais adequada com base na descrição da transação")
    public ResponseEntity<ClassificacaoResponse> classificar(@Valid @RequestBody ClassificacaoRequest request) {
        return ResponseEntity.ok(classificacaoService.classificar(request));
    }
}
