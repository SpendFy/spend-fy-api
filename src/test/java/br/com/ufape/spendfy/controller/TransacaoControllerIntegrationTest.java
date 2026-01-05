package br.com.ufape.spendfy.controller;

import br.com.ufape.spendfy.dto.transacao.TransacaoRequest;
import br.com.ufape.spendfy.entity.Categoria;
import br.com.ufape.spendfy.entity.Conta;
import br.com.ufape.spendfy.entity.Transacao;
import br.com.ufape.spendfy.entity.Usuario;
import br.com.ufape.spendfy.repository.CategoriaRepository;
import br.com.ufape.spendfy.repository.ContaRepository;
import br.com.ufape.spendfy.repository.TransacaoRepository;
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
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Testes de Integração - TransacaoController")
class TransacaoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario usuario;
    private Conta conta;
    private Categoria categoria;
    private TransacaoRequest transacaoRequest;

    @BeforeEach
    void setUp() {
        transacaoRepository.deleteAll();
        contaRepository.deleteAll();
        categoriaRepository.deleteAll();
        usuarioRepository.deleteAll();

        usuario = Usuario.builder()
                .nome("João Silva")
                .email("joao@email.com")
                .senha(passwordEncoder.encode("senha123"))
                .status("ATIVO")
                .build();
        usuario = usuarioRepository.save(usuario);

        conta = Conta.builder()
                .nome("Conta Corrente")
                .tipo("Corrente")
                .saldoInicial(BigDecimal.valueOf(1000.00))
                .usuario(usuario)
                .build();
        conta = contaRepository.save(conta);

        categoria = Categoria.builder()
                .nome("Alimentação")
                .cor("verde")
                .usuario(usuario)
                .build();
        categoria = categoriaRepository.save(categoria);

        transacaoRequest = TransacaoRequest.builder()
                .tipo("DESPESA")
                .data(LocalDate.now())
                .valor(BigDecimal.valueOf(50.00))
                .descricao("Supermercado")
                .observacao("Compras do mês")
                .status("CONFIRMADA")
                .idConta(conta.getId())
                .idCategoria(categoria.getId())
                .build();
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve criar transação com sucesso")
    void deveCriarTransacaoComSucesso() throws Exception {
        mockMvc.perform(post("/api/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transacaoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.tipo").value("DESPESA"))
                .andExpect(jsonPath("$.valor").value(50.00))
                .andExpect(jsonPath("$.descricao").value("Supermercado"))
                .andExpect(jsonPath("$.status").value("CONFIRMADA"))
                .andExpect(jsonPath("$.idConta").value(conta.getId()))
                .andExpect(jsonPath("$.idCategoria").value(categoria.getId()))
                .andExpect(jsonPath("$.nomeConta").value("Conta Corrente"))
                .andExpect(jsonPath("$.nomeCategoria").value("Alimentação"));

        assertThat(transacaoRepository.count()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve criar transação de receita")
    void deveCriarTransacaoDeReceita() throws Exception {
        transacaoRequest.setTipo("RECEITA");
        transacaoRequest.setDescricao("Salário");
        transacaoRequest.setValor(BigDecimal.valueOf(5000.00));

        mockMvc.perform(post("/api/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transacaoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("RECEITA"))
                .andExpect(jsonPath("$.descricao").value("Salário"))
                .andExpect(jsonPath("$.valor").value(5000.00));
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve retornar erro 400 ao criar transação sem campos obrigatórios")
    void deveRetornarErro400AoCriarTransacaoSemCamposObrigatorios() throws Exception {
        TransacaoRequest requestInvalido = TransacaoRequest.builder().build();

        mockMvc.perform(post("/api/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve retornar erro 400 ao criar transação com valor negativo")
    void deveRetornarErro400AoCriarTransacaoComValorNegativo() throws Exception {
        transacaoRequest.setValor(BigDecimal.valueOf(-10.00));

        mockMvc.perform(post("/api/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transacaoRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar erro 403 ao criar transação sem autenticação")
    void deveRetornarErro403AoCriarTransacaoSemAutenticacao() throws Exception {
        mockMvc.perform(post("/api/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transacaoRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve listar todas as transações do usuário")
    void deveListarTodasTransacoesDoUsuario() throws Exception {
        Transacao transacao1 = Transacao.builder()
                .tipo("DESPESA")
                .data(LocalDate.now())
                .valor(BigDecimal.valueOf(50.00))
                .descricao("Mercado")
                .status("CONFIRMADA")
                .usuario(usuario)
                .conta(conta)
                .categoria(categoria)
                .build();
        transacaoRepository.save(transacao1);

        Transacao transacao2 = Transacao.builder()
                .tipo("RECEITA")
                .data(LocalDate.now())
                .valor(BigDecimal.valueOf(1000.00))
                .descricao("Salário")
                .status("CONFIRMADA")
                .usuario(usuario)
                .conta(conta)
                .categoria(categoria)
                .build();
        transacaoRepository.save(transacao2);

        mockMvc.perform(get("/api/transacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].tipo").value("DESPESA"))
                .andExpect(jsonPath("$[1].tipo").value("RECEITA"));
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve retornar lista vazia quando não há transações")
    void deveRetornarListaVaziaQuandoNaoHaTransacoes() throws Exception {
        mockMvc.perform(get("/api/transacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve buscar transação por ID")
    void deveBuscarTransacaoPorId() throws Exception {
        Transacao transacao = Transacao.builder()
                .tipo("DESPESA")
                .data(LocalDate.now())
                .valor(BigDecimal.valueOf(50.00))
                .descricao("Mercado")
                .status("CONFIRMADA")
                .usuario(usuario)
                .conta(conta)
                .categoria(categoria)
                .build();
        transacao = transacaoRepository.save(transacao);

        mockMvc.perform(get("/api/transacoes/{id}", transacao.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transacao.getId()))
                .andExpect(jsonPath("$.descricao").value("Mercado"));
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve retornar erro 404 ao buscar transação inexistente")
    void deveRetornarErro404AoBuscarTransacaoInexistente() throws Exception {
        mockMvc.perform(get("/api/transacoes/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve atualizar transação com sucesso")
    void deveAtualizarTransacaoComSucesso() throws Exception {
        Transacao transacao = Transacao.builder()
                .tipo("DESPESA")
                .data(LocalDate.now())
                .valor(BigDecimal.valueOf(50.00))
                .descricao("Mercado")
                .status("CONFIRMADA")
                .usuario(usuario)
                .conta(conta)
                .categoria(categoria)
                .build();
        transacao = transacaoRepository.save(transacao);

        transacaoRequest.setDescricao("Farmácia");
        transacaoRequest.setValor(BigDecimal.valueOf(75.00));

        mockMvc.perform(put("/api/transacoes/{id}", transacao.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transacaoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transacao.getId()))
                .andExpect(jsonPath("$.descricao").value("Farmácia"))
                .andExpect(jsonPath("$.valor").value(75.00));
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve retornar erro 404 ao atualizar transação inexistente")
    void deveRetornarErro404AoAtualizarTransacaoInexistente() throws Exception {
        mockMvc.perform(put("/api/transacoes/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transacaoRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve deletar transação com sucesso")
    void deveDeletarTransacaoComSucesso() throws Exception {
        Transacao transacao = Transacao.builder()
                .tipo("DESPESA")
                .data(LocalDate.now())
                .valor(BigDecimal.valueOf(50.00))
                .descricao("Mercado")
                .status("CONFIRMADA")
                .usuario(usuario)
                .conta(conta)
                .categoria(categoria)
                .build();
        transacao = transacaoRepository.save(transacao);

        mockMvc.perform(delete("/api/transacoes/{id}", transacao.getId()))
                .andExpect(status().isNoContent());

        assertThat(transacaoRepository.findById(transacao.getId())).isEmpty();
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve retornar erro 404 ao deletar transação inexistente")
    void deveRetornarErro404AoDeletarTransacaoInexistente() throws Exception {
        mockMvc.perform(delete("/api/transacoes/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "outro@email.com")
    @DisplayName("Deve retornar erro 400 ao tentar acessar transação de outro usuário")
    void deveRetornarErro400AoTentarAcessarTransacaoDeOutroUsuario() throws Exception {
        Usuario outroUsuario = Usuario.builder()
                .nome("Maria")
                .email("outro@email.com")
                .senha(passwordEncoder.encode("senha123"))
                .status("ATIVO")
                .build();
        usuarioRepository.save(outroUsuario);

        Transacao transacao = Transacao.builder()
                .tipo("DESPESA")
                .data(LocalDate.now())
                .valor(BigDecimal.valueOf(50.00))
                .descricao("Mercado")
                .status("CONFIRMADA")
                .usuario(usuario)
                .conta(conta)
                .categoria(categoria)
                .build();
        transacao = transacaoRepository.save(transacao);

        mockMvc.perform(get("/api/transacoes/{id}", transacao.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve criar transação com valores decimais precisos")
    void deveCriarTransacaoComValoresDecimaisPrecisos() throws Exception {
        transacaoRequest.setValor(BigDecimal.valueOf(123.45));

        mockMvc.perform(post("/api/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transacaoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valor").value(123.45));
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve criar transação sem descrição e observação")
    void deveCriarTransacaoSemDescricaoEObservacao() throws Exception {
        transacaoRequest.setDescricao(null);
        transacaoRequest.setObservacao(null);

        mockMvc.perform(post("/api/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transacaoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descricao").isEmpty())
                .andExpect(jsonPath("$.observacao").isEmpty());
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve validar tamanho máximo dos campos de texto")
    void deveValidarTamanhoMaximoCamposTexto() throws Exception {
        transacaoRequest.setDescricao("a".repeat(101));

        mockMvc.perform(post("/api/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transacaoRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve retornar campos com nomes corretos em português")
    void deveRetornarCamposComNomesCorretosEmPortugues() throws Exception {
        mockMvc.perform(post("/api/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transacaoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeConta").exists())
                .andExpect(jsonPath("$.nomeCategoria").exists())
                .andExpect(jsonPath("$.dataCadastro").exists())
                .andExpect(jsonPath("$.dataAtualizacao").exists());
    }
}
