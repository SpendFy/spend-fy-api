package br.com.ufape.spendfy.service;

import br.com.ufape.spendfy.component.AuthenticatedUserResolver;
import br.com.ufape.spendfy.dto.insight.InsightMensalResponse;
import br.com.ufape.spendfy.dto.insight.PrevisaoGastoResponse;
import br.com.ufape.spendfy.dto.insight.ScoreResponse;
import br.com.ufape.spendfy.dto.orcamento.OrcamentoResponse;
import br.com.ufape.spendfy.entity.Categoria;
import br.com.ufape.spendfy.entity.Usuario;
import br.com.ufape.spendfy.enums.TipoTransacao;
import br.com.ufape.spendfy.repository.CategoriaRepository;
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
public class InsightService {

    private final AuthenticatedUserResolver userResolver;
    private final TransacaoRepository transacaoRepository;
    private final CategoriaRepository categoriaRepository;
    private final OrcamentoService orcamentoService;
    private final ClaudeApiService claudeApiService;

    @Transactional(readOnly = true)
    public List<PrevisaoGastoResponse> getPrevisaoGastos() {
        Usuario usuario = userResolver.getUsuarioAutenticado();
        List<Categoria> categorias = categoriaRepository.findByUsuarioId(usuario.getId());

        LocalDate hoje = LocalDate.now();
        LocalDate inicioMesAtual = hoje.withDayOfMonth(1);
        LocalDate fimMesAtual = hoje.withDayOfMonth(hoje.lengthOfMonth());

        List<PrevisaoGastoResponse> previsoes = new ArrayList<>();

        for (Categoria categoria : categorias) {
            BigDecimal mediaMensal = calcularMediaMensal(usuario.getId(), categoria.getId(), hoje);
            if (mediaMensal.compareTo(BigDecimal.ZERO) == 0) continue;

            BigDecimal gastoAtualMes = Objects.requireNonNullElse(
                    transacaoRepository.sumValorByUsuarioIdAndCategoriaIdAndTipoAndPeriodo(
                            usuario.getId(), categoria.getId(), TipoTransacao.DESPESA,
                            inicioMesAtual, fimMesAtual),
                    BigDecimal.ZERO);

            previsoes.add(PrevisaoGastoResponse.builder()
                    .idCategoria(categoria.getId())
                    .nomeCategoria(categoria.getNome())
                    .mediaMensal(mediaMensal)
                    .previsaoMesAtual(mediaMensal)
                    .gastoAtualMes(gastoAtualMes)
                    .diferenca(gastoAtualMes.subtract(mediaMensal))
                    .build());
        }

        previsoes.sort((a, b) -> b.getGastoAtualMes().compareTo(a.getGastoAtualMes()));
        return previsoes;
    }

    @Transactional(readOnly = true)
    public ScoreResponse calcularScore() {
        Usuario usuario = userResolver.getUsuarioAutenticado();

        LocalDate hoje = LocalDate.now();
        LocalDate inicioMes = hoje.withDayOfMonth(1);
        LocalDate fimMes = hoje.withDayOfMonth(hoje.lengthOfMonth());

        BigDecimal receitas = Objects.requireNonNullElse(
                transacaoRepository.sumValorByUsuarioIdAndTipoAndPeriodo(
                        usuario.getId(), TipoTransacao.RECEITA, inicioMes, fimMes),
                BigDecimal.ZERO);
        BigDecimal despesas = Objects.requireNonNullElse(
                transacaoRepository.sumValorByUsuarioIdAndTipoAndPeriodo(
                        usuario.getId(), TipoTransacao.DESPESA, inicioMes, fimMes),
                BigDecimal.ZERO);

        List<OrcamentoResponse> orcamentos = orcamentoService.listarTodos().stream()
                .filter(o -> !o.getDataFim().isBefore(inicioMes) && !o.getDataInicio().isAfter(fimMes))
                .collect(Collectors.toList());

        List<String> positivos = new ArrayList<>();
        List<String> negativos = new ArrayList<>();
        int score = 0;

        // Critério 1: Relação receita/despesa (30 pontos)
        if (receitas.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal ratio = receitas.subtract(despesas)
                    .divide(receitas, 4, RoundingMode.HALF_UP);
            if (ratio.compareTo(BigDecimal.valueOf(0.3)) >= 0) {
                score += 30;
                positivos.add("Você poupou mais de 30% da sua renda este mês");
            } else if (ratio.compareTo(BigDecimal.ZERO) > 0) {
                score += 15;
                positivos.add("Suas receitas superam suas despesas");
            } else {
                negativos.add("Suas despesas superaram suas receitas este mês");
            }
        }

        // Critério 2: Aderência a orçamentos (40 pontos)
        if (!orcamentos.isEmpty()) {
            long respeitados = orcamentos.stream()
                    .filter(o -> o.getPercentualUtilizado().compareTo(BigDecimal.valueOf(100)) <= 0)
                    .count();
            int percentualRespeitado = (int) (respeitados * 100 / orcamentos.size());
            int pontosOrcamento = percentualRespeitado * 40 / 100;
            score += pontosOrcamento;

            if (percentualRespeitado == 100) {
                positivos.add("Todos os orçamentos foram respeitados");
            } else if (percentualRespeitado >= 50) {
                positivos.add(percentualRespeitado + "% dos orçamentos estão dentro do limite");
            } else {
                negativos.add("Mais da metade dos orçamentos foram ultrapassados");
            }
        } else {
            score += 20;
        }

        // Critério 3: Saldo positivo (30 pontos)
        if (despesas.compareTo(BigDecimal.ZERO) > 0) {
            long mesesComSaldo = contarMesesComSaldoPositivo(usuario.getId(), hoje);
            int pontosHistorico = (int) Math.min(30, mesesComSaldo * 10);
            score += pontosHistorico;
            if (mesesComSaldo >= 2) {
                positivos.add("Saldo positivo nos últimos " + mesesComSaldo + " meses");
            }
        }

        String classificacao = score >= 80 ? "EXCELENTE"
                : score >= 60 ? "BOM"
                : score >= 40 ? "REGULAR"
                : "ATENÇÃO";

        return ScoreResponse.builder()
                .score(score)
                .classificacao(classificacao)
                .fatoresPositivos(positivos)
                .fatoresNegativos(negativos)
                .build();
    }

    @Transactional(readOnly = true)
    public InsightMensalResponse getInsightMensal() {
        Usuario usuario = userResolver.getUsuarioAutenticado();
        ScoreResponse score = calcularScore();
        List<PrevisaoGastoResponse> previsoes = getPrevisaoGastos();

        LocalDate hoje = LocalDate.now();
        LocalDate inicioMes = hoje.withDayOfMonth(1);
        LocalDate fimMes = hoje.withDayOfMonth(hoje.lengthOfMonth());

        BigDecimal receitas = Objects.requireNonNullElse(
                transacaoRepository.sumValorByUsuarioIdAndTipoAndPeriodo(
                        usuario.getId(), TipoTransacao.RECEITA, inicioMes, fimMes),
                BigDecimal.ZERO);
        BigDecimal despesas = Objects.requireNonNullElse(
                transacaoRepository.sumValorByUsuarioIdAndTipoAndPeriodo(
                        usuario.getId(), TipoTransacao.DESPESA, inicioMes, fimMes),
                BigDecimal.ZERO);

        String topCategoria = previsoes.isEmpty() ? "nenhuma" : previsoes.get(0).getNomeCategoria();
        BigDecimal topGasto = previsoes.isEmpty() ? BigDecimal.ZERO : previsoes.get(0).getGastoAtualMes();

        String dadosContexto = String.format("""
                Mês atual: %s/%d
                Total de receitas: R$ %.2f
                Total de despesas: R$ %.2f
                Score financeiro: %d (%s)
                Categoria com maior gasto: %s (R$ %.2f)
                Fatores positivos: %s
                Pontos de atenção: %s
                """,
                hoje.getMonth().getDisplayName(java.time.format.TextStyle.FULL,
                        new java.util.Locale("pt", "BR")),
                hoje.getYear(),
                receitas, despesas, score.getScore(), score.getClassificacao(),
                topCategoria, topGasto,
                String.join("; ", score.getFatoresPositivos()),
                String.join("; ", score.getFatoresNegativos()));

        String prompt = """
                Você é um consultor financeiro pessoal amigável e direto.
                Com base nos dados financeiros do mês, gere um resumo personalizado em português
                de até 3 frases, com tom encorajador mas honesto.

                Dados do usuário:
                """ + dadosContexto + """

                Responda apenas com o resumo, sem títulos ou marcadores.
                """;

        String resumo = claudeApiService.chat(prompt);

        List<String> destaques = new ArrayList<>(score.getFatoresPositivos());
        destaques.addAll(score.getFatoresNegativos());

        return InsightMensalResponse.builder()
                .resumo(resumo)
                .destaques(destaques)
                .totalReceitas(receitas)
                .totalDespesas(despesas)
                .score(score.getScore())
                .build();
    }

    private BigDecimal calcularMediaMensal(Long idUsuario, Long idCategoria, LocalDate hoje) {
        BigDecimal total = BigDecimal.ZERO;
        int mesesComDados = 0;

        for (int i = 1; i <= 3; i++) {
            LocalDate mesRef = hoje.minusMonths(i);
            LocalDate inicio = mesRef.withDayOfMonth(1);
            LocalDate fim = mesRef.withDayOfMonth(mesRef.lengthOfMonth());

            BigDecimal gasto = transacaoRepository.sumValorByUsuarioIdAndCategoriaIdAndTipoAndPeriodo(
                    idUsuario, idCategoria, TipoTransacao.DESPESA, inicio, fim);

            if (gasto != null && gasto.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal peso = BigDecimal.valueOf(4 - i);
                total = total.add(gasto.multiply(peso));
                mesesComDados += (4 - i);
            }
        }

        if (mesesComDados == 0) return BigDecimal.ZERO;
        return total.divide(BigDecimal.valueOf(mesesComDados), 2, RoundingMode.HALF_UP);
    }

    private long contarMesesComSaldoPositivo(Long idUsuario, LocalDate hoje) {
        long meses = 0;
        for (int i = 0; i < 3; i++) {
            LocalDate mesRef = hoje.minusMonths(i);
            LocalDate inicio = mesRef.withDayOfMonth(1);
            LocalDate fim = mesRef.withDayOfMonth(mesRef.lengthOfMonth());

            BigDecimal rec = Objects.requireNonNullElse(
                    transacaoRepository.sumValorByUsuarioIdAndTipoAndPeriodo(
                            idUsuario, TipoTransacao.RECEITA, inicio, fim),
                    BigDecimal.ZERO);
            BigDecimal des = Objects.requireNonNullElse(
                    transacaoRepository.sumValorByUsuarioIdAndTipoAndPeriodo(
                            idUsuario, TipoTransacao.DESPESA, inicio, fim),
                    BigDecimal.ZERO);

            if (rec.compareTo(des) > 0) meses++;
        }
        return meses;
    }
}
