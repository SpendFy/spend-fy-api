package br.com.ufape.spendfy.controller;

import br.com.ufape.spendfy.dto.categoria.CategoriaDTO;
import br.com.ufape.spendfy.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management endpoints")
public class CategoriaController {
    
    private final CategoriaService categoriaService;
    
    @PostMapping
    @Operation(summary = "Create a new category", description = "Create a new expense or income category")
    public ResponseEntity<CategoriaDTO> createCategoria(
            @Valid @RequestBody CategoriaDTO dto,
            @RequestHeader("X-User-Id") String userId) {
        CategoriaDTO categoria = categoriaService.createCategoria(dto, userId);
        return new ResponseEntity<>(categoria, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieve a category by its unique ID")
    public ResponseEntity<CategoriaDTO> getCategoriaById(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        CategoriaDTO categoria = categoriaService.getCategoriaById(id, userId);
        return ResponseEntity.ok(categoria);
    }
    
    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieve all categories for the authenticated user")
    public ResponseEntity<List<CategoriaDTO>> getAllCategorias(@RequestHeader("X-User-Id") String userId) {
        List<CategoriaDTO> categorias = categoriaService.getAllCategoriasByUser(userId);
        return ResponseEntity.ok(categorias);
    }
    
    @GetMapping("/type/{type}")
    @Operation(summary = "Get categories by type", description = "Retrieve categories filtered by type (INCOME or EXPENSE)")
    public ResponseEntity<List<CategoriaDTO>> getCategoriasByType(
            @PathVariable String type,
            @RequestHeader("X-User-Id") String userId) {
        List<CategoriaDTO> categorias = categoriaService.getCategoriasByType(userId, type);
        return ResponseEntity.ok(categorias);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Update an existing category")
    public ResponseEntity<CategoriaDTO> updateCategoria(
            @PathVariable String id,
            @Valid @RequestBody CategoriaDTO dto,
            @RequestHeader("X-User-Id") String userId) {
        CategoriaDTO categoria = categoriaService.updateCategoria(id, dto, userId);
        return ResponseEntity.ok(categoria);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Delete a category")
    public ResponseEntity<Void> deleteCategoria(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        categoriaService.deleteCategoria(id, userId);
        return ResponseEntity.noContent().build();
    }
}