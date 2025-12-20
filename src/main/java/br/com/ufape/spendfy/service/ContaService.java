package br.com.ufape.spendfy.service;

import br.com.ufape.spendfy.dto.conta.ContaRequest;
import br.com.ufape.spendfy.dto.conta.ContaResponse;
import br.com.ufape.spendfy.entity.Conta;
import br.com.ufape.spendfy.entity.Usuario;
import br.com.ufape.spendfy.exception.BusinessException;
import br.com.ufape.spendfy.exception.ResourceNotFoundException;
import br.com.ufape.spendfy.repository.ContaRepository;
import br.com.ufape.spendfy.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContaService {

    private final ContaRepository contaRepository;
    private final UsuarioRepository usuarioRepository;

    private Usuario getUsuarioAutenticado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    @Transactional
    public ContaResponse criar(ContaRequest request) {
        Usuario usuario = getUsuarioAutenticado();

        if (contaRepository.existsByNomeAndUsuarioId(request.getNome(), usuario.getId())) {
            throw new BusinessException("Já existe uma conta com este nome");
        }

        Conta conta = Conta.builder()
                .nome(request.getNome())
                .tipo(request.getTipo())
                .saldoInicial(request.getSaldoInicial())
                .usuario(usuario)
                .build();

        conta = contaRepository.save(conta);

        return toResponse(conta);
    }

    public List<ContaResponse> listarTodas() {
        Usuario usuario = getUsuarioAutenticado();
        return contaRepository.findByUsuarioId(usuario.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ContaResponse buscarPorId(Long id) {
        Usuario usuario = getUsuarioAutenticado();
        Conta conta = contaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta", "id", id));

        if (!conta.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Conta não pertence ao usuário autenticado");
        }

        return toResponse(conta);
    }

    @Transactional
    public ContaResponse atualizar(Long id, ContaRequest request) {
        Usuario usuario = getUsuarioAutenticado();
        Conta conta = contaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta", "id", id));

        if (!conta.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Conta não pertence ao usuário autenticado");
        }

        if (!conta.getNome().equals(request.getNome()) &&
                contaRepository.existsByNomeAndUsuarioId(request.getNome(), usuario.getId())) {
            throw new BusinessException("Já existe uma conta com este nome");
        }

        conta.setNome(request.getNome());
        conta.setTipo(request.getTipo());
        conta.setSaldoInicial(request.getSaldoInicial());

        conta = contaRepository.save(conta);

        return toResponse(conta);
    }

    @Transactional
    public void deletar(Long id) {
        Usuario usuario = getUsuarioAutenticado();
        Conta conta = contaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta", "id", id));

        if (!conta.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Conta não pertence ao usuário autenticado");
        }

        contaRepository.delete(conta);
    }

    private ContaResponse toResponse(Conta conta) {
        return ContaResponse.builder()
                .id(conta.getId())
                .nome(conta.getNome())
                .tipo(conta.getTipo())
                .saldoInicial(conta.getSaldoInicial())
                .idUsuario(conta.getUsuario().getId())
                .dataCadastro(conta.getDataCadastro())
                .dataAtualizacao(conta.getDataAtualizacao())
                .build();
    }
}
