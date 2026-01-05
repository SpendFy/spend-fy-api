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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - OrcamentoService")
class OrcamentoServiceTest {

    @Mock
    private OrcamentoRepository orcamentoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OrcamentoService orcamentoService;

    private Usuario usuario;
    private Categoria categoria;
    private Orcamento orcamento;
    private OrcamentoRequest orcamentoRequest;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .nome("João Silva")
                .email("joao@email.com")
                .senha("senha123")
                .status("ATIVO")
                .build();

        categoria = Categoria.builder()
                .id(1L)
                .nome("Alimentação")
                .cor("verde")
                .usuario(usuario)
                .build();

        orcamento = Orcamento.builder()
                .id(1L)
                .valorLimite(BigDecimal.valueOf(1000.00))
                .dataInicio(LocalDate.of(2024, 1, 1))
                .dataFim(LocalDate.of(2024, 1, 31))
                .usuario(usuario)
                .categoria(categoria)
                .dataCadastro(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();

        orcamentoRequest = OrcamentoRequest.builder()
                .valorLimite(BigDecimal.valueOf(1000.00))
                .dataInicio(LocalDate.of(2024, 1, 1))
                .dataFim(LocalDate.of(2024, 1, 31))
                .idCategoria(1L)
                .build();

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("joao@email.com");
        when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    }

    @Test
    @DisplayName("Deve criar orçamento com sucesso")
    void deveCriarOrcamentoComSucesso() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(orcamentoRepository.findOverlappingOrcamentos(anyLong(), anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(orcamentoRepository.save(any(Orcamento.class))).thenReturn(orcamento);

        OrcamentoResponse response = orcamentoService.criar(orcamentoRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getValorLimite()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
        assertThat(response.getDataInicio()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(response.getDataFim()).isEqualTo(LocalDate.of(2024, 1, 31));
        assertThat(response.getIdCategoria()).isEqualTo(1L);

        verify(orcamentoRepository, times(1)).save(any(Orcamento.class));
    }

    @Test
    @DisplayName("Deve criar orçamento com período de um dia")
    void deveCriarOrcamentoComPeriodoDeUmDia() {
        LocalDate data = LocalDate.of(2024, 1, 15);
        orcamentoRequest.setDataInicio(data);
        orcamentoRequest.setDataFim(data);
        orcamento.setDataInicio(data);
        orcamento.setDataFim(data);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(orcamentoRepository.findOverlappingOrcamentos(anyLong(), anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(orcamentoRepository.save(any(Orcamento.class))).thenReturn(orcamento);

        OrcamentoResponse response = orcamentoService.criar(orcamentoRequest);

        assertThat(response.getDataInicio()).isEqualTo(response.getDataFim());
    }

    @Test
    @DisplayName("Deve lançar exceção quando data fim é anterior à data início")
    void deveLancarExcecaoQuandoDataFimAnteriorDataInicio() {
        orcamentoRequest.setDataInicio(LocalDate.of(2024, 2, 1));
        orcamentoRequest.setDataFim(LocalDate.of(2024, 1, 31));

        assertThatThrownBy(() -> orcamentoService.criar(orcamentoRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Data de fim não pode ser anterior à data de início");

        verify(orcamentoRepository, never()).save(any(Orcamento.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar orçamento com categoria inexistente")
    void deveLancarExcecaoAoCriarOrcamentoComCategoriaInexistente() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orcamentoService.criar(orcamentoRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Categoria");

        verify(orcamentoRepository, never()).save(any(Orcamento.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar orçamento com categoria de outro usuário")
    void deveLancarExcecaoAoCriarOrcamentoComCategoriaDeOutroUsuario() {
        Usuario outroUsuario = Usuario.builder()
                .id(2L)
                .nome("Maria")
                .email("maria@email.com")
                .build();

        categoria.setUsuario(outroUsuario);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        assertThatThrownBy(() -> orcamentoService.criar(orcamentoRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Categoria não pertence ao usuário autenticado");

        verify(orcamentoRepository, never()).save(any(Orcamento.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando há orçamento sobreposto - cenário 1: novo início dentro de existente")
    void deveLancarExcecaoQuandoOrcamentoSobreposto_NovoInicioDentro() {
        // Orçamento existente: 01/01 a 31/01
        // Novo orçamento: 15/01 a 15/02 (início dentro)
        orcamentoRequest.setDataInicio(LocalDate.of(2024, 1, 15));
        orcamentoRequest.setDataFim(LocalDate.of(2024, 2, 15));

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(orcamentoRepository.findOverlappingOrcamentos(anyLong(), anyLong(), any(), any()))
                .thenReturn(Collections.singletonList(orcamento));

        assertThatThrownBy(() -> orcamentoService.criar(orcamentoRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Já existe um orçamento para esta categoria no período informado");

        verify(orcamentoRepository, never()).save(any(Orcamento.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando há orçamento sobreposto - cenário 2: novo fim dentro de existente")
    void deveLancarExcecaoQuandoOrcamentoSobreposto_NovoFimDentro() {
        // Orçamento existente: 01/01 a 31/01
        // Novo orçamento: 15/12 a 15/01 (fim dentro)
        orcamentoRequest.setDataInicio(LocalDate.of(2023, 12, 15));
        orcamentoRequest.setDataFim(LocalDate.of(2024, 1, 15));

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(orcamentoRepository.findOverlappingOrcamentos(anyLong(), anyLong(), any(), any()))
                .thenReturn(Collections.singletonList(orcamento));

        assertThatThrownBy(() -> orcamentoService.criar(orcamentoRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Já existe um orçamento para esta categoria no período informado");

        verify(orcamentoRepository, never()).save(any(Orcamento.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando há orçamento sobreposto - cenário 3: novo contém existente")
    void deveLancarExcecaoQuandoOrcamentoSobreposto_NovoContemExistente() {
        // Orçamento existente: 01/01 a 31/01
        // Novo orçamento: 15/12 a 15/02 (contém o existente completamente)
        orcamentoRequest.setDataInicio(LocalDate.of(2023, 12, 15));
        orcamentoRequest.setDataFim(LocalDate.of(2024, 2, 15));

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(orcamentoRepository.findOverlappingOrcamentos(anyLong(), anyLong(), any(), any()))
                .thenReturn(Collections.singletonList(orcamento));

        assertThatThrownBy(() -> orcamentoService.criar(orcamentoRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Já existe um orçamento para esta categoria no período informado");

        verify(orcamentoRepository, never()).save(any(Orcamento.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando há orçamento sobreposto - cenário 4: existente contém novo")
    void deveLancarExcecaoQuandoOrcamentoSobreposto_ExistenteContemNovo() {
        // Orçamento existente: 01/01 a 31/01
        // Novo orçamento: 10/01 a 20/01 (dentro do existente)
        orcamentoRequest.setDataInicio(LocalDate.of(2024, 1, 10));
        orcamentoRequest.setDataFim(LocalDate.of(2024, 1, 20));

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(orcamentoRepository.findOverlappingOrcamentos(anyLong(), anyLong(), any(), any()))
                .thenReturn(Collections.singletonList(orcamento));

        assertThatThrownBy(() -> orcamentoService.criar(orcamentoRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Já existe um orçamento para esta categoria no período informado");

        verify(orcamentoRepository, never()).save(any(Orcamento.class));
    }

    @Test
    @DisplayName("Deve permitir criar orçamento adjacente (sem sobreposição)")
    void devePermitirCriarOrcamentoAdjacente() {
        // Orçamento existente: 01/01 a 31/01
        // Novo orçamento: 01/02 a 28/02 (adjacente, sem sobreposição)
        orcamentoRequest.setDataInicio(LocalDate.of(2024, 2, 1));
        orcamentoRequest.setDataFim(LocalDate.of(2024, 2, 28));

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(orcamentoRepository.findOverlappingOrcamentos(anyLong(), anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(orcamentoRepository.save(any(Orcamento.class))).thenReturn(orcamento);

        OrcamentoResponse response = orcamentoService.criar(orcamentoRequest);

        assertThat(response).isNotNull();
        verify(orcamentoRepository, times(1)).save(any(Orcamento.class));
    }

    @Test
    @DisplayName("Deve permitir criar orçamento para categoria diferente no mesmo período")
    void devePermitirCriarOrcamentoParaCategoriaDiferenteNoMesmoPeriodo() {
        Categoria outraCategoria = Categoria.builder()
                .id(2L)
                .nome("Transporte")
                .cor("azul")
                .usuario(usuario)
                .build();

        orcamentoRequest.setIdCategoria(2L);

        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(outraCategoria));
        when(orcamentoRepository.findOverlappingOrcamentos(anyLong(), anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(orcamentoRepository.save(any(Orcamento.class))).thenReturn(orcamento);

        OrcamentoResponse response = orcamentoService.criar(orcamentoRequest);

        assertThat(response).isNotNull();
        verify(orcamentoRepository, times(1)).save(any(Orcamento.class));
    }

    @Test
    @DisplayName("Deve listar todos os orçamentos do usuário")
    void deveListarTodosOrcamentosDoUsuario() {
        Orcamento orcamento2 = Orcamento.builder()
                .id(2L)
                .valorLimite(BigDecimal.valueOf(500.00))
                .dataInicio(LocalDate.of(2024, 2, 1))
                .dataFim(LocalDate.of(2024, 2, 29))
                .usuario(usuario)
                .categoria(categoria)
                .build();

        when(orcamentoRepository.findByUsuarioId(1L))
                .thenReturn(Arrays.asList(orcamento, orcamento2));

        List<OrcamentoResponse> responses = orcamentoService.listarTodos();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getValorLimite()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
        assertThat(responses.get(1).getValorLimite()).isEqualByComparingTo(BigDecimal.valueOf(500.00));
    }

    @Test
    @DisplayName("Deve buscar orçamento por ID com sucesso")
    void deveBuscarOrcamentoPorIdComSucesso() {
        when(orcamentoRepository.findById(1L)).thenReturn(Optional.of(orcamento));

        OrcamentoResponse response = orcamentoService.buscarPorId(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getValorLimite()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar orçamento inexistente")
    void deveLancarExcecaoAoBuscarOrcamentoInexistente() {
        when(orcamentoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orcamentoService.buscarPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Orcamento");
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar orçamento de outro usuário")
    void deveLancarExcecaoAoBuscarOrcamentoDeOutroUsuario() {
        Usuario outroUsuario = Usuario.builder()
                .id(2L)
                .nome("Maria")
                .email("maria@email.com")
                .build();

        orcamento.setUsuario(outroUsuario);

        when(orcamentoRepository.findById(1L)).thenReturn(Optional.of(orcamento));

        assertThatThrownBy(() -> orcamentoService.buscarPorId(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Orçamento não pertence ao usuário autenticado");
    }

    @Test
    @DisplayName("Deve atualizar orçamento com sucesso")
    void deveAtualizarOrcamentoComSucesso() {
        OrcamentoRequest requestAtualizado = OrcamentoRequest.builder()
                .valorLimite(BigDecimal.valueOf(1500.00))
                .dataInicio(LocalDate.of(2024, 1, 1))
                .dataFim(LocalDate.of(2024, 1, 31))
                .idCategoria(1L)
                .build();

        when(orcamentoRepository.findById(1L)).thenReturn(Optional.of(orcamento));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(orcamentoRepository.findOverlappingOrcamentos(anyLong(), anyLong(), any(), any()))
                .thenReturn(Collections.singletonList(orcamento)); // Retorna o próprio orçamento
        when(orcamentoRepository.save(any(Orcamento.class))).thenReturn(orcamento);

        OrcamentoResponse response = orcamentoService.atualizar(1L, requestAtualizado);

        assertThat(response).isNotNull();
        verify(orcamentoRepository, times(1)).save(any(Orcamento.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar orçamento com data fim anterior")
    void deveLancarExcecaoAoAtualizarOrcamentoComDataFimAnterior() {
        OrcamentoRequest requestAtualizado = OrcamentoRequest.builder()
                .valorLimite(BigDecimal.valueOf(1500.00))
                .dataInicio(LocalDate.of(2024, 2, 1))
                .dataFim(LocalDate.of(2024, 1, 31))
                .idCategoria(1L)
                .build();

        when(orcamentoRepository.findById(1L)).thenReturn(Optional.of(orcamento));

        assertThatThrownBy(() -> orcamentoService.atualizar(1L, requestAtualizado))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Data de fim não pode ser anterior à data de início");

        verify(orcamentoRepository, never()).save(any(Orcamento.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar orçamento de outro usuário")
    void deveLancarExcecaoAoAtualizarOrcamentoDeOutroUsuario() {
        Usuario outroUsuario = Usuario.builder()
                .id(2L)
                .nome("Maria")
                .email("maria@email.com")
                .build();

        orcamento.setUsuario(outroUsuario);

        when(orcamentoRepository.findById(1L)).thenReturn(Optional.of(orcamento));

        assertThatThrownBy(() -> orcamentoService.atualizar(1L, orcamentoRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Orçamento não pertence ao usuário autenticado");

        verify(orcamentoRepository, never()).save(any(Orcamento.class));
    }

    @Test
    @DisplayName("Deve deletar orçamento com sucesso")
    void deveDeletarOrcamentoComSucesso() {
        when(orcamentoRepository.findById(1L)).thenReturn(Optional.of(orcamento));

        orcamentoService.deletar(1L);

        verify(orcamentoRepository, times(1)).delete(orcamento);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar orçamento inexistente")
    void deveLancarExcecaoAoDeletarOrcamentoInexistente() {
        when(orcamentoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orcamentoService.deletar(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Orcamento");

        verify(orcamentoRepository, never()).delete(any(Orcamento.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar orçamento de outro usuário")
    void deveLancarExcecaoAoDeletarOrcamentoDeOutroUsuario() {
        Usuario outroUsuario = Usuario.builder()
                .id(2L)
                .nome("Maria")
                .email("maria@email.com")
                .build();

        orcamento.setUsuario(outroUsuario);

        when(orcamentoRepository.findById(1L)).thenReturn(Optional.of(orcamento));

        assertThatThrownBy(() -> orcamentoService.deletar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Orçamento não pertence ao usuário autenticado");

        verify(orcamentoRepository, never()).delete(any(Orcamento.class));
    }

    @Test
    @DisplayName("Deve criar orçamento com valor decimal preciso")
    void deveCriarOrcamentoComValorDecimalPreciso() {
        orcamentoRequest.setValorLimite(BigDecimal.valueOf(1234.56));
        orcamento.setValorLimite(BigDecimal.valueOf(1234.56));

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(orcamentoRepository.findOverlappingOrcamentos(anyLong(), anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(orcamentoRepository.save(any(Orcamento.class))).thenReturn(orcamento);

        OrcamentoResponse response = orcamentoService.criar(orcamentoRequest);

        assertThat(response.getValorLimite()).isEqualByComparingTo(BigDecimal.valueOf(1234.56));
    }
}
