package br.com.ufape.spendfy.dto.orcamento;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrcamentoRequest {

    @NotNull(message = "Valor limite é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor limite deve ser maior que zero")
    private BigDecimal valorLimite;

    @NotNull(message = "Data de início é obrigatória")
    private LocalDate dataInicio;

    @NotNull(message = "Data de fim é obrigatória")
    private LocalDate dataFim;

    @NotNull(message = "ID da categoria é obrigatório")
    private Long idCategoria;
}
