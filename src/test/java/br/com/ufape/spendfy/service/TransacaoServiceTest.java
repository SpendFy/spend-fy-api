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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - TransacaoService")
class TransacaoServiceTest {

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TransacaoService transacaoService;

    private Usuario usuario;
    private Conta conta;
    private Categoria categoria;
    private Transacao transacao;
    private TransacaoRequest transacaoRequest;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .nome("João Silva")
                .email("joao@email.com")
                .senha("senha123")
                .status("ATIVO")
                .build();

        conta = Conta.builder()
                .id(1L)
                .nome("Conta Corrente")
                .tipo("Corrente")
                .saldoInicial(BigDecimal.valueOf(1000.00))
                .usuario(usuario)
                .build();

        categoria = Categoria.builder()
                .id(1L)
                .nome("Alimentação")
                .cor("verde")
                .usuario(usuario)
                .build();

        transacao = Transacao.builder()
                .id(1L)
                .tipo("DESPESA")
                .data(LocalDate.now())
                .valor(BigDecimal.valueOf(50.00))
                .descricao("Supermercado")
                .observacao("Compras do mês")
                .status("CONFIRMADA")
                .usuario(usuario)
                .conta(conta)
                .categoria(categoria)
                .dataCadastro(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();

        transacaoRequest = TransacaoRequest.builder()
                .tipo("DESPESA")
                .data(LocalDate.now())
                .valor(BigDecimal.valueOf(50.00))
                .descricao("Supermercado")
                .observacao("Compras do mês")
                .status("CONFIRMADA")
                .idConta(1L)
                .idCategoria(1L)
                .build();

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("joao@email.com");
        when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    }

    @Test
    @DisplayName("Deve criar transação com sucesso")
    void deveCriarTransacaoComSucesso() {
        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(transacaoRepository.save(any(Transacao.class))).thenReturn(transacao);

        TransacaoResponse response = transacaoService.criar(transacaoRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTipo()).isEqualTo("DESPESA");
        assertThat(response.getValor()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
        assertThat(response.getDescricao()).isEqualTo("Supermercado");
        assertThat(response.getStatus()).isEqualTo("CONFIRMADA");
        assertThat(response.getIdConta()).isEqualTo(1L);
        assertThat(response.getIdCategoria()).isEqualTo(1L);

        verify(transacaoRepository, times(1)).save(any(Transacao.class));
    }

    @Test
    @DisplayName("Deve criar transação de receita com sucesso")
    void deveCriarTransacaoReceitaComSucesso() {
        transacaoRequest.setTipo("RECEITA");
        transacao.setTipo("RECEITA");
        transacao.setDescricao("Salário");
        transacao.setValor(BigDecimal.valueOf(5000.00));

        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(transacaoRepository.save(any(Transacao.class))).thenReturn(transacao);

        TransacaoResponse response = transacaoService.criar(transacaoRequest);

        assertThat(response).isNotNull();
        assertThat(response.getTipo()).isEqualTo("RECEITA");
        verify(transacaoRepository, times(1)).save(any(Transacao.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar transação com conta inexistente")
    void deveLancarExcecaoAoCriarTransacaoComContaInexistente() {
        when(contaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transacaoService.criar(transacaoRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Conta");

        verify(transacaoRepository, never()).save(any(Transacao.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar transação com categoria inexistente")
    void deveLancarExcecaoAoCriarTransacaoComCategoriaInexistente() {
        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transacaoService.criar(transacaoRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Categoria");

        verify(transacaoRepository, never()).save(any(Transacao.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar transação com conta de outro usuário")
    void deveLancarExcecaoAoCriarTransacaoComContaDeOutroUsuario() {
        Usuario outroUsuario = Usuario.builder()
                .id(2L)
                .nome("Maria")
                .email("maria@email.com")
                .build();

        conta.setUsuario(outroUsuario);

        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> transacaoService.criar(transacaoRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Conta não pertence ao usuário autenticado");

        verify(transacaoRepository, never()).save(any(Transacao.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar transação com categoria de outro usuário")
    void deveLancarExcecaoAoCriarTransacaoComCategoriaDeOutroUsuario() {
        Usuario outroUsuario = Usuario.builder()
                .id(2L)
                .nome("Maria")
                .email("maria@email.com")
                .build();

        categoria.setUsuario(outroUsuario);

        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        assertThatThrownBy(() -> transacaoService.criar(transacaoRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Categoria não pertence ao usuário autenticado");

        verify(transacaoRepository, never()).save(any(Transacao.class));
    }

    @Test
    @DisplayName("Deve listar todas as transações do usuário")
    void deveListarTodasTransacoesDoUsuario() {
        Transacao transacao2 = Transacao.builder()
                .id(2L)
                .tipo("RECEITA")
                .data(LocalDate.now())
                .valor(BigDecimal.valueOf(1000.00))
                .descricao("Salário")
                .status("CONFIRMADA")
                .usuario(usuario)
                .conta(conta)
                .categoria(categoria)
                .build();

        when(transacaoRepository.findByUsuarioId(1L))
                .thenReturn(Arrays.asList(transacao, transacao2));

        List<TransacaoResponse> responses = transacaoService.listarTodas();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getTipo()).isEqualTo("DESPESA");
        assertThat(responses.get(1).getTipo()).isEqualTo("RECEITA");
    }

    @Test
    @DisplayName("Deve buscar transação por ID com sucesso")
    void deveBuscarTransacaoPorIdComSucesso() {
        when(transacaoRepository.findById(1L)).thenReturn(Optional.of(transacao));

        TransacaoResponse response = transacaoService.buscarPorId(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getDescricao()).isEqualTo("Supermercado");
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar transação inexistente")
    void deveLancarExcecaoAoBuscarTransacaoInexistente() {
        when(transacaoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transacaoService.buscarPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transacao");
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar transação de outro usuário")
    void deveLancarExcecaoAoBuscarTransacaoDeOutroUsuario() {
        Usuario outroUsuario = Usuario.builder()
                .id(2L)
                .nome("Maria")
                .email("maria@email.com")
                .build();

        transacao.setUsuario(outroUsuario);

        when(transacaoRepository.findById(1L)).thenReturn(Optional.of(transacao));

        assertThatThrownBy(() -> transacaoService.buscarPorId(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Transação não pertence ao usuário autenticado");
    }

    @Test
    @DisplayName("Deve atualizar transação com sucesso")
    void deveAtualizarTransacaoComSucesso() {
        TransacaoRequest requestAtualizado = TransacaoRequest.builder()
                .tipo("DESPESA")
                .data(LocalDate.now())
                .valor(BigDecimal.valueOf(75.00))
                .descricao("Farmácia")
                .observacao("Remédios")
                .status("CONFIRMADA")
                .idConta(1L)
                .idCategoria(1L)
                .build();

        when(transacaoRepository.findById(1L)).thenReturn(Optional.of(transacao));
        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(transacaoRepository.save(any(Transacao.class))).thenReturn(transacao);

        TransacaoResponse response = transacaoService.atualizar(1L, requestAtualizado);

        assertThat(response).isNotNull();
        verify(transacaoRepository, times(1)).save(any(Transacao.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar transação de outro usuário")
    void deveLancarExcecaoAoAtualizarTransacaoDeOutroUsuario() {
        Usuario outroUsuario = Usuario.builder()
                .id(2L)
                .nome("Maria")
                .email("maria@email.com")
                .build();

        transacao.setUsuario(outroUsuario);

        when(transacaoRepository.findById(1L)).thenReturn(Optional.of(transacao));

        assertThatThrownBy(() -> transacaoService.atualizar(1L, transacaoRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Transação não pertence ao usuário autenticado");

        verify(transacaoRepository, never()).save(any(Transacao.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar com conta de outro usuário")
    void deveLancarExcecaoAoAtualizarComContaDeOutroUsuario() {
        Usuario outroUsuario = Usuario.builder()
                .id(2L)
                .nome("Maria")
                .email("maria@email.com")
                .build();

        Conta contaOutroUsuario = Conta.builder()
                .id(2L)
                .nome("Outra Conta")
                .usuario(outroUsuario)
                .build();

        transacaoRequest.setIdConta(2L);

        when(transacaoRepository.findById(1L)).thenReturn(Optional.of(transacao));
        when(contaRepository.findById(2L)).thenReturn(Optional.of(contaOutroUsuario));

        assertThatThrownBy(() -> transacaoService.atualizar(1L, transacaoRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Conta não pertence ao usuário autenticado");

        verify(transacaoRepository, never()).save(any(Transacao.class));
    }

    @Test
    @DisplayName("Deve deletar transação com sucesso")
    void deveDeletarTransacaoComSucesso() {
        when(transacaoRepository.findById(1L)).thenReturn(Optional.of(transacao));

        transacaoService.deletar(1L);

        verify(transacaoRepository, times(1)).delete(transacao);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar transação inexistente")
    void deveLancarExcecaoAoDeletarTransacaoInexistente() {
        when(transacaoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transacaoService.deletar(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transacao");

        verify(transacaoRepository, never()).delete(any(Transacao.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar transação de outro usuário")
    void deveLancarExcecaoAoDeletarTransacaoDeOutroUsuario() {
        Usuario outroUsuario = Usuario.builder()
                .id(2L)
                .nome("Maria")
                .email("maria@email.com")
                .build();

        transacao.setUsuario(outroUsuario);

        when(transacaoRepository.findById(1L)).thenReturn(Optional.of(transacao));

        assertThatThrownBy(() -> transacaoService.deletar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Transação não pertence ao usuário autenticado");

        verify(transacaoRepository, never()).delete(any(Transacao.class));
    }

    @Test
    @DisplayName("Deve criar transação com valor decimal preciso")
    void deveCriarTransacaoComValorDecimalPreciso() {
        transacaoRequest.setValor(BigDecimal.valueOf(123.45));
        transacao.setValor(BigDecimal.valueOf(123.45));

        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(transacaoRepository.save(any(Transacao.class))).thenReturn(transacao);

        TransacaoResponse response = transacaoService.criar(transacaoRequest);

        assertThat(response.getValor()).isEqualByComparingTo(BigDecimal.valueOf(123.45));
    }

    @Test
    @DisplayName("Deve criar transação sem descrição e observação")
    void deveCriarTransacaoSemDescricaoEObservacao() {
        transacaoRequest.setDescricao(null);
        transacaoRequest.setObservacao(null);
        transacao.setDescricao(null);
        transacao.setObservacao(null);

        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(transacaoRepository.save(any(Transacao.class))).thenReturn(transacao);

        TransacaoResponse response = transacaoService.criar(transacaoRequest);

        assertThat(response.getDescricao()).isNull();
        assertThat(response.getObservacao()).isNull();
    }
}
