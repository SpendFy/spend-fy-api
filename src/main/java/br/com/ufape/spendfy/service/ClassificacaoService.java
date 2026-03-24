package br.com.ufape.spendfy.service;

import br.com.ufape.spendfy.component.AuthenticatedUserResolver;
import br.com.ufape.spendfy.dto.classificacao.ClassificacaoRequest;
import br.com.ufape.spendfy.dto.classificacao.ClassificacaoResponse;
import br.com.ufape.spendfy.entity.Categoria;
import br.com.ufape.spendfy.entity.Usuario;
import br.com.ufape.spendfy.exception.BusinessException;
import br.com.ufape.spendfy.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassificacaoService {

    private final AuthenticatedUserResolver userResolver;
    private final CategoriaRepository categoriaRepository;
    private final ClaudeApiService claudeApiService;

    @Transactional(readOnly = true)
    public ClassificacaoResponse classificar(ClassificacaoRequest request) {
        Usuario usuario = userResolver.getUsuarioAutenticado();

        List<Categoria> categorias = categoriaRepository.findByUsuarioId(usuario.getId());
        if (categorias.isEmpty()) {
            throw new BusinessException("Usuário não possui categorias cadastradas para classificação");
        }

        String listaCategorias = categorias.stream()
                .map(Categoria::getNome)
                .collect(Collectors.joining(", "));

        String prompt = String.format("""
                Você é um classificador de transações financeiras.
                Dado a descrição de uma transação, identifique qual categoria melhor se encaixa.

                Categorias disponíveis: %s

                Descrição da transação: "%s"

                Responda APENAS com o nome exato de uma das categorias listadas acima.
                Não adicione explicações, apenas o nome da categoria.
                """, listaCategorias, request.getDescricao());

        String nomeCategoriaSugerida = claudeApiService.chat(prompt).trim();

        Categoria categoriaEncontrada = categorias.stream()
                .filter(c -> c.getNome().equalsIgnoreCase(nomeCategoriaSugerida))
                .findFirst()
                .orElse(categorias.get(0));

        return ClassificacaoResponse.builder()
                .idCategoria(categoriaEncontrada.getId())
                .nomeCategoria(categoriaEncontrada.getNome())
                .cor(categoriaEncontrada.getCor())
                .justificativa("Categoria sugerida com base na descrição: \"" + request.getDescricao() + "\"")
                .build();
    }
}
