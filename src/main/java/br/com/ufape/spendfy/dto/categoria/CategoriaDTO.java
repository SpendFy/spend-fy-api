package br.com.ufape.spendfy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import br.com.ufape.spendfy.entity.Category;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {

    private Long id;

    @NotBlank(message = "Category name is required")
    private String name;

    private String description;

    private String color;

    private String icon;

    @NotBlank(message = "Category type is required")
    private String type;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
