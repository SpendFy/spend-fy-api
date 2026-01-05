package br.com.ufape.spendfy.controller;

import br.com.ufape.spendfy.dto.orcamento.OrcamentoRequest;
import br.com.ufape.spendfy.entity.Categoria;
import br.com.ufape.spendfy.entity.Orcamento;
import br.com.ufape.spendfy.entity.Usuario;
import br.com.ufape.spendfy.repository.CategoriaRepository;
import br.com.ufape.spendfy.repository.OrcamentoRepository;
import br.com.ufape.spendfy.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Testes de Integração - OrcamentoController")
class OrcamentoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrcamentoRepository orcamentoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario usuario;
    private Categoria categoria;
    private OrcamentoRequest orcamentoRequest;

    @BeforeEach
    void setUp() {
        orcamentoRepository.deleteAll();
        categoriaRepository.deleteAll();
        usuarioRepository.deleteAll();

        usuario = Usuario.builder()
                .nome("João Silva")
                .email("joao@email.com")
                .senha(passwordEncoder.encode("senha123"))
                .status("ATIVO")
                .build();
        usuario = usuarioRepository.save(usuario);

        categoria = Categoria.builder()
                .nome("Alimentação")
                .cor("verde")
                .usuario(usuario)
                .build();
        categoria = categoriaRepository.save(categoria);

        orcamentoRequest = OrcamentoRequest.builder()
                .valorLimite(BigDecimal.valueOf(1000.00))
                .dataInicio(LocalDate.of(2024, 1, 1))
                .dataFim(LocalDate.of(2024, 1, 31))
                .idCategoria(categoria.getId())
                .build();
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve criar orçamento com sucesso")
    void deveCriarOrcamentoComSucesso() throws Exception {
        mockMvc.perform(post("/api/orcamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orcamentoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.valorLimite").value(1000.00))
                .andExpect(jsonPath("$.dataInicio").value("2024-01-01"))
                .andExpect(jsonPath("$.dataFim").value("2024-01-31"))
                .andExpect(jsonPath("$.nomeCategoria").value("Alimentação"));

        assertThat(orcamentoRepository.count()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve lançar erro ao criar orçamento com data fim anterior")
    void deveLancarErroAoCriarOrcamentoComDataFimAnterior() throws Exception {
        orcamentoRequest.setDataInicio(LocalDate.of(2024, 2, 1));
        orcamentoRequest.setDataFim(LocalDate.of(2024, 1, 31));

        mockMvc.perform(post("/api/orcamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orcamentoRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Data de fim não pode ser anterior à data de início"));
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve lançar erro ao criar orçamento sobreposto")
    void deveLancarErroAoCriarOrcamentoSobreposto() throws Exception {
        // Criar primeiro orçamento
        Orcamento orcamento = Orcamento.builder()
                .valorLimite(BigDecimal.valueOf(1000.00))
                .dataInicio(LocalDate.of(2024, 1, 1))
                .dataFim(LocalDate.of(2024, 1, 31))
                .usuario(usuario)
                .categoria(categoria)
                .build();
        orcamentoRepository.save(orcamento);

        // Tentar criar orçamento sobreposto
        orcamentoRequest.setDataInicio(LocalDate.of(2024, 1, 15));
        orcamentoRequest.setDataFim(LocalDate.of(2024, 2, 15));

        mockMvc.perform(post("/api/orcamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orcamentoRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Já existe um orçamento para esta categoria no período informado"));
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve permitir criar orçamento adjacente")
    void devePermitirCriarOrcamentoAdjacente() throws Exception {
        // Criar primeiro orçamento
        Orcamento orcamento = Orcamento.builder()
                .valorLimite(BigDecimal.valueOf(1000.00))
                .dataInicio(LocalDate.of(2024, 1, 1))
                .dataFim(LocalDate.of(2024, 1, 31))
                .usuario(usuario)
                .categoria(categoria)
                .build();
        orcamentoRepository.save(orcamento);

        // Criar orçamento adjacente (fevereiro)
        orcamentoRequest.setDataInicio(LocalDate.of(2024, 2, 1));
        orcamentoRequest.setDataFim(LocalDate.of(2024, 2, 29));

        mockMvc.perform(post("/api/orcamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orcamentoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dataInicio").value("2024-02-01"));

        assertThat(orcamentoRepository.count()).isEqualTo(2);
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve listar todos os orçamentos do usuário")
    void deveListarTodosOrcamentosDoUsuario() throws Exception {
        Orcamento orcamento1 = Orcamento.builder()
                .valorLimite(BigDecimal.valueOf(1000.00))
                .dataInicio(LocalDate.of(2024, 1, 1))
                .dataFim(LocalDate.of(2024, 1, 31))
                .usuario(usuario)
                .categoria(categoria)
                .build();
        orcamentoRepository.save(orcamento1);

        Categoria categoria2 = Categoria.builder()
                .nome("Transporte")
                .cor("azul")
                .usuario(usuario)
                .build();
        categoriaRepository.save(categoria2);

        Orcamento orcamento2 = Orcamento.builder()
                .valorLimite(BigDecimal.valueOf(500.00))
                .dataInicio(LocalDate.of(2024, 1, 1))
                .dataFim(LocalDate.of(2024, 1, 31))
                .usuario(usuario)
                .categoria(categoria2)
                .build();
        orcamentoRepository.save(orcamento2);

        mockMvc.perform(get("/api/orcamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].valorLimite").value(1000.00))
                .andExpect(jsonPath("$[1].valorLimite").value(500.00));
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve buscar orçamento por ID")
    void deveBuscarOrcamentoPorId() throws Exception {
        Orcamento orcamento = Orcamento.builder()
                .valorLimite(BigDecimal.valueOf(1000.00))
                .dataInicio(LocalDate.of(2024, 1, 1))
                .dataFim(LocalDate.of(2024, 1, 31))
                .usuario(usuario)
                .categoria(categoria)
                .build();
        orcamento = orcamentoRepository.save(orcamento);

        mockMvc.perform(get("/api/orcamentos/{id}", orcamento.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orcamento.getId()))
                .andExpect(jsonPath("$.valorLimite").value(1000.00));
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve atualizar orçamento com sucesso")
    void deveAtualizarOrcamentoComSucesso() throws Exception {
        Orcamento orcamento = Orcamento.builder()
                .valorLimite(BigDecimal.valueOf(1000.00))
                .dataInicio(LocalDate.of(2024, 1, 1))
                .dataFim(LocalDate.of(2024, 1, 31))
                .usuario(usuario)
                .categoria(categoria)
                .build();
        orcamento = orcamentoRepository.save(orcamento);

        orcamentoRequest.setValorLimite(BigDecimal.valueOf(1500.00));

        mockMvc.perform(put("/api/orcamentos/{id}", orcamento.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orcamentoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorLimite").value(1500.00));
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve deletar orçamento com sucesso")
    void deveDeletarOrcamentoComSucesso() throws Exception {
        Orcamento orcamento = Orcamento.builder()
                .valorLimite(BigDecimal.valueOf(1000.00))
                .dataInicio(LocalDate.of(2024, 1, 1))
                .dataFim(LocalDate.of(2024, 1, 31))
                .usuario(usuario)
                .categoria(categoria)
                .build();
        orcamento = orcamentoRepository.save(orcamento);

        mockMvc.perform(delete("/api/orcamentos/{id}", orcamento.getId()))
                .andExpect(status().isNoContent());

        assertThat(orcamentoRepository.findById(orcamento.getId())).isEmpty();
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve retornar erro 404 ao buscar orçamento inexistente")
    void deveRetornarErro404AoBuscarOrcamentoInexistente() throws Exception {
        mockMvc.perform(get("/api/orcamentos/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}
