package br.com.ufape.spendfy.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoriaGastoResponse {
    private Long idCategoria;
    private String nome;
    private String cor;
    private BigDecimal total;
    private BigDecimal percentualDoTotal;
}
