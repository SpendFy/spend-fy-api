package br.com.ufape.spendfy.service;

import br.com.ufape.spendfy.dto.transacao.TransacaoRequest;
import br.com.ufape.spendfy.dto.transacao.TransacaoResponse;
import br.com.ufape.spendfy.entity.Categoria;
import br.com.ufape.spendfy.entity.Conta;
import br.com.ufape.spendfy.entity.Transacao;
import br.com.ufape.spendfy.entity.Usuario;
import br.com.ufape.spendfy.exception.BusinessException;
import br.com.ufape.spendfy.exception.ResourceNotFoundException;
import br.com.ufape.spendfy.repository.CategoriaRepository;
import br.com.ufape.spendfy.repository.ContaRepository;
import br.com.ufape.spendfy.repository.TransacaoRepository;
import br.com.ufape.spendfy.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", "id", request.getIdCategoria()));

        if (!categoria.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Categoria não pertence ao usuário autenticado");
        }

        Transacao transacao = Transacao.builder()
                .tipo(request.getTipo())
                .data(request.getData())
                .valor(request.getValor())
                .descricao(request.getDescricao())
                .observacao(request.getObservacao())
                .status(request.getStatus())
                .usuario(usuario)
                .conta(conta)
                .categoria(categoria)
                .build();

        transacao = transacaoRepository.save(transacao);

        return toResponse(transacao);
    }

    public List<TransacaoResponse> listarTodas() {
        Usuario usuario = getUsuarioAutenticado();
        return transacaoRepository.findByUsuarioId(usuario.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

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
                .idUsuario(transacao.getUsuario().getId())
                .idConta(transacao.getConta().getId())
                .nomeConta(transacao.getConta().getNome())
                .idCategoria(transacao.getCategoria().getId())
                .nomeCategoria(transacao.getCategoria().getNome())
                .dataCadastro(transacao.getDataCadastro())
                .dataAtualizacao(transacao.getDataAtualizacao())
                .build();
    }
}
