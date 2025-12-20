package br.com.ufape.spendfy.service;

import br.com.ufape.spendfy.dto.orcamento.OrcamentoRequest;
import br.com.ufape.spendfy.dto.orcamento.OrcamentoResponse;
import br.com.ufape.spendfy.entity.Categoria;
import br.com.ufape.spendfy.entity.Orcamento;
import br.com.ufape.spendfy.entity.Usuario;
import br.com.ufape.spendfy.exception.BusinessException;
import br.com.ufape.spendfy.exception.ResourceNotFoundException;
import br.com.ufape.spendfy.repository.CategoriaRepository;
import br.com.ufape.spendfy.repository.OrcamentoRepository;
import br.com.ufape.spendfy.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrcamentoService {

    private final OrcamentoRepository orcamentoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    private Usuario getUsuarioAutenticado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    @Transactional
    public OrcamentoResponse criar(OrcamentoRequest request) {
        Usuario usuario = getUsuarioAutenticado();

        if (request.getDataFim().isBefore(request.getDataInicio())) {
            throw new BusinessException("Data de fim não pode ser anterior à data de início");
        }

        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", "id", request.getIdCategoria()));

        if (!categoria.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Categoria não pertence ao usuário autenticado");
        }

        List<Orcamento> overlapping = orcamentoRepository.findOverlappingOrcamentos(
                usuario.getId(),
                request.getIdCategoria(),
                request.getDataInicio(),
                request.getDataFim()
        );

        if (!overlapping.isEmpty()) {
            throw new BusinessException("Já existe um orçamento para esta categoria no período informado");
        }

        Orcamento orcamento = Orcamento.builder()
                .valorLimite(request.getValorLimite())
                .dataInicio(request.getDataInicio())
                .dataFim(request.getDataFim())
                .usuario(usuario)
                .categoria(categoria)
                .build();

        orcamento = orcamentoRepository.save(orcamento);

        return toResponse(orcamento);
    }

    public List<OrcamentoResponse> listarTodos() {
        Usuario usuario = getUsuarioAutenticado();
        return orcamentoRepository.findByUsuarioId(usuario.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public OrcamentoResponse buscarPorId(Long id) {
        Usuario usuario = getUsuarioAutenticado();
        Orcamento orcamento = orcamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orcamento", "id", id));

        if (!orcamento.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Orçamento não pertence ao usuário autenticado");
        }

        return toResponse(orcamento);
    }

    @Transactional
    public OrcamentoResponse atualizar(Long id, OrcamentoRequest request) {
        Usuario usuario = getUsuarioAutenticado();
        Orcamento orcamento = orcamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orcamento", "id", id));

        if (!orcamento.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Orçamento não pertence ao usuário autenticado");
        }

        if (request.getDataFim().isBefore(request.getDataInicio())) {
            throw new BusinessException("Data de fim não pode ser anterior à data de início");
        }

        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", "id", request.getIdCategoria()));

        if (!categoria.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Categoria não pertence ao usuário autenticado");
        }

        List<Orcamento> overlapping = orcamentoRepository.findOverlappingOrcamentos(
                usuario.getId(),
                request.getIdCategoria(),
                request.getDataInicio(),
                request.getDataFim()
        );

        overlapping = overlapping.stream()
                .filter(o -> !o.getId().equals(id))
                .collect(Collectors.toList());

        if (!overlapping.isEmpty()) {
            throw new BusinessException("Já existe um orçamento para esta categoria no período informado");
        }

        orcamento.setValorLimite(request.getValorLimite());
        orcamento.setDataInicio(request.getDataInicio());
        orcamento.setDataFim(request.getDataFim());
        orcamento.setCategoria(categoria);

        orcamento = orcamentoRepository.save(orcamento);

        return toResponse(orcamento);
    }

    @Transactional
    public void deletar(Long id) {
        Usuario usuario = getUsuarioAutenticado();
        Orcamento orcamento = orcamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orcamento", "id", id));

        if (!orcamento.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Orçamento não pertence ao usuário autenticado");
        }

        orcamentoRepository.delete(orcamento);
    }

    private OrcamentoResponse toResponse(Orcamento orcamento) {
        return OrcamentoResponse.builder()
                .id(orcamento.getId())
                .valorLimite(orcamento.getValorLimite())
                .dataInicio(orcamento.getDataInicio())
                .dataFim(orcamento.getDataFim())
                .idUsuario(orcamento.getUsuario().getId())
                .idCategoria(orcamento.getCategoria().getId())
                .nomeCategoria(orcamento.getCategoria().getNome())
                .dataCadastro(orcamento.getDataCadastro())
                .dataAtualizacao(orcamento.getDataAtualizacao())
                .build();
    }
}
