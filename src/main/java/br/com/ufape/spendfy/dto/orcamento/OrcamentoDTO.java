package br.com.ufape.spendfy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetDTO {

    private Long id;

    @NotBlank(message = "Budget name is required")
    private String name;

    @NotNull(message = "Budget limit is required")
    @DecimalMin(value = "0.01", message = "Limit must be greater than 0")
    private BigDecimal limit;

    private BigDecimal spent;

    @NotBlank(message = "Period is required")
    private String period;

    private String description;

    private Long categoryId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
