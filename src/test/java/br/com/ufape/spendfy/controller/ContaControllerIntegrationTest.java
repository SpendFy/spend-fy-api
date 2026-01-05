package br.com.ufape.spendfy.controller;

import br.com.ufape.spendfy.dto.conta.ContaRequest;
import br.com.ufape.spendfy.entity.Conta;
import br.com.ufape.spendfy.entity.Usuario;
import br.com.ufape.spendfy.repository.ContaRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Testes de Integração - ContaController")
class ContaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario usuario;
    private ContaRequest contaRequest;

    @BeforeEach
    void setUp() {
        contaRepository.deleteAll();
        usuarioRepository.deleteAll();

        usuario = Usuario.builder()
                .nome("João Silva")
                .email("joao@email.com")
                .senha(passwordEncoder.encode("senha123"))
                .status("ATIVO")
                .build();
        usuario = usuarioRepository.save(usuario);

        contaRequest = ContaRequest.builder()
                .nome("Conta Corrente")
                .tipo("Corrente")
                .saldoInicial(BigDecimal.valueOf(1000.00))
                .build();
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve criar conta com sucesso")
    void deveCriarContaComSucesso() throws Exception {
        mockMvc.perform(post("/api/contas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contaRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Conta Corrente"))
                .andExpect(jsonPath("$.tipo").value("Corrente"))
                .andExpect(jsonPath("$.saldoInicial").value(1000.00));

        assertThat(contaRepository.count()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve criar conta poupança")
    void deveCriarContaPoupanca() throws Exception {
        contaRequest.setNome("Conta Poupança");
        contaRequest.setTipo("Poupança");
        contaRequest.setSaldoInicial(BigDecimal.valueOf(5000.00));

        mockMvc.perform(post("/api/contas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contaRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Conta Poupança"))
                .andExpect(jsonPath("$.tipo").value("Poupança"))
                .andExpect(jsonPath("$.saldoInicial").value(5000.00));
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve lançar erro ao criar conta com nome duplicado")
    void deveLancarErroAoCriarContaComNomeDuplicado() throws Exception {
        Conta conta = Conta.builder()
                .nome("Conta Corrente")
                .tipo("Corrente")
                .saldoInicial(BigDecimal.valueOf(1000.00))
                .usuario(usuario)
                .build();
        contaRepository.save(conta);

        mockMvc.perform(post("/api/contas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contaRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Já existe uma conta com este nome"));
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve retornar erro 400 ao criar conta sem campos obrigatórios")
    void deveRetornarErro400AoCriarContaSemCamposObrigatorios() throws Exception {
        ContaRequest requestInvalido = ContaRequest.builder().build();

        mockMvc.perform(post("/api/contas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve listar todas as contas do usuário")
    void deveListarTodasContasDoUsuario() throws Exception {
        Conta conta1 = Conta.builder()
                .nome("Conta Corrente")
                .tipo("Corrente")
                .saldoInicial(BigDecimal.valueOf(1000.00))
                .usuario(usuario)
                .build();
        contaRepository.save(conta1);

        Conta conta2 = Conta.builder()
                .nome("Conta Poupança")
                .tipo("Poupança")
                .saldoInicial(BigDecimal.valueOf(5000.00))
                .usuario(usuario)
                .build();
        contaRepository.save(conta2);

        mockMvc.perform(get("/api/contas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome").value("Conta Corrente"))
                .andExpect(jsonPath("$[1].nome").value("Conta Poupança"));
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve buscar conta por ID")
    void deveBuscarContaPorId() throws Exception {
        Conta conta = Conta.builder()
                .nome("Conta Corrente")
                .tipo("Corrente")
                .saldoInicial(BigDecimal.valueOf(1000.00))
                .usuario(usuario)
                .build();
        conta = contaRepository.save(conta);

        mockMvc.perform(get("/api/contas/{id}", conta.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(conta.getId()))
                .andExpect(jsonPath("$.nome").value("Conta Corrente"));
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve retornar erro 404 ao buscar conta inexistente")
    void deveRetornarErro404AoBuscarContaInexistente() throws Exception {
        mockMvc.perform(get("/api/contas/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve atualizar conta com sucesso")
    void deveAtualizarContaComSucesso() throws Exception {
        Conta conta = Conta.builder()
                .nome("Conta Corrente")
                .tipo("Corrente")
                .saldoInicial(BigDecimal.valueOf(1000.00))
                .usuario(usuario)
                .build();
        conta = contaRepository.save(conta);

        contaRequest.setNome("Conta Corrente Atualizada");
        contaRequest.setSaldoInicial(BigDecimal.valueOf(1500.00));

        mockMvc.perform(put("/api/contas/{id}", conta.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contaRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Conta Corrente Atualizada"))
                .andExpect(jsonPath("$.saldoInicial").value(1500.00));
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve deletar conta com sucesso")
    void deveDeletarContaComSucesso() throws Exception {
        Conta conta = Conta.builder()
                .nome("Conta Corrente")
                .tipo("Corrente")
                .saldoInicial(BigDecimal.valueOf(1000.00))
                .usuario(usuario)
                .build();
        conta = contaRepository.save(conta);

        mockMvc.perform(delete("/api/contas/{id}", conta.getId()))
                .andExpect(status().isNoContent());

        assertThat(contaRepository.findById(conta.getId())).isEmpty();
    }

    @Test
    @WithMockUser(username = "outro@email.com")
    @DisplayName("Deve retornar erro ao tentar acessar conta de outro usuário")
    void deveRetornarErroAoTentarAcessarContaDeOutroUsuario() throws Exception {
        Usuario outroUsuario = Usuario.builder()
                .nome("Maria")
                .email("outro@email.com")
                .senha(passwordEncoder.encode("senha123"))
                .status("ATIVO")
                .build();
        usuarioRepository.save(outroUsuario);

        Conta conta = Conta.builder()
                .nome("Conta Corrente")
                .tipo("Corrente")
                .saldoInicial(BigDecimal.valueOf(1000.00))
                .usuario(usuario)
                .build();
        conta = contaRepository.save(conta);

        mockMvc.perform(get("/api/contas/{id}", conta.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve criar conta com saldo inicial zero")
    void deveCriarContaComSaldoInicialZero() throws Exception {
        contaRequest.setSaldoInicial(BigDecimal.ZERO);

        mockMvc.perform(post("/api/contas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contaRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldoInicial").value(0.00));
    }
}
