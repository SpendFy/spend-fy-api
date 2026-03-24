package br.com.ufape.spendfy.service;

import br.com.ufape.spendfy.entity.Transacao;
import br.com.ufape.spendfy.enums.RecorrenciaTransacao;
import br.com.ufape.spendfy.repository.TransacaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransacaoRecorrenciaScheduler {

    private final TransacaoRepository transacaoRepository;

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void processarRecorrencias() {
        LocalDate hoje = LocalDate.now();
        List<Transacao> recorrentes = transacaoRepository
                .findByRecorrenciaNotAndDataProximaOcorrenciaLessThanEqual(
                        RecorrenciaTransacao.NENHUMA, hoje);

        log.info("Processando {} transações recorrentes", recorrentes.size());

        for (Transacao original : recorrentes) {
            clonarTransacao(original, hoje);
        }
    }

    private void clonarTransacao(Transacao original, LocalDate hoje) {
        LocalDate proximaData = original.getDataProximaOcorrencia();

        while (proximaData != null && !proximaData.isAfter(hoje)) {
            Transacao clone = Transacao.builder()
                    .tipo(original.getTipo())
                    .data(proximaData)
                    .valor(original.getValor())
                    .descricao(original.getDescricao())
                    .observacao(original.getObservacao())
                    .status(original.getStatus())
                    .recorrencia(RecorrenciaTransacao.NENHUMA)
                    .usuario(original.getUsuario())
                    .conta(original.getConta())
                    .categoria(original.getCategoria())
                    .build();

            transacaoRepository.save(clone);

            proximaData = calcularProxima(proximaData, original.getRecorrencia());
        }

        original.setDataProximaOcorrencia(proximaData);
        transacaoRepository.save(original);
    }

    private LocalDate calcularProxima(LocalDate data, RecorrenciaTransacao recorrencia) {
        return switch (recorrencia) {
            case DIARIA -> data.plusDays(1);
            case SEMANAL -> data.plusWeeks(1);
            case MENSAL -> data.plusMonths(1);
            case ANUAL -> data.plusYears(1);
            default -> null;
        };
    }
}
