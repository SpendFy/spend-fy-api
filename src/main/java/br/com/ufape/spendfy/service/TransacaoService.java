package br.com.ufape.spendfy.service;

import br.com.ufape.spendfy.dto.transacao.TransacaoRequest;
import br.com.ufape.spendfy.dto.transacao.TransacaoResponse;
import br.com.ufape.spendfy.entity.Categoria;
import br.com.ufape.spendfy.entity.Conta;
import br.com.ufape.spendfy.entity.Transacao;
import br.com.ufape.spendfy.entity.Usuario;
import br.com.ufape.spendfy.entity.enums.RecorrenciaTransacao;
import br.com.ufape.spendfy.entity.enums.StatusTransacao;
import br.com.ufape.spendfy.entity.enums.TipoTransacao;
import br.com.ufape.spendfy.exception.BusinessException;
import br.com.ufape.spendfy.exception.ResourceNotFoundException;
import br.com.ufape.spendfy.repository.CategoriaRepository;
import br.com.ufape.spendfy.repository.ContaRepository;
import br.com.ufape.spendfy.repository.TransacaoRepository;
import br.com.ufape.spendfy.repository.UsuarioRepository;
import br.com.ufape.spendfy.specification.TransacaoSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final ContaRepository contaRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    private Usuario getUsuarioAutenticado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    @Transactional
    public TransacaoResponse criar(TransacaoRequest request) {
        Usuario usuario = getUsuarioAutenticado();

        Conta conta = contaRepository.findById(request.getIdConta())
                .orElseThrow(() -> new ResourceNotFoundException("Conta", "id", request.getIdConta()));

        if (!conta.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Conta não pertence ao usuário autenticado");
        }

        if (TipoTransacao.DESPESA.equals(request.getTipo())) {
            BigDecimal receitas = transacaoRepository.sumValorByContaIdAndTipo(conta.getId(), TipoTransacao.RECEITA);
            BigDecimal despesas = transacaoRepository.sumValorByContaIdAndTipo(conta.getId(), TipoTransacao.DESPESA);
            BigDecimal saldoDisponivel = conta.getSaldoInicial().add(receitas).subtract(despesas);

            if (request.getValor().compareTo(saldoDisponivel) > 0) {
                throw new BusinessException("Saldo insuficiente na conta " + conta.getNome() +
                                            ". Saldo disponível: R$ " + saldoDisponivel);
            }
        }

        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", "id", request.getIdCategoria()));

        if (!categoria.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Categoria não pertence ao usuário autenticado");
        }

        RecorrenciaTransacao recorrencia = request.getRecorrencia() != null
                ? request.getRecorrencia() : RecorrenciaTransacao.NENHUMA;

        LocalDate dataProximaOcorrencia = null;
        if (recorrencia != RecorrenciaTransacao.NENHUMA) {
            dataProximaOcorrencia = calcularProximaOcorrencia(request.getData(), recorrencia);
        }

        Transacao transacao = Transacao.builder()
                .tipo(request.getTipo())
                .data(request.getData())
                .valor(request.getValor())
                .descricao(request.getDescricao())
                .observacao(request.getObservacao())
                .status(request.getStatus())
                .recorrencia(recorrencia)
                .dataProximaOcorrencia(dataProximaOcorrencia)
                .usuario(usuario)
                .conta(conta)
                .categoria(categoria)
                .build();

        transacao = transacaoRepository.save(transacao);

        return toResponse(transacao);
    }

    @Transactional(readOnly = true)
    public List<TransacaoResponse> listarTodas() {
        Usuario usuario = getUsuarioAutenticado();
        return transacaoRepository.findByUsuarioId(usuario.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<TransacaoResponse> listarTodas(Pageable pageable) {
        Usuario usuario = getUsuarioAutenticado();
        return transacaoRepository.findByUsuarioId(usuario.getId(), pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<TransacaoResponse> listarComFiltros(TipoTransacao tipo, StatusTransacao status,
            Long categoriaId, Long contaId, LocalDate dataInicio, LocalDate dataFim, Pageable pageable) {
        Usuario usuario = getUsuarioAutenticado();
        Specification<Transacao> spec = Specification.where(TransacaoSpecification.doUsuario(usuario.getId()))
                .and(TransacaoSpecification.comTipo(tipo))
                .and(TransacaoSpecification.comStatus(status))
                .and(TransacaoSpecification.daCategoria(categoriaId))
                .and(TransacaoSpecification.daConta(contaId))
                .and(TransacaoSpecification.dataInicio(dataInicio))
                .and(TransacaoSpecification.dataFim(dataFim));
        return transacaoRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public TransacaoResponse buscarPorId(Long id) {
        Usuario usuario = getUsuarioAutenticado();
        Transacao transacao = transacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transacao", "id", id));

        if (!transacao.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Transação não pertence ao usuário autenticado");
        }

        return toResponse(transacao);
    }

    @Transactional
    public TransacaoResponse atualizar(Long id, TransacaoRequest request) {
        Usuario usuario = getUsuarioAutenticado();
        Transacao transacao = transacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transacao", "id", id));

        if (!transacao.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Transação não pertence ao usuário autenticado");
        }

        Conta conta = contaRepository.findById(request.getIdConta())
                .orElseThrow(() -> new ResourceNotFoundException("Conta", "id", request.getIdConta()));

        if (!conta.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Conta não pertence ao usuário autenticado");
        }

        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", "id", request.getIdCategoria()));

        if (!categoria.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Categoria não pertence ao usuário autenticado");
        }

        transacao.setTipo(request.getTipo());
        transacao.setData(request.getData());
        transacao.setValor(request.getValor());
        transacao.setDescricao(request.getDescricao());
        transacao.setObservacao(request.getObservacao());
        transacao.setStatus(request.getStatus());
        transacao.setConta(conta);
        transacao.setCategoria(categoria);

        transacao = transacaoRepository.save(transacao);

        return toResponse(transacao);
    }

    @Transactional
    public void deletar(Long id) {
        Usuario usuario = getUsuarioAutenticado();
        Transacao transacao = transacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transacao", "id", id));

        if (!transacao.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Transação não pertence ao usuário autenticado");
        }

        transacaoRepository.delete(transacao);
    }

    private TransacaoResponse toResponse(Transacao transacao) {
        return TransacaoResponse.builder()
                .id(transacao.getId())
                .tipo(transacao.getTipo())
                .data(transacao.getData())
                .valor(transacao.getValor())
                .descricao(transacao.getDescricao())
                .observacao(transacao.getObservacao())
                .status(transacao.getStatus())
                .recorrencia(transacao.getRecorrencia())
                .dataProximaOcorrencia(transacao.getDataProximaOcorrencia())
                .idUsuario(transacao.getUsuario().getId())
                .idConta(transacao.getConta().getId())
                .nomeConta(transacao.getConta().getNome())
                .idCategoria(transacao.getCategoria().getId())
                .nomeCategoria(transacao.getCategoria().getNome())
                .dataCadastro(transacao.getDataCadastro())
                .dataAtualizacao(transacao.getDataAtualizacao())
                .build();
    }

    private LocalDate calcularProximaOcorrencia(LocalDate data, RecorrenciaTransacao recorrencia) {
        return switch (recorrencia) {
            case DIARIA -> data.plusDays(1);
            case SEMANAL -> data.plusWeeks(1);
            case MENSAL -> data.plusMonths(1);
            case ANUAL -> data.plusYears(1);
            default -> null;
        };
    }
}
