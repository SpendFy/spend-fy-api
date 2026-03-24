package br.com.ufape.spendfy.dto.insight;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InsightMensalResponse {
    private String resumo;
    private List<String> destaques;
    private BigDecimal totalReceitas;
    private BigDecimal totalDespesas;
    private int score;
}
