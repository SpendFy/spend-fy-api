package br.com.ufape.spendfy.controller;

import br.com.ufape.spendfy.dto.categoria.CategoriaRequest;
import br.com.ufape.spendfy.dto.categoria.CategoriaResponse;
import br.com.ufape.spendfy.service.CategoriaService;
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
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorias", description = "Endpoints para gerenciamento de categorias")
@SecurityRequirement(name = "bearerAuth")
public class CategoriaController {

    private final CategoriaService categoriaService;

    @PostMapping
    @Operation(summary = "Criar categoria", description = "Cria uma nova categoria para o usuário autenticado")
    public ResponseEntity<CategoriaResponse> criar(@Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.criar(request));
    }

    @GetMapping
    @Operation(summary = "Listar categorias", description = "Lista todas as categorias do usuário autenticado")
    public ResponseEntity<List<CategoriaResponse>> listarTodas() {
        return ResponseEntity.ok(categoriaService.listarTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar categoria por ID", description = "Busca uma categoria específica pelo ID")
    public ResponseEntity<CategoriaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar categoria", description = "Atualiza uma categoria existente")
    public ResponseEntity<CategoriaResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequest request
    ) {
        return ResponseEntity.ok(categoriaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar categoria", description = "Deleta uma categoria existente")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        categoriaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
