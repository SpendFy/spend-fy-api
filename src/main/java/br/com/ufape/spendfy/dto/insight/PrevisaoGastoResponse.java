package br.com.ufape.spendfy.dto.insight;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrevisaoGastoResponse {
    private Long idCategoria;
    private String nomeCategoria;
    private BigDecimal mediaMensal;
    private BigDecimal previsaoMesAtual;
    private BigDecimal gastoAtualMes;
    private BigDecimal diferenca;
}
