package br.com.ufape.spendfy.dto.categoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaDTO {
    private String id;
    
    @NotBlank(message = "Category name is required")
    private String name;
    
    private String description;
    
    @Pattern(regexp = "^#[A-Fa-f0-9]{6}$", message = "Color must be a valid hex color code")
    private String color;
    
    @NotBlank(message = "Category type is required")
    private String type;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}