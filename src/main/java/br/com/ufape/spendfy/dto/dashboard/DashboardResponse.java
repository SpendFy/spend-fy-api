package br.com.ufape.spendfy.dto.dashboard;

import br.com.ufape.spendfy.dto.orcamento.OrcamentoResponse;
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
public class DashboardResponse {
    private BigDecimal saldoTotal;
    private BigDecimal totalReceitasMes;
    private BigDecimal totalDespesasMes;
    private BigDecimal saldoMes;
    private List<CategoriaGastoResponse> topCategorias;
    private List<OrcamentoResponse> orcamentosAtivos;
}
