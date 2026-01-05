package br.com.ufape.spendfy.service;

import br.com.ufape.spendfy.dto.conta.ContaRequest;
import br.com.ufape.spendfy.dto.conta.ContaResponse;
import br.com.ufape.spendfy.entity.Conta;
import br.com.ufape.spendfy.entity.Usuario;
import br.com.ufape.spendfy.exception.BusinessException;
import br.com.ufape.spendfy.exception.ResourceNotFoundException;
import br.com.ufape.spendfy.repository.ContaRepository;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - ContaService")
class ContaServiceTest {

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ContaService contaService;

    private Usuario usuario;
    private Conta conta;
    private ContaRequest contaRequest;

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
                .dataCadastro(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();

        contaRequest = ContaRequest.builder()
                .nome("Conta Corrente")
                .tipo("Corrente")
                .saldoInicial(BigDecimal.valueOf(1000.00))
                .build();

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("joao@email.com");
        when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    }

    @Test
    @DisplayName("Deve criar conta com sucesso")
    void deveCriarContaComSucesso() {
        when(contaRepository.existsByNomeAndUsuarioId("Conta Corrente", 1L)).thenReturn(false);
        when(contaRepository.save(any(Conta.class))).thenReturn(conta);

        ContaResponse response = contaService.criar(contaRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNome()).isEqualTo("Conta Corrente");
        assertThat(response.getTipo()).isEqualTo("Corrente");
        assertThat(response.getSaldoInicial()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
        assertThat(response.getIdUsuario()).isEqualTo(1L);

        verify(contaRepository, times(1)).save(any(Conta.class));
    }

    @Test
    @DisplayName("Deve criar conta com saldo inicial zero")
    void deveCriarContaComSaldoInicialZero() {
        contaRequest.setSaldoInicial(BigDecimal.ZERO);
        conta.setSaldoInicial(BigDecimal.ZERO);

        when(contaRepository.existsByNomeAndUsuarioId("Conta Corrente", 1L)).thenReturn(false);
        when(contaRepository.save(any(Conta.class))).thenReturn(conta);

        ContaResponse response = contaService.criar(contaRequest);

        assertThat(response.getSaldoInicial()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(contaRepository, times(1)).save(any(Conta.class));
    }

    @Test
    @DisplayName("Deve criar conta poupança")
    void deveCriarContaPoupanca() {
        contaRequest.setNome("Conta Poupança");
        contaRequest.setTipo("Poupança");
        conta.setNome("Conta Poupança");
        conta.setTipo("Poupança");

        when(contaRepository.existsByNomeAndUsuarioId("Conta Poupança", 1L)).thenReturn(false);
        when(contaRepository.save(any(Conta.class))).thenReturn(conta);

        ContaResponse response = contaService.criar(contaRequest);

        assertThat(response.getNome()).isEqualTo("Conta Poupança");
        assertThat(response.getTipo()).isEqualTo("Poupança");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar conta com nome duplicado")
    void deveLancarExcecaoAoCriarContaComNomeDuplicado() {
        when(contaRepository.existsByNomeAndUsuarioId("Conta Corrente", 1L)).thenReturn(true);

        assertThatThrownBy(() -> contaService.criar(contaRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Já existe uma conta com este nome");

        verify(contaRepository, never()).save(any(Conta.class));
    }

    @Test
    @DisplayName("Deve permitir mesmo nome de conta para usuários diferentes")
    void devePermitirMesmoNomeContaParaUsuariosDiferentes() {
        when(contaRepository.existsByNomeAndUsuarioId("Conta Corrente", 1L)).thenReturn(false);
        when(contaRepository.save(any(Conta.class))).thenReturn(conta);

        ContaResponse response = contaService.criar(contaRequest);

        assertThat(response).isNotNull();
        verify(contaRepository, times(1)).existsByNomeAndUsuarioId("Conta Corrente", 1L);
    }

    @Test
    @DisplayName("Deve listar todas as contas do usuário")
    void deveListarTodasContasDoUsuario() {
        Conta conta2 = Conta.builder()
                .id(2L)
                .nome("Conta Poupança")
                .tipo("Poupança")
                .saldoInicial(BigDecimal.valueOf(5000.00))
                .usuario(usuario)
                .build();

        when(contaRepository.findByUsuarioId(1L))
                .thenReturn(Arrays.asList(conta, conta2));

        List<ContaResponse> responses = contaService.listarTodas();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getNome()).isEqualTo("Conta Corrente");
        assertThat(responses.get(1).getNome()).isEqualTo("Conta Poupança");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando usuário não tem contas")
    void deveRetornarListaVaziaQuandoUsuarioNaoTemContas() {
        when(contaRepository.findByUsuarioId(1L)).thenReturn(Arrays.asList());

        List<ContaResponse> responses = contaService.listarTodas();

        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar conta por ID com sucesso")
    void deveBuscarContaPorIdComSucesso() {
        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));

        ContaResponse response = contaService.buscarPorId(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNome()).isEqualTo("Conta Corrente");
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar conta inexistente")
    void deveLancarExcecaoAoBuscarContaInexistente() {
        when(contaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contaService.buscarPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Conta");
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar conta de outro usuário")
    void deveLancarExcecaoAoBuscarContaDeOutroUsuario() {
        Usuario outroUsuario = Usuario.builder()
                .id(2L)
                .nome("Maria")
                .email("maria@email.com")
                .build();

        conta.setUsuario(outroUsuario);

        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> contaService.buscarPorId(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Conta não pertence ao usuário autenticado");
    }

    @Test
    @DisplayName("Deve atualizar conta com sucesso")
    void deveAtualizarContaComSucesso() {
        ContaRequest requestAtualizado = ContaRequest.builder()
                .nome("Conta Corrente Atualizada")
                .tipo("Corrente")
                .saldoInicial(BigDecimal.valueOf(1500.00))
                .build();

        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(contaRepository.existsByNomeAndUsuarioId("Conta Corrente Atualizada", 1L)).thenReturn(false);
        when(contaRepository.save(any(Conta.class))).thenReturn(conta);

        ContaResponse response = contaService.atualizar(1L, requestAtualizado);

        assertThat(response).isNotNull();
        verify(contaRepository, times(1)).save(any(Conta.class));
    }

    @Test
    @DisplayName("Deve atualizar conta mantendo mesmo nome")
    void deveAtualizarContaMantendoMesmoNome() {
        ContaRequest requestAtualizado = ContaRequest.builder()
                .nome("Conta Corrente")
                .tipo("Corrente Plus")
                .saldoInicial(BigDecimal.valueOf(2000.00))
                .build();

        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(contaRepository.save(any(Conta.class))).thenReturn(conta);

        ContaResponse response = contaService.atualizar(1L, requestAtualizado);

        assertThat(response).isNotNull();
        verify(contaRepository, never()).existsByNomeAndUsuarioId(anyString(), anyLong());
        verify(contaRepository, times(1)).save(any(Conta.class));
    }

    @Test
    @DisplayName("Deve atualizar saldo inicial da conta")
    void deveAtualizarSaldoInicialDaConta() {
        ContaRequest requestAtualizado = ContaRequest.builder()
                .nome("Conta Corrente")
                .tipo("Corrente")
                .saldoInicial(BigDecimal.valueOf(3000.00))
                .build();

        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(contaRepository.save(any(Conta.class))).thenReturn(conta);

        ContaResponse response = contaService.atualizar(1L, requestAtualizado);

        assertThat(response).isNotNull();
        verify(contaRepository, times(1)).save(any(Conta.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar conta com nome duplicado")
    void deveLancarExcecaoAoAtualizarContaComNomeDuplicado() {
        ContaRequest requestAtualizado = ContaRequest.builder()
                .nome("Conta Poupança")
                .tipo("Poupança")
                .saldoInicial(BigDecimal.valueOf(1000.00))
                .build();

        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(contaRepository.existsByNomeAndUsuarioId("Conta Poupança", 1L)).thenReturn(true);

        assertThatThrownBy(() -> contaService.atualizar(1L, requestAtualizado))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Já existe uma conta com este nome");

        verify(contaRepository, never()).save(any(Conta.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar conta de outro usuário")
    void deveLancarExcecaoAoAtualizarContaDeOutroUsuario() {
        Usuario outroUsuario = Usuario.builder()
                .id(2L)
                .nome("Maria")
                .email("maria@email.com")
                .build();

        conta.setUsuario(outroUsuario);

        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> contaService.atualizar(1L, contaRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Conta não pertence ao usuário autenticado");

        verify(contaRepository, never()).save(any(Conta.class));
    }

    @Test
    @DisplayName("Deve deletar conta com sucesso")
    void deveDeletarContaComSucesso() {
        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));

        contaService.deletar(1L);

        verify(contaRepository, times(1)).delete(conta);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar conta inexistente")
    void deveLancarExcecaoAoDeletarContaInexistente() {
        when(contaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contaService.deletar(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Conta");

        verify(contaRepository, never()).delete(any(Conta.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar conta de outro usuário")
    void deveLancarExcecaoAoDeletarContaDeOutroUsuario() {
        Usuario outroUsuario = Usuario.builder()
                .id(2L)
                .nome("Maria")
                .email("maria@email.com")
                .build();

        conta.setUsuario(outroUsuario);

        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> contaService.deletar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Conta não pertence ao usuário autenticado");

        verify(contaRepository, never()).delete(any(Conta.class));
    }

    @Test
    @DisplayName("Deve criar conta com valor decimal preciso")
    void deveCriarContaComValorDecimalPreciso() {
        contaRequest.setSaldoInicial(BigDecimal.valueOf(1234.56));
        conta.setSaldoInicial(BigDecimal.valueOf(1234.56));

        when(contaRepository.existsByNomeAndUsuarioId("Conta Corrente", 1L)).thenReturn(false);
        when(contaRepository.save(any(Conta.class))).thenReturn(conta);

        ContaResponse response = contaService.criar(contaRequest);

        assertThat(response.getSaldoInicial()).isEqualByComparingTo(BigDecimal.valueOf(1234.56));
    }

    @Test
    @DisplayName("Deve criar conta com valor grande")
    void deveCriarContaComValorGrande() {
        BigDecimal valorGrande = new BigDecimal("999999999999.99");
        contaRequest.setSaldoInicial(valorGrande);
        conta.setSaldoInicial(valorGrande);

        when(contaRepository.existsByNomeAndUsuarioId("Conta Corrente", 1L)).thenReturn(false);
        when(contaRepository.save(any(Conta.class))).thenReturn(conta);

        ContaResponse response = contaService.criar(contaRequest);

        assertThat(response.getSaldoInicial()).isEqualByComparingTo(valorGrande);
    }
}
