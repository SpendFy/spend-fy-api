package br.com.ufape.spendfy.service;

import br.com.ufape.spendfy.component.AuthenticatedUserResolver;
import br.com.ufape.spendfy.dto.conta.ContaResponse;
import br.com.ufape.spendfy.dto.dashboard.CategoriaGastoResponse;
import br.com.ufape.spendfy.dto.dashboard.DashboardResponse;
import br.com.ufape.spendfy.dto.orcamento.OrcamentoResponse;
import br.com.ufape.spendfy.entity.Usuario;
import br.com.ufape.spendfy.enums.TipoTransacao;
import br.com.ufape.spendfy.repository.OrcamentoRepository;
import br.com.ufape.spendfy.repository.TransacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AuthenticatedUserResolver userResolver;
    private final ContaService contaService;
    private final OrcamentoService orcamentoService;
    private final TransacaoRepository transacaoRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        Usuario usuario = userResolver.getUsuarioAutenticado();

        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate fimMes = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        BigDecimal saldoTotal = contaService.listarTodas().stream()
                .map(ContaResponse::getSaldoAtual)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalReceitas = Objects.requireNonNullElse(
                transacaoRepository.sumValorByUsuarioIdAndTipoAndPeriodo(
                        usuario.getId(), TipoTransacao.RECEITA, inicioMes, fimMes),
                BigDecimal.ZERO);

        BigDecimal totalDespesas = Objects.requireNonNullElse(
                transacaoRepository.sumValorByUsuarioIdAndTipoAndPeriodo(
                        usuario.getId(), TipoTransacao.DESPESA, inicioMes, fimMes),
                BigDecimal.ZERO);

        List<CategoriaGastoResponse> topCategorias = buildTopCategorias(
                usuario.getId(), inicioMes, fimMes, totalDespesas);

        List<OrcamentoResponse> orcamentosAtivos = orcamentoService.listarTodos().stream()
                .filter(o -> !o.getDataFim().isBefore(LocalDate.now())
                        && !o.getDataInicio().isAfter(LocalDate.now()))
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .saldoTotal(saldoTotal)
                .totalReceitasMes(totalReceitas)
                .totalDespesasMes(totalDespesas)
                .saldoMes(totalReceitas.subtract(totalDespesas))
                .topCategorias(topCategorias)
                .orcamentosAtivos(orcamentosAtivos)
                .build();
    }

    private List<CategoriaGastoResponse> buildTopCategorias(Long idUsuario, LocalDate inicio,
                                                              LocalDate fim, BigDecimal totalDespesas) {
        List<Object[]> rows = transacaoRepository.findGastosPorCategoria(
                idUsuario, TipoTransacao.DESPESA, inicio, fim);

        List<CategoriaGastoResponse> result = new ArrayList<>();
        int limite = Math.min(5, rows.size());

        for (int i = 0; i < limite; i++) {
            Object[] row = rows.get(i);
            BigDecimal total = (BigDecimal) row[3];
            BigDecimal percentual = totalDespesas.compareTo(BigDecimal.ZERO) > 0
                    ? total.multiply(BigDecimal.valueOf(100)).divide(totalDespesas, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            result.add(CategoriaGastoResponse.builder()
                    .idCategoria((Long) row[0])
                    .nome((String) row[1])
                    .cor((String) row[2])
                    .total(total)
                    .percentualDoTotal(percentual)
                    .build());
        }
        return result;
    }
}
