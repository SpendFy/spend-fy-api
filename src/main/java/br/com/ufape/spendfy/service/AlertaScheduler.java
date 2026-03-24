package br.com.ufape.spendfy.service;

import br.com.ufape.spendfy.entity.Conta;
import br.com.ufape.spendfy.entity.Orcamento;
import br.com.ufape.spendfy.entity.Usuario;
import br.com.ufape.spendfy.enums.TipoAlerta;
import br.com.ufape.spendfy.enums.TipoTransacao;
import br.com.ufape.spendfy.repository.ContaRepository;
import br.com.ufape.spendfy.repository.OrcamentoRepository;
import br.com.ufape.spendfy.repository.TransacaoRepository;
import br.com.ufape.spendfy.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertaScheduler {

    private static final BigDecimal LIMITE_SALDO_BAIXO = BigDecimal.valueOf(100);

    private final UsuarioRepository usuarioRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final ContaRepository contaRepository;
    private final TransacaoRepository transacaoRepository;
    private final AlertaService alertaService;

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void verificarAlertas() {
        log.info("Iniciando verificação de alertas financeiros");
        usuarioRepository.findAll().forEach(usuario -> {
            verificarOrcamentos(usuario);
            verificarSaldoBaixo(usuario);
        });
    }

    private void verificarOrcamentos(Usuario usuario) {
        LocalDate hoje = LocalDate.now();
        List<Orcamento> orcamentos = orcamentoRepository.findByUsuarioId(usuario.getId()).stream()
                .filter(o -> !o.getDataFim().isBefore(hoje) && !o.getDataInicio().isAfter(hoje))
                .toList();

        for (Orcamento orcamento : orcamentos) {
            if (orcamento.getValorLimite().compareTo(BigDecimal.ZERO) == 0) continue;

            BigDecimal valorGasto = Objects.requireNonNullElse(
                    transacaoRepository.sumValorByUsuarioIdAndCategoriaIdAndTipoAndPeriodo(
                            usuario.getId(),
                            orcamento.getCategoria().getId(),
                            TipoTransacao.DESPESA,
                            orcamento.getDataInicio(),
                            orcamento.getDataFim()),
                    BigDecimal.ZERO);

            BigDecimal percentual = valorGasto.multiply(BigDecimal.valueOf(100))
                    .divide(orcamento.getValorLimite(), 2, RoundingMode.HALF_UP);

            if (percentual.compareTo(BigDecimal.valueOf(100)) >= 0) {
                alertaService.criarAlerta(usuario, TipoAlerta.ORCAMENTO_ESTOURADO,
                        "O orçamento de " + orcamento.getCategoria().getNome() +
                                " foi ultrapassado! Gasto: R$ " + valorGasto.setScale(2, RoundingMode.HALF_UP) +
                                " de R$ " + orcamento.getValorLimite().setScale(2, RoundingMode.HALF_UP),
                        orcamento.getId());
            } else if (percentual.compareTo(BigDecimal.valueOf(80)) >= 0) {
                alertaService.criarAlerta(usuario, TipoAlerta.ORCAMENTO_80_PERCENT,
                        "Você já utilizou " + percentual + "% do orçamento de " +
                                orcamento.getCategoria().getNome(),
                        orcamento.getId());
            }
        }
    }

    private void verificarSaldoBaixo(Usuario usuario) {
        for (Conta conta : contaRepository.findByUsuarioId(usuario.getId())) {
            BigDecimal receitas = Objects.requireNonNullElse(
                    transacaoRepository.sumValorByContaIdAndTipo(conta.getId(), TipoTransacao.RECEITA),
                    BigDecimal.ZERO);
            BigDecimal despesas = Objects.requireNonNullElse(
                    transacaoRepository.sumValorByContaIdAndTipo(conta.getId(), TipoTransacao.DESPESA),
                    BigDecimal.ZERO);
            BigDecimal saldo = conta.getSaldoInicial().add(receitas).subtract(despesas);

            if (saldo.compareTo(LIMITE_SALDO_BAIXO) < 0) {
                alertaService.criarAlerta(usuario, TipoAlerta.SALDO_BAIXO,
                        "Saldo baixo na conta \"" + conta.getNome() +
                                "\": R$ " + saldo.setScale(2, RoundingMode.HALF_UP),
                        conta.getId());
            }
        }
    }
}
