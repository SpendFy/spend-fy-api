package br.com.ufape.spendfy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import br.com.ufape.spendfy.dto.CategoryDTO;
import br.com.ufape.spendfy.service.CategoryService;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO, Authentication authentication) {
        CategoryDTO created = categoryService.createCategory(categoryDTO, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories(Authentication authentication) {
        List<CategoryDTO> categories = categoryService.getCategoriesByUser(authentication.getName());
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id, Authentication authentication) {
        CategoryDTO category = categoryService.getCategoryById(id, authentication.getName());
        return ResponseEntity.ok(category);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO categoryDTO,
            Authentication authentication) {
        CategoryDTO updated = categoryService.updateCategory(id, categoryDTO, authentication.getName());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id, Authentication authentication) {
        categoryService.deleteCategory(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
