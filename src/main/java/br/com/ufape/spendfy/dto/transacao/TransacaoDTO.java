package br.com.ufape.spendfy.dto.transacao;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransacaoDTO {
    private String id;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;
    
    @NotBlank(message = "Transaction type is required")
    private String type;
    
    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;
    
    @NotBlank(message = "Account ID is required")
    private String contaId;
    
    @NotBlank(message = "Category ID is required")
    private String categoriaId;
    
    private String notes;
    private Boolean reconciled;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}