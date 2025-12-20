package br.com.ufape.spendfy.service;

import br.com.ufape.spendfy.dto.categoria.CategoriaRequest;
import br.com.ufape.spendfy.dto.categoria.CategoriaResponse;
import br.com.ufape.spendfy.entity.Categoria;
import br.com.ufape.spendfy.entity.Usuario;
import br.com.ufape.spendfy.exception.BusinessException;
import br.com.ufape.spendfy.exception.ResourceNotFoundException;
import br.com.ufape.spendfy.repository.CategoriaRepository;
import br.com.ufape.spendfy.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    private Usuario getUsuarioAutenticado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    @Transactional
    public CategoriaResponse criar(CategoriaRequest request) {
        Usuario usuario = getUsuarioAutenticado();

        if (categoriaRepository.existsByNomeAndUsuarioId(request.getNome(), usuario.getId())) {
            throw new BusinessException("Já existe uma categoria com este nome");
        }

        Categoria categoria = Categoria.builder()
                .nome(request.getNome())
                .cor(request.getCor())
                .usuario(usuario)
                .build();

        categoria = categoriaRepository.save(categoria);

        return toResponse(categoria);
    }

    public List<CategoriaResponse> listarTodas() {
        Usuario usuario = getUsuarioAutenticado();
        return categoriaRepository.findByUsuarioId(usuario.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CategoriaResponse buscarPorId(Long id) {
        Usuario usuario = getUsuarioAutenticado();
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", "id", id));

        if (!categoria.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Categoria não pertence ao usuário autenticado");
        }

        return toResponse(categoria);
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
        Usuario usuario = getUsuarioAutenticado();
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", "id", id));

        if (!categoria.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Categoria não pertence ao usuário autenticado");
        }

        if (!categoria.getNome().equals(request.getNome()) &&
                categoriaRepository.existsByNomeAndUsuarioId(request.getNome(), usuario.getId())) {
            throw new BusinessException("Já existe uma categoria com este nome");
        }

        categoria.setNome(request.getNome());
        categoria.setCor(request.getCor());

        categoria = categoriaRepository.save(categoria);

        return toResponse(categoria);
    }

    @Transactional
    public void deletar(Long id) {
        Usuario usuario = getUsuarioAutenticado();
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", "id", id));

        if (!categoria.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Categoria não pertence ao usuário autenticado");
        }

        categoriaRepository.delete(categoria);
    }

    private CategoriaResponse toResponse(Categoria categoria) {
        return CategoriaResponse.builder()
                .id(categoria.getId())
                .nome(categoria.getNome())
                .cor(categoria.getCor())
                .idUsuario(categoria.getUsuario().getId())
                .dataCadastro(categoria.getDataCadastro())
                .dataAtualizacao(categoria.getDataAtualizacao())
                .build();
    }
}
